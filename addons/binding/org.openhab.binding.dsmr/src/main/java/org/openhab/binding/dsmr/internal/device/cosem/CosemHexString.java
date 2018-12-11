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

import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link CosemHexString} represents a string value stored as Hexadecimal values.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
class CosemHexString extends CosemValueDescriptor<StringType> {

    public static final CosemHexString INSTANCE = new CosemHexString();

    private static final String NO_VALUE = "00";

    /**
     * Parses a String representing the hex value to a {@link StringType}.
     *
     * @param cosemValue the value to parse
     * @return {@link StringType} representing the value the cosem hex value
     * @throws ParseException if parsing failed
     */
    @Override
    protected StringType getStateValue(String cosemValue) throws ParseException {
        final String cosemHexValue = cosemValue.replaceAll("\\r\\n", "");

        if (cosemHexValue.length() % 2 != 0) {
            throw new ParseException(cosemHexValue + " is not a valid hexadecimal string", 0);
        } else {
            final StringBuilder sb = new StringBuilder();

            for (int i = 0; i < cosemHexValue.length(); i += 2) {
                final String hexValue = cosemHexValue.substring(i, i + 2);

                if (!NO_VALUE.equals(hexValue)) {
                    sb.append((char) Integer.parseInt(hexValue, 16));
                }
            }
            return new StringType(sb.toString());
        }
    }
}
