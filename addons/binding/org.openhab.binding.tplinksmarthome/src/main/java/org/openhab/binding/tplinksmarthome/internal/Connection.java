/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class acts as and interface to the physical device.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Reorganized code an put connection in single class
 */
@NonNullByDefault
public class Connection {

    public static final int TP_LINK_SMART_HOME_PORT = 9999;

    private Logger logger = LoggerFactory.getLogger(Connection.class);

    private @Nullable String ipAddress;

    /**
     * Initializes a connection to the given ip address.
     *
     * @param ipAddress ip address of the connection
     */
    public Connection(@Nullable String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Set the ip address to connect to.
     *
     * @param ipAddress The ip address to connect to
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Sends the command, which is a json string, encrypted to the device and decrypts the json result and returns it
     *
     * @param command json command to send to the device
     * @return decrypted returned json result from the device
     * @throws IOException exception in case device not reachable
     */
    public String sendCommand(String command) throws IOException {
        logger.trace("Executing command: {}", command);
        try (Socket socket = createSocket(); final OutputStream outputStream = socket.getOutputStream()) {
            outputStream.write(CryptUtil.encryptWithLength(command));
            String response = readReturnValue(socket);

            logger.trace("Command response: {}", response);
            return response;
        }
    }

    /**
     * Reads and decrypts result returned from the device.
     *
     * @param socket socket to read result from
     * @return decrypted result
     * @throws IOException exception in case device not reachable
     */
    private String readReturnValue(Socket socket) throws IOException {
        try (InputStream is = socket.getInputStream()) {
            return CryptUtil.decryptWithLength(is);
        }
    }

    /**
     * Wrapper around socket creation to make mocking possible.
     *
     * @return new Socket instance
     * @throws UnknownHostException exception in case the host could not be determined
     * @throws IOException          exception in case device not reachable
     */
    protected Socket createSocket() throws UnknownHostException, IOException {
        if (ipAddress == null) {
            throw new IOException("Ip address not set. Wait for discovery or manually trigger discovery process.");
        }
        return new Socket(ipAddress, TP_LINK_SMART_HOME_PORT);
    }
}
