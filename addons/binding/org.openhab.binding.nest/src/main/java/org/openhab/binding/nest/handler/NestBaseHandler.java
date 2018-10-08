/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;
import org.openhab.binding.nest.internal.data.Camera;
import org.openhab.binding.nest.internal.data.NestIdentifiable;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Thermostat;
import org.openhab.binding.nest.internal.listener.NestDeviceDataListener;
import org.openhab.binding.nest.internal.rest.NestUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author David Bennett - initial contribution
 * @author Martin van Wingerden - Splitted of NestBaseHandler
 * @author Wouter Born - Add generic update data type
 *
 * @param <T> the type of update data
 */
@NonNullByDefault
abstract class NestBaseHandler<T> extends BaseThingHandler implements NestDeviceDataListener, NestIdentifiable {
    private final Logger logger = LoggerFactory.getLogger(NestBaseHandler.class);

    private @Nullable T lastUpdate;

    NestBaseHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable T getLastUpdate() {
        return lastUpdate;
    }

    protected void setLastUpdate(T lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());
        if (getNestBridgeHandler() != null) {
            boolean success = getNestBridgeHandler().addDeviceDataListener(this);
            logger.debug("Adding {} with ID '{}' as device data listener, result: {}", getClass().getSimpleName(),
                    getId(), success);
        } else {
            logger.debug("Unable to add {} with ID '{}' as device data listener because bridge is null");
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for refresh");
    }

    @Override
    public void dispose() {
        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            handler.removeDeviceDataListener(this);
        }
    }

    protected void addUpdateRequest(String updatePath, String field, Object value) {
        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            // @formatter:off
            handler.addUpdateRequest(new NestUpdateRequest.Builder()
                .withBasePath(updatePath)
                .withIdentifier(getId())
                .withAdditionalValue(field, value)
                .build());
            // @formatter:on
        }
    }

    @Override
    public String getId() {
        return getDeviceId();
    }

    protected String getDeviceId() {
        return getConfigAs(NestDeviceConfiguration.class).deviceId;
    }

    protected @Nullable NestBridgeHandler getNestBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (NestBridgeHandler) bridge.getHandler() : null;
    }

    protected abstract State getChannelState(ChannelUID channelUID, T data);

    protected State getAsDateTimeTypeOrNull(@Nullable Date date) {
        if (date == null) {
            return UnDefType.NULL;
        }

        long offsetMillis = TimeZone.getDefault().getOffset(date.getTime());
        Instant instant = date.toInstant().plusMillis(offsetMillis);
        return new DateTimeType(ZonedDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()));
    }

    protected State getAsDecimalTypeOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new DecimalType(value);
    }

    protected State getAsOnOffTypeOrNull(@Nullable Boolean value) {
        return value == null ? UnDefType.NULL : value ? OnOffType.ON : OnOffType.OFF;
    }

    protected <U extends Quantity<U>> State getAsQuantityTypeOrNull(@Nullable Number value, Unit<U> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<U>(value, unit);
    }

    protected State getAsStringTypeOrNull(@Nullable Object value) {
        return value == null ? UnDefType.NULL : new StringType(value.toString());
    }

    protected State getAsStringTypeListOrNull(@Nullable Collection<? extends Object> values) {
        return values == null || values.isEmpty() ? UnDefType.NULL : new StringType(StringUtils.join(values, ","));
    }

    protected boolean isNotHandling(NestIdentifiable nestIdentifiable) {
        return !(getId().equals(nestIdentifiable.getId()));
    }

    protected void updateChannels(T data) {
        getThing().getChannels().forEach(c -> updateState(c.getUID(), getChannelState(c.getUID(), data)));
    }

    @Override
    public void onNewNestCameraData(Camera camera) {
        // can be overridden by subclasses for handling new camera data
    }

    @Override
    public void onNewNestSmokeDetectorData(SmokeDetector smokeDetector) {
        // can be overridden by subclasses for handling new smoke detector data
    }

    @Override
    public void onNewNestStructureData(Structure structure) {
        // can be overridden by subclasses for handling new structure data
    }

    @Override
    public void onNewNestThermostatData(Thermostat thermostat) {
        // can be overridden by subclasses for handling new thermostat data
    }
}
