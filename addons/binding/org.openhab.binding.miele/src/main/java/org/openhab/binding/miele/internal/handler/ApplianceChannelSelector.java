/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.internal.handler;

import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler.DeviceMetaData;

/**
 * The {@link ApplianceChannelSelector} class defines a common interface for
 * all the data structures used by appliance thing handlers. It is used to traverse
 * the channels that possibly exist for an appliance, and convert data
 * returned by the appliance to a ESH compatible State
 *
 * @author Karel Goderis - Initial contribution
 */
public interface ApplianceChannelSelector {

    @Override
    String toString();

    /**
     * Returns the ESH ChannelID for the given datapoint
     */
    String getChannelID();

    /**
     * Returns the Miele defined ID for the given datapoint
     */
    String getMieleID();

    /**
     * Returns true if the given datapoint is to be considered as a Property
     * instead of a regular modifiable datapoint
     */
    boolean isProperty();

    /**
     *
     * Returns a State for the given string, taking into
     * account the metadata provided. The meta data is sent by
     * the Miele appliance and is used to decide the State type
     *
     * @param s - the value to be used to instantiate the State
     * @param dmd - the device meta data
     */
    State getState(String s, DeviceMetaData dmd);

    /**
     * Returns "compatible" Type for this datapoint
     */
    Class<? extends Type> getTypeClass();
}
