/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

/**
 * Used to map received messages from the Visonic alarm panel to a ENUM value
 *
 * @author Laurent Garnier - Initial contribution
 */
public enum PowermaxReceiveType {

    ACK((byte) 0x02, 0, false),
    TIMEOUT((byte) 0x06, 0, false),
    DENIED((byte) 0x08, 0, true),
    STOP((byte) 0x0B, 0, true),
    DOWNLOAD_RETRY((byte) 0x25, 14, true),
    SETTINGS((byte) 0x33, 14, true),
    INFO((byte) 0x3C, 14, true),
    SETTINGS_ITEM((byte) 0x3F, 0, true),
    EVENT_LOG((byte) 0xA0, 15, true),
    ZONESNAME((byte) 0xA3, 15, true),
    STATUS((byte) 0xA5, 15, true),
    ZONESTYPE((byte) 0xA6, 15, true),
    PANEL((byte) 0xA7, 15, true),
    POWERLINK((byte) 0xAB, 15, false),
    POWERMASTER((byte) 0xB0, 0, true),
    F1((byte) 0xF1, 9, false);

    private byte code;
    private int length;
    private boolean ackRequired;

    private PowermaxReceiveType(byte code, int length, boolean ackRequired) {
        this.code = code;
        this.length = length;
        this.ackRequired = ackRequired;
    }

    /**
     * @return the code identifying the message (second byte in the message)
     */
    public byte getCode() {
        return code;
    }

    /**
     * @return the message expected length
     */
    public int getLength() {
        return length;
    }

    /**
     * @return true if the received message requires the sending of an ACK, false if not
     */
    public boolean isAckRequired() {
        return ackRequired;
    }

    /**
     * Get the ENUM value from its identifying code
     *
     * @param code the identifying code
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this code
     */
    public static PowermaxReceiveType fromCode(byte code) throws IllegalArgumentException {
        for (PowermaxReceiveType messageType : PowermaxReceiveType.values()) {
            if (messageType.getCode() == code) {
                return messageType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
