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
 * A class for DOWNLOAD RETRY message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxDownloadRetryMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxDownloadRetryMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        byte[] message = getRawData();
        int waitTime = message[4] & 0x000000FF;

        commManager.sendMessageLater(PowermaxSendType.DOWNLOAD, waitTime);

        return null;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        int waitTime = message[4] & 0x000000FF;

        str += "\n - wait time = " + waitTime + " seconds";

        return str;
    }

}
