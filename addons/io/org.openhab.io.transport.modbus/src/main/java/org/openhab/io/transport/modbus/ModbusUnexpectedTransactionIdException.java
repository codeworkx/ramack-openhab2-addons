/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception representing situation where transaction id of the response does not match request
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusUnexpectedTransactionIdException extends ModbusTransportException {

    private static final long serialVersionUID = -2453232634024813933L;
    private int requestId;
    private int responseId;

    public ModbusUnexpectedTransactionIdException(int requestId, int responseId) {
        this.requestId = requestId;
        this.responseId = responseId;

    }

    @Override
    public String getMessage() {
        return String.format("Transaction id of response (%d) does not equal request (%d)", requestId, responseId);
    }

    @Override
    public String toString() {
        return String.format(
                "ModbusUnexpectedTransactionIdException(requestTransactionId=%d, responseTransactionId=%d)", requestId,
                responseId);
    }

    public int getRequestId() {
        return requestId;
    }

    public int getResponseId() {
        return responseId;
    }

}
