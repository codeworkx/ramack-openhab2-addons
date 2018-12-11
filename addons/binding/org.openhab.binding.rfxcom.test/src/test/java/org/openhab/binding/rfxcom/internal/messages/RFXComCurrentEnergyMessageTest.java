/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComCurrentEnergyMessageTest {
    private void testMessage(String hexMsg, RFXComCurrentEnergyMessage.SubType subType, int seqNbr, String deviceId,
            int count, double channel1, double channel2, double channel3, double totalUsage, int signalLevel,
            int batteryLevel) throws RFXComException {
        final RFXComCurrentEnergyMessage msg = (RFXComCurrentEnergyMessage) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Count", count, msg.count);
        assertEquals("Channel 1", channel1, msg.channel1Amps, 0.01);
        assertEquals("Channel 2", channel2, msg.channel2Amps, 0.01);
        assertEquals("Channel 3", channel3, msg.channel3Amps, 0.01);
        assertEquals("Total usage", totalUsage, msg.totalUsage, 0.05);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);
        assertEquals("Battery Level", batteryLevel, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("135B0106B800000016000000000000006F148889", RFXComCurrentEnergyMessage.SubType.ELEC4, 6, "47104", 0,
                2.2d, 0d, 0d, 32547.4d, 8, 9);
        testMessage("135B014FB80002001D0000000000000000000079", RFXComCurrentEnergyMessage.SubType.ELEC4, 79, "47104",
                2, 2.9d, 0d, 0d, 0d, 7, 9);

    }
}
