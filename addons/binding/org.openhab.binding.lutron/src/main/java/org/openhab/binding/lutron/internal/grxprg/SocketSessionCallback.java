/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.grxprg;

/**
 * Interface defining a callback from {@link SocketSession} when a response was received (or an exception occurred)
 *
 * @author Tim Roberts - Initial contribution
 */
public interface SocketSessionCallback {
    /**
     * Called when a command has completed with the response for the command
     *
     * @param response a non-null, possibly empty response
     */
    public void responseReceived(String response);

    /**
     * Called when a command finished with an exception
     *
     * @param e a non-null exception
     */
    public void responseException(Exception e);
}
