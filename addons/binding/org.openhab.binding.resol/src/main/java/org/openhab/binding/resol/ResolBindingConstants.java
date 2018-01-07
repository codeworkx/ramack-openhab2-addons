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

    // List of all ChannelTypeUIDs is empty, as we got totally rid of static channel types.
    // ChannelTypeUIDs are constructed from the BINDING_ID and the UnitCodeTextIndex from teh VSF

    // List of all Thing Type
    public static final String THING_ID_DEVICE = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UID_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_VBUSLAN);

    public static final ThingTypeUID THING_TYPE_UID_DEVICE = new ThingTypeUID(BINDING_ID, THING_ID_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE,
            THING_TYPE_UID_DEVICE);

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_UID_BRIDGE);

}
