/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.binding.smartmeter.internal.conformity.Conformity;
import org.openhab.binding.smartmeter.internal.helper.Baudrate;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the configuration options for a meter device.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class SmartMeterConfigProvider implements ConfigOptionProvider {

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        return ConfigOptionProvider.super.getParameterOptions(uri, param, context, locale);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable Locale locale) {
        if (!SmartMeterBindingConstants.THING_TYPE_SMLREADER.getAsString().equals(uri.getSchemeSpecificPart())) {
            return null;
        }

        switch (param) {

            case SmartMeterBindingConstants.CONFIGURATION_SERIAL_MODE:
                List<ParameterOption> options = new ArrayList<ParameterOption>();

                for (ProtocolMode mode : ProtocolMode.values()) {
                    options.add(new ParameterOption(mode.name(), mode.toString()));
                }
                return options;

            case SmartMeterBindingConstants.CONFIGURATION_BAUDRATE:
                options = new ArrayList<ParameterOption>();

                for (Baudrate baudrate : Baudrate.values()) {
                    options.add(new ParameterOption(baudrate.getBaudrate() + "", baudrate.toString()));
                }
                return options;

            case SmartMeterBindingConstants.CONFIGURATION_CONFORMITY:
                options = new ArrayList<ParameterOption>();

                for (Conformity conformity : Conformity.values()) {
                    options.add(new ParameterOption(conformity.name(), conformity.toString()));
                }
                return options;
        }
        return null;
    }

}
