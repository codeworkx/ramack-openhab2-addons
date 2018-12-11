/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import java.time.Instant;

/**
 * The {@link NukiBaseResponse} class is the base class for API Responses.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBaseResponse {

    private int status;
    private String message;
    private boolean success;
    private Instant created;

    public NukiBaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.created = Instant.now();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Instant getCreated() {
        return this.created;
    }
}
