/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.exceptions;

/**
 * Will be thrown when there is an attempt to put a new message line into the message processor,
 * the processor detects a known message indicator, but the message could not be parsed correctly.
 *
 * @author Christian Rockrohr <christian@rockrohr.de>
 */
public class UnprocessableMessageException extends Exception {

    /**
     * required variable to avoid IncorrectMultilineIndexException warning
     */
    private static final long serialVersionUID = -9071779402960309265L;

}
