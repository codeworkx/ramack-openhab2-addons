/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComThermostat2MessageTest {
    @Test(expected = RFXComMessageNotImplementedException.class)
    public void checkNotImplemented() throws Exception {
        // TODO Note that this message is supported in the 1.9 binding
        RFXComMessageFactory.createMessage(PacketType.THERMOSTAT2);
    }
}
