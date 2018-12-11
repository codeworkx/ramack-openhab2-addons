/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Hue API state object
 *
 * @author Dan Cunningham - Initial contribution
 * @author David Graeff - "extended color light bulbs" support
 *
 */
public class HueStateColorBulb extends HueStateBulb {
    public static int MAX_HUE = 65535; // For extended color light bulbs
    public int hue = 0;
    public static int MAX_SAT = 254;
    public int sat = 0;

    // color as array of xy-coordinates
    public double[] xy = { 0, 0 };

    public String effect = "none";

    /** time for transition in centiseconds. */
    public int transitiontime;

    public static enum ColorMode {
        ct,
        hs,
        xy
    }

    public ColorMode colormode = ColorMode.ct;

    protected HueStateColorBulb() {
    }

    public HueStateColorBulb(boolean on) {
        super(on);
        this.bri = on ? MAX_BRI : 0;
        colormode = ColorMode.ct;
    }

    /**
     * Create a hue state with the given brightness percentage
     *
     * @param brightness Brightness percentage
     * @param on On value
     */
    public HueStateColorBulb(PercentType brightness, boolean on) {
        super(brightness, on);
        colormode = ColorMode.ct;
    }

    /**
     * Creates a hue state with the given color information
     *
     * @param hsb Color information. Sets the hue state to "on" if brightness is > 0.
     */
    public HueStateColorBulb(HSBType hsb) {
        super(hsb.getBrightness().intValue() > 0);
        this.hue = hsb.getHue().intValue() * MAX_HUE / 360;
        this.sat = hsb.getSaturation().intValue() * MAX_SAT / 100;
        this.bri = hsb.getBrightness().intValue() * MAX_BRI / 100;
        colormode = this.sat > 0 ? ColorMode.hs : ColorMode.ct;
    }

    /**
     * Converts this HueState to a HSBType
     */
    public HSBType toHSBType() {
        int bri = this.bri * 100 / MAX_BRI;
        int sat = this.sat * 100 / MAX_SAT;
        int hue = this.hue * 360 / MAX_HUE;

        if (!this.on) {
            bri = 0;
        }
        if (colormode == ColorMode.ct) {
            sat = 0;
        }
        return new HSBType(new DecimalType(hue), new PercentType(sat), new PercentType(bri));
    }

    @Override
    public String toString() {
        String xyString = "{";
        for (double d : xy) {
            xyString += d + " ";
        }
        xyString += "}";
        return "on: " + on + ", brightness: " + bri + ", hue: " + hue + ", sat: " + sat + ", xy: " + xyString + ", ct: "
                + ct + ", alert: " + alert + ", effect: " + effect + ", colormode: " + colormode + ", reachable: "
                + reachable;
    }
}
