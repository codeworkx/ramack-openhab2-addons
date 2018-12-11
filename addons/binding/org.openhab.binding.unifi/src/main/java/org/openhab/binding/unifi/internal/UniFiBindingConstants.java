/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UniFiBindingConstants} class defines common constants, which are
 * used across the UniFi binding.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiBindingConstants {

    public static final String BINDING_ID = "unifi";

    // List of all Thing Types
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_WIRED_CLIENT = new ThingTypeUID(BINDING_ID, "wiredClient");
    public static final ThingTypeUID THING_TYPE_WIRELESS_CLIENT = new ThingTypeUID(BINDING_ID, "wirelessClient");

    // List of common wired + wireless client channels
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_SITE = "site";
    public static final String CHANNEL_MAC_ADDRESS = "macAddress";
    public static final String CHANNEL_IP_ADDRESS = "ipAddress";
    public static final String CHANNEL_UPTIME = "uptime";
    public static final String CHANNEL_LAST_SEEN = "lastSeen";

    // List of additional wired client channels
    // ..coming soon..

    // List of additional wireless client channels
    public static final String CHANNEL_AP = "ap";
    public static final String CHANNEL_ESSID = "essid";
    public static final String CHANNEL_RSSI = "rssi";

    // List of all Parameters
    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_USERNAME = "username";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_SITE = "site";
    public static final String PARAMETER_CID = "cid";

}
