/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Extends the ChannelTypeProvider for user defined channel and channel group types.
 *
 * @author Markus Eckhardt - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, KM200ChannelTypeProvider.class }, immediate = true)
public class KM200ChannelTypeProvider implements ChannelTypeProvider {
    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<ChannelType>();
    private List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<ChannelGroupType>();

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes;
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<ChannelType>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }
}
