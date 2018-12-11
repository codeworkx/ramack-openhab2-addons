/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.plclogo.handler.PLCAnalogHandler;
import org.openhab.binding.plclogo.handler.PLCBridgeHandler;
import org.openhab.binding.plclogo.handler.PLCDateTimeHandler;
import org.openhab.binding.plclogo.handler.PLCDigitalHandler;
import org.openhab.binding.plclogo.handler.PLCMemoryHandler;
import org.openhab.binding.plclogo.handler.PLCPulseHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * The {@link PLCLogoHandlerFactory} is responsible for creating things and
 * thing handlers supported by PLCLogo binding.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plclogo", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class PLCLogoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;
    static {
        Set<ThingTypeUID> buffer = new HashSet<>();
        buffer.add(THING_TYPE_DEVICE);
        buffer.add(THING_TYPE_MEMORY);
        buffer.add(THING_TYPE_ANALOG);
        buffer.add(THING_TYPE_DIGITAL);
        buffer.add(THING_TYPE_DATETIME);
        buffer.add(THING_TYPE_PULSE);
        SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(buffer);
    }

    /**
     * Constructor.
     */
    public PLCLogoHandlerFactory() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_DEVICE.equals(thing.getThingTypeUID()) && (thing instanceof Bridge)) {
            return new PLCBridgeHandler((Bridge) thing);
        } else if (THING_TYPE_ANALOG.equals(thing.getThingTypeUID())) {
            return new PLCAnalogHandler(thing);
        } else if (THING_TYPE_DIGITAL.equals(thing.getThingTypeUID())) {
            return new PLCDigitalHandler(thing);
        } else if (THING_TYPE_DATETIME.equals(thing.getThingTypeUID())) {
            return new PLCDateTimeHandler(thing);
        } else if (THING_TYPE_MEMORY.equals(thing.getThingTypeUID())) {
            return new PLCMemoryHandler(thing);
        } else if (THING_TYPE_PULSE.equals(thing.getThingTypeUID())) {
            return new PLCPulseHandler(thing);
        }

        return null;
    }

}
