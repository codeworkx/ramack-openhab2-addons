/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.resol.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
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

    private Logger logger = LoggerFactory.getLogger(ResolHandlerFactory.class);

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
            ResolBridgeHandler handler = new ResolBridgeHandler((Bridge) thing);
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
