/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.event;

/**
 * Event listener interface. All classes that want to receive Satel events must
 * implement this interface.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public interface SatelEventListener {

    /**
     * Event handler for Satel events.
     *
     * @param event
     *            incoming event to handle
     */
    void incomingEvent(SatelEvent event);
}
