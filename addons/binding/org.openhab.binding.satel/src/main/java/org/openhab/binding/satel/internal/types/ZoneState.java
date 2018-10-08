/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.internal.types;

/**
 * Available zone states.
 *
 * @author Krzysztof Goworek - Initial contribution
 * @since 1.7.0
 */
public enum ZoneState implements StateType {
    VIOLATION(0x00),
    TAMPER(0x01),
    ALARM(0x02),
    TAMPER_ALARM(0x03),
    ALARM_MEMORY(0x04),
    TAMPER_ALARM_MEMORY(0x05),
    BYPASS(0x06),
    NO_VIOLATION_TROUBLE(0x07),
    LONG_VIOLATION_TROUBLE(0x08),
    ISOLATE(0x26),
    MASKED(0x28),
    MASKED_MEMORY(0x29);

    private byte refreshCommand;

    ZoneState(int refreshCommand) {
        this.refreshCommand = (byte) refreshCommand;
    }

    @Override
    public byte getRefreshCommand() {
        return refreshCommand;
    }

    @Override
    public int getPayloadLength(boolean extendedCmd) {
        return extendedCmd ? 32 : 16;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.ZONE;
    }

    @Override
    public int getStartByte() {
        return 0;
    }

    @Override
    public int getBytesCount(boolean extendedCmd) {
        return getPayloadLength(extendedCmd);
    }

}
