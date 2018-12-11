/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.handler;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.internal.NestBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.data.SmokeDetector;
import org.openhab.binding.nest.internal.data.SmokeDetector.BatteryHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The smoke detector handler, it handles the data from Nest for the smoke detector.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class NestSmokeDetectorHandler extends NestBaseHandler<SmokeDetector> {
    private final Logger logger = LoggerFactory.getLogger(NestSmokeDetectorHandler.class);

    public NestSmokeDetectorHandler(Thing thing) {
        super(thing, SmokeDetector.class);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, SmokeDetector smokeDetector) {
        switch (channelUID.getId()) {
            case CHANNEL_CO_ALARM_STATE:
                return getAsStringTypeOrNull(smokeDetector.getCoAlarmState());
            case CHANNEL_LAST_CONNECTION:
                return getAsDateTimeTypeOrNull(smokeDetector.getLastConnection());
            case CHANNEL_LAST_MANUAL_TEST_TIME:
                return getAsDateTimeTypeOrNull(smokeDetector.getLastManualTestTime());
            case CHANNEL_LOW_BATTERY:
                return getAsOnOffTypeOrNull(smokeDetector.getBatteryHealth() == null ? null
                        : smokeDetector.getBatteryHealth() == BatteryHealth.REPLACE);
            case CHANNEL_MANUAL_TEST_ACTIVE:
                return getAsOnOffTypeOrNull(smokeDetector.isManualTestActive());
            case CHANNEL_SMOKE_ALARM_STATE:
                return getAsStringTypeOrNull(smokeDetector.getSmokeAlarmState());
            case CHANNEL_UI_COLOR_STATE:
                return getAsStringTypeOrNull(smokeDetector.getUiColorState());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    /**
     * Handles any incoming command requests.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            SmokeDetector lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        }
    }

    @Override
    protected void update(SmokeDetector oldSmokeDetector, SmokeDetector smokeDetector) {
        logger.debug("Updating {}", getThing().getUID());

        updateLinkedChannels(oldSmokeDetector, smokeDetector);
        updateProperty(PROPERTY_FIRMWARE_VERSION, smokeDetector.getSoftwareVersion());

        ThingStatus newStatus = smokeDetector.isOnline() == null ? ThingStatus.UNKNOWN
                : smokeDetector.isOnline() ? ThingStatus.ONLINE : ThingStatus.OFFLINE;
        if (newStatus != thing.getStatus()) {
            updateStatus(newStatus);
        }
    }

}
