/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer.internal.config;

import org.openhab.binding.synopanalyzer.internal.handler.SynopAnalyzerHandler;

/**
 * The {@link SynopAnalyzerConfiguration} is responsible for holding configuration
 * informations needed for {@link SynopAnalyzerHandler}
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SynopAnalyzerConfiguration {
    public long refreshInterval;
    public String stationId;
}
