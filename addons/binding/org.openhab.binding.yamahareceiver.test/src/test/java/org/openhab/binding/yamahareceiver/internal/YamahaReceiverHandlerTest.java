/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaBridgeHandler;
import org.openhab.binding.yamahareceiver.internal.protocol.ConnectionStateListener;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.AbstractXMLProtocolTest;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test cases for {@link YamahaBridgeHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class YamahaReceiverHandlerTest extends AbstractXMLProtocolTest {

    private YamahaBridgeHandler subject;

    @Mock
    private YamahaBridgeConfig bridgeConfig;

    @Mock
    private Configuration configuration;

    @Mock
    private ProtocolFactory protocolFactory;

    @Mock
    private DeviceInformation deviceInformation;

    @Mock
    private SystemControl systemControl;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge bridge;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        initMocks(this);

        ctx.prepareForModel(TestModels.RX_S601D);
        ctx.respondWith("<Main_Zone><Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input></Main_Zone>", "Main_Zone_Input_Input_Sel.xml");

        when(bridgeConfig.getHostWithPort()).thenReturn(Optional.of("localhost:80"));
        when(bridgeConfig.getInputMapping()).thenReturn("");
        when(bridgeConfig.getRefreshInterval()).thenReturn(10);

        when(configuration.as(YamahaBridgeConfig.class)).thenReturn(bridgeConfig);
        when(bridge.getConfiguration()).thenReturn(configuration);

        when(protocolFactory.DeviceInformation(any(), any())).thenReturn(deviceInformation);
        when(protocolFactory.SystemControl(any(), any(), any())).thenReturn(systemControl);

        subject = new YamahaBridgeHandler(bridge);
        subject.setProtocolFactory(protocolFactory);
        subject.setCallback(callback);

        doAnswer(a -> {
            ((ConnectionStateListener) a.getArgument(1)).connectionEstablished(ctx.getConnection());
            return null;
        }).when(protocolFactory).createConnection(anyString(), same(subject));
    }

    @Test
    public void afterInitializeBridgeShouldBeOnline() throws InterruptedException {

        // when
        subject.initialize();
        // internally there is an timer, let's allow it to execute
        Thread.sleep(200);

        // then
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(same(bridge), statusInfoCaptor.capture());

        List<ThingStatusInfo> thingStatusInfo = statusInfoCaptor.getAllValues();
        // the first one will be OFFLINE
        assertThat(thingStatusInfo.get(0).getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        // depending on the internal timer, several status updates and especially the last one will be ONLINE
        assertThat(thingStatusInfo.get(thingStatusInfo.size() - 1).getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }
}
