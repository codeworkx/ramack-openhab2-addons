/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.internal;

/**
 * The result of a seneye readout - The PH water level
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeDeviceReadingPh {
    public int trend;
    public int critical_in;
    public double avg;
    public boolean status;
    public double curr;
    public SeneyeDeviceReadingAdvice[] advices;

}
