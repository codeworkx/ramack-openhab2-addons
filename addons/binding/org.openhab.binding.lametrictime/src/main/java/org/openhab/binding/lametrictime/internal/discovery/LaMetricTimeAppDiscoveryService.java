/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.internal.discovery;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants;
import org.openhab.binding.lametrictime.internal.LaMetricTimeUtil;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeAppConfiguration;
import org.openhab.binding.lametrictime.internal.handler.LaMetricTimeAppHandler;
import org.openhab.binding.lametrictime.internal.handler.LaMetricTimeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.lametrictime.api.local.model.Application;
import org.syphr.lametrictime.api.local.model.Widget;
import org.syphr.lametrictime.api.model.CoreApps;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonPrimitive;

/**
 * The {@link LaMetricTimeAppDiscoveryService} is responsible for processing the
 * list of apps found on the LaMetric Time device.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LaMetricTimeAppDiscoveryService extends AbstractDiscoveryService {

    private static final Map<String, ThingTypeUID> CORE_APP_THING_TYPE_UIDS = Maps.newHashMap();
    static {
        CORE_APP_THING_TYPE_UIDS.put(CoreApps.clock().getPackageName(),
                LaMetricTimeBindingConstants.THING_TYPE_CLOCK_APP);
        CORE_APP_THING_TYPE_UIDS.put(CoreApps.countdown().getPackageName(),
                LaMetricTimeBindingConstants.THING_TYPE_COUNTDOWN_APP);
        CORE_APP_THING_TYPE_UIDS.put(CoreApps.radio().getPackageName(),
                LaMetricTimeBindingConstants.THING_TYPE_RADIO_APP);
        CORE_APP_THING_TYPE_UIDS.put(CoreApps.stopwatch().getPackageName(),
                LaMetricTimeBindingConstants.THING_TYPE_STOPWATCH_APP);
        CORE_APP_THING_TYPE_UIDS.put(CoreApps.weather().getPackageName(),
                LaMetricTimeBindingConstants.THING_TYPE_WEATHER_APP);
    }

    private static final int TIMEOUT = 60;

    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeAppDiscoveryService.class);

    private final LaMetricTimeHandler deviceHandler;

    /**
     * Discovers apps on the LaMetric Time device.
     *
     * @param deviceHandler the LaMetric Time device handler (bridge)
     */
    public LaMetricTimeAppDiscoveryService(final LaMetricTimeHandler deviceHandler) {
        super(Sets.newHashSet(CORE_APP_THING_TYPE_UIDS.values()), TIMEOUT, false);
        this.deviceHandler = deviceHandler;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scan for new apps");

        ThingUID bridgeUID = deviceHandler.getThing().getUID();
        List<Thing> existingThings = deviceHandler.getThing().getThings();

        for (Application app : deviceHandler.getApps().values()) {
            String packageName = app.getPackageName();

            ThingTypeUID thingType = CORE_APP_THING_TYPE_UIDS.get(packageName);
            if (thingType == null) {
                // skip generic apps
                continue;
            }

            for (Widget widget : app.getWidgets().values()) {
                String widgetId = widget.getId();
                ThingUID thingUID = new ThingUID(thingType, bridgeUID, widgetId);

                // check if thing already exists
                if (containsThing(existingThings, widgetId)) {
                    continue;
                }

                logger.debug("New app {} instance found with widget ID {}", packageName, widgetId);

                Map<String, Object> properties = Maps.newHashMap();
                properties.put(LaMetricTimeAppConfiguration.PACKAGE_NAME, app.getPackageName());
                properties.put(LaMetricTimeAppConfiguration.WIDGET_ID, widgetId);
                properties.put(Thing.PROPERTY_VENDOR, app.getVendor());
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, app.getVersion());

                Map<String, JsonPrimitive> settings = widget.getSettings();
                if (settings != null) {
                    settings.entrySet().stream().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
                }

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeUID).withLabel(LaMetricTimeUtil.getAppLabel(app, widget)).build();
                thingDiscovered(discoveryResult);
            }
        }
    }

    private boolean containsThing(List<Thing> things, String widgetId) {
        return things.stream().anyMatch(thing -> {
            ThingHandler handler = thing.getHandler();
            if (!(handler instanceof LaMetricTimeAppHandler)) {
                return false;
            }

            Widget widget = ((LaMetricTimeAppHandler) handler).getWidget();
            return widget.getId().equals(widgetId);
        });
    }
}
