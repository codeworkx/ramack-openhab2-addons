/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaControl.PlayStateListener;
import com.connectsdk.service.capability.PlaylistControl;

/**
 * Handles commands of a Player Item.
 *
 *
 * @author Sebastian Prehn - initial contribution
 */
public class MediaControlPlayer extends BaseChannelHandler<PlayStateListener, Object> {
    private final Logger logger = LoggerFactory.getLogger(MediaControlPlayer.class);

    private MediaControl getMediaControl(ConnectableDevice device) {
        return device.getCapability(MediaControl.class);
    }

    private PlaylistControl getPlayListControl(ConnectableDevice device) {
        return device.getCapability(PlaylistControl.class);
    }

    @Override
    public void onReceiveCommand(@Nullable ConnectableDevice device, String channelId, LGWebOSHandler handler,
            Command command) {
        if (device == null) {
            return;
        }
        if (NextPreviousType.NEXT == command) {
            if (device.hasCapabilities(PlaylistControl.Next)) {
                getPlayListControl(device).next(getDefaultResponseListener());
            }
        } else if (NextPreviousType.PREVIOUS == command) {
            if (device.hasCapabilities(PlaylistControl.Previous)) {
                getPlayListControl(device).previous(getDefaultResponseListener());
            }
        } else if (PlayPauseType.PLAY == command) {
            if (device.hasCapabilities(MediaControl.Play)) {
                getMediaControl(device).play(getDefaultResponseListener());
            }
        } else if (PlayPauseType.PAUSE == command) {
            if (device.hasCapabilities(MediaControl.Pause)) {
                getMediaControl(device).pause(getDefaultResponseListener());
            }
        } else if (RewindFastforwardType.FASTFORWARD == command) {
            if (device.hasCapabilities(MediaControl.FastForward)) {
                getMediaControl(device).fastForward(getDefaultResponseListener());
            }
        } else if (RewindFastforwardType.REWIND == command) {
            if (device.hasCapabilities(MediaControl.Rewind)) {
                getMediaControl(device).rewind(getDefaultResponseListener());
            }
        } else {
            logger.warn("Only accept NextPreviousType, PlayPauseType, RewindFastforwardType. Type was {}.",
                    command.getClass());
        }
    }
}
