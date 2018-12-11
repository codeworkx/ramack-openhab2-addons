/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Represents Satel INT-RS module. Implements methods required to connect and
 * communicate with that module over serial protocol.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class IntRSModule extends SatelModule {

    private final Logger logger = LoggerFactory.getLogger(IntRSModule.class);

    private String port;

    /**
     * Creates new instance with port and timeout set to specified values.
     *
     * @param port
     *            serial port the module is connected to
     * @param timeout
     *            timeout value in milliseconds for connect/read/write
     *            operations
     * @throws ConfigurationException
     *             unconditionally throws this exception as it is not
     *             implemented yet
     */
    public IntRSModule(String port, int timeout) {
        super(timeout);

        this.port = port;
    }

    @Override
    protected CommunicationChannel connect() throws ConnectionFailureException {
        logger.info("Connecting to INT-RS module at {}", this.port);

        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.port);
            SerialPort serialPort = portIdentifier.open("org.openhab.binding.satel", 2000);
            serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.enableReceiveTimeout(this.getTimeout());
            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event
            // loop
            serialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent ev) {
                    try {
                        logger.trace("RXTX library CPU load workaround, sleep forever");
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                    }
                }
            });
            serialPort.notifyOnDataAvailable(true);

            logger.info("INT-RS module connected successfuly");
            return new SerialCommunicationChannel(serialPort);
        } catch (NoSuchPortException e) {
            throw new ConnectionFailureException(String.format("Port %s does not exist", this.port), e);
        } catch (PortInUseException e) {
            throw new ConnectionFailureException(String.format("Port %s in use", this.port), e);
        } catch (UnsupportedCommOperationException e) {
            throw new ConnectionFailureException(String.format("Unsupported comm operation on port %s", this.port), e);
        } catch (TooManyListenersException e) {
            throw new ConnectionFailureException(String.format("Too many listeners on port %s", this.port), e);
        }
    }

    private class SerialCommunicationChannel implements CommunicationChannel {

        private SerialPort serialPort;

        public SerialCommunicationChannel(SerialPort serialPort) {
            this.serialPort = serialPort;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.serialPort.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.serialPort.getOutputStream();
        }

        @Override
        public void disconnect() {
            logger.debug("Closing connection to INT-RS module");
            try {
                this.serialPort.close();
                logger.info("Connection to INT-RS module has been closed");
            } catch (Exception e) {
                logger.error("An error occurred during closing serial port", e);
            }
        }
    }
}
