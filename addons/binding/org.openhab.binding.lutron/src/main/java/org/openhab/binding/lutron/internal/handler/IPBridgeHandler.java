/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lutron.internal.config.IPBridgeConfig;
import org.openhab.binding.lutron.internal.discovery.LutronDeviceDiscoveryService;
import org.openhab.binding.lutron.internal.net.TelnetSession;
import org.openhab.binding.lutron.internal.net.TelnetSessionListener;
import org.openhab.binding.lutron.internal.protocol.LutronCommand;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.openhab.binding.lutron.internal.protocol.LutronOperation;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with the main Lutron control hub.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added reconnect and heartbeat config parameters
 */
public class IPBridgeHandler extends BaseBridgeHandler {
    private static final Pattern STATUS_REGEX = Pattern.compile("~(OUTPUT|DEVICE|SYSTEM|TIMECLOCK|MODE),([^,]+),(.*)");

    private static final String DB_UPDATE_DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    private static final Integer MONITOR_PROMPT = 12;
    private static final Integer MONITOR_DISABLE = 2;

    private static final Integer SYSTEM_DBEXPORTDATETIME = 10;

    private static final int MAX_LOGIN_ATTEMPTS = 2;

    private static final String PROMPT_GNET = "GNET>";
    private static final String PROMPT_QNET = "QNET>";
    private static final String PROMPT_SAFE = "SAFE>";
    private static final String LOGIN_MATCH_REGEX = "(login:|[GQ]NET>|SAFE>)";

    private static final String DEFAULT_USER = "lutron";
    private static final String DEFAULT_PASSWORD = "integration";
    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);

    private IPBridgeConfig config;
    private int reconnectInterval;
    private int heartbeatInterval;

    private TelnetSession session;
    private BlockingQueue<LutronCommand> sendQueue = new LinkedBlockingQueue<>();

    private ScheduledFuture<?> messageSender;
    private ScheduledFuture<?> keepAlive;
    private ScheduledFuture<?> keepAliveReconnect;
    private ScheduledFuture<?> connectRetryJob;

    private Date lastDbUpdateDate;
    private ServiceRegistration<DiscoveryService> discoveryServiceRegistration;

    public class LutronSafemodeException extends Exception {
        private static final long serialVersionUID = 1L;

        public LutronSafemodeException(String message) {
            super(message);
        }
    }

    public IPBridgeHandler(Bridge bridge) {
        super(bridge);

        this.session = new TelnetSession();

        this.session.addListener(new TelnetSessionListener() {
            @Override
            public void inputAvailable() {
                parseUpdates();
            }

            @Override
            public void error(IOException exception) {
            }
        });
    }

    public IPBridgeConfig getIPBridgeConfig() {
        return this.config;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.config = getThing().getConfiguration().as(IPBridgeConfig.class);

        if (validConfiguration(this.config)) {
            LutronDeviceDiscoveryService discovery = new LutronDeviceDiscoveryService(this);
            reconnectInterval = (config.getReconnect() > 0) ? config.getReconnect() : DEFAULT_RECONNECT_MINUTES;
            heartbeatInterval = (config.getHeartbeat() > 0) ? config.getHeartbeat() : DEFAULT_HEARTBEAT_MINUTES;

            this.discoveryServiceRegistration = this.bundleContext.registerService(DiscoveryService.class, discovery,
                    null);

            this.scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, 0, TimeUnit.SECONDS);
        }
    }

    private boolean validConfiguration(IPBridgeConfig config) {
        if (this.config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge configuration missing");

            return false;
        }

        if (StringUtils.isEmpty(this.config.getIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge address not specified");

            return false;
        }

        return true;
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    private synchronized void connect() {
        if (this.session.isConnected()) {
            return;
        }

        logger.debug("Connecting to bridge at {}", config.getIpAddress());

        try {
            if (!login(config)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid username/password");

                return;
            }

            // Disable prompts
            sendCommand(new LutronCommand(LutronOperation.EXECUTE, LutronCommandType.MONITORING, -1, MONITOR_PROMPT,
                    MONITOR_DISABLE));

            // Check the time device database was last updated. On the initial connect, this will trigger
            // a scan for paired devices.
            sendCommand(
                    new LutronCommand(LutronOperation.QUERY, LutronCommandType.SYSTEM, -1, SYSTEM_DBEXPORTDATETIME));
        } catch (LutronSafemodeException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "main repeater is in safe mode");
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.

            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.

            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "login interrupted");
            disconnect();

            return;
        }

        this.messageSender = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                sendCommands();
            }
        }, 0, TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);

        keepAlive = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
                TimeUnit.MINUTES);
    }

    private void sendCommands() {
        try {
            while (true) {
                LutronCommand command = this.sendQueue.take();

                logger.debug("Sending command {}", command);

                try {
                    this.session.writeLine(command.toString());
                } catch (IOException e) {
                    logger.error("Communication error, will try to reconnect", e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

                    // Requeue command
                    this.sendQueue.add(command);

                    reconnect();

                    // reconnect() will start a new thread; terminate this one
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void disconnect() {
        logger.debug("Disconnecting from bridge");

        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }

        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }

        if (this.keepAliveReconnect != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            this.keepAliveReconnect.cancel(false);
        }

        if (this.messageSender != null) {
            this.messageSender.cancel(true);
        }

        try {
            this.session.close();
        } catch (IOException e) {
            logger.error("Error disconnecting", e);
        }
    }

    private synchronized void reconnect() {
        logger.debug("Keepalive timeout, attempting to reconnect to the bridge");

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE);
        disconnect();
        connect();
    }

    private boolean login(IPBridgeConfig config) throws IOException, InterruptedException, LutronSafemodeException {
        this.session.open(config.getIpAddress());
        this.session.waitFor("login:");

        // Sometimes the Lutron Smart Bridge Pro will request login more than once.
        for (int attempt = 0; attempt < MAX_LOGIN_ATTEMPTS; attempt++) {
            this.session.writeLine(config.getUser() != null ? config.getUser() : DEFAULT_USER);
            this.session.waitFor("password:");
            this.session.writeLine(config.getPassword() != null ? config.getPassword() : DEFAULT_PASSWORD);

            MatchResult matchResult = this.session.waitFor(LOGIN_MATCH_REGEX);

            if (PROMPT_GNET.equals(matchResult.group()) || PROMPT_QNET.equals(matchResult.group())) {
                return true;
            } else if (PROMPT_SAFE.equals(matchResult.group())) {
                logger.warn("Lutron repeater is in safe mode. Unable to connect.");
                throw new LutronSafemodeException("Lutron repeater in safe mode");
            }

            else {
                logger.debug("got another login prompt, logging in again");
                // we already got the login prompt so go straight to sending user
            }
        }
        return false;
    }

    void sendCommand(LutronCommand command) {
        this.sendQueue.add(command);
    }

    private LutronHandler findThingHandler(int integrationId) {
        for (Thing thing : getThing().getThings()) {
            if (thing.getHandler() instanceof LutronHandler) {
                LutronHandler handler = (LutronHandler) thing.getHandler();

                if (handler != null && handler.getIntegrationId() == integrationId) {
                    return handler;
                }
            }
        }

        return null;
    }

    private void parseUpdates() {
        String paramString;

        for (String line : this.session.readLines()) {
            if (line.trim().equals("")) {
                // Sometimes we get an empty line (possibly only when prompts are disabled). Ignore them.
                continue;
            }

            logger.debug("Received message {}", line);

            // System is alive, cancel reconnect task.
            if (this.keepAliveReconnect != null) {
                this.keepAliveReconnect.cancel(true);
            }

            Matcher matcher = STATUS_REGEX.matcher(line);

            if (matcher.find()) {
                LutronCommandType type = LutronCommandType.valueOf(matcher.group(1));

                if (type == LutronCommandType.SYSTEM) {
                    // SYSTEM messages are assumed to be a response to the SYSTEM_DBEXPORTDATETIME
                    // query. The response returns the last time the device database was updated.
                    setDbUpdateDate(matcher.group(2), matcher.group(3));

                    continue;
                }

                Integer integrationId;

                try {
                    integrationId = Integer.valueOf(matcher.group(2));
                } catch (NumberFormatException e1) {
                    logger.warn("Integer conversion error parsing update: {}", line);
                    continue;
                }
                paramString = matcher.group(3);

                // Now dispatch update to the proper thing handler
                LutronHandler handler = findThingHandler(integrationId);

                if (handler != null) {
                    try {
                        handler.handleUpdate(type, paramString.split(","));
                    } catch (Exception e) {
                        logger.error("Error processing update", e);
                    }
                } else {
                    logger.debug("No thing configured for integration ID {}", integrationId);
                }
            } else {
                logger.debug("Ignoring message {}", line);
            }
        }
    }

    private void sendKeepAlive() {
        // Reconnect if no response is received within 30 seconds.
        this.keepAliveReconnect = this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                reconnect();
            }
        }, 30, TimeUnit.SECONDS);

        sendCommand(new LutronCommand(LutronOperation.QUERY, LutronCommandType.SYSTEM, -1, SYSTEM_DBEXPORTDATETIME));
    }

    private void setDbUpdateDate(String dateString, String timeString) {
        try {
            Date date = new SimpleDateFormat(DB_UPDATE_DATE_FORMAT).parse(dateString + " " + timeString);

            if (this.lastDbUpdateDate == null || date.after(this.lastDbUpdateDate)) {
                scanForDevices();

                this.lastDbUpdateDate = date;
            }
        } catch (ParseException e) {
            logger.error("Failed to parse DB update date {} {}", dateString, timeString);
        }
    }

    private void scanForDevices() {
        try {
            DiscoveryService service = this.bundleContext.getService(this.discoveryServiceRegistration.getReference());

            if (service != null) {
                service.startScan(null);
            }
        } catch (Exception e) {
            logger.error("Error scanning for paired devices", e);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        IPBridgeConfig newConfig = thing.getConfiguration().as(IPBridgeConfig.class);
        boolean validConfig = validConfiguration(newConfig);
        boolean needsReconnect = validConfig && !this.config.sameConnectionParameters(newConfig);

        if (!validConfig || needsReconnect) {
            dispose();
        }

        this.thing = thing;
        this.config = newConfig;

        if (needsReconnect) {
            initialize();
        }
    }

    @Override
    public void dispose() {
        disconnect();

        if (this.discoveryServiceRegistration != null) {
            this.discoveryServiceRegistration.unregister();
            this.discoveryServiceRegistration = null;
        }
    }
}
