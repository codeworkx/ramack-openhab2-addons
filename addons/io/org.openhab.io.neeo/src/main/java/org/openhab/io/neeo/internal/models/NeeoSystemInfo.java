/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The model representing an NEEO system information (serialize/deserialize json use only). This model only represents a
 * small portion of the system information
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoSystemInfo {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoSystemInfo.class);

    /** The host name of the brain. */
    private final String hostname;

    /** The firmware version of the brain */
    private final String firmwareVersion;

    /** Regular expression to parse semver version strings */
    private static final Pattern semVerPattern = Pattern.compile("[\\D]*([\\d]{1,3})\\.([\\d]{1,3})\\.([\\d]{1,3}).*");

    /**
     * Creates the system information from the hostname/firmwareVersion
     *
     * @param hostname the non-empty hostname
     * @param firmwareVersion the non-empty firmware version of the brain
     */
    public NeeoSystemInfo(String hostname, String firmwareVersion) {
        NeeoUtil.requireNotEmpty(hostname, "hostname cannot be null");
        NeeoUtil.requireNotEmpty(firmwareVersion, "firmwareVersion cannot be null");
        this.hostname = hostname;
        this.firmwareVersion = firmwareVersion;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the firmware verison of the brain
     *
     * @return the firmware version
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Checks to see if the brain's firmware version is greater or equal to the passed version. Version must be similar
     * to a semver (M-m-P)
     *
     * @param checkFirmwareVersion a possibly null, possibly empty firmware version
     * @return true if greater than or equal, false otherwise
     */
    public boolean isFirmwareGreaterOrEqual(String checkFirmwareVersion) {
        if (StringUtils.isEmpty(checkFirmwareVersion)) {
            return true;
        }

        final Matcher brainVersion = semVerPattern.matcher(this.firmwareVersion);
        final Matcher checkVersion = semVerPattern.matcher(checkFirmwareVersion);

        if (!brainVersion.matches() || brainVersion.groupCount() < 3) {
            logger.warn("Cannot parse the brain's firmware {}", this.firmwareVersion);
            return false;
        }

        if (!checkVersion.matches() || checkVersion.groupCount() < 3) {
            logger.warn("Cannot parse the check firmware {}", checkFirmwareVersion);
            return false;
        }

        for (int i = 1; i <= 3; i++) {
            int brainPart;
            try {
                brainPart = Integer.parseInt(brainVersion.group(i));
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse the brain's firmware {}", this.firmwareVersion);
                return false;
            }

            int checkPart;
            try {
                checkPart = Integer.parseInt(checkVersion.group(i));
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse the check firmware {}", checkFirmwareVersion);
                return false;
            }

            if (brainPart > checkPart) {
                return true;
            }
            if (brainPart < checkPart) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hostname.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof NeeoSystemInfo)) {
            return false;
        }

        return StringUtils.equals(hostname, ((NeeoSystemInfo) obj).hostname);
    }

}
