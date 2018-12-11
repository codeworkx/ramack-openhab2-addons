/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents an {@link HttpRequest} response
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class HttpResponse {

    /** The http status */
    private final int httpStatus;

    /** The http reason */
    private final String httpReason;

    /** The http headers */
    private final Map<String, String> headers = new HashMap<>();

    /** The contents as a raw byte array */
    private final byte @Nullable [] contents;

    /**
     * Instantiates a new http response from the {@link Response}.
     *
     * @param response the non-null response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    HttpResponse(Response response) throws IOException {
        Objects.requireNonNull(response, "response cannot be null");

        httpStatus = response.getStatus();
        httpReason = response.getStatusInfo().getReasonPhrase();

        if (response.hasEntity()) {
            InputStream is = response.readEntity(InputStream.class);
            contents = IOUtils.toByteArray(is);
        } else {
            contents = null;
        }

        for (String key : response.getHeaders().keySet()) {
            headers.put(key, response.getHeaderString(key));
        }
    }

    /**
     * Instantiates a new http response.
     *
     * @param httpCode the http code
     * @param msg the msg
     */
    HttpResponse(int httpCode, String msg) {
        httpStatus = httpCode;
        httpReason = msg;
        contents = null;
    }

    /**
     * Gets the HTTP status code.
     *
     * @return the HTTP status code
     */
    public int getHttpCode() {
        return httpStatus;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    public String getContent() {
        final byte[] localContents = contents;
        if (localContents == null || localContents.length == 0) {
            return "";
        }

        final Charset charSet = Charset.forName("utf-8");
        return new String(localContents, charSet);
    }

    /**
     * Creates an {@link IOException} from the {@link #httpReason}
     *
     * @return the IO exception
     */
    public IOException createException() {
        return new IOException(httpReason);
    }

    @Override
    public String toString() {
        return getHttpCode() + " (" + (contents == null ? ("http reason: " + httpReason) : getContent()) + ")";
    }
}
