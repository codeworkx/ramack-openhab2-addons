/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.handler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.evohome.internal.api.models.v2.response.Locations;
import org.openhab.binding.evohome.internal.configuration.EvohomeThingConfiguration;

/**
 * Base class for an evohome handler
 *
 * @author Jasper van Zuijlen - Initial contribution
 */
public abstract class BaseEvohomeHandler extends BaseThingHandler {
    private EvohomeThingConfiguration configuration;

    public BaseEvohomeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(EvohomeThingConfiguration.class);
        checkConfig();
    }

    @Override
    public void dispose() {
        configuration = null;
    }

    public String getId() {
        if (configuration != null) {
            return configuration.id;
        }
        return null;
    }

    /**
     * Returns the configuration of the Thing
     *
     * @return The parsed configuration or null
     */
    protected EvohomeThingConfiguration getEvohomeThingConfig() {
        return configuration;
    }

    /**
     * Retrieves the bridge
     *
     * @return The evohome brdige
     */
    protected EvohomeAccountBridgeHandler getEvohomeBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (EvohomeAccountBridgeHandler) bridge.getHandler();
        }

        return null;
    }

    /**
     * Retrieves the evohome configuration from the bridge
     *
     * @return The current evohome configuration
     */
    protected Locations getEvohomeConfig() {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        if (bridge != null) {
            return bridge.getEvohomeConfig();
        }

        return null;
    }

    /**
     * Retrieves the evohome configuration from the bridge
     *
     * @return The current evohome configuration
     */
    protected void requestUpdate() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ((EvohomeAccountBridgeHandler) bridge).getEvohomeConfig();
        }
    }

    /**
     * Updates the status of the evohome thing when it changes
     *
     * @param newStatus The new status to update to
     */
    protected void updateEvohomeThingStatus(ThingStatus newStatus) {
        updateEvohomeThingStatus(newStatus, ThingStatusDetail.NONE, null);
    }

    /**
     * Updates the status of the evohome thing when it changes
     *
     * @param newStatus The new status to update to
     * @param detail The status detail value
     * @param message The message to show with the status
     */
    protected void updateEvohomeThingStatus(ThingStatus newStatus, ThingStatusDetail detail, String message) {
        // Prevent spamming the log file
        if (!newStatus.equals(getThing().getStatus())) {
            updateStatus(newStatus, detail, message);
        }
    }

    /**
     * Checks the configuration for validity, result is reflected in the status of the Thing
     *
     * @param configuration The configuration to check
     */
    private void checkConfig() {
        if (configuration == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration is missing or corrupted");
        } else if (StringUtils.isEmpty(configuration.id)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Id not configured");
        }
    }

}
