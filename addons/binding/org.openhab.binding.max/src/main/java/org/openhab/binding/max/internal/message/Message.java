/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import org.slf4j.Logger;

/**
 * Base message for the messages received from the MAX! Cube.
 *
 * @author Andreas Heil (info@aheil.de)
 * @since 1.4.0
 */
public abstract class Message {

    public static final String DELIMETER = ",";

    private String raw = null;

    public Message(String raw) {
        this.raw = raw;
    }

    public abstract void debug(Logger logger);

    public abstract MessageType getType();

    protected final String getPayload() {
        return raw.substring(2, raw.length());
    }
}
