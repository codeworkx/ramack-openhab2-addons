/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Maps 'On' and 'Off' string values to a boolean
 *
 * @author Jeroen Idserda - Initial contribution
 */
public class OnOffAdapter extends XmlAdapter<String, Boolean> {

    @Override
    public Boolean unmarshal(String v) throws Exception {
        if (v != null) {
            return Boolean.valueOf(v.toLowerCase().equals("on"));
        }

        return Boolean.FALSE;
    }

    @Override
    public String marshal(Boolean v) throws Exception {
        return v ? "On" : "Off";
    }
}
