/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.eclipse.smarthome.core.util.HexUtils;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.binding.smartmeter.SmartMeterConfiguration;
import org.openhab.binding.smartmeter.internal.conformity.Conformity;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

/**
 * The {@link SmartMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Steigenberger - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.ARRAY_CONTENTS, DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE,
        DefaultLocation.TYPE_ARGUMENT })
public class SmartMeterHandler extends BaseThingHandler {

    private static final int DEFAULT_REFRESH_PERIOD = 30;
    private Logger logger = LoggerFactory.getLogger(SmartMeterHandler.class);
    private MeterDevice<?> smlDevice;
    private Disposable valueReader;
    private Conformity conformity;
    private MeterValueListener valueChangeListener;
    private SmartMeterChannelTypeProvider channelTypeProvider;
    private @NonNull Supplier<SerialPortManager> serialPortManagerSupplier;

    public SmartMeterHandler(Thing thing, SmartMeterChannelTypeProvider channelProvider,
            Supplier<SerialPortManager> serialPortManagerSupplier) {
        super(thing);
        Objects.requireNonNull(channelProvider, "SmartMeterChannelTypeProvider must not be null");
        this.channelTypeProvider = channelProvider;
        this.serialPortManagerSupplier = serialPortManagerSupplier;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Smartmeter handler.");
        cancelRead();

        SmartMeterConfiguration config = getConfigAs(SmartMeterConfiguration.class);
        logger.debug("config port = {}", config.port);

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.port) == null) {
            errorMsg = "Parameter 'port' is mandatory and must be configured";
            validConfig = false;
        }

        if (validConfig) {
            byte[] pullSequence = config.initMessage == null ? null
                    : HexUtils.hexToBytes(StringUtils.deleteWhitespace(config.initMessage));
            int baudrate = config.baudrate == null ? Baudrate.AUTO.getBaudrate()
                    : Baudrate.fromString(config.baudrate).getBaudrate();
            this.conformity = config.conformity == null ? Conformity.NONE : Conformity.valueOf(config.conformity);
            this.smlDevice = MeterDeviceFactory.getDevice(serialPortManagerSupplier, config.mode,
                    this.thing.getUID().getAsString(), config.port, pullSequence, baudrate, config.baudrateChangeDelay);
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                    "Waiting for messages from device");

            smlDevice.addValueChangeListener(channelTypeProvider);

            updateOBISValue();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelRead();
        if (this.valueChangeListener != null) {
            this.smlDevice.removeValueChangeListener(valueChangeListener);
        }
        if (this.channelTypeProvider != null) {
            this.smlDevice.removeValueChangeListener(channelTypeProvider);
        }
    }

    private void cancelRead() {
        if (this.valueReader != null) {
            this.valueReader.dispose();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateOBISChannel(channelUID);
        } else {
            logger.debug("The SML reader binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Get new data the device
     *
     */
    private void updateOBISValue() {
        cancelRead();

        valueChangeListener = new MeterValueListener() {
            @Override
            public <Q extends @NonNull Quantity<Q>> void valueChanged(MeterValue<Q> value) {
                ThingBuilder thingBuilder = editThing();

                String obis = value.getObisCode();

                String obisChannelString = SmartMeterBindingConstants.getObisChannelId(obis);
                Channel channel = thing.getChannel(obisChannelString);
                ChannelTypeUID channelTypeId = channelTypeProvider.getChannelTypeIdForObis(obis);

                ChannelType channelType = channelTypeProvider.getChannelType(channelTypeId, null);
                if (channelType != null) {
                    String itemType = channelType.getItemType();

                    State state = getStateForObisValue(value, channel);
                    if (channel == null) {
                        logger.debug("Adding channel: {} with item type: {}", obisChannelString, itemType);

                        // channel has not been created yet
                        ChannelBuilder channelBuilder = ChannelBuilder
                                .create(new ChannelUID(thing.getUID(), obisChannelString), itemType)
                                .withType(channelTypeId);

                        Configuration configuration = new Configuration();
                        configuration.put(SmartMeterBindingConstants.CONFIGURATION_CONVERSION, 1);
                        channelBuilder.withConfiguration(configuration);
                        channelBuilder.withLabel(obis);
                        Map<String, String> channelProps = new HashMap<>();
                        channelProps.put(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                        channelBuilder.withProperties(channelProps);
                        channelBuilder.withDescription(
                                MessageFormat.format("Value for OBIS code: {0} with Unit: {1}", obis, value.getUnit()));
                        channel = channelBuilder.build();
                        ChannelUID channelId = channel.getUID();

                        // add all valid channels to the thing builder
                        List<Channel> channels = new ArrayList<Channel>(getThing().getChannels());
                        if (channels.stream().filter((element) -> element.getUID().equals(channelId)).count() == 0) {
                            channels.add(channel);
                            thingBuilder.withChannels(channels);
                            updateThing(thingBuilder.build());
                        }
                    }

                    if (!channel.getProperties().containsKey(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS)) {
                        addObisPropertyToChannel(obis, channel);
                    }
                    updateState(channel.getUID(), state);

                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                } else {
                    logger.warn("No ChannelType found for OBIS {}", obis);
                }
            }

            private void addObisPropertyToChannel(String obis, Channel channel) {
                String description = channel.getDescription();
                String label = channel.getLabel();
                ChannelBuilder newChannel = ChannelBuilder.create(channel.getUID(), channel.getAcceptedItemType())
                        .withDefaultTags(channel.getDefaultTags()).withConfiguration(channel.getConfiguration())
                        .withDescription(description == null ? "" : description).withKind(channel.getKind())
                        .withLabel(label == null ? "" : label).withType(channel.getChannelTypeUID());
                HashMap<String, String> properties = new HashMap<>(channel.getProperties());
                properties.put(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS, obis);
                newChannel.withProperties(properties);
                updateThing(editThing().withoutChannel(channel.getUID()).withChannel(newChannel.build()).build());
            }

            @Override
            public <Q extends @NonNull Quantity<Q>> void valueRemoved(MeterValue<Q> value) {
                // channels that are not available are removed
                String obisChannelId = SmartMeterBindingConstants.getObisChannelId(value.getObisCode());
                logger.debug("Removing channel: {}", obisChannelId);
                ThingBuilder thingBuilder = editThing();
                thingBuilder.withoutChannel(new ChannelUID(thing.getUID(), obisChannelId));
                updateThing(thingBuilder.build());
            }

            @Override
            public void errorOccurred(Throwable e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        };
        this.smlDevice.addValueChangeListener(valueChangeListener);

        SmartMeterConfiguration config = getConfigAs(SmartMeterConfiguration.class);
        int delay = config.refresh != null ? config.refresh : DEFAULT_REFRESH_PERIOD;
        valueReader = this.smlDevice.readValues(this.scheduler, Duration.ofSeconds(delay));
    }

    private void updateOBISChannel(ChannelUID channelId) {
        if (isLinked(channelId.getId())) {
            Channel channel = this.thing.getChannel(channelId.getId());
            if (channel != null) {

                String obis = channel.getProperties().get(SmartMeterBindingConstants.CHANNEL_PROPERTY_OBIS);
                MeterValue<?> value = this.smlDevice.getMeterValue(obis);
                if (value != null) {

                    State state = getStateForObisValue(value, channel);
                    updateState(channel.getUID(), state);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <Q extends Quantity<Q>> State getStateForObisValue(MeterValue<?> value, @Nullable Channel channel) {
        Unit<?> unit = value.getUnit();
        String valueString = value.getValue();
        if (unit != null) {
            valueString += " " + value.getUnit();
        }
        State state = TypeParser.parseState(Arrays.asList(QuantityType.class, StringType.class), valueString);
        if (channel != null && state instanceof QuantityType) {
            state = applyConformity(channel, (QuantityType<Q>) state);
            Number conversionRatio = (Number) channel.getConfiguration()
                    .get(SmartMeterBindingConstants.CONFIGURATION_CONVERSION);
            if (conversionRatio != null) {
                state = ((QuantityType<?>) state).divide(BigDecimal.valueOf(conversionRatio.doubleValue()));
            }
        }
        return state;
    }

    private <Q extends Quantity<Q>> State applyConformity(Channel channel, QuantityType<Q> currentState) {
        try {
            return this.conformity.apply(channel, currentState, getThing(), this.smlDevice);
        } catch (Exception e) {
            logger.warn("Failed to apply negation for channel: {}", channel.getUID(), e);
        }
        return currentState;
    }

}
