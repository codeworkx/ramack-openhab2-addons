/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto.response;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * This response object is a bit complicated. The response is required to look like this:
 *
 * <pre>
 * {
 *   "success":{
 *   "/1d49eeed-1fa7-434a-8e6c-70bb2cdc3e8f/lights/1/state/on": true
 *   }
 * }
 * </pre>
 *
 * This object describes the right hand side of "success". The json key itself is the uri path
 * and the value is either a boolean, number or string.
 *
 * This is done with a custom serializer that creates the proper {@link JsonObject}.
 *
 * @author David Graeff - Initial contribution
 */
public class HueSuccessResponseStateChanged implements HueSuccessResponse {
    private transient Object value;
    private transient String relURI;

    public HueSuccessResponseStateChanged(String relURI, Object value) {
        this.relURI = relURI;
        this.value = value;
    }

    public static class Serializer implements JsonSerializer<HueSuccessResponseStateChanged> {

        @Override
        public JsonElement serialize(HueSuccessResponseStateChanged product, Type type, JsonSerializationContext jsc) {
            JsonObject jObj = new JsonObject();
            if (product.value instanceof Float) {
                jObj.addProperty(product.relURI, (Float) product.value);
            }
            if (product.value instanceof Double) {
                jObj.addProperty(product.relURI, (Double) product.value);
            }
            if (product.value instanceof Integer) {
                jObj.addProperty(product.relURI, (Integer) product.value);
            }
            if (product.value instanceof Boolean) {
                jObj.addProperty(product.relURI, (Boolean) product.value);
            }
            if (product.value instanceof String) {
                jObj.addProperty(product.relURI, (String) product.value);
            }
            return jObj;
        }
    }
}
