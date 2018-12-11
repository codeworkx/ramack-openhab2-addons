/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.internal.handler;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.CHANNEL_APP_CONTROL;

import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeAppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.lametrictime.api.local.ApplicationActionException;
import org.syphr.lametrictime.api.model.CoreApps;

/**
 * The {@link RadioAppHandler} represents an instance of the built-in radio app.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class RadioAppHandler extends AbstractLaMetricTimeAppHandler {
    private static final String PACKAGE_NAME = "com.lametric.radio";

    private final Logger logger = LoggerFactory.getLogger(RadioAppHandler.class);

    public RadioAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_CONTROL:
                    handleControl(command);
                    updateActiveAppOnDevice();
                    break;
                default:
                    logger.debug("Channel '{}' not supported", channelUID);
                    break;
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Failed to perform action - taking app offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void handleControl(final Command command) throws ApplicationActionException {
        if (command instanceof PlayPauseType) {
            switch ((PlayPauseType) command) {
                case PLAY:
                    play();
                    return;
                case PAUSE:
                    stop();
                    return;
                default:
                    logger.debug("{} command not supported by LaMetric Time Radio App", command);
                    return;
            }
        }

        if (command instanceof NextPreviousType) {
            switch ((NextPreviousType) command) {
                case NEXT:
                    next();
                    return;
                case PREVIOUS:
                    previous();
                    return;
                default:
                    logger.debug("{} command not supported by LaMetric Time Radio App", command);
                    return;
            }
        }
    }

    private void next() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().next());
    }

    private void play() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().play());
    }

    private void previous() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().previous());
    }

    private void stop() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().stop());
    }

    @Override
    protected String getPackageName(LaMetricTimeAppConfiguration config) {
        return PACKAGE_NAME;
    }
}
