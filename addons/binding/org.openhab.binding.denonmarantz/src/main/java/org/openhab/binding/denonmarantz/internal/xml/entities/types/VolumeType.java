/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.entities.types;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openhab.binding.denonmarantz.internal.xml.adapters.VolumeAdapter;

/**
 * Contains a volume value (percentage)
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class VolumeType {

    @XmlJavaTypeAdapter(value = VolumeAdapter.class)
    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

}
