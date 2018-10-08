/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader.internal.filereader.api;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface for log file readers.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface LogFileReader {

    /**
     * Register listener.
     *
     * @param fileReaderListener callback implementation to register.
     * @return true if registering successfully done.
     */
    boolean registerListener(FileReaderListener fileReaderListener);

    /**
     * Unregister listener.
     *
     * @param fileReaderListener callback implementation to unregister.
     * @return true if unregistering successfully done.
     */
    boolean unregisterListener(FileReaderListener fileReaderListener);

    /**
     * Start log file reader.
     *
     * @param filePath file to read.
     * @param refreshRate how often file is read.
     * @param scheduler executor service to use.
     * @throws FileReaderException
     */
    void start(String filePath, long refreshRate, ScheduledExecutorService scheduler) throws FileReaderException;

    /**
     * Stop log file reader.
     */
    void stop();
}
