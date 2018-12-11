/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.api;

/**
 * The {@link UniFiCommunicationException} signals there was a problem communicating with the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiCommunicationException extends UniFiException {

    private static final long serialVersionUID = -7261308872245069364L;

    public UniFiCommunicationException(Throwable cause) {
        super(cause);
    }

}
