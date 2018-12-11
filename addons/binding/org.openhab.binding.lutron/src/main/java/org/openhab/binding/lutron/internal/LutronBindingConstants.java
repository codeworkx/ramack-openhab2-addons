/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LutronBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Allan Tong - Initial contribution
 */
@NonNullByDefault
public class LutronBindingConstants {

    public static final String BINDING_ID = "lutron";

    // Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_IPBRIDGE = new ThingTypeUID(BINDING_ID, "ipbridge");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_SHADE = new ThingTypeUID(BINDING_ID, "shade");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_OCCUPANCYSENSOR = new ThingTypeUID(BINDING_ID, "occupancysensor");
    public static final ThingTypeUID THING_TYPE_KEYPAD = new ThingTypeUID(BINDING_ID, "keypad");
    public static final ThingTypeUID THING_TYPE_TTKEYPAD = new ThingTypeUID(BINDING_ID, "ttkeypad");
    public static final ThingTypeUID THING_TYPE_PICO = new ThingTypeUID(BINDING_ID, "pico");
    public static final ThingTypeUID THING_TYPE_VIRTUALKEYPAD = new ThingTypeUID(BINDING_ID, "virtualkeypad");
    public static final ThingTypeUID THING_TYPE_VCRX = new ThingTypeUID(BINDING_ID, "vcrx");
    public static final ThingTypeUID THING_TYPE_CCO = new ThingTypeUID(BINDING_ID, "cco");
    public static final ThingTypeUID THING_TYPE_CCO_PULSED = new ThingTypeUID(BINDING_ID, "ccopulsed");
    public static final ThingTypeUID THING_TYPE_CCO_MAINTAINED = new ThingTypeUID(BINDING_ID, "ccomaintained");
    public static final ThingTypeUID THING_TYPE_TIMECLOCK = new ThingTypeUID(BINDING_ID, "timeclock");
    public static final ThingTypeUID THING_TYPE_GREENMODE = new ThingTypeUID(BINDING_ID, "greenmode");

    // List of all Channel ids
    public static final String CHANNEL_LIGHTLEVEL = "lightlevel";
    public static final String CHANNEL_SHADELEVEL = "shadelevel";
    public static final String CHANNEL_SWITCH = "switchstatus";
    public static final String CHANNEL_OCCUPANCYSTATUS = "occupancystatus";
    public static final String CHANNEL_CLOCKMODE = "clockmode";
    public static final String CHANNEL_SUNRISE = "sunrise";
    public static final String CHANNEL_SUNSET = "sunset";
    public static final String CHANNEL_EXECEVENT = "execevent";
    public static final String CHANNEL_ENABLEEVENT = "enableevent";
    public static final String CHANNEL_DISABLEEVENT = "disableevent";
    public static final String CHANNEL_STEP = "step";

    // Bridge config properties (used by discovery service)
    public static final String HOST = "ipAddress";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String SERIAL_NUMBER = "serialNumber";

    // Thing config properties
    public static final String INTEGRATION_ID = "integrationId";

    // CCO config properties
    public static final String OUTPUT_TYPE = "outputType";
    public static final String OUTPUT_TYPE_PULSED = "Pulsed";
    public static final String OUTPUT_TYPE_MAINTAINED = "Maintained";
    public static final String DEFAULT_PULSE = "pulseLength";

    // GreenMode config properties
    public static final String POLL_INTERVAL = "pollInterval";
}
