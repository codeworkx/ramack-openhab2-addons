/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.discovery;

import static org.openhab.binding.kodi.KodiBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UpnpDiscoveryParticipant which allows to discover Kodi AVRs.
 *
 * @author Paul Frank
 *
 */
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class KodiUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(KodiUpnpDiscoveryParticipant.class);

    private boolean isAutoDiscoveryEnabled = true;
    private Set<ThingTypeUID> supportedThingTypes = SUPPORTED_THING_TYPES_UIDS;

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() != null) {
            String autoDiscoveryPropertyValue = (String) componentContext.getProperties().get("enableAutoDiscovery");
            if (StringUtils.isNotEmpty(autoDiscoveryPropertyValue)) {
                isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
            }
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? SUPPORTED_THING_TYPES_UIDS : Collections.emptySet();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID thingUid = getThingUID(device);
        if (thingUid != null) {
            String label = StringUtils.isEmpty(device.getDetails().getFriendlyName()) ? device.getDisplayString()
                    : device.getDetails().getFriendlyName();
            Map<String, Object> properties = new HashMap<>();
            properties.put(HOST_PARAMETER, device.getIdentity().getDescriptorURL().getHost());

            DiscoveryResult result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties)
                    .withRepresentationProperty(HOST_PARAMETER).build();

            return result;
        }
        return null;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (isAutoDiscoveryEnabled) {
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            if (StringUtils.containsIgnoreCase(manufacturer, MANUFACTURER)) {
                logger.debug("Manufacturer matched: search: {}, device value: {}.", MANUFACTURER,
                        device.getDetails().getManufacturerDetails().getManufacturer());
                if (StringUtils.containsIgnoreCase(device.getType().getType(), UPNP_DEVICE_TYPE)) {
                    logger.debug("Device type matched: search: {}, device value: {}.", UPNP_DEVICE_TYPE,
                            device.getType().getType());
                    return new ThingUID(THING_TYPE_KODI, device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }
        return null;
    }

}
