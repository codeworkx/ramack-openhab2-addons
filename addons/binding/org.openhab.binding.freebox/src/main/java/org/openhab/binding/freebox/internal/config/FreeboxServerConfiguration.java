/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freebox.internal.config;

/**
 * The {@link FreeboxServerConfiguration} is responsible for holding
 * configuration informations needed to access/poll the freebox server
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - add discovery settings
 */
public class FreeboxServerConfiguration {

    public static final String FQDN = "fqdn";
    public static final String APP_TOKEN = "appToken";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String USE_ONLY_HTTP = "useOnlyHttp";
    public static final String DISCOVER_PHONE = "discoverPhone";
    public static final String DISCOVER_NET_DEVICE = "discoverNetDevice";
    public static final String DISCOVER_NET_INTERFACE = "discoverNetInterface";
    public static final String DISCOVER_AIRPLAY_RECEIVER = "discoverAirPlayReceiver";

    public String fqdn;
    public String appToken;
    public Integer refreshInterval;
    public Boolean useOnlyHttp;
    public Boolean discoverPhone;
    public Boolean discoverNetDevice;
    public Boolean discoverNetInterface;
    public Boolean discoverAirPlayReceiver;

}
