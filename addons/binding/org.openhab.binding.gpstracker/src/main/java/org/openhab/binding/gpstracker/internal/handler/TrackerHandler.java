/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.handler;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.binding.gpstracker.internal.message.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.NotificationBroker;
import org.openhab.binding.gpstracker.internal.message.NotificationHandler;
import org.openhab.binding.gpstracker.internal.message.TransitionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.*;
import static org.openhab.binding.gpstracker.internal.config.ConfigHelper.CONFIG_REGION_CENTER_LOCATION;

/**
 * The {@link TrackerHandler} class is a tracker thing handler.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class TrackerHandler extends BaseThingHandler {
    /**
     * Trigger events
     */
    private static final String EVENT_ENTER = "enter";
    private static final String EVENT_LEAVE = "leave";

    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(TrackerHandler.class);

    /**
     * Notification handler
     */
    private NotificationHandler notificationHandler;

    /**
     * Notification broker
     */
    private NotificationBroker notificationBroker;

    /**
     * Id of the tracker represented by the thing
     */
    private String trackerId;

    /**
     * Map of regionName/distance channels
     */
    private Map<String, Channel> distanceChannelMap = new HashMap<>();

    /**
     * Map of last trigger events per region
     */
    private Map<String, Boolean> lastTriggeredStates = new HashMap<>();

    /**
     * Set of all regions referenced by distance channels and extended by the received transition messages.
     */
    private Set<String> regions;

    /**
     * System location
     */
    private PointType sysLocation;

    /**
     * Unit provider
     */
    private UnitProvider unitProvider;

    /**
     * Last message received from the tracker
     */
    private LocationMessage lastMessage;

    /**
     * Constructor.
     *
     * @param thing Thing.
     * @param notificationBroker Notification broker
     * @param regions Global region set
     * @param sysLocation Location of the system
     * @param unitProvider Unit provider
     */
    public TrackerHandler(Thing thing, NotificationBroker notificationBroker, Set<String> regions, PointType sysLocation, UnitProvider unitProvider) {
        super(thing);

        this.notificationBroker = notificationBroker;
        this.notificationHandler = new NotificationHandler();
        this.regions = regions;
        this.sysLocation = sysLocation;
        this.unitProvider = unitProvider;

        trackerId = ConfigHelper.getTrackerId(thing.getConfiguration());
        notificationBroker.registerHandler(trackerId, notificationHandler);

        logger.debug("Tracker handler created: {}", trackerId);
    }

    /**
     * Returns tracker id configuration of the thing.
     *
     * @return Tracker id
     */
    public String getTrackerId() {
        return trackerId;
    }

    @Override
    public void initialize() {
        if (sysLocation != null) {
            createBasicDistanceChannel();
        } else {
            logger.debug("System location is not set. Skipping system distance channel setup.");
        }

        mapDistanceChannels();
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Create distance channel for measuring the distance between the tracker and the szstem.
     */
    private void createBasicDistanceChannel() {
        @Nullable ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            //find the system distance channel
            ChannelUID systemDistanceChannelUID = new ChannelUID(thing.getUID(), CHANNEL_DISTANCE_SYSTEM_ID);
            Channel systemDistance = thing.getChannel(CHANNEL_DISTANCE_SYSTEM_ID);
            ChannelBuilder channelBuilder = null;
            if (systemDistance != null) {
                if (!systemDistance.getConfiguration().get(CONFIG_REGION_CENTER_LOCATION).equals(sysLocation.toFullString())) {
                    logger.trace("Existing distance channel for system. Changing system location config parameter: {}", sysLocation.toFullString());

                    channelBuilder = callback.editChannel(thing, systemDistanceChannelUID);
                    Configuration configToUpdate = systemDistance.getConfiguration();
                    configToUpdate.put(CONFIG_REGION_CENTER_LOCATION, sysLocation.toFullString());
                    channelBuilder.withConfiguration(configToUpdate);
                } else {
                    logger.trace("Existing distance channel for system. No change.");
                }
            } else {
                logger.trace("Creating missing distance channel for system.");

                Configuration config = new Configuration();
                config.put(ConfigHelper.CONFIG_REGION_NAME, CHANNEL_DISTANCE_SYSTEM_NAME);
                config.put(CONFIG_REGION_CENTER_LOCATION, sysLocation.toFullString());
                config.put(ConfigHelper.CONFIG_REGION_RADIUS, CHANNEL_DISTANCE_SYSTEM_RADIUS);

                channelBuilder = callback.createChannelBuilder(systemDistanceChannelUID, CHANNEL_TYPE_DISTANCE)
                        .withLabel("System Distance")
                        .withConfiguration(config);
            }

            //update the thing with system distance channel
            if (channelBuilder != null) {
                List<Channel> channels = new ArrayList<>(thing.getChannels());
                if (systemDistance != null) {
                    channels.remove(systemDistance);
                }
                channels.add(channelBuilder.build());

                ThingBuilder thingBuilder = editThing();
                thingBuilder.withChannels(channels);
                updateThing(thingBuilder.build());

                logger.debug("Distance channel created for system: {}", systemDistanceChannelUID);
            }
        }
    }

    /**
     * Create a map of all configured distance channels to handle channel updates easily.
     */
    private void mapDistanceChannels() {
        distanceChannelMap = thing.getChannels().stream()
                .filter(c -> CHANNEL_TYPE_DISTANCE.equals(c.getChannelTypeUID()))
                .collect(Collectors.toMap(c -> ConfigHelper.getRegionName(c.getConfiguration()), Function.identity()));
        //register the collected regions
        regions.addAll(distanceChannelMap.keySet());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && lastMessage != null) {
            String channelId = channelUID.getId();
            switch (channelId) {
                case CHANNEL_LAST_REPORT:
                    updateBaseChannels(lastMessage, CHANNEL_LAST_REPORT);
                    break;
                case CHANNEL_LAST_LOCATION:
                    updateBaseChannels(lastMessage, CHANNEL_LAST_LOCATION);
                    break;
                case CHANNEL_BATTERY_LEVEL:
                    updateBaseChannels(lastMessage, CHANNEL_BATTERY_LEVEL);
                    break;
                case CHANNEL_GPS_ACCURACY:
                    updateBaseChannels(lastMessage, CHANNEL_GPS_ACCURACY);
                    break;
                default: //distance channels
                    @Nullable Channel channel = thing.getChannel(channelId);
                    if (channel != null) {
                        updateDistanceChannelFromMessage(lastMessage, channel);
                    }
            }
        }
    }

    /**
     * Handle transition messages by firing the trigger channel with regionName/event payload.
     *
     * @param message TransitionMessage message.
     */
    private void updateTriggerChannelsWithTransition(TransitionMessage message) {
        String regionName = message.getRegionName();
        triggerRegionChannel(regionName, message.getEvent());
    }

    /**
     * Fire trigger event with regionName/enter|leave payload but only if the event differs from the last event.
     *
     * @param regionName Region name
     * @param event Occurred event
     */
    private void triggerRegionChannel(@NonNull String regionName, @NonNull String event) {
        Boolean lastState = lastTriggeredStates.get(regionName);
        Boolean newState = EVENT_ENTER.equals(event);
        if (!newState.equals(lastState) && lastState != null) {
            String payload = regionName + "/" + event;
            triggerChannel(CHANNEL_REGION_TRIGGER, payload);
            lastTriggeredStates.put(regionName, newState);
            logger.trace("Triggering {} for {}/{}", regionName, trackerId, payload);
        }
        lastTriggeredStates.put(regionName, newState);
    }

    /**
     * Update state channels from location message. This includes basic channel updates and recalculations of all distances.
     *
     * @param message Message.
     */
    private void updateChannelsWithLocation(LocationMessage message) {
        updateBaseChannels(message, CHANNEL_BATTERY_LEVEL, CHANNEL_LAST_LOCATION, CHANNEL_LAST_REPORT, CHANNEL_GPS_ACCURACY);

        String trackerId = message.getTrackerId();
        logger.debug("Updating distance channels tracker {}", trackerId);
        distanceChannelMap.values()
                .forEach(c -> updateDistanceChannelFromMessage(message, c));
    }

    private void updateDistanceChannelFromMessage(LocationMessage message, Channel c) {
        String regionName = ConfigHelper.getRegionName(c.getConfiguration());
        PointType center = ConfigHelper.getRegionCenterLocation(c.getConfiguration());
        PointType newLocation = message.getTrackerLocation();
        if (center != null) {
            double newDistance = newLocation.distanceFrom(center).doubleValue();
            updateState(c.getUID(), new QuantityType<>(newDistance / 1000, MetricPrefix.KILO(SIUnits.METRE)));
            logger.trace("Region {} center distance from tracker location {} is {}m", regionName, newLocation, newDistance);

            //fire trigger based on distance calculation only in case of pure location message
            if (!(message instanceof TransitionMessage)) {
                //convert into meters which is the unit of the calculated distance
                double radiusMeter = convertRadiusToMeters(ConfigHelper.getRegionRadius(c.getConfiguration()));
                if (radiusMeter > newDistance) {
                    triggerRegionChannel(regionName, EVENT_ENTER);
                } else {
                    triggerRegionChannel(regionName, EVENT_LEAVE);
                }
            }
        }
    }

    private double convertRadiusToMeters(double radius) {
        if (unitProvider != null) {
            @Nullable Unit<Length> unit = unitProvider.getUnit(Length.class);
            if (unit != null && !SIUnits.METRE.equals(unit)) {
                double value = ImperialUnits.YARD.getConverterTo(SIUnits.METRE).convert(radius);
                logger.trace("Region radius converted: {}yd->{}m", radius, value);
                return value;
            } else {
                logger.trace("System uses SI measurement units. No conversion is needed.");
            }
        } else {
            logger.trace("No unit provider. Considering region radius {} in meters.", radius);
        }
        return radius;
    }

    /**
     * Update basic channels: batteryLevel, lastLocation, lastReport
     *
     * @param message Received message.
     */
    private void updateBaseChannels(LocationMessage message, String... channels) {
        logger.debug("Update base channels for tracker {} from message: {}", trackerId, message);

        for (String channel : channels) {
            switch (channel) {
                case CHANNEL_LAST_REPORT:
                    DateTimeType timestamp = message.getTimestamp();
                    if (timestamp != null) {
                        updateState(CHANNEL_LAST_REPORT, timestamp);
                        logger.trace("{} -> {}", CHANNEL_LAST_REPORT, timestamp);
                    }
                    break;
                case CHANNEL_LAST_LOCATION:
                    PointType newLocation = message.getTrackerLocation();
                    if (newLocation != null) {
                        updateState(CHANNEL_LAST_LOCATION, newLocation);
                        logger.trace("{} -> {}", CHANNEL_LAST_LOCATION, newLocation);
                    }
                    break;
                case CHANNEL_BATTERY_LEVEL:
                    DecimalType batteryLevel = message.getBatteryLevel();
                    if (batteryLevel != null) {
                        updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
                        logger.trace("{} -> {}", CHANNEL_BATTERY_LEVEL, batteryLevel);
                    }
                    break;
                case CHANNEL_GPS_ACCURACY:
                    BigDecimal accuracy = message.getGpsAccuracy();
                    if (accuracy != null) {
                        updateState(CHANNEL_GPS_ACCURACY, new QuantityType<>(accuracy.intValue(), SIUnits.METRE));
                        logger.trace("{} -> {}", CHANNEL_GPS_ACCURACY, accuracy);
                    }
                    break;
            }
        }
    }

    /**
     * Location message handling.
     *
     * @param lm Location message
     */
    public void updateLocation(LocationMessage lm) {
        this.lastMessage = lm;
        updateStatus(ThingStatus.ONLINE);
        updateChannelsWithLocation(lm);
        notificationBroker.sendNotification(lm);
    }

    /**
     * Transition message handling
     *
     * @param tm Transition message
     */
    public void doTransition(TransitionMessage tm) {
        this.lastMessage = tm;
        updateStatus(ThingStatus.ONLINE);
        String regionName = tm.getRegionName();
        logger.debug("ConfigHelper transition event received: {}", regionName);
        regions.add(regionName);

        updateChannelsWithLocation(tm);
        updateTriggerChannelsWithTransition(tm);

        notificationBroker.sendNotification(tm);
    }

    /**
     * Get notification to return to the tracker (supported by OwnTracks only)
     *
     * @return List of notifications received from other trackers
     */
    public List<LocationMessage> getNotifications() {
        return notificationHandler.getNotifications();
    }
}
