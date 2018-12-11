/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing Neeo Rooms (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoRooms {

    /** The rooms. */
    private final NeeoRoom @Nullable [] rooms;

    /**
     * Instantiates a new neeo rooms.
     *
     * @param rooms the rooms
     */
    NeeoRooms(NeeoRoom[] rooms) {
        Objects.requireNonNull(rooms, "rooms cannot be null");
        this.rooms = rooms;
    }

    /**
     * Gets the rooms.
     *
     * @return the rooms
     */
    public NeeoRoom[] getRooms() {
        final NeeoRoom @Nullable [] localRooms = rooms;
        return localRooms == null ? new NeeoRoom[0] : localRooms;
    }

    /**
     * Gets the room.
     *
     * @param key the key
     * @return the room
     */
    NeeoRoom getRoom(String key) {
        for (NeeoRoom room : getRooms()) {
            if (StringUtils.equalsIgnoreCase(key, room.getKey())) {
                return room;
            }
        }
        return new NeeoRoom();
    }

    @Override
    public String toString() {
        return "NeeoRooms [rooms=" + Arrays.toString(rooms) + "]";
    }
}
