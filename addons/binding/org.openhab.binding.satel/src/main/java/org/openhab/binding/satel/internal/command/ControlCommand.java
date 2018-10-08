/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.command;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.satel.internal.protocol.SatelMessage;

/**
 * Base class for all commands that return result code in the response.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.9.0
 */
public abstract class ControlCommand extends SatelCommandBase {

    /**
     * Creates new command class instance.
     *
     * @param commandCode
     *            command code
     * @param payload
     */
    public ControlCommand(byte commandCode, byte[] payload) {
        super(commandCode, payload);
    }

    @Override
    protected boolean isResponseValid(SatelMessage response) {
        return true;
    }

    protected static byte[] userCodeToBytes(String userCode) {
        if (StringUtils.isEmpty(userCode)) {
            throw new IllegalArgumentException("User code is empty");
        }
        if (userCode.length() > 8) {
            throw new IllegalArgumentException("User code too long");
        }
        byte[] bytes = new byte[8];
        int digitsNbr = 2 * bytes.length;
        for (int i = 0; i < digitsNbr; ++i) {
            if (i < userCode.length()) {
                char digit = userCode.charAt(i);
                if (!Character.isDigit(digit)) {
                    throw new IllegalArgumentException("User code must contain digits only");
                }
                if (i % 2 == 0) {
                    bytes[i / 2] = (byte) ((digit - '0') << 4);
                } else {
                    bytes[i / 2] |= (byte) (digit - '0');
                }
            } else if (i % 2 == 0) {
                bytes[i / 2] = (byte) 0xff;
            } else if (i == userCode.length()) {
                bytes[i / 2] |= 0x0f;
            }
        }

        return bytes;
    }

}
