/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.handler;

import java.util.SortedMap;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lametrictime.config.LaMetricTimeAppConfiguration;
import org.openhab.binding.lametrictime.internal.WidgetRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.lametrictime.api.LaMetricTime;
import org.syphr.lametrictime.api.local.ApplicationNotFoundException;
import org.syphr.lametrictime.api.local.model.Application;
import org.syphr.lametrictime.api.local.model.Widget;

/**
 * The {@link AbstractLaMetricTimeAppHandler} is the parent of all app handlers for
 * the LaMetric Time device.
 *
 * @author Gregory Moyer - Initial contribution
 */
public abstract class AbstractLaMetricTimeAppHandler extends BaseThingHandler implements LaMetricTimeAppHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractLaMetricTimeAppHandler.class);

    private Widget widget;

    public AbstractLaMetricTimeAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Verifying LaMetric Time device configuration");

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device bridge has been configured");
            return;
        }

        if (ThingStatus.ONLINE != bridge.getStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        updateWidget(bridge.getHandler());
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateWidget(getBridge().getHandler());
        }
    }

    private void updateWidget(ThingHandler handler) {
        if (!(handler instanceof LaMetricTimeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect bridge thing found");
            return;
        }
        LaMetricTimeHandler deviceHandler = (LaMetricTimeHandler) handler;

        logger.debug("Reading LaMetric Time app thing configuration");
        LaMetricTimeAppConfiguration config = getConfigAs(LaMetricTimeAppConfiguration.class);
        String packageName = getPackageName(config);
        try {
            Application app = deviceHandler.getClock().getApplication(packageName);

            SortedMap<String, Widget> widgets = app.getWidgets();
            if (config.widgetId != null) {
                widget = widgets.get(config.widgetId);

                if (widget == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "No widget found with package name " + packageName + " and widget ID " + config.widgetId);
                    return;
                }
            } else {
                widget = widgets.get(widgets.firstKey());
            }
        } catch (ApplicationNotFoundException e) {
            logger.debug("LaMetric Time application not found", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No application found with package name " + packageName);
            return;
        }
    }

    protected void updateActiveAppOnDevice() {
        String widgetId = new WidgetRef(widget.getPackageName(), widget.getId()).toString();
        ((LaMetricTimeHandler) getBridge().getHandler()).updateActiveApp(widgetId);
    }

    protected String getPackageName(LaMetricTimeAppConfiguration config) {
        return config.packageName;
    }

    @Override
    public void dispose() {
        widget = null;
    }

    @Override
    public Widget getWidget() {
        if (widget == null) {
            getBridge().getHandler().initialize();
        }
        return widget;
    }

    protected LaMetricTime getDevice() {
        return ((LaMetricTimeHandler) getBridge().getHandler()).getClock();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        try {
            if (command instanceof RefreshType) {
                // verify communication
                getDevice().getApplication(getWidget().getPackageName());
                return;
            }

            handleAppCommand(channelUID, command);
        } catch (Exception e) {
            logger.debug("Failed to communicate - taking app offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected abstract void handleAppCommand(ChannelUID channelUID, Command command);
}
