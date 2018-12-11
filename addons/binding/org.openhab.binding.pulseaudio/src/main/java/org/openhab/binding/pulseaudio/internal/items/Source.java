/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.internal.items;

/**
 * A Source is a device which is the source of an audio stream (recording
 * device) For example microphones or line-in jacks.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class Source extends AbstractAudioDeviceConfig {

    protected Sink monitorOf;

    public Source(int id, String name, Module module) {
        super(id, name, module);
    }

    public Sink getMonitorOf() {
        return monitorOf;
    }

    public void setMonitorOf(Sink sink) {
        this.monitorOf = sink;
    }

}
