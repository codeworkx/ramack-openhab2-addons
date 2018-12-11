/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.plugwise.internal.handler.PlugwiseStickHandler;

/**
 * Interface for listeners of {@link PlugwiseStickHandler} thing status changes.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface PlugwiseStickStatusListener {

    public void stickStatusChanged(ThingStatus status);

}
