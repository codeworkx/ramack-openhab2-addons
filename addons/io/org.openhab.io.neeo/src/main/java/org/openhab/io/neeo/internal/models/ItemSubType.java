/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Describes the 'subtypes' for an item. A subtype is basically a property on the type that we will create a channel
 * for. Example: HSBType has hue, brightnes and saturation
 *
 * @author Tim Roberts - Initial Contribution
 *
 */
@NonNullByDefault
public enum ItemSubType {
    /** No subtypes (the type is the only value) */
    NONE("none"),

    /** The HUE subtype (valid on HSBType) */
    HUE("hue"),

    /** The Brightness subtype (valid on HSBType) */
    BRIGHTNESS("brightness"),

    /** The Saturation subtype (valid on HSBType) */
    SATURATION("saturation");

    /** The text value of the enum */
    private final String text;

    /**
     * Constructs the ItemSubType using the specified text
     *
     * @param text the text
     */
    private ItemSubType(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = text;
    }

    /**
     * Parses the text into a ItemSubType enum (ignoring case)
     *
     * @param text the text to parse
     * @return the ItemSubType type
     */
    public static ItemSubType parse(final String text) {
        if (StringUtils.isEmpty(text)) {
            return NONE;
        }
        for (ItemSubType enm : ItemSubType.values()) {
            if (StringUtils.equalsIgnoreCase(text, enm.text)) {
                return enm;
            }
        }

        return NONE;
    }

    /**
     * Determines if the specified text is a valid ItemSubType
     *
     * @param text the text to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(final String text) {
        if (StringUtils.isEmpty(text)) {
            return true;
        }
        for (ItemSubType enm : ItemSubType.values()) {
            if (StringUtils.equalsIgnoreCase(text, enm.text)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return text;
    }
}
