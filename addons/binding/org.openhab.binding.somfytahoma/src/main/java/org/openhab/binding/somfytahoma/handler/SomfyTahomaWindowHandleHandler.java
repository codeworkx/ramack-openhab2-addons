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

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.HANDLE_STATE;

/**
 * The {@link SomfyTahomaWindowHandleHandler} is responsible for handling commands,
 * which are sent to one of the channels of the window handle thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaWindowHandleHandler extends SomfyTahomaBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaWindowHandleHandler.class);

    public SomfyTahomaWindowHandleHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
                put(HANDLE_STATE, "core:ThreeWayHandleDirectionState");
            }};
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} for channel {}", command, channelUID);
        if (!HANDLE_STATE.equals(channelUID.getId())) {
            return;
        }

        if (RefreshType.REFRESH.equals(command)) {
            updateChannelState(channelUID);
        }
    }
}
