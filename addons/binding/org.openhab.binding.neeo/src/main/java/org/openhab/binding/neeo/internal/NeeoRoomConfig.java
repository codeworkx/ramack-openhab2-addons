/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.neeo.internal.handler.NeeoRoomHandler;

/**
 * THe configuration class for the room used by {@link NeeoRoomHandler}
 *
 * @author Tim Roberts - initial contribution
 */
@NonNullByDefault
public class NeeoRoomConfig {

    /** The NEEO room key */
    @Nullable
    private String roomKey;

    /** The refresh polling (in seconds) */
    private int refreshPolling;

    /** Whether to exclude things */
    private boolean excludeThings;

    /**
     * Gets the room key
     *
     * @return the room key
     */
    @Nullable
    public String getRoomKey() {
        return roomKey;
    }

    /**
     * Sets the room key.
     *
     * @param roomKey the new room key
     */
    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    /**
     * Gets the refresh polling (in seconds)
     *
     * @return the refresh polling
     */
    public int getRefreshPolling() {
        return refreshPolling;
    }

    /**
     * Set's the refresh polling
     *
     * @param refreshPolling the refresh polling
     */
    public void setRefreshPolling(int refreshPolling) {
        this.refreshPolling = refreshPolling;
    }

    /**
     * Whether to exclude things or not
     *
     * @return true to exclude, false otherwise
     */
    public boolean isExcludeThings() {
        return excludeThings;
    }

    /**
     * Sets whether to exclude things
     *
     * @param excludeThings true to exclude, false otherwise
     */
    public void setExcludeThings(boolean excludeThings) {
        this.excludeThings = excludeThings;
    }
}
