/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nest.internal.NestBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.config.NestStructureConfiguration;
import org.openhab.binding.nest.internal.data.Structure;
import org.openhab.binding.nest.internal.data.Structure.HomeAwayState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Handle channel refresh command
 */
@NonNullByDefault
public class NestStructureHandler extends NestBaseHandler<Structure> {
    private final Logger logger = LoggerFactory.getLogger(NestStructureHandler.class);

    private @Nullable String structureId;

    public NestStructureHandler(Thing thing) {
        super(thing, Structure.class);
    }

    @Override
    protected State getChannelState(ChannelUID channelUID, Structure structure) {
        switch (channelUID.getId()) {
            case CHANNEL_AWAY:
                return getAsStringTypeOrNull(structure.getAway());
            case CHANNEL_CO_ALARM_STATE:
                return getAsStringTypeOrNull(structure.getCoAlarmState());
            case CHANNEL_COUNTRY_CODE:
                return getAsStringTypeOrNull(structure.getCountryCode());
            case CHANNEL_ETA_BEGIN:
                return getAsDateTimeTypeOrNull(structure.getEtaBegin());
            case CHANNEL_PEAK_PERIOD_END_TIME:
                return getAsDateTimeTypeOrNull(structure.getPeakPeriodEndTime());
            case CHANNEL_PEAK_PERIOD_START_TIME:
                return getAsDateTimeTypeOrNull(structure.getPeakPeriodStartTime());
            case CHANNEL_POSTAL_CODE:
                return getAsStringTypeOrNull(structure.getPostalCode());
            case CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT:
                return getAsOnOffTypeOrNull(structure.isRhrEnrollment());
            case CHANNEL_SECURITY_STATE:
                return getAsStringTypeOrNull(structure.getWwnSecurityState());
            case CHANNEL_SMOKE_ALARM_STATE:
                return getAsStringTypeOrNull(structure.getSmokeAlarmState());
            case CHANNEL_TIME_ZONE:
                return getAsStringTypeOrNull(structure.getTimeZone());
            default:
                logger.error("Unsupported channelId '{}'", channelUID.getId());
                return UnDefType.UNDEF;
        }
    }

    @Override
    public String getId() {
        return getStructureId();
    }

    private String getStructureId() {
        String localStructureId = structureId;
        if (localStructureId == null) {
            localStructureId = getConfigAs(NestStructureConfiguration.class).structureId;
            structureId = localStructureId;
        }
        return localStructureId;
    }

    /**
     * Handles updating the details on this structure by sending the request all the way
     * to Nest.
     *
     * @param channelUID the channel to update
     * @param command the command to apply
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (REFRESH.equals(command)) {
            Structure lastUpdate = getLastUpdate();
            if (lastUpdate != null) {
                updateState(channelUID, getChannelState(channelUID, lastUpdate));
            }
        } else if (CHANNEL_AWAY.equals(channelUID.getId())) {
            // Change the home/away state.
            if (command instanceof StringType) {
                StringType cmd = (StringType) command;
                // Set the mode to be the cmd value.
                addUpdateRequest(NEST_STRUCTURE_UPDATE_PATH, "away", HomeAwayState.valueOf(cmd.toString()));
            }
        }
    }

    @Override
    protected void update(Structure oldStructure, Structure structure) {
        logger.debug("Updating {}", getThing().getUID());

        updateLinkedChannels(oldStructure, structure);

        if (ThingStatus.ONLINE != thing.getStatus()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

}
