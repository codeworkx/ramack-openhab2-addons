/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for ZONESTYPE message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxZonesTypeMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxZonesTypeMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        int rowCnt = message[3] & 0x000000FF;

        for (int i = 1; i <= 8; i++) {
            int zoneIdx = (rowCnt - 1) * 8 + i;
            int zoneInfo = (message[3 + i] & 0x000000FF) - 0x0000001E;
            updatedState.updateZoneInfo(zoneIdx, zoneInfo);
        }

        return updatedState;
    }

}
