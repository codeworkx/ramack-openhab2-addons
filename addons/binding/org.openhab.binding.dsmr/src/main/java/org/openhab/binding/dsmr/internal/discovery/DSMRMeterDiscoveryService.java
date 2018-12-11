/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.discovery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObjectType;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterDescriptor;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implements the discovery service for new DSMR Meters on a active DSMR bridge.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored code to detect meters during actual discovery phase.
 */
@NonNullByDefault
public class DSMRMeterDiscoveryService extends DSMRDiscoveryService implements P1TelegramListener {

    private final Logger logger = LoggerFactory.getLogger(DSMRMeterDiscoveryService.class);

    /**
     * The {@link DSMRBridgeHandler} instance
     */
    private final DSMRBridgeHandler dsmrBridgeHandler;

    /**
     * Constructs a new {@link DSMRMeterDiscoveryService} attached to the give bridge handler.
     *
     * @param dsmrBridgeHandler The bridge handler this discovery service is attached to
     */
    public DSMRMeterDiscoveryService(DSMRBridgeHandler dsmrBridgeHandler) {
        this.dsmrBridgeHandler = dsmrBridgeHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Start discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(true);
        dsmrBridgeHandler.registerDSMRMeterListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop discovery on existing DSMR bridge.");
        dsmrBridgeHandler.setLenientMode(false);
        super.stopScan();
        dsmrBridgeHandler.unregisterDSMRMeterListener(this);
    }

    @Override
    public void telegramReceived(P1Telegram telegram) {
        if (logger.isDebugEnabled()) {
            logger.debug("Detect meters from #{} objects", telegram.getCosemObjects().size());
        }
        final Entry<Collection<DSMRMeterDescriptor>, Map<CosemObjectType, CosemObject>> detectedMeters = meterDetector
                .detectMeters(telegram);
        verifyUnregisteredCosemObjects(telegram, detectedMeters.getValue());
        validateConfiguredMeters(dsmrBridgeHandler.getThing().getThings(),
                detectedMeters.getKey().stream().map(md -> md.getMeterType()).collect(Collectors.toSet()));
        detectedMeters.getKey().forEach(m -> meterDiscovered(m, dsmrBridgeHandler.getThing().getUID()));
    }

    protected void verifyUnregisteredCosemObjects(P1Telegram telegram,
            Map<CosemObjectType, CosemObject> undetectedCosemObjects) {
        if (!undetectedCosemObjects.isEmpty()) {
            if (undetectedCosemObjects.entrySet().stream()
                    .anyMatch(e -> e.getKey() == CosemObjectType.METER_EQUIPMENT_IDENTIFIER
                            && e.getValue().getCosemValues().entrySet().stream().anyMatch(
                                    cv -> cv.getValue() instanceof StringType && cv.getValue().toString().isEmpty()))) {
                // Unregistered meter detected. log to the user.
                reportUnregisteredMeters();
            } else {
                reportUnrecognizedCosemObjects(undetectedCosemObjects);
                logger.info(
                        "There are some unrecognized values, which means some meters might not be detected. Not all values are recognized. Please report your raw data as example:",
                        telegram.getRawTelegram());
            }
        }
    }

    /**
     * Called when Unrecognized cosem objects where found. This can be a bug or a new meter not yet supported.
     *
     * @param unidentifiedCosemObjects Map with the unrecognized.
     */
    protected void reportUnrecognizedCosemObjects(Map<CosemObjectType, CosemObject> unidentifiedCosemObjects) {
        unidentifiedCosemObjects
                .forEach((k, v) -> logger.debug("Unrecognized cosem object '{}' found in the data: {}", k, v));
    }

    /**
     * Called when a meter equipment identifier is found that has an empty value. This
     */
    protected void reportUnregisteredMeters() {
        logger.info(
                "An unregistered meter has been found. Probably a new meter. Retry discovery once the meter is registered with the energy provider.");
    }

    /**
     * Validates if the meters configured by the user match with what is detected in the telegram. Some meters are a
     * subset of other meters and therefore an invalid configured meter does work, but not all available data is
     * available to the user.
     *
     * @param things The list of configured things
     * @param configuredMeterTypes The set of meters detected in the telegram
     */
    private void validateConfiguredMeters(List<Thing> things, Set<DSMRMeterType> configuredMeterTypes) {
        // @formatter:off
        final Set<DSMRMeterType> configuredMeters = things.stream()
                .map(Thing::getHandler)
                .filter(DSMRMeterHandler.class::isInstance)
                .map(DSMRMeterHandler.class::cast)
                .map(DSMRMeterHandler::getMeterDescriptor)
                .filter(Objects::nonNull)
                .map(h -> h.getMeterType())
                .collect(Collectors.toSet());
        // @formatter:on
        // Create list of all configured meters that are not in the detected list. If not empty meters might not be
        // correctly configured.
        final List<DSMRMeterType> invalidConfigured = configuredMeters.stream()
                .filter(dm -> !configuredMeterTypes.contains(dm)).collect(Collectors.toList());
        // Create a list of all detected meters not yet configured.
        final List<DSMRMeterType> unconfiguredMeters = configuredMeterTypes.stream()
                .filter(dm -> !configuredMeters.contains(dm)).collect(Collectors.toList());

        if (!invalidConfigured.isEmpty()) {
            reportConfigurationValidationResults(invalidConfigured, unconfiguredMeters);
        }
    }

    /**
     * Called when the validation finds in inconsistency between configured meters.
     *
     * @param invalidConfigured The list of invalid configured meters
     * @param unconfiguredMeters The list of meters that were detected, but not configured
     */
    protected void reportConfigurationValidationResults(List<DSMRMeterType> invalidConfigured,
            List<DSMRMeterType> unconfiguredMeters) {
        logger.info(
                "Possible incorrect meters configured. These are configured: {}."
                        + "But the following unconfigured meters are found in the data received from the meter: {}",
                invalidConfigured.stream().map(m -> m.name()).collect(Collectors.joining(", ")),
                unconfiguredMeters.stream().map(m -> m.name()).collect(Collectors.joining(", ")));
    }

    public void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    public void unsetLocaleProvider() {
        this.localeProvider = null;
    }

    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetTranslationProvider() {
        this.i18nProvider = null;
    }
}
