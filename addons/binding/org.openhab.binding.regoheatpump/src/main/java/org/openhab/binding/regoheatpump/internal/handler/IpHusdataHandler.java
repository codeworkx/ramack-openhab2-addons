/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.IpRegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;

/**
 * The {@link IpHusdataHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class IpHusdataHandler extends HusdataHandler {
    public IpHusdataHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected RegoConnection createConnection() {
        String host = (String) getConfig().get(RegoHeatPumpBindingConstants.HOST_PARAMETER);
        Integer port = ((Number) getConfig().get(RegoHeatPumpBindingConstants.TCP_PORT_PARAMETER)).intValue();

        return new IpRegoConnection(host, port);
    }
}
