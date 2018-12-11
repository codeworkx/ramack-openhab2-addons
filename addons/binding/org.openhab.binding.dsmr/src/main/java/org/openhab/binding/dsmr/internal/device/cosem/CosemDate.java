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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CosemDate represents a datetime value and will try to autodetect the format
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Class now a factory instead of data containing class
 */
class CosemDate extends CosemValueDescriptor<DateTimeType> {

    public static final CosemDate INSTANCE = new CosemDate("timestamp");

    private final Logger logger = LoggerFactory.getLogger(CosemDate.class);

    public CosemDate(String ohChannelId) {
        super(ohChannelId);
    }

    /**
     * This enum contains the known date formats for the DSMR-specification
     */
    private enum CosemDateFormat {
        /*
         * Ignore DST setting for general format. We use local time that is already DST
         */
        COSEM_DATE_GENERAL("(\\d{12})([S,W]?)", "yyMMddHHmmss"),
        COSEM_DATE_DSMR_V2("(\\d{2}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", "yy-MM-dd HH:mm:ss");

        /**
         * Cached compiled pattern
         */
        private final Pattern pattern;

        /**
         * Cached java date formatter
         */
        private final DateTimeFormatter formatter;

        /**
         * Constructs a new CosemDateFormat
         *
         * @param regex String containing the regular expression to check the value against (the date format
         *            should at least contain 1 regex group
         * @param javaDateFormat String containing the datetime format to use for parsing
         */
        private CosemDateFormat(String regex, String javaDateFormat) {
            pattern = Pattern.compile(regex);
            formatter = DateTimeFormatter.ofPattern(javaDateFormat);
        }
    }

    /**
     * Parses a String value to an openHAB DateTimeType
     * <p>
     * The input string must be in the format yyMMddHHmmssX
     * <p>
     * Based on the DSMR specification X is:
     * <p>
     * <ul>
     * <li>''. Valid for DSMR v3 specification
     * <li>'S'. Specifies a summer time (DST = 1) datetime
     * <li>'W'. Specifies a winter time (DST = 0) datetime
     * </ul>
     *
     * @param cosemValue the value to parse
     * @return {@link DateTimeType} representing the value the cosem value
     * @throws ParseException if parsing failed
     */
    @Override
    protected DateTimeType getStateValue(String cosemValue) throws ParseException {
        for (CosemDateFormat cosemDateFormat : CosemDateFormat.values()) {
            logger.trace("Trying pattern: {}", cosemDateFormat.pattern);

            Matcher m = cosemDateFormat.pattern.matcher(cosemValue);

            if (m.matches()) {
                logger.trace("{} matches pattern: {}", cosemValue, cosemDateFormat.pattern);

                LocalDateTime localDateTime = LocalDateTime.parse(m.group(1), cosemDateFormat.formatter);
                return new DateTimeType(ZonedDateTime.of(localDateTime, ZoneId.systemDefault()));
            }
            logger.debug("{} does not match pattern: {}", cosemValue, cosemDateFormat.pattern);
        }
        throw new ParseException("value: " + cosemValue + " is not a known CosemDate string", 0);
    }
}
