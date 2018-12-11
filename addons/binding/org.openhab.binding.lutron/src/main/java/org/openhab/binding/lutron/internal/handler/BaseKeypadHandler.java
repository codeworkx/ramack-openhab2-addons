/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.protocol.LutronCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Abstract class providing common definitions and methods for derived keypad classes
 *
 * @author Bob Adair - Initial contribution, based partly on Allan Tong's KeypadHandler class
 */
public abstract class BaseKeypadHandler extends LutronHandler {

    protected static final Integer ACTION_PRESS = 3;
    protected static final Integer ACTION_RELEASE = 4;
    protected static final Integer ACTION_LED_STATE = 9;

    protected static final Integer LED_OFF = 0;
    protected static final Integer LED_ON = 1;
    protected static final Integer LED_FLASH = 2; // Same as 1 on RA2 keypads
    protected static final Integer LED_RAPIDFLASH = 3; // Same as 1 on RA2 keypads

    private final Logger logger = LoggerFactory.getLogger(BaseKeypadHandler.class);

    protected List<KeypadComponent> buttonList = new ArrayList<>();
    protected List<KeypadComponent> ledList = new ArrayList<>();
    protected List<KeypadComponent> cciList = new ArrayList<>();

    protected int integrationId;
    protected String model;
    protected Boolean autoRelease;
    protected Boolean advancedChannels = false;

    protected BiMap<Integer, String> componentChannelMap = HashBiMap.create(50);

    protected abstract void configureComponents(String model);

    protected abstract boolean isLed(int id);

    protected abstract boolean isButton(int id);

    protected abstract boolean isCCI(int id);

    private final Object asyncInitLock = new Object();

    public BaseKeypadHandler(Thing thing) {
        super(thing);
    }

    protected void configureChannels() {
        Channel channel;
        ChannelTypeUID channelTypeUID;
        ChannelUID channelUID;

        logger.debug("Configuring channels for keypad {}", integrationId);

        List<Channel> channelList = new ArrayList<>();
        List<Channel> existingChannels = getThing().getChannels();

        if (existingChannels != null && !existingChannels.isEmpty()) {
            // Clear existing channels
            logger.debug("Clearing existing channels for keypad {}", integrationId);
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }

        ThingBuilder thingBuilder = editThing();

        // add channels for buttons
        for (KeypadComponent component : buttonList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "buttonAdvanced" : "button");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        // add channels for LEDs
        for (KeypadComponent component : ledList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, advancedChannels ? "ledIndicatorAdvanced" : "ledIndicator");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Switch").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        // add channels for CCIs (for VCRX or eventually HomeWorks CCI)
        for (KeypadComponent component : cciList) {
            channelTypeUID = new ChannelTypeUID(BINDING_ID, "cciState");
            channelUID = new ChannelUID(getThing().getUID(), component.channel());
            channel = ChannelBuilder.create(channelUID, "Contact").withType(channelTypeUID)
                    .withLabel(component.description()).build();
            channelList.add(channel);
        }

        thingBuilder.withChannels(channelList);
        updateThing(thingBuilder.build());
        logger.debug("Done configuring channels for keypad {}", integrationId);
    }

    protected ChannelUID channelFromComponent(int component) {
        String channel = null;

        // Get channel string from Lutron component ID using HashBiMap
        channel = componentChannelMap.get(component);
        if (channel == null) {
            logger.debug("Unknown component {}", component);
        }
        return channel == null ? null : new ChannelUID(getThing().getUID(), channel);
    }

    protected Integer componentFromChannel(ChannelUID channelUID) {
        return componentChannelMap.inverse().get(channelUID.getId());
    }

    @Override
    public int getIntegrationId() {
        return integrationId;
    }

    @Override
    public void initialize() {
        Number id = (Number) getThing().getConfiguration().get("integrationId");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No integrationId");
            return;
        }
        integrationId = id.intValue();

        logger.debug("Initializing Keypad Handler for integration ID {}", id);

        model = (String) getThing().getConfiguration().get("model");
        if (model != null) {
            model = model.toUpperCase();
            if (model.contains("-")) {
                // strip off system prefix if model is of the form "system-model"
                String[] modelSplit = model.split("-", 2);
                model = modelSplit[1];
            }
        }

        Boolean arParam = (Boolean) getThing().getConfiguration().get("autorelease");
        autoRelease = arParam == null ? true : arParam;

        // schedule a thread to finish initialization asynchronously since it can take several seconds
        scheduler.schedule(this::asyncInitialize, 0, TimeUnit.SECONDS);
    }

    private void asyncInitialize() {
        synchronized (asyncInitLock) {
            logger.debug("Async init thread staring for keypad handler {}", integrationId);

            configureComponents(model);

            // load the channel-id map
            for (KeypadComponent component : buttonList) {
                componentChannelMap.put(component.id(), component.channel());
            }
            for (KeypadComponent component : ledList) {
                componentChannelMap.put(component.id(), component.channel());
            }
            for (KeypadComponent component : cciList) {
                componentChannelMap.put(component.id(), component.channel());
            }

            configureChannels();

            initDeviceState();

            logger.debug("Async init thread finishing for keypad handler {}", integrationId);
        }
    }

    @Override
    public void initDeviceState() {
        synchronized (asyncInitLock) {
            logger.debug("Initializing device state for Keypad {}", integrationId);
            Bridge bridge = getBridge();
            if (bridge == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");
            } else if (bridge.getStatus() == ThingStatus.ONLINE) {
                if (ledList.isEmpty()) {
                    // Device with no LEDs has nothing to query. Assume it is online if bridge is online.
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    // Query LED states. Method handleUpdate() will set thing status to online when response arrives.
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Awaiting initial response");
                    // To reduce query volume, query only 1st LED and LEDs with linked channels.
                    for (KeypadComponent component : ledList) {
                        if (component.id() == ledList.get(0).id() || isLinked(channelFromComponent(component.id()))) {
                            queryDevice(component.id(), ACTION_LED_STATE);
                        }
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, Command command) {
        logger.debug("Handling command {} for channel {}", command, channelUID);

        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID, getThing().getUID());
            return;
        }

        Integer componentID = componentFromChannel(channelUID);
        if (componentID == null) {
            logger.warn("Command received on invalid channel {} for device {}", channelUID, getThing().getUID());
            return;
        }

        // For LEDs, handle RefreshType and OnOffType commands
        if (isLed(componentID)) {
            if (command instanceof RefreshType) {
                queryDevice(componentID, ACTION_LED_STATE);
            } else if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    device(componentID, ACTION_LED_STATE, LED_ON);
                } else if (command == OnOffType.OFF) {
                    device(componentID, ACTION_LED_STATE, LED_OFF);
                }
            } else {
                logger.warn("Invalid command {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }

        // For buttons and CCIs, handle OnOffType commands
        if (isButton(componentID) || isCCI(componentID)) {
            if (command instanceof OnOffType) {
                if (command == OnOffType.ON) {
                    device(componentID, ACTION_PRESS);
                    if (autoRelease) {
                        device(componentID, ACTION_RELEASE);
                    }
                } else if (command == OnOffType.OFF) {
                    device(componentID, ACTION_RELEASE);
                }
            } else {
                logger.warn("Invalid command type {} received for channel {} device {}", command, channelUID,
                        getThing().getUID());
            }
            return;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("Linking keypad {} channel {}", integrationId, channelUID.getId());

        Integer id = componentFromChannel(channelUID);
        if (id == null) {
            logger.warn("Unrecognized channel ID {} linked", channelUID.getId());
            return;
        }

        // if this channel is for an LED, query the Lutron controller for the current state
        if (isLed(id)) {
            queryDevice(id, ACTION_LED_STATE);
        }
        // Button and CCI state can't be queried, only monitored for updates.
        // Init button state to OFF on channel init.
        if (isButton(id)) {
            updateState(channelUID, OnOffType.OFF);
        }
        // Leave CCI channel state undefined on channel init.
    }

    @Override
    public void handleUpdate(LutronCommandType type, String... parameters) {
        logger.trace("Handling command {} {} from keypad {}", type, parameters, integrationId);
        if (type == LutronCommandType.DEVICE && parameters.length >= 2) {
            int component;

            try {
                component = Integer.parseInt(parameters[0]);
            } catch (NumberFormatException e) {
                logger.error("Invalid component {} in keypad update event message", parameters[0]);
                return;
            }

            ChannelUID channelUID = channelFromComponent(component);

            if (channelUID != null) {
                if (ACTION_LED_STATE.toString().equals(parameters[1]) && parameters.length >= 3) {
                    if (getThing().getStatus() == ThingStatus.UNKNOWN) {
                        updateStatus(ThingStatus.ONLINE); // set thing status online if this is an initial response
                    }
                    if (LED_ON.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.ON);
                    } else if (LED_OFF.toString().equals(parameters[2])) {
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (ACTION_PRESS.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.ON);
                    if (autoRelease) {
                        updateState(channelUID, OnOffType.OFF);
                    }
                } else if (ACTION_RELEASE.toString().equals(parameters[1])) {
                    updateState(channelUID, OnOffType.OFF);
                }
            } else {
                logger.warn("Unable to determine channel for component {} in keypad update event message",
                        parameters[0]);
            }
        }
    }

}
