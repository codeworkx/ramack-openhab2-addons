/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.discovery.project;

import java.util.Collections;
import java.util.List;

/**
 * This class represents a Lutron system and the topology of device things within
 * that system.
 *
 * @author Allan Tong - Initial contribution
 * @author Bob Adair - Added Timeclock and Green Mode support
 */
public class Project {
    private String appVersion;
    private String xmlVersion;
    private List<Area> areas;
    private List<Timeclock> timeclocks;
    private List<GreenMode> greenmodes;

    public String getAppVersion() {
        return appVersion;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public List<Area> getAreas() {
        return areas != null ? areas : Collections.<Area> emptyList();
    }

    public List<Timeclock> getTimeclocks() {
        return timeclocks != null ? timeclocks : Collections.<Timeclock> emptyList();
    }

    public List<GreenMode> getGreenModes() {
        return greenmodes != null ? greenmodes : Collections.<GreenMode> emptyList();
    }

}
