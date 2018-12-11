/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal.handler;

import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yeelight.internal.YeelightBindingConstants;
import org.openhab.binding.yeelight.internal.lib.device.ConnectState;
import org.openhab.binding.yeelight.internal.lib.device.DeviceBase;
import org.openhab.binding.yeelight.internal.lib.device.DeviceFactory;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceAction;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceMode;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceConnectionStateListener;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceStatusChangeListener;
import org.openhab.binding.yeelight.internal.lib.services.DeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightHandlerBase} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public abstract class YeelightHandlerBase extends BaseThingHandler
        implements DeviceConnectionStateListener, DeviceStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(YeelightHandlerBase.class);
    protected DeviceBase mDevice;

    // Reading the deviceId from the properties map.
    private String deviceId = getThing().getConfiguration().get(YeelightBindingConstants.PARAMETER_DEVICE_ID)
            .toString();

    public YeelightHandlerBase(Thing thing) {
        super(thing);
    }

    protected void updateUI(DeviceStatus status) {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing, Device ID: {}", deviceId);
        mDevice = DeviceFactory.build(getDeviceModel(getThing().getThingTypeUID()).name(), deviceId);
        mDevice.setDeviceName(getThing().getLabel());
        mDevice.setAutoConnect(true);
        DeviceManager.getInstance().addDevice(mDevice);
        mDevice.registerConnectStateListener(this);
        mDevice.registerStatusChangedListener(this);
        updateStatusHelper(mDevice.getConnectionState());
        DeviceManager.getInstance().startDiscovery(15 * 1000);
    }

    private DeviceType getDeviceModel(ThingTypeUID typeUID) {
        if (typeUID.equals(YeelightBindingConstants.THING_TYPE_CEILING)) {
            return DeviceType.ceiling;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_CEILING3)) {
            return DeviceType.ceiling3;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_WONDER)) {
            return DeviceType.color;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_DOLPHIN)) {
            return DeviceType.mono;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_CTBULB)) {
            return DeviceType.ct_bulb;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_STRIPE)) {
            return DeviceType.stripe;
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChanged(ConnectState connectState) {
        logger.debug("onConnectionStateChanged -> {}", connectState.name());
        updateStatusHelper(connectState);
    }

    public void updateStatusHelper(ConnectState connectState) {
        switch (connectState) {
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is offline!");
                if (mDevice.isAutoConnect()) {
                    DeviceManager.sInstance.startDiscovery(5 * 1000);
                    logger.debug("Thing OFFLINE. Initiated discovery");
                }
                break;
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                mDevice.queryStatus();
                break;
            default:
                updateStatus(ThingStatus.UNKNOWN);
                break;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("ChannelLinked -> {}", channelUID.getId());
        super.channelLinked(channelUID);

        Runnable task = () -> {
            mDevice.queryStatus();
        };
        scheduler.schedule(task, 500, TimeUnit.MILLISECONDS);
    }

    public void handleCommandHelper(ChannelUID channelUID, Command command, String logInfo) {
        logger.debug(logInfo, command);

        // If device is disconnected, start discovery to reconnect.
        if (mDevice.isAutoConnect() && mDevice.getConnectionState() != ConnectState.CONNECTED) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
        }
        if (command instanceof RefreshType) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
            DeviceStatus s = mDevice.getDeviceStatus();
            switch (channelUID.getId()) {
                case YeelightBindingConstants.CHANNEL_BRIGHTNESS:
                    updateState(channelUID, new PercentType(s.getBrightness()));
                    break;
                case YeelightBindingConstants.CHANNEL_COLOR:
                    HSBType hsb = new HSBType();
                    updateState(channelUID, HSBType.fromRGB(s.getR(), s.getG(), s.getB()));
                    break;
                case YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE:
                    updateState(channelUID, new PercentType(s.getCt()));
                    break;
                default:
                    break;
            }
            return;
        }
        switch (channelUID.getId()) {
            case YeelightBindingConstants.CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    handlePercentMessage((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseBrightnessCommand((IncreaseDecreaseType) command);
                }
                break;
            case YeelightBindingConstants.CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        handleOnOffCommand(OnOffType.OFF);
                    } else {
                        handleHSBCommand(hsbCommand);
                    }
                } else if (command instanceof PercentType) {
                    handlePercentMessage((PercentType) command);
                } else if (command instanceof OnOffType) {
                    handleOnOffCommand((OnOffType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseBrightnessCommand((IncreaseDecreaseType) command);
                }
                break;
            case YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    handleColorTemperatureCommand((PercentType) command);
                } else if (command instanceof IncreaseDecreaseType) {
                    handleIncreaseDecreaseBrightnessCommand((IncreaseDecreaseType) command);
                }
                break;
            default:
                break;
        }
    }

    void handlePercentMessage(PercentType brightness) {
        if (brightness.intValue() == 0) {
            DeviceManager.getInstance().doAction(deviceId, DeviceAction.close);
        } else {
            if (mDevice.getDeviceStatus().isPowerOff()) {
                DeviceManager.getInstance().doAction(deviceId, DeviceAction.open);
            }
            DeviceAction baction = DeviceAction.brightness;
            baction.putValue(brightness.intValue());
            DeviceManager.getInstance().doAction(deviceId, baction);
        }
    }

    void handleIncreaseDecreaseBrightnessCommand(IncreaseDecreaseType increaseDecrease) {
        DeviceManager.getInstance().doAction(deviceId,
                increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_bright
                        : DeviceAction.decrease_bright);
    }

    void handleIncreaseDecreaseColorTemperatureCommand(IncreaseDecreaseType increaseDecrease) {
        DeviceManager.getInstance().doAction(deviceId,
                increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_ct
                        : DeviceAction.decrease_ct);
    }

    void handleOnOffCommand(OnOffType onoff) {
        DeviceManager.getInstance().doAction(deviceId, onoff == OnOffType.ON ? DeviceAction.open : DeviceAction.close);
    }

    void handleHSBCommand(HSBType color) {
        DeviceAction caction = DeviceAction.color;
        caction.putValue(color.getRGB() & 0xFFFFFF);
        DeviceManager.getInstance().doAction(deviceId, caction);
    }

    void handleColorTemperatureCommand(PercentType ct) {
        DeviceAction ctaction = DeviceAction.colortemperature;
        ctaction.putValue(COLOR_TEMPERATURE_STEP * ct.intValue() + COLOR_TEMPERATURE_MINIMUM);
        DeviceManager.getInstance().doAction(deviceId, ctaction);
    }

    @Override
    public void onStatusChanged(String prop, DeviceStatus status) {
        logger.debug("UpdateState->{}", status);
        updateUI(status);
    }

    void updateBrightnessAndColorUI(DeviceStatus status) {
        if (status.isPowerOff()) {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(0));
        } else {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(status.getBrightness()));
            HSBType hsbType = null;
            if (status.getMode() == DeviceMode.MODE_COLOR) {
                hsbType = HSBType.fromRGB(status.getR(), status.getG(), status.getB());
            } else if (status.getMode() == DeviceMode.MODE_HSV) {
                hsbType = new HSBType(new DecimalType(status.getHue()), new PercentType(status.getSat()),
                        new PercentType(1));
            }
            if (hsbType != null) {
                updateState(YeelightBindingConstants.CHANNEL_COLOR, hsbType);
            }
            updateState(YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE,
                    new PercentType((status.getCt() - COLOR_TEMPERATURE_MINIMUM) / COLOR_TEMPERATURE_STEP));
        }
    }
}
