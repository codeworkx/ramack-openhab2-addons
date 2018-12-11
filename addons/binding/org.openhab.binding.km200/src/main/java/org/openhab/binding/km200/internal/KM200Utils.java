/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200.internal;

import static org.openhab.binding.km200.KM200BindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KM200Utils is a class with common utilities.
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KM200Utils.class);

    /**
     * Translates a service name to a service path (Replaces # through /)
     *
     */
    public static String translatesNameToPath(String name) {
        return name.replace("#", "/");
    }

    /**
     * Translates a service path to a service name (Replaces / through #)
     *
     */
    public static String translatesPathToName(String path) {
        return path.replace("/", "#");
    }

    /**
     * This function checks whether the service has a replacement parameter
     *
     */
    public static String checkParameterReplacement(Channel channel, KM200Device device) {
        String service = KM200Utils.translatesNameToPath(channel.getProperties().get("root"));
        if (service.contains(SWITCH_PROGRAM_REPLACEMENT)) {
            String currentService = KM200Utils
                    .translatesNameToPath(channel.getProperties().get(SWITCH_PROGRAM_CURRENT_PATH_NAME));
            if (device.containsService(currentService)) {
                if ("stringValue".equals(device.getServiceObject(currentService).getServiceType())) {
                    String val = (String) device.getServiceObject(currentService).getValue();
                    service = service.replace(SWITCH_PROGRAM_REPLACEMENT, val);
                    return service;
                }
            }
        }
        return service;
    }

    /**
     * This function checks whether the channel has channel parameters
     *
     */
    public static Map<String, String> getChannelConfigurationStrings(Channel channel) {
        Map<String, String> paraNames = new HashMap<String, String>();
        if (channel.getConfiguration().containsKey("on")) {
            paraNames.put("on", channel.getConfiguration().get("on").toString());
            LOGGER.debug("Added ON: {}", channel.getConfiguration().get("on"));
        }

        if (channel.getConfiguration().containsKey("off")) {
            paraNames.put("off", channel.getConfiguration().get("off").toString());
            LOGGER.debug("Added OFF: {}", channel.getConfiguration().get("off"));
        }
        return paraNames;
    }
}
