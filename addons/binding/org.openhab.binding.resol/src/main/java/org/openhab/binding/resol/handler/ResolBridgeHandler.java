/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.resol.ResolBindingConfiguration;
import org.openhab.binding.resol.ResolBindingConstants;
import org.openhab.binding.resol.internal.discovery.ResolDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.ConnectionAdapter;
import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.Specification.PacketFieldValue;
import de.resol.vbus.SpecificationFile;
import de.resol.vbus.TcpDataSource;
import de.resol.vbus.TcpDataSourceProvider;

/**
 * The {@link ResolBridgeHandler} class handles the connection to the
 * optolink adapter.
 *
 * @author Raphael Mack - Initial contribution
 */
public class ResolBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(ResolBridgeHandler.class);

    private String ipAddress;
    private String password;
    private int port;
    private int refreshInterval = 900; /* 15 mins for refreshing the available things should be enough */
    private Socket socket;
    private PrintStream out;
    private InputStream inStream;
    private boolean isConnected = false;

    public ResolBridgeHandler(Bridge bridge) {
        super(bridge);
        spec = Specification.getDefaultSpecification();
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        updateThingHandlersStatus(status);

    }

    public void updateStatus() {
        if (isConnected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

    }

    // Managing Thing Discovery Service

    private ResolDiscoveryService discoveryService = null;

    public void registerDiscoveryService(ResolDiscoveryService discoveryService) {

        if (discoveryService == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingDiscoveryListener.");
        } else {
            this.discoveryService = discoveryService;
            logger.trace("register Discovery Service");
        }
    }

    public void unregisterDiscoveryService() {
        discoveryService = null;
        logger.trace("unregister Discovery Service");
    }

    // Handles Thing discovery

    private void createThing(String thingType, String thingID) {
        logger.trace("Create thing Type='{}' id='{}'", thingType, thingID);
        if (discoveryService != null) {
            discoveryService.addResolThing(thingType, thingID);
        }
    }

    // Managing ThingHandler

    private Map<String, ResolThingHandler> thingHandlerMap = new HashMap<String, ResolThingHandler>();

    public void registerResolThingListener(ResolThingHandler thingHandler) {
        if (thingHandler == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingHandler.");
        } else {
            Thing t = thingHandler.getThing();

            String thingType = t.getProperties().get("type");

            if (thingHandlerMap.get(thingType) == null) {
                thingHandlerMap.put(thingType, thingHandler);
                logger.trace("register thingHandler for thing: {}", thingType);
                updateThingHandlerStatus(thingHandler, this.getStatus());
            } else {
                logger.trace("thingHandler for thing: '{}' allready registerd", thingType);
            }

        }
    }

    public void unregisterThingListener(ResolThingHandler thingHandler) {
        if (thingHandler != null) {
            String thingID = thingHandler.getThing().getUID().getId();
            if (thingHandlerMap.remove(thingID) == null) {
                logger.trace("thingHandler for thing: {} not registered", thingID);
            } else {
                thingHandler.updateStatus(ThingStatus.OFFLINE);
            }
        }

    }

    private void updateThingHandlerStatus(@NonNull ResolThingHandler thingHandler, @NonNull ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    private void updateThingHandlersStatus(@NonNull ThingStatus status) {
        for (Map.Entry<String, ResolThingHandler> entry : thingHandlerMap.entrySet()) {
            entry.getValue().updateStatus(status);
        }
    }

    // Background Runables

    private ScheduledFuture<?> pollingJob;

    private TcpDataSource dataSource;
    private Connection tcpConnection;
    private Specification spec;
    private Set<String> availableDevices = new HashSet<String>();

    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            logger.trace("Polling job called");
            if (!isConnected) {
                try {
                    dataSource = TcpDataSourceProvider.fetchInformation(InetAddress.getByName(ipAddress), 500);
                    dataSource.setLivePassword(password);
                    tcpConnection = dataSource.connectLive(0, 0x0020);

                    Thread.sleep(5000); // Wait
                                        // for
                                        // connection
                                        // .

                    // Add a listener to the Connection to monitor state changes and
                    // add incoming packets to the HeaderSetConsolidator
                    tcpConnection.addListener(new ConnectionAdapter() {

                        @Override
                        public void connectionStateChanged(Connection connection) {
                            isConnected = (tcpConnection.getConnectionState()
                                    .equals(Connection.ConnectionState.CONNECTED));
                            logger.trace("Connection state changed to: {} isConnected = {}",
                                    tcpConnection.getConnectionState().toString(), isConnected);
                            updateStatus();
                        }

                        @Override
                        public void packetReceived(Connection connection, Packet packet) {
                            String thingType = spec.getSourceDeviceSpec(packet).getName();
                            thingType = thingType.replace(" [", "-");
                            thingType = thingType.replace("]", "");
                            thingType = thingType.replace(" #", "-");
                            thingType = thingType.replace(" ", "_");
                            thingType = thingType.replace("/", "_");
                            thingType = thingType.replaceAll("[^A-Za-z0-9_-]+", "_");

                            if (spec.getSourceDeviceSpec(packet).getPeerAddress() == 0x10) {
                                logger.trace("Received Data from " + spec.getSourceDeviceSpec(packet).getName() + " (0x"
                                        + Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()) + "/0x"
                                        + Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()) + ")"
                                        + " naming it " + thingType);
                            } else {
                                logger.trace("Ignoring Data from " + spec.getSourceDeviceSpec(packet).getName() + " (0x"
                                        + Integer.toHexString(spec.getSourceDeviceSpec(packet).getSelfAddress()) + "/0x"
                                        + Integer.toHexString(spec.getSourceDeviceSpec(packet).getPeerAddress()) + ")"
                                        + " naming it " + thingType);
                                return;
                            }

                            // TODO: if the thing gets deleted, we should also remove it from this list...
                            if (!availableDevices.contains(thingType)) {
                                // register new device
                                createThing(ResolBindingConstants.THING_ID_DEVICE, thingType);
                                availableDevices.add(thingType);
                            }

                            PacketFieldValue[] pfvs = spec.getPacketFieldValuesForHeaders(new Packet[] { packet });
                            for (PacketFieldValue pfv : pfvs) {
                                logger.trace("Id: {}, Name: {}, Raw: {}, Text: {}", pfv.getPacketFieldId(),
                                        pfv.getName(), pfv.getRawValueDouble(), pfv.formatTextValue(null, null));
                                ResolThingHandler thingHandler = thingHandlerMap.get(thingType);
                                if (thingHandler != null) {
                                    @NonNull
                                    String channelId = pfv.getName();
                                    channelId = channelId.replace(" [", "-");
                                    channelId = channelId.replace("]", "");
                                    channelId = channelId.replace("(", "-");
                                    channelId = channelId.replace(")", "");
                                    channelId = channelId.replace(" #", "-");
                                    channelId = channelId.replaceAll("[^A-Za-z0-9_-]+", "_");

                                    ChannelTypeUID channelTypeUID;

                                    if (pfv.getPacketFieldSpec().getUnit().getUnitId() >= 0) {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                pfv.getPacketFieldSpec().getUnit().getUnitCodeText());
                                        // TODO: add precision
                                    } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Number) {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
                                    } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.DateTime) {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                                "DateTime");
                                        /*
                                         * so far there seems no reasonable type for WeekDay and Time types, so we just
                                         * make them strings
                                         */
                                        /*
                                         * } else if (pfv.getPacketFieldSpec().getType() ==
                                         * SpecificationFile.Type.WeekTime) {
                                         * channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                         * "WeekTime");
                                         * } else if (pfv.getPacketFieldSpec().getType() == SpecificationFile.Type.Time)
                                         * {
                                         * channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
                                         * "Time");
                                         */
                                    } else {
                                        channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, "None");
                                    }
                                    // TODO: use StringListType for interpreted String lists like Operation Status?

                                    String acceptedItemType = "String";

                                    Thing thing = thingHandler.getThing();
                                    switch (pfv.getPacketFieldSpec().getType()) {
                                        case DateTime:
                                            acceptedItemType = "DateTime";
                                            break;
                                        case WeekTime:
                                        case Number:
                                            acceptedItemType = "Number";
                                            break;
                                        case Time:
                                        default:
                                            acceptedItemType = "String";
                                            break;

                                    }

                                    if (thing.getChannel(channelId) == null && pfv.getRawValueDouble() != null) {
                                        if (pfv.getName().contains("date")) {
                                            logger.trace("adding channel {} as {} to {}", pfv.getName(), channelId,
                                                    thingHandler.getThing().getUID());
                                        }
                                        ThingBuilder thingBuilder = thingHandler.editThing();

                                        ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
                                        Channel channel = ChannelBuilder.create(channelUID, acceptedItemType)
                                                .withType(channelTypeUID).withLabel(pfv.getName()).build();

                                        thingBuilder.withChannel(channel).withLabel(thing.getLabel());

                                        thingHandler.updateThing(thingBuilder.build());
                                    }
                                    switch (pfv.getPacketFieldSpec().getType()) {
                                        case Number:
                                            Double dd = pfv.getRawValueDouble();
                                            if (dd != null) {
                                                thingHandler.setChannelValue(channelId, dd.doubleValue());
                                            } else {
                                                /*
                                                 * field not available in this packet, e. g. old firmware version not
                                                 * (yet) transmitting it
                                                 */
                                            }
                                            break;
                                        case DateTime:
                                            thingHandler.setChannelValue(channelId, pfv.getRawValueDate());
                                            break;
                                        case WeekTime:
                                        case Time:
                                        default:
                                            thingHandler.setChannelValue(channelId, pfv.formatTextValue(null, null));
                                    }
                                } else {
                                    logger.trace("ThingHandler for {} not registered.", thingType);
                                }
                            }
                        }

                    });

                    // Establish the connection
                    tcpConnection.connect();
                    Thread.sleep(1000); // after a reconnect wait 1 sec
                    isConnected = (tcpConnection.getConnectionState().equals(Connection.ConnectionState.CONNECTED));
                } catch (Exception e) {
                    logger.trace("Connection failed", e);

                    isConnected = false;
                }
                if (!isConnected) {
                    logger.info("Cannot establish connection to {}", ipAddress);
                } else {
                    updateStatus();
                }
            }
        }

    };

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    // Methods for ThingHandler
    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    // internal Methods

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels - nothing to do
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Resol bridge handler {}", this.toString());
        updateStatus();
        ResolBindingConfiguration configuration = getConfigAs(ResolBindingConfiguration.class);
        ipAddress = configuration.ipAddress;
        port = configuration.port;
        refreshInterval = configuration.refreshInterval;
        password = configuration.password;
        // TODO: check how OpenHab can be forced to ask for the password on bridge thing creation
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Resol bridge handler{}", this.toString());

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            try {
                tcpConnection.disconnect();
            } catch (IOException ioe) {
                // we don't care
            }
            pollingJob = null;
        }
        updateStatus(ThingStatus.OFFLINE); // Set all State to offline
    }

}
