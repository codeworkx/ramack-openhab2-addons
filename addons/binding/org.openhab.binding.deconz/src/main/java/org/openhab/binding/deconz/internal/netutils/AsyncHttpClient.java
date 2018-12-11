/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.netutils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;;

/**
 * An asynchronous API for HTTP interactions.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class AsyncHttpClient {
    private final HttpClient client;

    public AsyncHttpClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Perform a POST request
     *
     * @param address The address
     * @param jsonString The message body
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public CompletableFuture<Result> post(String address, String jsonString, int timeout) {
        return doNetwork(HttpMethod.POST, address, jsonString, timeout);
    }

    /**
     * Perform a PUT request
     *
     * @param address The address
     * @param jsonString The message body
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public CompletableFuture<Result> put(String address, String jsonString, int timeout) {
        return doNetwork(HttpMethod.PUT, address, jsonString, timeout);
    }

    /**
     * Perform a GET request
     *
     * @param address The address
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public CompletableFuture<Result> get(String address, int timeout) {
        return doNetwork(HttpMethod.GET, address, null, timeout);
    }

    /**
     * Perform a DELETE request
     *
     * @param address The address
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public CompletableFuture<Result> delete(String address, int timeout) {
        return doNetwork(HttpMethod.DELETE, address, null, timeout);
    }

    private CompletableFuture<Result> doNetwork(HttpMethod method, String address, @Nullable String body, int timeout) {
        final CompletableFuture<Result> f = new CompletableFuture<Result>();
        Request request = client.newRequest(URI.create(address));
        if (body != null) {
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
                    final InputStreamContentProvider inputStreamContentProvider = new InputStreamContentProvider(
                            byteArrayInputStream)) {
                request.content(inputStreamContentProvider, "application/json");
            } catch (Exception e) {
                f.completeExceptionally(e);
                return f;
            }
        }

        request.method(method).timeout(timeout, TimeUnit.MILLISECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(org.eclipse.jetty.client.api.Result result) {
                final HttpResponse response = (HttpResponse) result.getResponse();
                if (result.getFailure() != null) {
                    f.completeExceptionally(result.getFailure());
                    return;
                }
                f.complete(new Result(getContentAsString(), response.getStatus()));
            }
        });
        return f;
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }
}
