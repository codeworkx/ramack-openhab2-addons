/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.resol.internal.providers;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.resol.ResolBindingConstants;

import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile.Unit;

/**
 * @author Raphael Mack - Initial Contribution
 *
 */
public class ResolChannelTypeProvider implements ChannelTypeProvider {
    private Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<ChannelTypeUID, ChannelType>();

    public ResolChannelTypeProvider() {
        // let's add all channel types from known by the resol-vbus java library

        Specification spec = Specification.getDefaultSpecification();

        Unit[] units = spec.getUnits();
        for (Unit u : units) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, u.getUnitCodeText()); // TODO:
                                                                                                                       // precision
            int precision = 1; // TODO pfv.getPacketFieldSpec().getPrecision();
            if (u.getUnitId() >= 0) {
                ChannelType c = new ChannelType(channelTypeUID, false, "Number", u.getUnitFamily().toString(),
                        u.getUnitFamily().toString(), null, null, new StateDescription(null, null, null,
                                "%." + precision + "f " + u.getUnitTextText().replace("%", "%%"), true, null),
                        null);
                channelTypes.put(channelTypeUID, c);
            }
        }
    }

    @Override
    public @Nullable Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(@NonNull ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID)) {
            return channelTypes.get(channelTypeUID);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(@NonNull ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @Nullable Collection<@NonNull ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

}
