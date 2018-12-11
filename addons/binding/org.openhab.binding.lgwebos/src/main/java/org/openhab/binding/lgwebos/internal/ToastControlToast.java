/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.ToastControl;

/**
 * Handles Toast Control Command. This allows to send messages to the TV screen.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class ToastControlToast extends BaseChannelHandler<Void, Object> {
    private final Logger logger = LoggerFactory.getLogger(ToastControlToast.class);

    private ToastControl getControl(ConnectableDevice device) {
        return device.getCapability(ToastControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        if (device.hasCapabilities(ToastControl.Show_Toast)) {
            final String value = command.toString();
            final ToastControl control = getControl(device);
            try {
                BufferedImage bi = ImageIO.read(getClass().getResource("/openhab-logo-square.png"));
                try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                        OutputStream b64 = Base64.getEncoder().wrap(os);) {
                    ImageIO.write(bi, "png", b64);
                    control.showToast(value, os.toString(StandardCharsets.UTF_8.name()), "png",
                            getDefaultResponseListener());
                }
            } catch (IOException ex) {
                logger.warn("Failed to load toast icon: {}", ex.getMessage());
            }
        }
    }
}
