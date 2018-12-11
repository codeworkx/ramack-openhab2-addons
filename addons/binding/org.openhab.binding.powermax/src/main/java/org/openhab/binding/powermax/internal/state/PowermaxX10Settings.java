/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

/**
 * A class to store the settings of an X10 device
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxX10Settings {

    private String name;
    private boolean enabled;

    public PowermaxX10Settings(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    /**
     * @return the name of the X10 device
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if the X10 device is enabled; false if not
     */
    public boolean isEnabled() {
        return enabled;
    }

}
