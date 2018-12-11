/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.handler;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * Subclass that configures CcoHandler for Pulsed outputs.
 *
 * @author Bob Adair - Initial contribution
 */

public class PulsedCcoHandler extends CcoHandler {

    public PulsedCcoHandler(Thing thing) {
        super(thing);
        this.outputType = CcoOutputType.PULSED;
    }

}
