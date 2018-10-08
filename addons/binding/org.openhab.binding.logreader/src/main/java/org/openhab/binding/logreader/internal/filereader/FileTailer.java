/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.logreader.internal.filereader;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.logreader.internal.filereader.api.FileReaderException;
import org.openhab.binding.logreader.internal.filereader.api.LogFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache Tailer based log file reader implementation.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class FileTailer extends AbstractLogFileReader implements LogFileReader {

    private final Logger logger = LoggerFactory.getLogger(FileTailer.class);

    private Tailer tailer;

    TailerListener logListener = new TailerListenerAdapter() {

        @Override
        public void handle(@Nullable String line) {
            sendLineToListeners(line);
        }

        @Override
        public void fileNotFound() {
            sendFileNotFoundToListeners();
        }

        @Override
        public void handle(@Nullable Exception e) {
            sendExceptionToListeners(e);
        }

        @Override
        public void fileRotated() {
            sendFileRotationToListeners();
        }
    };

    @Override
    public void start(String filePath, long refreshRate, ScheduledExecutorService scheduler)
            throws FileReaderException {
        tailer = new Tailer(new File(filePath), logListener, refreshRate, true, false, true);

        try {
            logger.debug("Start executor");
            scheduler.execute(tailer);
        } catch (Exception e) {
            throw new FileReaderException(e);
        }
    }

    @Override
    public void stop() {
        logger.debug("Shutdown");

        if (tailer != null) {
            tailer.stop();
        }
    }
}
