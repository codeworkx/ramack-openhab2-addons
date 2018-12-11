/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorMessage {
    // For websocket change events
    public String e = ""; // "changed"
    public String r = ""; // "sensors"
    public String t = ""; // "event"
    public String id = ""; // "3"

    // for rest API
    public String manufacturername = "";
    public String modelid = "";
    public String name = "";
    public String swversion = "";
    public String type = "";
    /** the API endpoint **/
    public String ep = "";
    public SensorConfig config = new SensorConfig();

    // websocket and rest api
    public String uniqueid = ""; // "00:0b:57:ff:fe:94:6b:dd-01-1000"
    public SensorState state = new SensorState();
}
