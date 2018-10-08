/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol;

/**
 * Thrown when a command type is not supported by the channel
 *
 * @author Antoine Besnard - Initial contribution
 */
public class CommandTypeNotSupportedException extends Exception {

    private static final long serialVersionUID = -7970958467980752003L;

    public CommandTypeNotSupportedException() {
        super();
    }

    public CommandTypeNotSupportedException(String message) {
        super(message);
    }

    public CommandTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandTypeNotSupportedException(Throwable cause) {
        super(cause);
    }

}
