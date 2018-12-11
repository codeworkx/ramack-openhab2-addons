/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackAddResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackListResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeCallbackRemoveResponse;
import org.openhab.binding.nuki.internal.dataexchange.BridgeInfoResponse;
import org.openhab.binding.nuki.internal.dataexchange.NukiHttpClient;
import org.openhab.binding.nuki.internal.dto.BridgeApiCallbackListCallbackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NukiBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NukiBridgeHandler.class);
    private static final int JOB_INTERVAL = 600;

    private HttpClient httpClient;
    private NukiHttpClient nukiHttpClient;
    private String callbackUrl;
    private ScheduledFuture<?> checkBridgeOnlineJob;
    private String bridgeIp;
    private boolean initializable;

    public NukiBridgeHandler(Bridge bridge, HttpClient httpClient, String callbackUrl) {
        super(bridge);
        logger.debug("Instantiating NukiBridgeHandler({}, {}, {})", bridge, httpClient, callbackUrl);
        this.httpClient = httpClient;
        this.callbackUrl = callbackUrl;
        this.bridgeIp = (String) getConfig().get(NukiBindingConstants.CONFIG_IP);
        if (bridgeIp == null || getConfig().get(NukiBindingConstants.CONFIG_API_TOKEN) == null) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, check required configuration!",
                    getThing().getUID());
            return;
        }
        this.initializable = true;
    }

    public NukiHttpClient getNukiHttpClient() {
        if (nukiHttpClient == null) {
            nukiHttpClient = new NukiHttpClient(httpClient, getConfig());
        }
        return nukiHttpClient;
    }

    public boolean isInitializable() {
        return initializable;
    }

    @Override
    public void initialize() {
        logger.debug("initialize() for Bridge[{}].", bridgeIp);
        scheduler.execute(() -> initializeHandler());
        checkBridgeOnlineJob = scheduler.scheduleWithFixedDelay(() -> checkBridgeOnline(), JOB_INTERVAL, JOB_INTERVAL,
                TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand({}, {}) for Bridge[{}] not implemented!", channelUID, command, bridgeIp);
    }

    @Override
    public void dispose() {
        logger.debug("dispose() for Bridge[{}].", bridgeIp);
        nukiHttpClient = null;
        checkBridgeOnlineJob.cancel(true);
        checkBridgeOnlineJob = null;
    }

    private synchronized void initializeHandler() {
        logger.debug("initializeHandler() for Bridge[{}].", bridgeIp);
        BridgeInfoResponse bridgeInfoResponse = getNukiHttpClient().getBridgeInfo();
        if (bridgeInfoResponse.getStatus() == HttpStatus.OK_200) {
            boolean manageCallbacks = (Boolean) getConfig().get(NukiBindingConstants.CONFIG_MANAGECB);
            if (manageCallbacks) {
                manageNukiBridgeCallbacks();
            }
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, bridgeInfoResponse.getMessage());
        }
    }

    private void checkBridgeOnline() {
        logger.debug("checkBridgeOnline():bridgeIp[{}] status[{}]", bridgeIp, getThing().getStatus());
        if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.trace("Requesting BridgeInfo to ensure Bridge[{}] is online.", bridgeIp);
            BridgeInfoResponse bridgeInfoResponse = getNukiHttpClient().getBridgeInfo();
            int status = bridgeInfoResponse.getStatus();
            if (status == HttpStatus.OK_200) {
                logger.trace("Bridge[{}] responded with status[{}]. Bridge is online.", bridgeIp, status);
            } else if (status == HttpStatus.SERVICE_UNAVAILABLE_503) {
                logger.trace(
                        "Bridge[{}] responded with status[{}]. REST service seems to be busy but Bridge is online.",
                        bridgeIp, status);
            } else {
                logger.debug("Bridge[{}] responded with status[{}]. Bridge is offline!.", bridgeIp, status);
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            initializeHandler();
        }
    }

    private void manageNukiBridgeCallbacks() {
        logger.debug("manageNukiBridgeCallbacks() for Bridge[{}].", bridgeIp);
        BridgeCallbackListResponse bridgeCallbackListResponse = getNukiHttpClient().getBridgeCallbackList();
        List<BridgeApiCallbackListCallbackDto> callbacks = bridgeCallbackListResponse.getCallbacks();
        boolean callbackExists = false;
        int callbackCount = callbacks.size();
        for (BridgeApiCallbackListCallbackDto callback : callbacks) {
            if (callback.getUrl().equals(callbackUrl)) {
                logger.debug("callbackUrl[{}] already existing on Bridge[{}].", callbackUrl, bridgeIp);
                callbackExists = true;
                continue;
            }
            if (callback.getUrl().contains(NukiBindingConstants.CALLBACK_ENDPOINT)) {
                logger.debug("Partial callbackUrl[{}] found on Bridge[{}] - Removing it!", callbackUrl, bridgeIp);
                BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = getNukiHttpClient()
                        .getBridgeCallbackRemove(callback.getId());
                if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                    logger.debug("Successfully removed callbackUrl[{}] on Bridge[{}]!", callbackUrl, bridgeIp);
                    callbackCount--;
                }
            }
        }
        if (!callbackExists) {
            if (callbackCount == 3) {
                logger.debug("Already 3 callback URLs existing on Bridge[{}] - Removing ID 0!", bridgeIp);
                BridgeCallbackRemoveResponse bridgeCallbackRemoveResponse = getNukiHttpClient()
                        .getBridgeCallbackRemove(0);
                if (bridgeCallbackRemoveResponse.getStatus() == HttpStatus.OK_200) {
                    logger.debug("Successfully removed callbackUrl[{}] on Bridge[{}]!", callbackUrl, bridgeIp);
                    callbackCount--;
                }
            }
            logger.debug("Adding callbackUrl[{}] to Bridge[{}]!", callbackUrl, bridgeIp);
            BridgeCallbackAddResponse bridgeCallbackAddResponse = getNukiHttpClient().getBridgeCallbackAdd(callbackUrl);
            if (bridgeCallbackAddResponse.getStatus() == HttpStatus.OK_200) {
                logger.debug("Successfully added callbackUrl[{}] on Bridge[{}]!", callbackUrl, bridgeIp);
                callbackExists = true;
            }
        }
    }

}
