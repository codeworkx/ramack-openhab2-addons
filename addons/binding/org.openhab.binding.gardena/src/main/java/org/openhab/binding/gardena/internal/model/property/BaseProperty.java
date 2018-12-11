/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.property;

/**
 * Base class to send properties to Gardena.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class BaseProperty {
    private String name;

    public BaseProperty(String name) {
        this.name = name;
    }

    /**
     * Returns the property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the property.
     */
    public abstract String getValue();
}
