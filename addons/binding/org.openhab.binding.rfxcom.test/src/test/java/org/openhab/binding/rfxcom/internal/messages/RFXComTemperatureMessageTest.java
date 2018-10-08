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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureMessage.SubType.*;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 * @since 1.9.0
 */
public class RFXComTemperatureMessageTest {
    private void testMessage(String hexMsg, RFXComTemperatureMessage.SubType subType, int seqNbr, String deviceId,
            double temperature, int signalLevel, int bateryLevel) throws RFXComException {
        final RFXComTemperatureMessage msg = (RFXComTemperatureMessage) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Temperature", temperature, msg.temperature, 0.001);
        assertEquals("Signal Level", signalLevel, msg.signalLevel);
        assertEquals("Battery", bateryLevel, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("08500110000180BC69", TEMP1, 16, "1", -18.8d, 6, 9);
        testMessage("0850021DFB0100D770", TEMP2, 29, "64257", 21.5d, 7, 0);
        testMessage("08500502770000D389", TEMP5, 2, "30464", 21.1d, 8, 9);
        testMessage("0850091A00C3800689", TEMP9, 26, "195", -0.6d, 8, 9);
        testMessage("0850097200C300E089", TEMP9, 114, "195", 22.4d, 8, 9);
    }
}
