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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.util.Base64;
import org.openhab.binding.max.internal.Utils;
import org.openhab.binding.max.internal.device.DeviceInformation;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.RoomInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The M message contains metadata about the MAX! Cube setup.
 *
 * @author Andreas Heil (info@aheil.de) - Initial Contribution
 * @author Marcel Verpaalen - Room details parse
 */
public final class MMessage extends Message {
    private final Logger logger = LoggerFactory.getLogger(MMessage.class);

    public List<RoomInformation> rooms;
    public List<DeviceInformation> devices;
    private Boolean hasConfiguration;

    public MMessage(String raw) {
        super(raw);
        hasConfiguration = false;

        String[] tokens = this.getPayload().split(Message.DELIMETER);

        if (tokens.length <= 1) {
            logger.debug("No rooms defined. Configure your Max! Cube");
            hasConfiguration = false;
            return;
        }
        try {
            byte[] bytes = Base64.decodeBase64(tokens[2].getBytes(StandardCharsets.UTF_8));

            hasConfiguration = true;
            logger.trace("*** M Message trace**** ");
            logger.trace("\tMagic? (expect 86) : {}", (int) bytes[0]);
            logger.trace("\tVersion? (expect 2): {}", (int) bytes[1]);
            logger.trace("\t#defined rooms in M: {}", (int) bytes[2]);

            rooms = new ArrayList<>();
            devices = new ArrayList<>();

            int roomCount = bytes[2];

            int byteOffset = 3; // start of rooms

            /* process room */

            for (int i = 0; i < roomCount; i++) {

                int position = bytes[byteOffset++];

                int nameLength = bytes[byteOffset++] & 0xff;
                byte[] data = new byte[nameLength];
                System.arraycopy(bytes, byteOffset, data, 0, nameLength);
                byteOffset += nameLength;
                String name = new String(data, StandardCharsets.UTF_8);

                String rfAddress = Utils.toHex((bytes[byteOffset] & 0xff), (bytes[byteOffset + 1] & 0xff),
                        (bytes[byteOffset + 2] & 0xff));
                byteOffset += 3;

                rooms.add(new RoomInformation(position, name, rfAddress));
            }

            /* process devices */

            int deviceCount = bytes[byteOffset++];

            for (int deviceId = 0; deviceId < deviceCount; deviceId++) {
                DeviceType deviceType = DeviceType.create(bytes[byteOffset++]);

                String rfAddress = Utils.toHex((bytes[byteOffset] & 0xff), (bytes[byteOffset + 1] & 0xff),
                        (bytes[byteOffset + 2] & 0xff));
                byteOffset += 3;

                final StringBuilder serialNumberBuilder = new StringBuilder(10);

                for (int i = 0; i < 10; i++) {
                    serialNumberBuilder.append((char) bytes[byteOffset++]);
                }

                int nameLength = bytes[byteOffset++] & 0xff;
                byte[] data = new byte[nameLength];
                System.arraycopy(bytes, byteOffset, data, 0, nameLength);
                byteOffset += nameLength;
                String deviceName = new String(data, StandardCharsets.UTF_8);

                int roomId = bytes[byteOffset++] & 0xff;
                devices.add(new DeviceInformation(deviceType, serialNumberBuilder.toString(), rfAddress, deviceName,
                        roomId));
            }
        } catch (Exception e) {
            logger.debug("Unknown error parsing the M Message: {}", e.getMessage(), e);
            logger.debug("\tRAW : {}", this.getPayload());
        }
    }

    @Override
    public void debug(Logger logger) {
        logger.debug("=== M Message === ");
        if (hasConfiguration) {
            logger.trace("\tRAW : {}", this.getPayload());
            for (RoomInformation room : rooms) {
                logger.debug("\t=== Rooms ===");
                logger.debug("\tRoom Pos   : {}", room.getPosition());
                logger.debug("\tRoom Name  : {}", room.getName());
                logger.debug("\tRoom RF Adr: {}", room.getRFAddress());
                for (DeviceInformation device : devices) {
                    if (room.getPosition() == device.getRoomId()) {
                        logger.debug("\t=== Devices ===");
                        logger.debug("\tDevice Type    : {}", device.getDeviceType());
                        logger.debug("\tDevice Name    : {}", device.getName());
                        logger.debug("\tDevice Serialnr: {}", device.getSerialNumber());
                        logger.debug("\tDevice RF Adr  : {}", device.getRFAddress());
                        logger.debug("\tRoom Id        : {}", device.getRoomId());
                    }
                }

            }
        } else {
            logger.debug("M Message empty. No Configuration");
        }
    }

    @Override
    public MessageType getType() {
        return MessageType.M;
    }
}
