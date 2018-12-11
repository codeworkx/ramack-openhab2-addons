/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.config;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PointType;

import java.math.BigDecimal;

/**
 * The {@link ConfigHelper} class is a configuration helper for channels and profiles.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class ConfigHelper {
    //configuration constants
    public static final String CONFIG_TRACKER_ID = "trackerId";
    public static final String CONFIG_REGION_NAME = "regionName";
    public static final String CONFIG_REGION_RADIUS = "regionRadius";
    public static final String CONFIG_REGION_CENTER_LOCATION = "regionCenterLocation";

    /**
     * Constructor.
     */
    private ConfigHelper() {}

    public static double getRegionRadius(Configuration config) {
        return ((BigDecimal) config.get(CONFIG_REGION_RADIUS)).doubleValue();
    }

    public static String getRegionName(Configuration config) {
        return (String) config.get(CONFIG_REGION_NAME);
    }

    public static String getTrackerId(Configuration config) {
        return (String) config.get(CONFIG_TRACKER_ID);
    }

    public static PointType getRegionCenterLocation(Configuration config) {
        String location = (String) config.get(CONFIG_REGION_CENTER_LOCATION);
        return location != null ? new PointType(location): null;
    }
}
