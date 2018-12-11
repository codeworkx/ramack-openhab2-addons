/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.Thing;

import java.util.HashMap;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.CONTACT;

/**
 * The {@link SomfyTahomaSmokeSensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the smoke sensor thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaSmokeSensorHandler extends SomfyTahomaContactSensorHandler {

    public SomfyTahomaSmokeSensorHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
            put(CONTACT, "core:SmokeState");
        }};
    }
}
