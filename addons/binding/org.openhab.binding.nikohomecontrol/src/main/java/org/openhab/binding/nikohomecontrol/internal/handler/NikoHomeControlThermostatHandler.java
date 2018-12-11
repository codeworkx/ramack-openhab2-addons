/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlThermostatHandler.class);

    @Nullable
    private volatile ScheduledFuture<?> refreshTimer; // used to refresh the remaining overrule time every minute

    public NikoHomeControlThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer thermostatId = ((Number) this.getConfig().get(CONFIG_THERMOSTAT_ID)).intValue();

        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute thermostat command "
                            + thermostatId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute thermostat command "
                            + thermostatId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: bridge communication not initialized when trying to execute thermostat command "
                            + thermostatId);
            return;
        }

        NhcThermostat nhcThermostat = nhcComm.getThermostats().get(thermostatId);
        if (nhcThermostat == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: thermostatId " + thermostatId
                            + " does not match a thermostat in the controller");
            return;
        }

        if (nhcComm.communicationActive()) {
            handleCommandSelection(nhcThermostat, channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                nhcComm.restartCommunication();
                // If still not active, take thing offline and return.
                if (!nhcComm.communicationActive()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Niko Home Control: communication socket error");
                    return;
                }
                // Also put the bridge back online
                nhcBridgeHandler.bridgeOnline();

                // And finally handle the command
                handleCommandSelection(nhcThermostat, channelUID, command);
            });
        }
    }

    private void handleCommandSelection(NhcThermostat nhcThermostat, ChannelUID channelUID, Command command) {
        logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

        if (REFRESH.equals(command)) {
            handleStateUpdate(nhcThermostat);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_MEASURED:
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_MODE:
                if (command instanceof DecimalType) {
                    // first reset the overrule
                    // nhcThermostat.executeOverruletime(0);
                    nhcThermostat.executeMode(((DecimalType) command).intValue());
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_SETPOINT:
                QuantityType<Temperature> setpoint = null;
                if (command instanceof QuantityType) {
                    setpoint = ((QuantityType<Temperature>) command).toUnit(CELSIUS);
                    // Always set the new setpoint temperature as an overrule
                    // If no overrule time is given yet, set the overrule to the configuration parameter
                    int time = nhcThermostat.getOverruletime();
                    if (time <= 0) {
                        time = ((Number) this.getConfig().get(CONFIG_OVERRULETIME)).intValue();
                    }
                    nhcThermostat.executeOverrule(Math.round(setpoint.floatValue() * 10), time);
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_OVERRULETIME:
                if (command instanceof DecimalType) {
                    Integer overruletime = ((DecimalType) command).intValue();
                    int overrule = nhcThermostat.getOverrule();
                    if (overruletime <= 0) {
                        overruletime = 0;
                        overrule = 0;
                    }
                    nhcThermostat.executeOverrule(overrule, overruletime);
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Niko Home Control: channel unknown " + channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer thermostatId = ((Number) config.get(CONFIG_THERMOSTAT_ID)).intValue();

        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for thermostat " + thermostatId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for thermostat " + thermostatId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();
        if (nhcComm == null || !nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: no connection with Niko Home Control, could not initialize thermostat "
                            + thermostatId);
            return;
        }

        NhcThermostat nhcThermostat = nhcComm.getThermostats().get(thermostatId);
        if (nhcThermostat == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: thermostatId does not match a thermostat in the controller " + thermostatId);
            return;
        }

        String thermostatLocation = nhcThermostat.getLocation();

        nhcThermostat.setThingHandler(this);

        if (thing.getLocation() == null) {
            thing.setLocation(thermostatLocation);
        }

        handleStateUpdate(nhcThermostat);

        logger.debug("Niko Home Control: thermostat intialized {}", thermostatId);
    }

    /**
     * Method to update state of all channels, called from Niko Home Control thermostat.
     *
     * @param nhcThermostat Niko Home Control thermostat
     *
     */
    public void handleStateUpdate(NhcThermostat nhcThermostat) {
        updateState(CHANNEL_MEASURED, new QuantityType<Temperature>(nhcThermostat.getMeasured() / 10.0, CELSIUS));

        long overruletime = nhcThermostat.getRemainingOverruletime();
        updateState(CHANNEL_OVERRULETIME, new DecimalType(overruletime));
        // refresh the remaining time every minute
        scheduleRefreshOverruletime(nhcThermostat);

        // If there is an overrule temperature set, use this in the setpoint channel, otherwise use the original
        // setpoint temperature
        if (overruletime == 0) {
            updateState(CHANNEL_SETPOINT, new QuantityType<Temperature>(nhcThermostat.getSetpoint() / 10.0, CELSIUS));
        } else {
            updateState(CHANNEL_SETPOINT, new QuantityType<Temperature>(nhcThermostat.getOverrule() / 10.0, CELSIUS));
        }

        updateState(CHANNEL_MODE, new DecimalType(nhcThermostat.getMode()));

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Method to update state of overruletime channel every minute with remaining time.
     *
     * @param NhcThermostat object
     *
     */
    private void scheduleRefreshOverruletime(NhcThermostat nhcThermostat) {
        cancelRefreshTimer();

        if (nhcThermostat.getRemainingOverruletime() <= 0) {
            return;
        }

        this.refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            long remainingTime = nhcThermostat.getRemainingOverruletime();
            updateState(CHANNEL_OVERRULETIME, new DecimalType(remainingTime));
            if (remainingTime <= 0) {
                cancelRefreshTimer();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void cancelRefreshTimer() {
        ScheduledFuture<?> timer = this.refreshTimer;
        if (timer != null) {
            timer.cancel(true);
        }
        this.refreshTimer = null;
    }
}
