/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.discovery;

import java.util.Date;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.handler.DeviceStatusListener;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDeviceDiscoveryService} class is used to discover MAX! Cube
 * devices that are connected to the Lan gateway.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxDeviceDiscoveryService extends AbstractDiscoveryService implements DeviceStatusListener {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(MaxDeviceDiscoveryService.class);

    private MaxCubeBridgeHandler maxCubeBridgeHandler;

    public MaxDeviceDiscoveryService(MaxCubeBridgeHandler maxCubeBridgeHandler) {
        super(MaxBinding.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME, true);
        this.maxCubeBridgeHandler = maxCubeBridgeHandler;
    }

    public void activate() {
        maxCubeBridgeHandler.registerDeviceStatusListener(this);
    }

    @Override
    public void deactivate() {
        maxCubeBridgeHandler.unregisterDeviceStatusListener(this);
        removeOlderResults(new Date().getTime());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MaxBinding.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        logger.trace("Adding new MAX! {} with id '{}' to smarthome inbox", device.getType(), device.getSerialNumber());
        ThingUID thingUID = null;
        switch (device.getType()) {
            case WallMountedThermostat:
                thingUID = new ThingUID(MaxBinding.WALLTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case HeatingThermostat:
                thingUID = new ThingUID(MaxBinding.HEATINGTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case HeatingThermostatPlus:
                thingUID = new ThingUID(MaxBinding.HEATINGTHERMOSTATPLUS_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case ShutterContact:
                thingUID = new ThingUID(MaxBinding.SHUTTERCONTACT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case EcoSwitch:
                thingUID = new ThingUID(MaxBinding.ECOSWITCH_THING_TYPE, bridge.getUID(), device.getSerialNumber());
                break;
            default:
                break;
        }
        if (thingUID != null) {
            String name = device.getName();
            if (name.isEmpty()) {
                name = device.getSerialNumber();
            }
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(MaxBinding.PROPERTY_SERIAL_NUMBER, device.getSerialNumber())
                    .withBridge(bridge.getUID()).withLabel(device.getType() + ": " + name)
                    .withRepresentationProperty(MaxBinding.PROPERTY_SERIAL_NUMBER).build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered MAX! device is unsupported: type '{}' with id '{}'", device.getType(),
                    device.getSerialNumber());
        }
    }

    @Override
    protected void startScan() {
        if (maxCubeBridgeHandler != null) {
            maxCubeBridgeHandler.clearDeviceList();
            maxCubeBridgeHandler.deviceInclusion();
        }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceConfigUpdate(Bridge bridge, Device device) {
        // this can be ignored here
    }
}
