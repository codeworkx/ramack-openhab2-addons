/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.lacrosse;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Reading of a LaCrosse Temperature Sensor.
 *
 * @author Volker Bier - Initial contribution
 */
public class LaCrosseTemperatureReading implements Reading {
    private String sensorId;
    private int sensorType;
    private int channel;
    private Float temp;
    private Integer humidity;
    private boolean batteryNew;
    private boolean batteryLow;

    public LaCrosseTemperatureReading(int sensorId, int sensorType, int channel, Float temp, Integer humidity,
            boolean batteryNew, boolean batteryLow) {
        this(String.valueOf(sensorId), sensorType, channel, temp, humidity, batteryNew, batteryLow);
    }

    public LaCrosseTemperatureReading(String sensorId, int sensorType, int channel, Float temp, Integer humidity,
            boolean batteryNew, boolean batteryLow) {
        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.channel = channel;
        this.temp = temp;
        this.humidity = humidity;
        this.batteryNew = batteryNew;
        this.batteryLow = batteryLow;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public int getSensorType() {
        return sensorType;
    }

    public Float getTemperature() {
        return temp;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public boolean isBatteryLow() {
        return batteryLow;
    }

    @Override
    public String toString() {
        return "sensorId=" + sensorId + ": channel=" + channel + ", temp=" + temp + ", hum=" + humidity + ", batLow="
                + batteryLow + ", batNew=" + batteryNew;
    }

    public boolean isBatteryNew() {
        return batteryNew;
    }

    public int getChannel() {
        return channel;
    }
}
