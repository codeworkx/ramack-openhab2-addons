/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal;

/**
 * Exception used during Stick initialization.
 *
 * @author Karel Goderis
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseInitializationException extends Exception {

    private static final long serialVersionUID = 2095258016390913221L;

    public PlugwiseInitializationException(String msg) {
        super(msg);
    }

    public PlugwiseInitializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
