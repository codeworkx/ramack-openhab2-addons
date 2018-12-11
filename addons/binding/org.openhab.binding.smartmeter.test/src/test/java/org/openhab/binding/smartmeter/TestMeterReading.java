/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.MeterDevice;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.MeterValueListener;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
public class TestMeterReading {

    @Test
    public void testContinousReading() throws Exception {
        final Duration period = Duration.ofSeconds(1);
        final int executionCount = 5;
        MockMeterReaderConnector connector = getMockedConnector(false, () -> new Object());
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        meter.readValues(Executors.newScheduledThreadPool(1), period);
        verify(changeListener, after(executionCount * period.toMillis() + period.toMillis() / 2).never())
                .errorOccurred(any());
        verify(changeListener, times(executionCount)).valueChanged(any());
    }

    @Test
    public void testRetryHandling() {
        final Duration period = Duration.ofSeconds(1);
        MockMeterReaderConnector connector = spy(getMockedConnector(true, () -> {
            throw new IllegalArgumentException();
        }));
        MeterDevice<Object> meter = getMeterDevice(connector);
        MeterValueListener changeListener = Mockito.mock(MeterValueListener.class);
        meter.addValueChangeListener(changeListener);
        meter.readValues(Executors.newScheduledThreadPool(1), period);
        verify(changeListener, after(
                period.toMillis() + 2 * period.toMillis() * ConnectorBase.NUMBER_OF_RETRIES + period.toMillis() / 2)
                        .times(1)).errorOccurred(any());
        verify(connector, times(ConnectorBase.NUMBER_OF_RETRIES)).retryHook(ArgumentMatchers.anyInt());
    }

    MockMeterReaderConnector getMockedConnector(boolean applyRetry, Supplier<Object> readNextSupplier) {
        return new MockMeterReaderConnector("Test port", applyRetry, readNextSupplier);
    }

    MeterDevice<Object> getMeterDevice(ConnectorBase<Object> connector) {
        return new MeterDevice<Object>(() -> mock(SerialPortManager.class), "id", "port", null, 9600, 0,
                ProtocolMode.SML) {

            @Override
            protected @NonNull IMeterReaderConnector<Object> createConnector(
                    @NonNull Supplier<@NonNull SerialPortManager> serialPortManagerSupplier, @NonNull String serialPort,
                    int baudrate, int baudrateChangeDelay, @NonNull ProtocolMode protocolMode) {
                return connector;
            }

            @Override
            protected <Q extends @NonNull Quantity<Q>> void populateValueCache(Object smlFile) {
                addObisCache(new MeterValue("123", "333", null));
            }

        };
    }
}
