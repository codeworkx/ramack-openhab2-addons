/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdpowerview.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for Things that are managed through an HD Power View Hub
 *
 * @author Andy Lintner - Initial contribution
 */
abstract class AbstractHubbedThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AbstractHubbedThingHandler.class);

    public AbstractHubbedThingHandler(Thing thing) {
        super(thing);
    }

    protected HDPowerViewHubHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error("Thing {} must belong to a hub", getThing().getThingTypeUID().getId());
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof HDPowerViewHubHandler)) {
            logger.debug("Thing {} belongs to the wrong hub type", getThing().getThingTypeUID().getId());
            return null;
        }
        return (HDPowerViewHubHandler) handler;
    }

}
