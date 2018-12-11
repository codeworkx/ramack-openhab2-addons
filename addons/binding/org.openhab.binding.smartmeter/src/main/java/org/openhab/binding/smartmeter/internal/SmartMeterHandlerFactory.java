/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal;

import static org.openhab.binding.smartmeter.SmartMeterBindingConstants.THING_TYPE_SMLREADER;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SmartMeterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@NonNullByDefault
public class SmartMeterHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SMLREADER);
    private @NonNullByDefault({}) SmartMeterChannelTypeProvider channelProvider;
    private @NonNullByDefault({}) Supplier<SerialPortManager> serialPortManagerSupplier = () -> null;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Reference
    protected void setSmartMeterChannelTypeProvider(SmartMeterChannelTypeProvider provider) {
        this.channelProvider = provider;
    }

    protected void unsetSmartMeterChannelTypeProvider(SmartMeterChannelTypeProvider provider) {
        this.channelProvider = null;
    }

    @Reference
    protected void setSerialPortManager(SerialPortManager serialPortManager) {
        serialPortManagerSupplier = () -> serialPortManager;
    }

    protected void unsetSerialPortManager(SerialPortManager serialPortManager) {
        this.serialPortManagerSupplier = () -> null;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SMLREADER)) {
            return new SmartMeterHandler(thing, channelProvider, serialPortManagerSupplier);
        }

        return null;
    }

}
