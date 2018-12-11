/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal;

/**
 * Runtime exception in tellstick binding.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -1644730263645760297L;

    public TellstickRuntimeException(String msg) {
        super(msg);
    }
}
