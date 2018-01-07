/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ResolThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolThingHandler extends BaseThingHandler {

    private final @NonNull Logger logger = LoggerFactory.getLogger(ResolThingHandler.class);

    @Nullable
    ResolBridgeHandler bridgeHandler;

    public ResolThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String cmd = command.toString();
        logger.trace("command {} on {}", cmd, channelUID);
        // TODO ignoring bridgeHandler.updateChannel(getThing().getUID().getId(), channelUID.getId(),
        // command.toString());

        // if (channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        bridgeHandler = getBridgeHandler();
        logger.debug("Thing Handler for {} started", getThing().getUID().getId());
        registerResolThingListener(bridgeHandler);

    }

    @Override
    public void dispose() {
        logger.debug("Thing Handler for {} stop", getThing().getUID().getId());
        unregisterResolThingListener(bridgeHandler);
    }

    @Override
    public ThingBuilder editThing() {
        return super.editThing();
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateThing(Thing thing) {
        super.updateThing(thing);
    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler() {

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }

    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler(Bridge bridge) {

        ResolBridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof ResolBridgeHandler) {
            bridgeHandler = (ResolBridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }

    private void registerResolThingListener(@Nullable ResolBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.registerResolThingListener(this);
        } else {
            logger.debug("Can't register {} at bridge bridgeHandler is null.", this.getThing().getUID());
        }
    }

    private void unregisterResolThingListener(@Nullable ResolBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterThingListener(this);
        } else {
            logger.debug("Can't unregister {} at bridge bridgeHandler is null.", this.getThing().getUID());
        }

    }

    public void setChannelValue(String channelId, @Nullable String value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.trace("Cannel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
            return;
        }
        if (value == null) {
            logger.trace("Not setting cannel '{}:{}' to null", getThing().getUID().getId(), channelId);
            return;
        }

        logger.trace("Set {}:{}:{} = {}", getThing().getUID().getId(), channelId, channel.getAcceptedItemType(), value);
        String itmType = channel.getAcceptedItemType();
        // TODO check string type
        this.updateState(channelId, new StringType(value));
    }

    public void setChannelValue(String channelId, Date value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.trace("Cannel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
            return;
        }
        if (value == null) {
            logger.trace("Not setting cannel '{}:{}' to null", getThing().getUID().getId(), channelId);
            return;
        }

        logger.trace("Set {}:{}:{} = {}", getThing().getUID().getId(), channelId, channel.getAcceptedItemType(), value);
        String itmType = channel.getAcceptedItemType();
        this.updateState(channelId,
                new DateTimeType(new SimpleDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_GENERAL).format(value)));
    }

    public void setChannelValue(String channelId, double value) {
        Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.trace("Channel '{}:{}' not implemented", getThing().getUID().getId(), channelId);
            return;
        }

        logger.trace("Set {}:{}:{} = {}", getThing().getUID().getId(), channelId, channel.getAcceptedItemType(), value);
        String itmType = channel.getAcceptedItemType();
        if ("Number".equals(itmType)) {
            this.updateState(channelId, new DecimalType(value));
        } else {
            logger.trace("ItemType '{}' for channel '{}' not matching parameter type double",
                    channel.getAcceptedItemType(), channelId);
        }
    }

}
