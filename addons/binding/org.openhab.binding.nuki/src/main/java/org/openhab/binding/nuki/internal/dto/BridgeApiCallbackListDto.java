/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dto;

import java.util.List;

/**
 * The {@link BridgeApiCallbackListDto} class defines the Data Transfer Object (POJO)
 * for the Nuki Bridge API /callback/list endpoint.
 *
 * @author Markus Katter - Initial contribution
 */
public class BridgeApiCallbackListDto {

    private List<BridgeApiCallbackListCallbackDto> callbacks;

    public List<BridgeApiCallbackListCallbackDto> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<BridgeApiCallbackListCallbackDto> callbacks) {
        this.callbacks = callbacks;
    }

}
