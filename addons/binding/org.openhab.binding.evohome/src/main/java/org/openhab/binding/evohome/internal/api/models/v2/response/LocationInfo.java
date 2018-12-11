/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the location info
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class LocationInfo {

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("name")
    private String name;

    @SerializedName("streetAddress")
    private String streetAddress;

    @SerializedName("city")
    private String city;

    @SerializedName("country")
    private String country;

    @SerializedName("postcode")
    private String postcode;

    @SerializedName("locationType")
    private String locationType;

    @SerializedName("useDaylightSaveSwitching")
    private boolean useDaylightSaveSwitching;

    @SerializedName("timeZone")
    private TimeZone timeZone;

    @SerializedName("locationOwner")
    private LocationOwner locationOwner;

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

}
