/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.dsmr.internal.DSMRBindingConstants;
import org.openhab.binding.dsmr.internal.device.DSMRDeviceRunnable;
import org.openhab.binding.dsmr.internal.device.DSMREventListener;
import org.openhab.binding.dsmr.internal.device.DSMRSerialAutoDevice;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for detecting new DSMR Meters.
 *
 * The service will iterate over the available serial ports and open the given serial port and wait for telegrams. If
 * the port is already owned because it's already detected this service will ignore it. But it will report a warning in
 * case the port was locked due to a crash.
 * After {@link #BAUDRATE_SWITCH_TIMEOUT_SECONDS} seconds it will switch the baud rate and wait again for telegrams.
 * When that doesn't produce any results the service will give up (assuming no DSMR Bridge is present).
 *
 * If a telegram is received with at least 1 Cosem Object a bridge is assumed available and a Thing is added (regardless
 * if there were problems receiving the telegram) and the discovery is stopped.
 *
 * If there are communication problems the service will give an warning and give up
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.dsmr")
public class DSMRBridgeDiscoveryService extends DSMRDiscoveryService implements DSMREventListener {

    /**
     * The timeout used to switch baudrate if no valid data is received within that time frame.
     */
    private static final int BAUDRATE_SWITCH_TIMEOUT_SECONDS = 25;

    private final Logger logger = LoggerFactory.getLogger(DSMRBridgeDiscoveryService.class);

    /**
     * Serial Port Manager.
     */
    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    /**
     * DSMR Device that is scanned when discovery process in progress.
     */
    private @Nullable DSMRDeviceRunnable currentScannedDevice;

    /**
     * Name of the serial port that is scanned when discovery process in progress.
     */
    private String currentScannedPortName = "";

    /**
     * Keeps a boolean during time discovery process in progress.
     */
    private boolean scanning;

    /**
     * Starts a new discovery scan.
     *
     * All available Serial Ports are scanned for P1 telegrams.
     */
    @Override
    protected void startScan() {
        logger.debug("Started DSMR discovery scan");
        scanning = true;
        Stream<SerialPortIdentifier> portEnum = serialPortManager.getIdentifiers();

        // Traverse each available serial port
        portEnum.forEach(portIdentifier -> {
            if (scanning) {
                currentScannedPortName = portIdentifier.getName();
                if (portIdentifier.isCurrentlyOwned()) {
                    logger.trace("Possible port to check:{}, owned:{} by:{}", currentScannedPortName,
                            portIdentifier.isCurrentlyOwned(), portIdentifier.getCurrentOwner());
                    if (DSMRBindingConstants.DSMR_PORT_NAME.equals(portIdentifier.getCurrentOwner())) {
                        logger.debug("The port {} is owned by this binding. If no DSMR meters will be found it "
                                + "might indicate the port is locked by an older instance of this binding. "
                                + "Restart the system to unlock the port.", currentScannedPortName);
                    }
                } else {
                    logger.debug("Start discovery on serial port: {}", currentScannedPortName);
                    DSMRSerialAutoDevice device = new DSMRSerialAutoDevice(serialPortManager, portIdentifier.getName(),
                            this, scheduler, BAUDRATE_SWITCH_TIMEOUT_SECONDS);
                    device.setLenientMode(true);
                    currentScannedDevice = new DSMRDeviceRunnable(device, this);
                    currentScannedDevice.run();
                }
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        scanning = false;
        stopSerialPortScan();
        super.stopScan();
        logger.debug("Stop DSMR discovery scan");
    }

    /**
     * Stops the serial port device.
     */
    private void stopSerialPortScan() {
        logger.debug("Stop discovery scan on port [{}].", currentScannedPortName);
        if (currentScannedDevice != null) {
            currentScannedDevice.stop();
        }
        currentScannedDevice = null;
        currentScannedPortName = "";
    }

    /**
     * Handle if telegrams are received.
     *
     * If there are cosem objects received a new bridge will we discovered
     *
     * @param telegram the received telegram
     */
    @Override
    public void handleTelegramReceived(P1Telegram telegram) {
        List<CosemObject> cosemObjects = telegram.getCosemObjects();

        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Received {} cosemObjects", currentScannedPortName, cosemObjects.size());
        }
        if (!cosemObjects.isEmpty()) {
            bridgeDiscovered(telegram);
            stopSerialPortScan();
        }
    }

    /**
     *
     * Therefore this method will always return true
     *
     * @return true if bridge is accepted, false otherwise
     */
    private boolean bridgeDiscovered(P1Telegram telegram) {
        ThingUID thingUID = new ThingUID(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE,
                Integer.toHexString(currentScannedPortName.hashCode()));

        // Construct the configuration for this meter
        Map<String, Object> properties = new HashMap<>();
        properties.put("serialPort", currentScannedPortName);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(DSMRBindingConstants.THING_TYPE_DSMR_BRIDGE).withProperties(properties)
                .withLabel("@text/thing-type.dsmr.dsmrBridge.label").build();

        logger.debug("[{}] discovery result:{}", currentScannedPortName, discoveryResult);

        thingDiscovered(discoveryResult);
        meterDetector.detectMeters(telegram).getKey().forEach(m -> meterDiscovered(m, thingUID));
        return true;
    }

    @Override
    public void handleErrorEvent(DSMRConnectorErrorEvent portEvent) {
        logger.debug("[{}] Error on port during discovery: {}", currentScannedPortName, portEvent);
        stopSerialPortScan();
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
