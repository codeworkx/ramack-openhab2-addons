/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plclogo.internal.config;

import static org.openhab.binding.plclogo.PLCLogoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PLCDigitalConfiguration} is a base class for configuration
 * of Siemens LOGO! PLC digital input/outputs blocks.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
public class PLCDigitalConfiguration extends PLCCommonConfiguration {

    private String kind = "";

    @Override
    public String getBlockKind() {
        return kind;
    }

    /**
     * Set Siemens LOGO! blocks kind.
     * Can be I, Q, M, NI or NQ for digital blocks and
     * AI, AM, AQ, NAI or NAQ for analog
     *
     * @param kind Siemens LOGO! blocks kind
     */
    public void setBlockKind(final String kind) {
        this.kind = kind.trim();
    }

    @Override
    public String getChannelType() {
        return (kind.equalsIgnoreCase("I") || kind.equalsIgnoreCase("NI")) ? DIGITAL_INPUT_ITEM : DIGITAL_OUTPUT_ITEM;
    }

}
