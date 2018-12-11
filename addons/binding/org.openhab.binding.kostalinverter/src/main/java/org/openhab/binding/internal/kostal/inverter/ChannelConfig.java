/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.internal.kostal.inverter;

import javax.measure.Unit;

/**
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 */
public class ChannelConfig {
    public ChannelConfig(String id, String tag, int num, Unit<?> unit) {
        this.id = id;
        this.tag = tag;
        this.num = num;
        this.unit = unit;
    }

    String id;
    String tag;
    int num;
    Unit<?> unit;
}
