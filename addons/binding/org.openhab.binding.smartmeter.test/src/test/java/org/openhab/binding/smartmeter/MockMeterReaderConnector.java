/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter;

import java.io.IOException;
import java.util.function.Supplier;

import org.openhab.binding.smartmeter.connectors.ConnectorBase;

/**
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
public class MockMeterReaderConnector extends ConnectorBase<Object> {

    private boolean applyRetry;
    private Supplier<Object> readNextSupplier;

    protected MockMeterReaderConnector(String portName, boolean applyRetry, Supplier<Object> readNextSupplier) {
        super(portName);
        this.applyRetry = applyRetry;
        this.readNextSupplier = readNextSupplier;
    }

    @Override
    public void openConnection() throws IOException {
    }

    @Override
    public void closeConnection() {

    }

    @Override
    protected Object readNext(byte[] initMessage) throws IOException {
        return readNextSupplier.get();
    }

    @Override
    protected boolean applyRetryHandling() {
        return this.applyRetry;
    }

    @Override
    protected boolean applyPeriod() {
        return true;
    }

    @Override
    protected void retryHook(int retryCount) {
        super.retryHook(retryCount);
    }
}
