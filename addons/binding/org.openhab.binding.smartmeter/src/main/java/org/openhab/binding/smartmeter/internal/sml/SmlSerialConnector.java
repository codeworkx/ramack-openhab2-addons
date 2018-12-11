/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal.sml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Stack;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.smartmeter.connectors.ConnectorBase;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.openhab.binding.smartmeter.internal.helper.SerialParameter;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.transport.Transport;

/**
 * Represents a serial SML device connector.
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Mathias Gilhuber - Also-By
 */
@NonNullByDefault
public final class SmlSerialConnector extends ConnectorBase<SmlFile> {

    private static final Transport TRANSPORT = new Transport();

    private Supplier<SerialPortManager> serialManagerSupplier;
    @NonNullByDefault({})
    private SerialPort serialPort;
    @Nullable
    private DataInputStream is;
    @Nullable
    private DataOutputStream os;
    private int baudrate;

    /**
     * Constructor to create a serial connector instance.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     */
    public SmlSerialConnector(Supplier<SerialPortManager> serialPortManagerSupplier, String portName) {
        super(portName);
        this.serialManagerSupplier = serialPortManagerSupplier;
    }

    /**
     * Constructor to create a serial connector instance with a specific serial parameter.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     * @param baudrate
     * @throws IOException
     */
    public SmlSerialConnector(Supplier<SerialPortManager> serialPortManagerSupplier, String portName, int baudrate,
            int baudrateChangeDelay) {
        this(serialPortManagerSupplier, portName);
        this.baudrate = baudrate;
    }

    @Override
    protected SmlFile readNext(byte @Nullable [] initMessage) throws IOException {
        if (initMessage != null) {
            logger.debug("Writing init message: {}", HexUtils.bytesToHex(initMessage, " "));
            if (os != null) {
                os.write(initMessage);
                os.flush();
            }
        }

        // read out the whole buffer. We are only interested in the most recent SML file.
        Stack<SmlFile> smlFiles = new Stack<>();
        do {
            smlFiles.push(TRANSPORT.getSMLFile(is));
        } while (is != null && is.available() > 0);
        if (smlFiles.isEmpty()) {
            throw new IOException(getPortName() + " : There is no SML file in buffer. Try to increase Refresh rate.");
        }
        logger.debug("{} : Read {} SML files from Buffer", this.getPortName(), smlFiles.size());
        return smlFiles.pop();
    }

    @Override
    public void openConnection() throws IOException {
        closeConnection();
        SerialPortIdentifier id = serialManagerSupplier.get().getIdentifier(getPortName());
        if (id != null) {
            try {
                serialPort = id.open("meterreaderbinding", 0);
            } catch (PortInUseException e) {
                throw new IOException(MessageFormat
                        .format("Error at SerialConnector.openConnection: unable to open port {0}.", getPortName()), e);
            }
            SerialParameter serialParameter = SerialParameter._8N1;
            int baudrateToUse = this.baudrate == Baudrate.AUTO.getBaudrate() ? Baudrate._9600.getBaudrate()
                    : this.baudrate;
            try {
                serialPort.setSerialPortParams(baudrateToUse, serialParameter.getDatabits(),
                        serialParameter.getStopbits(), serialParameter.getParity());
            } catch (UnsupportedCommOperationException e) {
                throw new IOException(MessageFormat.format(
                        "Error at SerialConnector.openConnection: unable to set serial port parameters for port {0}.",
                        getPortName()), e);
            }
            // serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.notifyOnDataAvailable(true);
            is = new DataInputStream(serialPort.getInputStream());
            os = new DataOutputStream(serialPort.getOutputStream());
        } else {
            throw new IllegalStateException(MessageFormat.format("No provider for port {0} found", getPortName()));
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void closeConnection() {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (IOException e) {
            logger.error("Failed to close serial input stream", e);
        }
        try {
            if (os != null) {
                os.close();
                os = null;
            }
        } catch (IOException e) {
            logger.error("Failed to close serial output stream", e);
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }

    @Override
    protected boolean applyPeriod() {
        return true;
    }

}
