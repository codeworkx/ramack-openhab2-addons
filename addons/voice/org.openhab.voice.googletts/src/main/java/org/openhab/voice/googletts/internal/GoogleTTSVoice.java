/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal;

import java.util.Locale;

import org.eclipse.smarthome.core.voice.Voice;
import org.openhab.voice.googletts.internal.protocol.SsmlVoiceGender;

/**
 * Implementation of the Voice interface for Google Cloud TTS Service.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class GoogleTTSVoice implements Voice {

    /**
     * Voice locale
     */
    private final Locale locale;

    /**
     * Voice label
     */
    private final String label;

    /**
     * Gender
     */
    private final String ssmlGender;

    /**
     * Constructs a Google Cloud TTS Voice for the passed data
     *
     * @param locale The Locale of the voice
     * @param label The label of the voice
     * @param ssmlGender Voice gender
     */
    GoogleTTSVoice(Locale locale, String label, String ssmlGender) {
        this.locale = locale;
        this.ssmlGender = ssmlGender;
        this.label = label;
    }

    /**
     * Globally unique identifier of the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
    @Override
    public String getUID() {
        return "googletts:" + getTechnicalName();
    }

    /**
     * Technical name of the voice.
     *
     * @return A String voice technical name
     */
    String getTechnicalName() {
        return label.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * The voice label, used for GUI's or VUI's
     *
     * @return The voice label, may not be globally unique
     */
    @Override
    public String getLabel() {
        return this.label;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * The voice gender.
     *
     * @return {@link SsmlVoiceGender} enum name.
     */
    String getSsmlGender() {
        return ssmlGender;
    }
}
