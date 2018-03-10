/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.resol.ResolBindingConstants;
import org.openhab.binding.resol.handler.ResolBridgeHandler;
import org.openhab.binding.resol.handler.ResolThingHandler;
import org.openhab.binding.resol.internal.discovery.ResolDiscoveryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ResolHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Raphael Mack - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.resol")
@NonNullByDefault
public class ResolHandlerFactory extends BaseThingHandlerFactory {

    private @NonNull Logger logger = LoggerFactory.getLogger(ResolHandlerFactory.class);

    private @Nullable LocaleProvider localeProvider;

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ResolBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ResolBindingConstants.THING_TYPE_UID_DEVICE)) {
            return new ResolThingHandler(thing);
        }

        if (thingTypeUID.equals(ResolBindingConstants.THING_TYPE_UID_BRIDGE)) {
            ResolBridgeHandler handler = new ResolBridgeHandler((Bridge) thing, localeProvider);
            registerThingDiscovery(handler);
            return handler;
        }

        return null;
    }

    private synchronized void registerThingDiscovery(ResolBridgeHandler bridgeHandler) {
        ResolDiscoveryService discoveryService = new ResolDiscoveryService(bridgeHandler);
        logger.trace("Try to register VBUS Discovery service on BundleID: {} Service: {}",
                bundleContext.getBundle().getBundleId(), DiscoveryService.class.getName());

        Hashtable<String, String> prop = new Hashtable<String, String>();

        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, prop);
        discoveryService.activate();
    }

}
