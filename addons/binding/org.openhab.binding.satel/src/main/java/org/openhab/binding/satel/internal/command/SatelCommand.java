/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.command;

import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.protocol.SatelMessage;

/**
 * Interface for commands sent to communication module.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public interface SatelCommand {

    /**
     * State of command.
     * <ul>
     * <li>NEW - just created</li>
     * <li>ENQUEUED - currently waiting in the queue</li>
     * <li>SENT - sent to communication module</li>
     * <li>SUCCEEDED - response received and successfully handled</li>
     * <li>FAILED - either send failed or response is invalid</li>
     * </ul>
     *
     * @author Krzysztof Goworek - Initial contribution
     *
     */
    public enum State {
        NEW,
        ENQUEUED,
        SENT,
        SUCCEEDED,
        FAILED
    }

    /**
     * Returns current state of the command.
     *
     * @return current state
     */
    State getState();

    /**
     * Sets state of the command.
     *
     * @param state new state
     */
    void setState(State state);

    /**
     * Returns request's message object.
     *
     * @return {@link SatelMessage} request object
     */
    SatelMessage getRequest();

    /**
     * Handles response received for the command. Usually generates an event with received data.
     *
     * @param eventDispatcher event dispatcher
     * @param response response to handle
     * @return <code>true</code> if response has been successfully handled
     */
    boolean handleResponse(EventDispatcher eventDispatcher, SatelMessage response);

}
