/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.callback;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.binding.nibeuplink.config.NibeUplinkConfiguration;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.openhab.binding.nibeuplink.internal.connector.CommunicationStatus;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * base class for all commands. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractUplinkCommandCallback extends BufferingResponseListener implements NibeUplinkCommand {

    /**
     * logger
     */
    protected final Logger logger = LoggerFactory.getLogger(AbstractUplinkCommandCallback.class);

    /**
     * the configuration
     */
    protected final NibeUplinkConfiguration config;

    /**
     * status code of fulfilled request
     */
    private CommunicationStatus communicationStatus;

    /**
     * listener to provide updates to the WebInterface class
     */
    @Nullable
    private StatusUpdateListener listener;

    /**
     * JSON deserializer
     */
    protected final Gson gson;

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractUplinkCommandCallback(NibeUplinkConfiguration config) {
        this.communicationStatus = new CommunicationStatus();
        this.config = config;
        this.gson = new Gson();
    }

    /**
     * the constructor
     *
     * @param config
     */
    public AbstractUplinkCommandCallback(NibeUplinkConfiguration config, StatusUpdateListener listener) {
        this(config);
        this.listener = listener;
    }

    /**
     * Log request success
     */
    @Override
    public final void onSuccess(@Nullable Response response) {
        super.onSuccess(response);
        if (response != null) {
            communicationStatus.setHttpCode(HttpStatus.getCode(response.getStatus()));
            logger.debug("HTTP response {}", response.getStatus());
        }
    }

    /**
     * Log request failure
     */
    @Override
    public final void onFailure(@Nullable Response response, @Nullable Throwable failure) {
        super.onFailure(response, failure);
        if (failure != null) {
            logger.debug("Request failed: {}", failure.toString());
            communicationStatus.setError((Exception) failure);

            if (failure instanceof SocketTimeoutException || failure instanceof TimeoutException) {
                communicationStatus.setHttpCode(Code.REQUEST_TIMEOUT);
            } else if (failure instanceof UnknownHostException) {
                communicationStatus.setHttpCode(Code.BAD_GATEWAY);
            } else {
                communicationStatus.setHttpCode(Code.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Override
    public void onContent(@Nullable Response response, @Nullable ByteBuffer content) {
        super.onContent(response, content);
        logger.debug("received content, length: {}", getContentAsString().length());
    }

    @Override
    public void performAction(HttpClient asyncclient) {
        Request request = asyncclient.newRequest(getURL()).timeout(config.getAsyncTimeout(), TimeUnit.SECONDS);
        prepareRequest(request).send(this);
    }

    /**
     * returns Http Status Code
     */
    public CommunicationStatus getCommunicationStatus() {
        if (communicationStatus.getHttpCode() == null) {
            communicationStatus.setHttpCode(Code.INTERNAL_SERVER_ERROR);
        }
        return communicationStatus;
    }

    /**
     * concrete implementation has to prepare the requests with additional parameters, etc
     *
     * @return
     */
    protected abstract Request prepareRequest(Request requestToPrepare);

    /**
     * concrete implementation has to provide the URL
     *
     * @return
     */
    protected abstract String getURL();

    @Override
    public final @Nullable StatusUpdateListener getListener() {
        return listener;
    }

    @Override
    public final void setListener(StatusUpdateListener listener) {
        this.listener = listener;
    }

}
