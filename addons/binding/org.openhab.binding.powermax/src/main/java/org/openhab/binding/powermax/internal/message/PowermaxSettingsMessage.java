/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import java.util.Arrays;

import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for SETTINGS and SETTINGS_ITEM messages handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxSettingsMessage extends PowermaxBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(PowermaxSettingsMessage.class);

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxSettingsMessage(byte[] message) {
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
        int index = message[2] & 0x000000FF;
        int page = message[3] & 0x000000FF;
        int length = 0;

        if (getReceiveType() == PowermaxReceiveType.SETTINGS) {
            length = message.length - 6;
            updatedState.setUpdateSettings(Arrays.copyOfRange(message, 2, 2 + 2 + length));
        } else if (getReceiveType() == PowermaxReceiveType.SETTINGS_ITEM) {
            length = message[4] & 0x000000FF;
            byte[] data = new byte[length + 2];
            int i = 0;
            for (int j = 2; j <= 3; j++) {
                data[i++] = message[j];
            }
            for (int j = 0; j < length; j++) {
                data[i++] = message[j + 5];
            }
            updatedState.setUpdateSettings(data);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Received Powermax setting page {} index {} length {}", String.format("%02X (%d)", page, page),
                    String.format("%02X (%d)", index, index), String.format("%02X (%d)", length, length));
        }

        return updatedState;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        int index = message[2] & 0x000000FF;
        int page = message[3] & 0x000000FF;

        str += "\n - page = " + String.format("%02X (%d)", page, page);
        str += "\n - index = " + String.format("%02X (%d)", index, index);

        return str;
    }

}
