/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.ProtocolConstants;
import org.openhab.binding.milight.internal.protocol.QueuedSend;

/**
 * Implements the RGB cold white / warm white bulb. It is the most feature rich bulb.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MilightV6RGBCWWWHandler extends AbstractLedV6Handler {
    private static final int ADDR = 0x08;

    public MilightV6RGBCWWWHandler(Thing thing, QueuedSend sendQueue) {
        super(thing, sendQueue, 10);
    }

    @Override
    protected byte getAddr() {
        return ADDR;
    }

    @Override
    public void setPower(boolean on, MilightThingState state) {
        sendRepeatableCat(ProtocolConstants.CAT_POWER_MODE, 4, on ? 1 : 2);
    }

    @Override
    public void whiteMode(MilightThingState state) {
        sendRepeatableCat(ProtocolConstants.CAT_POWER_MODE, 5, state.colorTemperature);
    }

    @Override
    public void nightMode(MilightThingState state) {
        sendRepeatableCat(ProtocolConstants.CAT_POWER_MODE, 4, 5);
    }

    @Override
    public void setColorTemperature(int colorTemp, MilightThingState state) {
        int ct = (colorTemp * MAX_TEMP) / 100;
        ct = Math.min(ct, MAX_TEMP);
        ct = Math.max(ct, 0);
        sendRepeatableCat(ProtocolConstants.CAT_TEMPERATURE_SET, 5, ct);
        state.colorTemperature = colorTemp;
    }

    @Override
    protected byte getBrCmd() {
        return 3;
    }

    @Override
    public void setSaturation(int value, MilightThingState state) {
        int br = (value * MAX_SAT) / 100; // map value from [0,100] -> [0,MAX_SAT]
        br = MAX_SAT - br; // inverse value
        br = Math.min(br, MAX_SAT); // force maximum value
        br = Math.max(br, 0); // force minimum value
        sendRepeatableCat(ProtocolConstants.CAT_SATURATION_SET, 2, br);
        state.saturation = value;
    }

    @Override
    public void setLedMode(int newmode, MilightThingState state) {
        int mode = Math.max(Math.min(newmode, 9), 1);
        sendRepeatableCat(ProtocolConstants.CAT_MODE_SET, 6, mode);
        state.animationMode = mode;
    }

    @Override
    public void changeSpeed(int relativeSpeed, MilightThingState state) {
        sendNonRepeatable(4, relativeSpeed > 1 ? 3 : 4);
    }
}
