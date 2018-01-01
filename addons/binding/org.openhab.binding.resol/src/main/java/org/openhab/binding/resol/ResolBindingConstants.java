/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link ResolBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolBindingConstants {

    public static final String BINDING_ID = "resol";

    public static final String BRIDGE_VBUSLAN = "vbuslan";

    // List of all ChannelTypeUIDs
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMP = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "sensor_temp");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_VOLUME = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "volume");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_VOLUME_FLOW = new ChannelTypeUID(
            ResolBindingConstants.BINDING_ID, "volume_flow");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRESSURE = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "pressure");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_POWER = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "power");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_ENERGY = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "energy");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_TIME = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "time");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_DATETIME = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "dateTime");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_STRING = new ChannelTypeUID(ResolBindingConstants.BINDING_ID,
            "String");

    // List of all Thing Type
    public static final String THING_ID_DEVICE = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UID_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_VBUSLAN);

    public static final ThingTypeUID THING_TYPE_UID_DEVICE = new ThingTypeUID(BINDING_ID, THING_ID_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE,
            THING_TYPE_UID_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE);

}
