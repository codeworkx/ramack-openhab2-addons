/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PowermaxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxBindingConstants {

    public static final String BINDING_ID = "powermax";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_SERIAL = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID BRIDGE_TYPE_IP = new ThingTypeUID(BINDING_ID, "ip");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_X10 = new ThingTypeUID(BINDING_ID, "x10");

    // All supported Bridge types
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE_SERIAL, BRIDGE_TYPE_IP).collect(Collectors.toSet()));

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_ZONE, THING_TYPE_X10).collect(Collectors.toSet()));

    // List of all Channel ids
    public static final String MODE = "mode";
    public static final String TROUBLE = "trouble";
    public static final String ALERT_IN_MEMORY = "alert_in_memory";
    public static final String SYSTEM_STATUS = "system_status";
    public static final String READY = "ready";
    public static final String WITH_ZONES_BYPASSED = "with_zones_bypassed";
    public static final String ALARM_ACTIVE = "alarm_active";
    public static final String SYSTEM_ARMED = "system_armed";
    public static final String ARM_MODE = "arm_mode";
    public static final String TRIPPED = "tripped";
    public static final String LAST_TRIP = "last_trip";
    public static final String BYPASSED = "bypassed";
    public static final String ARMED = "armed";
    public static final String LOW_BATTERY = "low_battery";
    public static final String PGM_STATUS = "pgm_status";
    public static final String X10_STATUS = "x10_status";
    public static final String EVENT_LOG = "event_log_%s";
    public static final String UPDATE_EVENT_LOGS = "update_event_logs";
    public static final String DOWNLOAD_SETUP = "download_setup";

}
