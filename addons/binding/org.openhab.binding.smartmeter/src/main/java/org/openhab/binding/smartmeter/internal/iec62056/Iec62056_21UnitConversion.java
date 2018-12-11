/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal.iec62056;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a unit from IEC62056-21 protocol to a {@link Unit}
 * 
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class Iec62056_21UnitConversion {

    private final static Logger logger = LoggerFactory.getLogger(Iec62056_21UnitConversion.class);

    @SuppressWarnings("unchecked")
    public static @Nullable <Q extends Quantity<Q>> Unit<Q> getUnit(String unit) {
        if (!unit.isEmpty()) {
            try {
                return (Unit<Q>) UnitUtils.parseUnit(" " + unit);
            } catch (Exception e) {
                logger.warn("Failed to parse unit {}: {}", unit, e.getMessage());
                return null;
            }
        }
        return null;
    }

}
