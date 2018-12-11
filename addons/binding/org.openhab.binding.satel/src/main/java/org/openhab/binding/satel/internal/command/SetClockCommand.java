/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.ArrayUtils;

/**
 * Command class for command to set RTC clock.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SetClockCommand extends ControlCommand {

    public static final byte COMMAND_CODE = (byte) 0x8e;

    private static final ThreadLocal<DateFormat> DATETIME_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    /**
     * Creates new command class instance.
     *
     * @param dateTime
     *            date and time to set
     * @param userCode
     *            code of the user on behalf the control is made
     */
    public SetClockCommand(Calendar dateTime, String userCode) {
        super(COMMAND_CODE, ArrayUtils.addAll(userCodeToBytes(userCode), getDateTimeBytes(dateTime)));
    }

    private static byte[] getDateTimeBytes(Calendar dateTime) {
        return DATETIME_FORMAT.get().format(dateTime.getTime()).getBytes();
    }

}
