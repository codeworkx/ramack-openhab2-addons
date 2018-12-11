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
import org.openhab.binding.rfxcom.internal.messages.RFXComLighting1Message.Commands;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComLighting1MessageTest {

    private void testMessage(String hexMsg, RFXComLighting1Message.SubType subType, int seqNbr, String deviceId,
            byte signalLevel, RFXComLighting1Message.Commands command) throws RFXComException {
        final RFXComLighting1Message msg = (RFXComLighting1Message) RFXComMessageFactory
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals("SubType", subType, msg.subType);
        assertEquals("Seq Number", seqNbr, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", deviceId, msg.getDeviceId());
        assertEquals("Signal Level", signalLevel, msg.signalLevel);
        assertEquals("Command", command, msg.command);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMsg, HexUtils.bytesToHex(decoded));
    }

    @Test
    public void testSomeMessages() throws RFXComException {
        testMessage("0710015242080780", RFXComLighting1Message.SubType.ARC, 82, "B.8", (byte) 8, Commands.CHIME);

        testMessage("0710010047010070", RFXComLighting1Message.SubType.ARC, 0, "G.1", (byte) 7, Commands.OFF);
        testMessage("071001014D090160", RFXComLighting1Message.SubType.ARC, 1, "M.9", (byte) 6, Commands.ON);
        testMessage("0710010543080060", RFXComLighting1Message.SubType.ARC, 5, "C.8", (byte) 6, Commands.OFF);
        testMessage("0710010B43080160", RFXComLighting1Message.SubType.ARC, 11, "C.8", (byte) 6, Commands.ON);

        testMessage("0710000843010150", RFXComLighting1Message.SubType.X10, 8, "C.1", (byte) 5, Commands.ON);
        testMessage("0710007F41010000", RFXComLighting1Message.SubType.X10, 127, "A.1", (byte) 0, Commands.OFF);
        testMessage("0710009A41010170", RFXComLighting1Message.SubType.X10, 154, "A.1", (byte) 7, Commands.ON);
    }

}
