/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.model;

import java.util.ArrayList;

/**
 * The {@link SomfyTahomaVersionResponse} holds information about
 * response to getting gateway's firmware version.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaVersionResponse {

    private String result;

    public String getResult() {
        return result;
    }
}
