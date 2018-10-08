/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.model.device;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;

/**
 * Shutter device, containing level. Stoppable and pulseable attributes currently hardcoded to 0 (false).
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ShutterDevice extends AbstractEnergyLinkDevice {

    public ShutterDevice(Item item) {
        super(DeviceType.SHUTTER, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        int level = 0;

        PercentType percentState = (PercentType) item.getStateAs(PercentType.class);
        if (percentState != null) {
            level = percentState.intValue();
        }

        addParam(new DeviceParam(ParamType.PULSEABLE, "0"));
        addParam(new DeviceParam(ParamType.STOPPABLE, getLinks().containsKey("stopper") ? "1" : "0"));
        addParam(new DeviceParam(ParamType.LEVEL, String.valueOf(level)));
    }

}
