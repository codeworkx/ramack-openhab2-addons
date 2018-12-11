/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.discovery;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDataListener;

import io.swagger.client.model.NAHealthyHomeCoach;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;
import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAWelcomeHome;
import io.swagger.client.model.NAWelcomeHomeData;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService implements NetatmoDataListener {
    private static final int SEARCH_TIME = 2;
    @NonNull
    private final NetatmoBridgeHandler netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(@NonNull NetatmoBridgeHandler netatmoBridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
        netatmoBridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        netatmoBridgeHandler.unregisterDataListener(this);
        super.deactivate();
    }

    @Override
    public void startScan() {
        if (netatmoBridgeHandler.configuration.readStation) {
            NAStationDataBody stationDataBody = netatmoBridgeHandler.getStationsDataBody(null);
            if (stationDataBody != null) {
                stationDataBody.getDevices().forEach(station -> {
                    discoverWeatherStation(station);
                });
            }
        }
        if (netatmoBridgeHandler.configuration.readHealthyHomeCoach) {
            NAHealthyHomeCoachDataBody homecoachDataBody = netatmoBridgeHandler.getHomecoachDataBody(null);
            if (homecoachDataBody != null) {
                homecoachDataBody.getDevices().forEach(homecoach -> {
                    discoverHomeCoach(homecoach);
                });
            }
        }
        if (netatmoBridgeHandler.configuration.readThermostat) {
            NAThermostatDataBody thermostatsDataBody = netatmoBridgeHandler.getThermostatsDataBody(null);
            if (thermostatsDataBody != null) {
                thermostatsDataBody.getDevices().forEach(plug -> {
                    discoverThermostat(plug);
                });
            }
        }
        if (netatmoBridgeHandler.configuration.readWelcome) {
            NAWelcomeHomeData welcomeHomeData = netatmoBridgeHandler.getWelcomeDataBody(null);
            if (welcomeHomeData != null) {
                welcomeHomeData.getHomes().forEach(home -> {
                    discoverWelcomeHome(home);
                });
            }
        }
        stopScan();
    }

    @Override
    public void onDataRefreshed(Object data) {
        if (data instanceof NAMain) {
            discoverWeatherStation((NAMain) data);
        } else if (data instanceof NAPlug) {
            discoverThermostat((NAPlug) data);
        } else if (data instanceof NAHealthyHomeCoach) {
            discoverHomeCoach((NAHealthyHomeCoach) data);
        } else if (data instanceof NAWelcomeHome) {
            discoverWelcomeHome((NAWelcomeHome) data);
        }
    }

    private void discoverThermostat(NAPlug plug) {
        onDeviceAddedInternal(plug.getId(), null, plug.getType(), plug.getStationName(), plug.getFirmware());
        plug.getModules().forEach(thermostat -> {
            onDeviceAddedInternal(thermostat.getId(), plug.getId(), thermostat.getType(), thermostat.getModuleName(),
                    thermostat.getFirmware());
        });
    }

    private void discoverHomeCoach(NAHealthyHomeCoach homecoach) {
        onDeviceAddedInternal(homecoach.getId(), null, homecoach.getType(), homecoach.getName(),
                homecoach.getFirmware());
    }

    private void discoverWeatherStation(NAMain station) {
        onDeviceAddedInternal(station.getId(), null, station.getType(), station.getStationName(),
                station.getFirmware());
        station.getModules().forEach(module -> {
            onDeviceAddedInternal(module.getId(), station.getId(), module.getType(), module.getModuleName(),
                    module.getFirmware());
        });
    }

    private void discoverWelcomeHome(NAWelcomeHome home) {
        // I observed that Thermostat homes are also reported here by Netatmo API
        // So I ignore homes that have an empty list of cameras
        if (home.getCameras().size() > 0) {
            onDeviceAddedInternal(home.getId(), null, WELCOME_HOME_THING_TYPE.getId(), home.getName(), null);
            // Discover Cameras
            home.getCameras().forEach(camera -> {
                onDeviceAddedInternal(camera.getId(), home.getId(), camera.getType(), camera.getName(), null);
            });

            // Discover Known Persons
            home.getPersons().stream().filter(person -> person.getPseudo() != null).forEach(person -> {
                onDeviceAddedInternal(person.getId(), home.getId(), WELCOME_PERSON_THING_TYPE.getId(),
                        person.getPseudo(), null);
            });
        }
    }

    private void onDeviceAddedInternal(String id, String parentId, String type, String name, Integer firmwareVersion) {
        ThingUID thingUID = findThingUID(type, id);
        Map<String, Object> properties = new HashMap<>();

        properties.put(EQUIPMENT_ID, id);
        if (parentId != null) {
            properties.put(PARENT_ID, parentId);
        }
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
            properties.put(Thing.PROPERTY_MODEL_ID, type);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        }
        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel)
                .withRepresentationProperty(EQUIPMENT_ID).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {
                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

}
