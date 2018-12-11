/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.json.response.ICloudDeviceInformation;

/**
 * Classes that implement this interface are interested in device information updates.
 *
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public interface ICloudDeviceInformationListener {
    void deviceInformationUpdate(List<ICloudDeviceInformation> deviceInformationList);
}
