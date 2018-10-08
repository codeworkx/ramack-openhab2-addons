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
 * @since 1.9.0
 */
public class RFXComLighting2MessageTest {

    @Test
    public void testSomeMessages() throws RFXComException {
        String hexMessage = "0B11000600109B520B000080";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComLighting2Message msg = (RFXComLighting2Message) RFXComMessageFactory.createMessage(message);
        assertEquals("SubType", RFXComLighting2Message.SubType.AC, msg.subType);
        assertEquals("Seq Number", 6, (short) (msg.seqNbr & 0xFF));
        assertEquals("Sensor Id", "1088338.11", msg.getDeviceId());
        assertEquals("Command", RFXComLighting2Message.Commands.OFF, msg.command);
        assertEquals("Signal Level", (byte) 8, msg.signalLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
