/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.model.Realtime;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo;

/**
 * Data class to store device state.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DeviceState {

    private final Commands commands = new Commands();
    private final Realtime realtime;
    private final Sysinfo sysinfo;

    /**
     * Initializes the device state given the json response from the device.
     *
     * @param state device state as json string
     */
    public DeviceState(String state) {
        sysinfo = commands.getSysinfoReponse(state);
        realtime = commands.getRealtimeResponse(state);
    }

    /**
     * @return returns the device energy data (if present)
     */
    public Realtime getRealtime() {
        return realtime;
    }

    /**
     * @return returns the device state
     */
    public Sysinfo getSysinfo() {
        return sysinfo;
    }
}
