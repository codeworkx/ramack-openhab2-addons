/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.state;

/**
 * All defined sensor types for all panels except Master panels
 *
 * @author Laurent Garnier - Initial contribution
 */
public enum PowermaxSensorType {

    MOTION_SENSOR_1((byte) 0x03, "Motion"),
    MOTION_SENSOR_2((byte) 0x04, "Motion"),
    MAGNET_SENSOR_1((byte) 0x05, "Magnet"),
    MAGNET_SENSOR_2((byte) 0x06, "Magnet"),
    MAGNET_SENSOR_3((byte) 0x07, "Magnet"),
    SMOKE_SENSOR((byte) 0x0A, "Smoke"),
    GAS_SENSOR((byte) 0x0B, "Gas"),
    MOTION_SENSOR_3((byte) 0x0C, "Motion"),
    WIRED_SENSOR((byte) 0x0F, "Wired");

    private byte code;
    private String label;

    private PowermaxSensorType(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * @return the code identifying the sensor type
     */
    public byte getCode() {
        return code;
    }

    /**
     * @return the label associated to the sensor type
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the ENUM value from its identifying code
     *
     * @param code the identifying code
     *
     * @return the corresponding ENUM value
     *
     * @throws IllegalArgumentException if no ENUM value corresponds to this code
     */
    public static PowermaxSensorType fromCode(byte code) throws IllegalArgumentException {
        for (PowermaxSensorType sensorType : PowermaxSensorType.values()) {
            if (sensorType.getCode() == code) {
                return sensorType;
            }
        }

        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
