/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.smartmeter.internal.ObisCode;

/**
 * The {@link SmlReaderBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault
public class SmartMeterBindingConstants {

    public static final String BINDING_ID = "smartmeter";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMLREADER = new ThingTypeUID(BINDING_ID, "meter");
    public static final String CONFIGURATION_PORT = "port";
    public static final String CONFIGURATION_SERIAL_MODE = "mode";
    public static final String CONFIGURATION_BAUDRATE = "baudrate";
    public static final String CONFIGURATION_CONFORMITY = "conformity";
    public static final String CONFIGURATION_INIT_MESSAGE = "initMessage";
    public static final String CONFIGURATION_CONVERSION = "conversionRatio";
    public static final String CONFIGURATION_CHANNEL_NEGATE = "negate";
    public static final String CHANNEL_PROPERTY_OBIS = "obis";
    public static final String OBIS_PATTERN_CHANNELID = getObisChannelId(ObisCode.OBIS_PATTERN);
    /** Obis format */
    public static final String OBIS_FORMAT_MINIMAL = "%d-%d:%d.%d.%d";
    /** Obis format */
    public static final String OBIS_FORMAT = OBIS_FORMAT_MINIMAL + "*%d";
    public static final String CHANNEL_TYPE_METERREADER_OBIS = "channel-type:" + BINDING_ID + ":obis";

    public static String getObisChannelId(String obis) {
        return obis.replaceAll("\\.", "-").replaceAll(":|\\*", "_");
    }
}
