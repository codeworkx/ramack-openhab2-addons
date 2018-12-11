/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.config;

/**
 * The {@link SomfyTahomaConfig} is  is the base class for configuration
 * information held by devices and modules.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaConfig {
    private String email;
    private String password;
    private String thingUid;
    private int refresh = 30;
    private int statusTimeout = 300;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getThingUid() {
        return thingUid;
    }

    public void setThingUid(String thingUid) {
        this.thingUid = thingUid;
    }

    public int getRefresh() {
        return refresh;
    }

    public int getStatusTimeout() {
        return statusTimeout;
    }
}
