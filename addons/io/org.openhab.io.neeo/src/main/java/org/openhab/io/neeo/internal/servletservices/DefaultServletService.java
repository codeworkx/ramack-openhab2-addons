/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.servletservices;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;

/**
 * The default implementation of {@link ServletService} that simply does nothing (allowing subclasses to override
 * whatever functionality they need).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
class DefaultServletService implements ServletService {

    /**
     * Overridden to simple return false
     *
     * @see org.openhab.io.neeo.internal.servletservices.ServletService#canHandleRoute(java.lang.String[])
     */
    @Override
    public boolean canHandleRoute(String[] paths) {
        return false;
    }

    /**
     * Overridden to simply do nothing
     *
     * @see org.openhab.io.neeo.internal.servletservices.ServletService#handleGet(javax.servlet.http.HttpServletRequest,
     *      java.lang.String[], javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
    }

    /**
     * Overridden to simply do nothing
     *
     * @see
     *      org.openhab.io.neeo.internal.servletservices.ServletService#handlePost(javax.servlet.http.HttpServletRequest,
     *      java.lang.String[], javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void handlePost(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
    }

    /**
     * Overridden to simply return false
     *
     * @see org.openhab.io.neeo.internal.servletservices.ServletService#handleEvent(org.eclipse.smarthome.core.events.Event)
     */
    @Override
    public boolean handleEvent(Event event) {
        return false;
    }

    /**
     * Overridden to simply return null
     *
     * @see ServletService#getEventFilter()
     */
    @Nullable
    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    /**
     * Overridden to simply do nothing
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
    }
}
