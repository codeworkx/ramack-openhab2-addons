/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.internal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgwebos.handler.LGWebOSHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

/**
 * An abstract implementation of ChannelHander which serves as a base class for all concrete instances.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
abstract class BaseChannelHandler<T, R> implements ChannelHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseChannelHandler.class);

    private final ResponseListener<R> defaultResponseListener = new ResponseListener<R>() {

        @Override
        public void onError(@Nullable ServiceCommandError error) {
            logger.warn("{}: received error response: ", getClass().getName(), error);
        }

        @Override
        public void onSuccess(R object) {
            logger.debug("{}: {}.", getClass().getName(), object);
        }
    };

    // IP to Subscriptions map
    private Map<String, ServiceSubscription<T>> subscriptions = new ConcurrentHashMap<>();

    // lazy init
    private synchronized Map<String, ServiceSubscription<T>> getSubscriptions() {
        return subscriptions;
    }

    @Override
    public void onDeviceReady(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        // NOP
    }

    @Override
    public void onDeviceRemoved(ConnectableDevice device, String channelId, LGWebOSHandler handler) {
        // NOP
    }

    @Override
    public final synchronized void refreshSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        removeAnySubscription(device);
        if (handler.isChannelInUse(channelId)) { // only listen if least one item is configured for this channel
            Optional<ServiceSubscription<T>> listener = getSubscription(device, channelId, handler);

            if (listener.isPresent()) {
                logger.debug("Subscribed {} on IP: {}", this.getClass().getName(), device.getIpAddress());
                getSubscriptions().put(device.getIpAddress(), listener.get());
            }
        }
    }

    /**
     * Creates a subscription instance for this device if subscription is supported.
     *
     * @param device device to which state changes to subscribe to
     * @param channelID channel ID
     * @param handler
     * @return an {@code Optional} containing the ServiceSubscription, or an empty {@code Optional} if subscription is
     *         not supported.
     */
    protected Optional<ServiceSubscription<T>> getSubscription(ConnectableDevice device, String channelId,
            LGWebOSHandler handler) {
        return Optional.empty();
    }

    @Override
    public final synchronized void removeAnySubscription(ConnectableDevice device) {
        ServiceSubscription<T> l = subscriptions.remove(device.getIpAddress());

        if (l != null) {
            l.unsubscribe();
            logger.debug("Unsubscribed {} on IP: {}", this.getClass().getName(), device.getIpAddress());
        }
    }

    protected ResponseListener<R> getDefaultResponseListener() {
        return defaultResponseListener;
    }
}
