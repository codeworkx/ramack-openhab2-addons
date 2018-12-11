/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import org.openhab.binding.max.internal.Utils;

/**
 * The {@link ZCommand} send a wakeup request to MAX! devices.
 *
 * @author Marcel Verpaalen - Initial Contribution
 */
public class ZCommand extends CubeCommand {

    public enum WakeUpType {
        ALL,
        ROOM,
        DEVICE
    }

    private static final int DEFAULT_WAKETIME = 30;

    private final String address;
    private final WakeUpType wakeUpType;
    private final int wakeUpTime;

    public ZCommand(WakeUpType wakeUpType, String address, int wakeupTime) {
        this.address = address;
        this.wakeUpType = wakeUpType;
        this.wakeUpTime = wakeupTime;
    }

    public static ZCommand wakeupRoom(int roomId) {
        return new ZCommand(WakeUpType.ROOM, String.format("%02d", roomId), DEFAULT_WAKETIME);
    }

    public static ZCommand wakeupRoom(int roomId, int wakeupTime) {
        return new ZCommand(WakeUpType.ROOM, String.format("%02d", roomId), wakeupTime);
    }

    public static ZCommand wakeupDevice(String rfAddress) {
        return new ZCommand(WakeUpType.DEVICE, rfAddress, DEFAULT_WAKETIME);
    }

    public static ZCommand wakeupDevice(String rfAddress, int wakeupTime) {
        return new ZCommand(WakeUpType.DEVICE, rfAddress, wakeupTime);
    }

    public static ZCommand wakeupAllDevices() {
        return new ZCommand(WakeUpType.ALL, "0", DEFAULT_WAKETIME);
    }

    public static ZCommand wakeupAllDevices(int wakeupTime) {
        return new ZCommand(WakeUpType.ALL, "0", wakeupTime);
    }

    @Override
    public String getCommandString() {
        final String commandString;
        switch (wakeUpType) {
            case ALL:
                commandString = "A";
                break;
            case ROOM:
                commandString = "G," + address;
                break;
            case DEVICE:
                commandString = "D," + address;
                break;
            default:
                throw new IllegalStateException("Unknown wakeup type: " + wakeUpType);
        }

        return "z:" + Utils.toHex(wakeUpTime) + "," + commandString + '\r' + '\n';
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }

}
