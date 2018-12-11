/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device.cosem;

import java.text.ParseException;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * CosemInteger represents an decimal value
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Combined Integer and Double because {@link DecimalType} handles both
 */
class CosemDecimal extends CosemValueDescriptor<DecimalType> {

    public static final CosemDecimal INSTANCE = new CosemDecimal();

    private CosemDecimal() {
    }

    public CosemDecimal(String ohChannelId) {
        super(ohChannelId);
    }

    /**
     * Parses a String value (that represents an decimal) to an {@link DecimalType} object.
     *
     * @param cosemValue the value to parse
     * @return {@link DecimalType} representing the value of the cosem value
     * @throws ParseException if parsing failed
     */
    @Override
    protected DecimalType getStateValue(String cosemValue) throws ParseException {
        try {
            return new DecimalType(cosemValue);
        } catch (NumberFormatException nfe) {
            throw new ParseException("Failed to parse value '" + cosemValue + "' as integer", 0);
        }
    }
}
