/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Common zone test.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public abstract class AbstractZoneControlXMLTest extends AbstractXMLProtocolTest {

    @Mock
    protected YamahaZoneConfig zoneConfig;

    @Mock
    protected ZoneControlStateListener zoneControlStateListener;

    protected DeviceInformationState deviceInformationState;

    @Mock
    protected InputConverter inputConverter;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        deviceInformationState = new DeviceInformationState();

        when(zoneConfig.getVolumeDbMin()).thenReturn(-10f);
        when(zoneConfig.getVolumeDbMax()).thenReturn(10f);

        when(inputConverter.fromStateName(anyString())).thenAnswer(p -> p.getArgument(0));
        when(inputConverter.toCommandName(anyString())).thenAnswer(p -> p.getArgument(0));
    }

}
