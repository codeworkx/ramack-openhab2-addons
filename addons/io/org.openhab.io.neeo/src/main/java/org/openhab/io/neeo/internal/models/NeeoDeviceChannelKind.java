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
import org.eclipse.smarthome.core.thing.type.ChannelKind;

/**
 * Enumeration of channel kinds (item or trigger)
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public enum NeeoDeviceChannelKind {
    /** Represents an item */
    ITEM("item"),
    /** Represents an trigger item */
    TRIGGER("trigger");

    /** The text value of the enum */
    private final String text;

    /**
     * Constructs the NeeoDeviceChannelKind using the specified text
     *
     * @param text the text
     */
    private NeeoDeviceChannelKind(final String text) {
        Objects.requireNonNull(text, "text is required");
        this.text = text;
    }

    /**
     * Parses the text into a NeeoDeviceChannelKind enum (ignoring case)
     *
     * @param text the text to parse
     * @return the NeeoDeviceChannelKind type
     */
    public static NeeoDeviceChannelKind parse(final String text) {
        if (StringUtils.isEmpty(text)) {
            return ITEM;
        }
        for (NeeoDeviceChannelKind enm : NeeoDeviceChannelKind.values()) {
            if (StringUtils.equalsIgnoreCase(text, enm.text)) {
                return enm;
            }
        }

        return ITEM;
    }

    /**
     * Returns the {@link NeeoDeviceChannelKind} for the given {@link ChannelKind}
     *
     * @param kind a non-null {@link ChannelKind}
     * @return a non-null {@link NeeoDeviceChannelKind}
     */
    public static NeeoDeviceChannelKind get(ChannelKind kind) {
        Objects.requireNonNull(kind, "kind cannot be null");
        return kind == ChannelKind.TRIGGER ? TRIGGER : ITEM;
    }

    @Override
    public String toString() {
        return text;
    }
}
