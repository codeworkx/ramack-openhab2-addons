/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LaMetricTimeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeBindingConstants {

    public static final String BINDING_ID = "lametrictime";

    // Bridge (device) thing
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // App things
    public static final ThingTypeUID THING_TYPE_CLOCK_APP = new ThingTypeUID(BINDING_ID, "clockApp");
    public static final ThingTypeUID THING_TYPE_COUNTDOWN_APP = new ThingTypeUID(BINDING_ID, "countdownApp");
    public static final ThingTypeUID THING_TYPE_RADIO_APP = new ThingTypeUID(BINDING_ID, "radioApp");
    public static final ThingTypeUID THING_TYPE_STOPWATCH_APP = new ThingTypeUID(BINDING_ID, "stopwatchApp");
    public static final ThingTypeUID THING_TYPE_WEATHER_APP = new ThingTypeUID(BINDING_ID, "weatherApp");

    // List of all Channel ids
    public static final String CHANNEL_NOTIFICATIONS_INFO = "info";
    public static final String CHANNEL_NOTIFICATIONS_WARN = "warning";
    public static final String CHANNEL_NOTIFICATIONS_ALERT = "alert";

    public static final String CHANNEL_DISPLAY_BRIGHTNESS = "brightness";
    public static final String CHANNEL_DISPLAY_BRIGHTNESS_MODE = "brightnessMode";
    public static final String CHANNEL_AUDIO_VOLUME = "volume";
    public static final String CHANNEL_BLUETOOTH_ACTIVE = "bluetooth";
    public static final String CHANNEL_APP = "app";

    public static final String CHANNEL_APP_COMMAND = "command";
    public static final String CHANNEL_APP_SET_ALARM = "setAlarm";
    public static final String CHANNEL_APP_DURATION = "duration";
    public static final String CHANNEL_APP_CONTROL = "control";

    // List of non-standard Properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BT_DISCOVERABLE = "bluetoothDiscoverable";
    public static final String PROPERTY_BT_AVAILABLE = "bluetoothAvailable";
    public static final String PROPERTY_BT_PAIRABLE = "bluetoothPairable";
    public static final String PROPERTY_BT_MAC = "bluetoothMAC";
    public static final String PROPERTY_BT_NAME = "bluetoothName";
}
