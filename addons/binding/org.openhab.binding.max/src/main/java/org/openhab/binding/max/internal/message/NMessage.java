/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import java.nio.charset.StandardCharsets;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link: NMessage} contains information about a newly discovered Device
 * This is the response to a n: command
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public final class NMessage extends Message {
    private final Logger logger = LoggerFactory.getLogger(NMessage.class);

    private String decodedPayload;
    private DeviceType deviceType;
    private String rfAddress;
    private String serialnr;

    /**
     * The {@link: NMessage} contains information about a newly discovered Device
     *
     * @param raw String with raw message
     */
    public NMessage(String raw) {
        super(raw);
        String msgPayload = this.getPayload();

        if (msgPayload.length() > 0) {
            try {
                decodedPayload = new String(Base64.decodeBase64(msgPayload), StandardCharsets.UTF_8);
                byte[] bytes = Base64.decodeBase64(msgPayload);

                deviceType = DeviceType.create(bytes[0] & 0xFF);
                rfAddress = Utils.toHex(bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF);

                byte[] data = new byte[10];
                System.arraycopy(bytes, 4, data, 0, 10);
                serialnr = new String(data, StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.debug("Exception occurred during parsing of N message: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("No device found during inclusion");
        }
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getRfAddress() {
        return rfAddress;
    }

    public String getSerialNumber() {
        return serialnr;
    }

    @Override
    public void debug(Logger logger) {
        if (this.rfAddress != null) {
            logger.debug("=== N Message === ");
            logger.trace("\tRAW : {}", this.decodedPayload);
            logger.debug("\tDevice Type    : {}", this.deviceType);
            logger.debug("\tRF Address     : {}", this.rfAddress);
            logger.debug("\tSerial         : {}", this.serialnr);
        } else {
            logger.trace("=== N Message === ");
            logger.trace("\tRAW : {}", this.decodedPayload);
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.N;
    }
}
