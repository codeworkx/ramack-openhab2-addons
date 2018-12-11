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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.quantity.Volume;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * {@link CosemQuantity} represents a value with a unit.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 *
 * @param <Q> The {@link Quantity} type of the unit of this class
 */
class CosemQuantity<Q extends Quantity<Q>> extends CosemValueDescriptor<QuantityType<Q>> {

    public static final CosemQuantity<ElectricCurrent> AMPERE = new CosemQuantity<>(SmartHomeUnits.AMPERE);
    public static final CosemQuantity<Volume> CUBIC_METRE = new CosemQuantity<>(SIUnits.CUBIC_METRE);
    public static final CosemQuantity<Energy> GIGA_JOULE = new CosemQuantity<>(MetricPrefix.GIGA(SmartHomeUnits.JOULE));
    public static final CosemQuantity<Power> KILO_WATT = new CosemQuantity<>(MetricPrefix.KILO(SmartHomeUnits.WATT));
    public static final CosemQuantity<Energy> KILO_WATT_HOUR = new CosemQuantity<>(SmartHomeUnits.KILOWATT_HOUR);
    public static final CosemQuantity<ElectricPotential> VOLT = new CosemQuantity<>(SmartHomeUnits.VOLT);
    public static final CosemQuantity<Power> WATT = new CosemQuantity<>(SmartHomeUnits.WATT);

    /**
     * Pattern to convert a cosem value to a value that can be parsed by {@link QuantityType}.
     * The specification states that the delimiter between the value and the unit is a '*'-character.
     * We have seen on the Kaifa 0025 meter that both '*' and the '_' character are used.
     *
     * On the Kampstrup 162JxC in some CosemValues the separator is missing
     *
     * The above quirks are supported
     *
     * We also support unit that do not follow the exact case.
     */
    private static final Pattern COSEM_VALUE_WITH_UNIT_PATTERN = Pattern.compile("^([\\d\\.]+)[\\*_]?(.+)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Unit of this CosemValue
     */
    private final Unit<Q> unit;

    /**
     * Creates a new {@link CosemDouble}.
     *
     * @param unit the unit of the value
     */
    private CosemQuantity(Unit<Q> unit) {
        this(unit, "");
    }

    /**
     * Constructor.
     *
     * @param unit Unit of this CosemQuantity instance
     * @param channelId the channel for this CosemValueDescriptor
     */
    public CosemQuantity(Unit<Q> unit, String channelId) {
        super(channelId);
        this.unit = unit;
    }

    /**
     * Parses a String value (that represents a value with a unit) to a {@link QuantityType} object.
     *
     * @param cosemValue the value to parse
     * @return {@link QuanitytType} on success
     * @throws ParseException in case unit doesn't match.
     */
    @Override
    protected QuantityType<Q> getStateValue(String cosemValue) throws ParseException {
        try {
            QuantityType<Q> qt = new QuantityType<Q>(prepare(cosemValue));

            if (!unit.equals(qt.getUnit())) {
                throw new ParseException("Failed to parse value '" + cosemValue + "' as unit " + unit, 0);
            }
            return qt;
        } catch (IllegalArgumentException nfe) {
            throw new ParseException("Failed to parse value '" + cosemValue + "' as unit " + unit, 0);
        }
    }

    /**
     * Check if COSEM value has a unit, check and parse the value. We assume here numbers (float or integers)
     * The specification states that the delimiter between the value and the unit is a '*'-character.
     * We have seen on the Kaifa 0025 meter that both '*' and the '_' character are used.
     *
     * On the Kampstrup 162JxC in some CosemValues the separator is missing. This
     *
     * The above quirks are supported
     *
     * We also support unit that do not follow the exact case.
     */
    private String prepare(String cosemValue) {
        Matcher matcher = COSEM_VALUE_WITH_UNIT_PATTERN.matcher(cosemValue.replace("m3", "m³"));

        return matcher.find() ? matcher.group(1) + ' ' + matcher.group(2) : cosemValue;
    }
}
