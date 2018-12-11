/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaHeatingSystemHandler} is responsible for handling commands,
 * which are sent to one of the channels of the heating system thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaHeatingSystemHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaHeatingSystemHandler.class);

    public SomfyTahomaHeatingSystemHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {
            {
                put(TARGET_TEMPERATURE, "core:TargetTemperatureState");
                put(CURRENT_TEMPERATURE, "zwave:SetPointHeatingValueState");
                put(BATTERY_LEVEL, "core:BatteryLevelState");
                put(CURRENT_STATE, "zwave:SetPointTypeState");
            }
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        } else {
            if (TARGET_TEMPERATURE.equals(channelUID.getId())) {
                String param = "[" + command.toString() + "]";
                sendCommand("setTargetTemperature", param);
            }
        }
    }
}
