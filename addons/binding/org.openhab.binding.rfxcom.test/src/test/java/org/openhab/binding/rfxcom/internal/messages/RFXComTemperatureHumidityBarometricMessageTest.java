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
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.ForecastStatus.RAIN;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.HumidityStatus.DRY;
import static org.openhab.binding.rfxcom.internal.messages.RFXComTemperatureHumidityBarometricMessage.SubType.THB2;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden
 */
public class RFXComTemperatureHumidityBarometricMessageTest {

    @Test
    public void testSomeMessages() throws RFXComException, RFXComMessageNotImplementedException {
        String hexMessage = "0D54020EE90000C9270203E70439";
        byte[] message = HexUtils.hexToBytes(hexMessage);
        RFXComTemperatureHumidityBarometricMessage msg = (RFXComTemperatureHumidityBarometricMessage) RFXComMessageFactory
                .createMessage(message);
        assertEquals("SubType", THB2, msg.subType);
        assertEquals("Seq Number", 14, msg.seqNbr);
        assertEquals("Sensor Id", "59648", msg.getDeviceId());
        assertEquals("Temperature", 20.1, msg.temperature, 0.01);
        assertEquals("Humidity", 39, msg.humidity);
        assertEquals("Humidity status", DRY, msg.humidityStatus);
        assertEquals("Barometer", 999.0, msg.pressure, 0.001);
        assertEquals("Forecast", RAIN, msg.forecastStatus);
        assertEquals("Signal Level", 3, msg.signalLevel);
        assertEquals("Battery Level", 9, msg.batteryLevel);

        byte[] decoded = msg.decodeMessage();

        assertEquals("Message converted back", hexMessage, HexUtils.bytesToHex(decoded));
    }
}
