/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.dto.HueAuthorizedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advertises a Hue UPNP compatible bridge
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - Refactored
 */
@NonNullByDefault
public class HueEmulationUpnpServer implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(HueEmulationUpnpServer.class);

    /**
     * According to the UPnP specification, the minimum MaxAge is 1800 seconds.
     */
    static final int MIN_MAX_AGE_MSECS = 1800000;

    // jUPNP shares port 1900, but since this is multicast, we can also bind to it
    private static final int UPNP_PORT_RECV = 1900;
    private static final String MULTI_ADDR = "239.255.255.250";
    private final InetAddress MULTI_ADDR_IP;
    private boolean running;
    private final InetAddress address;
    private final String[] stVersions = { "", "", "" };
    private @Nullable Thread thread;
    private @Nullable DatagramSocket socket;
    private int webPort;

    /**
     * Server to send UDP packets onto the network when requested by a Hue API compatible device.
     *
     * @param relativePath The URI path where the discovery xml document can be retrieved
     * @param config The hue datastore. Contains the bridgeid and uuid.
     * @param address IP to advertise for UPNP
     */
    public HueEmulationUpnpServer(String relativePath, HueAuthorizedConfig config, InetAddress address, int webPort) {
        this.webPort = webPort;
        this.running = true;
        this.address = address;
        try {
            MULTI_ADDR_IP = InetAddress.getByName(MULTI_ADDR);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        final String[] stVersions = { "upnp:rootdevice", "urn:schemas-upnp-org:device:basic:1", "uuid:" + config.uuid };
        for (int i = 0; i < stVersions.length; ++i) {
            this.stVersions[i] = String.format(
                    "HTTP/1.1 200 OK\r\n" + "HOST: %s:%d\r\n" + "EXT:\r\n" + "CACHE-CONTROL: max-age=100\r\n"
                            + "LOCATION: %s\r\n" + "SERVER: FreeRTOS/7.4.2, UPnP/1.0, IpBridge/1.15.0\r\n"
                            + "hue-bridgeid: %s\r\n" + "ST: %s\r\n" + "USN: uuid:%s::upnp:rootdevice\r\n\r\n",
                    MULTI_ADDR, UPNP_PORT_RECV,
                    "http://" + address.getHostAddress().toString() + ":" + webPort + relativePath, config.bridgeid,
                    stVersions[i], config.uuid);
        }

    }

    public void start() {
        if (socket != null) {
            return;
        }

        running = true;
        Thread thread = new Thread(this);
        this.thread = thread;
        thread.start();
    }

    /**
     * Stops the upnp server from running
     *
     * @throws InterruptedException
     */
    public void shutdown() {
        Thread thread = this.thread;
        DatagramSocket socket = this.socket;
        if (thread == null || socket == null) {
            return;
        }

        this.running = false;
        socket.close();

        try {
            thread.join();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
        thread = null;
    }

    @Override
    public void run() {
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try (MulticastSocket recvSocket = new MulticastSocket(UPNP_PORT_RECV);
                DatagramSocket sendSocket = new DatagramSocket()) {
            recvSocket.setReuseAddress(true);
            recvSocket.setLoopbackMode(true);
            recvSocket.setSoTimeout(MIN_MAX_AGE_MSECS);
            this.socket = recvSocket;
            recvSocket.joinGroup(new InetSocketAddress(MULTI_ADDR, UPNP_PORT_RECV),
                    NetworkInterface.getByInetAddress(address));

            while (running) {
                try {
                    recvSocket.receive(recv);
                } catch (SocketTimeoutException ignore) {
                    // Broadcast every MIN_MAX_AGE_MSECS
                    sendUPNPDatagrams(recvSocket, MULTI_ADDR_IP, UPNP_PORT_RECV);
                    continue;
                }
                if (recv.getLength() == 0
                        || (recv.getAddress() == address && recv.getPort() == sendSocket.getLocalPort())) {
                    continue;
                }
                String data = new String(recv.getData());
                if (!data.startsWith("M-SEARCH")) {
                    continue;
                }

                sendUPNPDatagrams(sendSocket, recv.getAddress(), recv.getPort());
            }
        } catch (SocketException e) {
            if (running) {
                logger.warn("Socket error with UPNP server", e);
            }
        } catch (IOException e) {
            if (running) {
                logger.warn("IO Error with UPNP server", e);
            }
        }
        this.socket = null;
    }

    private void sendUPNPDatagrams(DatagramSocket sendSocket, InetAddress address, int port) {
        for (String msg : stVersions) {
            DatagramPacket response = new DatagramPacket(msg.getBytes(), msg.length(), address, port);
            try {
                logger.trace("Sending to {} : {}", address.getHostAddress(), msg);
                sendSocket.send(response);
            } catch (IOException e) {
                logger.warn("Could not send UPNP response: {}", e.getMessage());
            }
        }
    }

    InetAddress getAddress() {
        return address;
    }

    public int getWebPort() {
        return webPort;
    }
}
