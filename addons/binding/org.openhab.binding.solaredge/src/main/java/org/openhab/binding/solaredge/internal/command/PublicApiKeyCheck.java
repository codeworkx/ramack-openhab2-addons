/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * checks validity of the api key by accessing the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
public class PublicApiKeyCheck extends AbstractCommandCallback implements SolarEdgeCommand {

    public PublicApiKeyCheck(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        // as a key is used no real login is to be done here. It is just checked if a protected page can be retrieved
        // and therefore the key is valid.
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PUBLIC_DATA_API_URL + config.getSolarId() + PUBLIC_DATA_API_URL_LIVE_DATA_SUFFIX;
    }

    @Override
    public void onComplete(Result result) {
        getListener().update(getCommunicationStatus());
    }

}
