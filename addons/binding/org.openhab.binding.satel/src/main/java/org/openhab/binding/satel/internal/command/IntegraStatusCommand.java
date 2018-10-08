/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.command;

import java.util.Calendar;

import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.event.IntegraStatusEvent;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command class for command that returns Integra RTC and basic status.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public class IntegraStatusCommand extends SatelCommandBase {

    private final Logger logger = LoggerFactory.getLogger(IntegraStatusCommand.class);

    public static final byte COMMAND_CODE = 0x1a;

    /**
     * Creates new command class instance.
     */
    public IntegraStatusCommand() {
        super(COMMAND_CODE, false);
    }

    /**
     * @return date and time
     */
    public Calendar getIntegraTime() {
        // parse current date and time
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, bcdToInt(response.getPayload(), 0, 2));
        c.set(Calendar.MONTH, bcdToInt(response.getPayload(), 2, 1) - 1);
        c.set(Calendar.DAY_OF_MONTH, bcdToInt(response.getPayload(), 3, 1));
        c.set(Calendar.HOUR_OF_DAY, bcdToInt(response.getPayload(), 4, 1));
        c.set(Calendar.MINUTE, bcdToInt(response.getPayload(), 5, 1));
        c.set(Calendar.SECOND, bcdToInt(response.getPayload(), 6, 1));
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /**
     * @return first status byte
     */
    public byte getStatusByte1() {
        return response.getPayload()[7];
    }

    /**
     * @return second status byte
     */
    public byte getStatusByte2() {
        return response.getPayload()[8];
    }

    @Override
    public boolean handleResponse(EventDispatcher eventDispatcher, SatelMessage response) {
        if (super.handleResponse(eventDispatcher, response)) {
            // dispatch version event
            eventDispatcher.dispatchEvent(new IntegraStatusEvent(getIntegraTime(), getStatusByte1(), getStatusByte2()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        if (response.getCommand() != COMMAND_CODE) {
            logger.debug("Invalid response code: {}", response.getCommand());
            return false;
        }
        if (response.getPayload().length != 9) {
            logger.debug("Invalid payload length: {}", response.getPayload().length);
            return false;
        }
        return true;
    }

}
