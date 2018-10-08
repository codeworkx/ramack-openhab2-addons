/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pulseaudio.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.cli.Parser;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig.State;
import org.openhab.binding.pulseaudio.internal.items.Module;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.openhab.binding.pulseaudio.internal.items.Source;
import org.openhab.binding.pulseaudio.internal.items.SourceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client connects to a pulseaudio server via TCP. It reads the current state of the
 * pulseaudio server (available sinks, sources,...) and can send commands to the server.
 * The syntax of the commands is the same as for the pactl command line tool provided by pulseaudio.
 *
 * On the pulseaudio server the module-cli-protocol-tcp has to be loaded.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @since 1.2.0
 */
public class PulseaudioClient {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioClient.class);

    private String host;
    private int port;
    private Socket client;

    private List<AbstractAudioDeviceConfig> items;
    private List<Module> modules;

    /**
     * corresponding name to execute actions on sink items
     */
    private static final String ITEM_SINK = "sink";

    /**
     * corresponding name to execute actions on source items
     */
    private static final String ITEM_SOURCE = "source";

    /**
     * corresponding name to execute actions on sink-input items
     */
    private static final String ITEM_SINK_INPUT = "sink-input";

    /**
     * corresponding name to execute actions on source-output items
     */
    private static final String ITEM_SOURCE_OUTPUT = "source-output";

    /**
     * command to list the loaded modules
     */
    private static final String CMD_LIST_MODULES = "list-modules";

    /**
     * command to list the sinks
     */
    private static final String CMD_LIST_SINKS = "list-sinks";

    /**
     * command to list the sources
     */
    private static final String CMD_LIST_SOURCES = "list-sources";

    /**
     * command to list the sink-inputs
     */
    private static final String CMD_LIST_SINK_INPUTS = "list-sink-inputs";

    /**
     * command to list the source-outputs
     */
    private static final String CMD_LIST_SOURCE_OUTPUTS = "list-source-outputs";

    /**
     * command to load a module
     */
    private static final String CMD_LOAD_MODULE = "load-module";

    /**
     * command to unload a module
     */
    private static final String CMD_UNLOAD_MODULE = "unload-module";

    /**
     * name of the module-combine-sink
     */
    private static final String MODULE_COMBINE_SINK = "module-combine-sink";

    public PulseaudioClient() throws IOException {
        this("localhost", 4712);
    }

    public PulseaudioClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        items = new ArrayList<>();
        modules = new ArrayList<>();

        connect();
        update();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * updates the item states and their relationships
     */
    public void update() {
        modules.clear();
        modules.addAll(Parser.parseModules(listModules()));

        items.clear();
        if (PulseaudioBindingConstants.TYPE_FILTERS.get(PulseaudioBindingConstants.SINK_THING_TYPE.getId()) == true) {
            logger.debug("reading sinks");
            items.addAll(Parser.parseSinks(listSinks(), this));
        }
        if (PulseaudioBindingConstants.TYPE_FILTERS.get(PulseaudioBindingConstants.SOURCE_THING_TYPE.getId()) == true) {
            logger.debug("reading sources");
            items.addAll(Parser.parseSources(listSources(), this));
        }
        if (PulseaudioBindingConstants.TYPE_FILTERS
                .get(PulseaudioBindingConstants.SINK_INPUT_THING_TYPE.getId()) == true) {
            logger.debug("reading sink-inputs");
            items.addAll(Parser.parseSinkInputs(listSinkInputs(), this));
        }
        if (PulseaudioBindingConstants.TYPE_FILTERS
                .get(PulseaudioBindingConstants.SOURCE_OUTPUT_THING_TYPE.getId()) == true) {
            logger.debug("reading source-outputs");
            items.addAll(Parser.parseSourceOutputs(listSourceOutputs(), this));
        }
        logger.debug("Pulseaudio server {}: {} modules and {} items updated", host, modules.size(), items.size());
    }

    private String listModules() {
        return this.sendRawRequest(CMD_LIST_MODULES);
    }

    private String listSinks() {
        return this.sendRawRequest(CMD_LIST_SINKS);
    }

    private String listSources() {
        return this.sendRawRequest(CMD_LIST_SOURCES);
    }

    private String listSinkInputs() {
        return this.sendRawRequest(CMD_LIST_SINK_INPUTS);
    }

    private String listSourceOutputs() {
        return this.sendRawRequest(CMD_LIST_SOURCE_OUTPUTS);
    }

    /**
     * retrieves a module by its id
     *
     * @param id
     * @return the corresponding {@link Module} to the given <code>id</code>
     */
    public Module getModule(int id) {
        for (Module module : modules) {
            if (module.getId() == id) {
                return module;
            }
        }
        return null;
    }

    /**
     * send the command directly to the pulseaudio server
     * for a list of available commands please take a look at
     * http://www.freedesktop.org/wiki/Software/PulseAudio/Documentation/User/CLI
     *
     * @param command
     */
    public void sendCommand(String command) {
        sendRawCommand(command);
    }

    /**
     * retrieves a {@link Sink} by its name
     *
     * @return the corresponding {@link Sink} to the given <code>name</code>
     */
    public Sink getSink(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name) && item instanceof Sink) {
                return (Sink) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link Sink} by its id
     *
     * @return the corresponding {@link Sink} to the given <code>id</code>
     */
    public Sink getSink(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof Sink) {
                return (Sink) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link SinkInput} by its name
     *
     * @return the corresponding {@link SinkInput} to the given <code>name</code>
     */
    public SinkInput getSinkInput(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name) && item instanceof SinkInput) {
                return (SinkInput) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link SinkInput} by its id
     *
     * @return the corresponding {@link SinkInput} to the given <code>id</code>
     */
    public SinkInput getSinkInput(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof SinkInput) {
                return (SinkInput) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link Source} by its name
     *
     * @return the corresponding {@link Source} to the given <code>name</code>
     */
    public Source getSource(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name) && item instanceof Source) {
                return (Source) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link Source} by its id
     *
     * @return the corresponding {@link Source} to the given <code>id</code>
     */
    public Source getSource(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof Source) {
                return (Source) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link SourceOutput} by its name
     *
     * @return the corresponding {@link SourceOutput} to the given <code>name</code>
     */
    public SourceOutput getSourceOutput(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name) && item instanceof SourceOutput) {
                return (SourceOutput) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link SourceOutput} by its id
     *
     * @return the corresponding {@link SourceOutput} to the given <code>id</code>
     */
    public SourceOutput getSourceOutput(int id) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getId() == id && item instanceof SourceOutput) {
                return (SourceOutput) item;
            }
        }
        return null;
    }

    /**
     * retrieves a {@link AbstractAudioDeviceConfig} by its name
     *
     * @return the corresponding {@link AbstractAudioDeviceConfig} to the given <code>name</code>
     */
    public AbstractAudioDeviceConfig getGenericAudioItem(String name) {
        for (AbstractAudioDeviceConfig item : items) {
            if (item.getPaName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public List<AbstractAudioDeviceConfig> getItems() {
        return items;
    }

    /**
     * changes the <code>mute</code> state of the corresponding {@link Sink}
     *
     * @param item the {@link Sink} to handle
     * @param mute mutes the sink if true, unmutes if false
     */
    public void setMute(AbstractAudioDeviceConfig item, boolean mute) {
        if (item == null) {
            return;
        }
        String itemCommandName = getItemCommandName(item);
        if (itemCommandName == null) {
            return;
        }
        String muteString = mute ? "1" : "0";
        sendRawCommand("set-" + itemCommandName + "-mute " + item.getId() + " " + muteString);
        // update internal data
        item.setMuted(mute);
    }

    /**
     * change the volume of a {@link AbstractAudioDeviceConfig}
     *
     * @param item the {@link AbstractAudioDeviceConfig} to handle
     * @param vol the new volume value the {@link AbstractAudioDeviceConfig} should be changed to (possible values from
     *            0 - 65536)
     */
    public void setVolume(AbstractAudioDeviceConfig item, int vol) {
        if (item == null) {
            return;
        }
        String itemCommandName = getItemCommandName(item);
        if (itemCommandName == null) {
            return;
        }
        sendRawCommand("set-" + itemCommandName + "-volume " + item.getId() + " " + vol);
        item.setVolume(Math.round(100f / 65536f * vol));
    }

    /**
     * returns the item names that can be used in commands
     *
     * @param item
     * @return
     */
    private String getItemCommandName(AbstractAudioDeviceConfig item) {
        if (item instanceof Sink) {
            return ITEM_SINK;
        } else if (item instanceof Source) {
            return ITEM_SOURCE;
        } else if (item instanceof SinkInput) {
            return ITEM_SINK_INPUT;
        } else if (item instanceof SourceOutput) {
            return ITEM_SOURCE_OUTPUT;
        }
        return null;
    }

    /**
     * change the volume of a {@link AbstractAudioDeviceConfig}
     *
     * @param item the {@link AbstractAudioDeviceConfig} to handle
     * @param vol the new volume percent value the {@link AbstractAudioDeviceConfig} should be changed to (possible
     *            values from 0 - 100)
     */
    public void setVolumePercent(AbstractAudioDeviceConfig item, int vol) {
        if (item == null) {
            return;
        }
        if (vol <= 100) {
            vol = toAbsoluteVolume(vol);
        }
        setVolume(item, vol);
    }

    /**
     * transform a percent volume to a value that can be send to the pulseaudio server (0-65536)
     *
     * @param percent
     * @return
     */
    private int toAbsoluteVolume(int percent) {
        return (int) Math.round(65536f / 100f * Double.valueOf(percent));
    }

    /**
     * changes the combined sinks slaves to the given <code>sinks</code>
     *
     * @param combinedSink the combined sink which slaves should be changed
     * @param sinks the list of new slaves
     */
    public void setCombinedSinkSlaves(Sink combinedSink, List<Sink> sinks) {
        if (combinedSink == null || !combinedSink.isCombinedSink()) {
            return;
        }
        List<String> slaves = new ArrayList<>();
        for (Sink sink : sinks) {
            slaves.add(sink.getPaName());
        }
        // 1. delete old combined-sink
        sendRawCommand(CMD_UNLOAD_MODULE + " " + combinedSink.getModule().getId());
        // 2. add new combined-sink with same name and all slaves
        sendRawCommand(CMD_LOAD_MODULE + " " + MODULE_COMBINE_SINK + " sink_name=" + combinedSink.getPaName()
                + " slaves=" + StringUtils.join(slaves, ","));
        // 3. update internal data structure because the combined sink has a new number + other slaves
        update();
    }

    /**
     * sets the sink a sink-input should be routed to
     *
     * @param sinkInput the sink-input to be rerouted
     * @param sink the new sink the sink-input should be routed to
     */
    public void moveSinkInput(SinkInput sinkInput, Sink sink) {
        if (sinkInput == null || sink == null) {
            return;
        }
        sendRawCommand("move-sink-input " + sinkInput.getId() + " " + sink.getId());
        sinkInput.setSink(sink);
    }

    /**
     * sets the sink a source-output should be routed to
     *
     * @param sourceOutput the source-output to be rerouted
     * @param source the new source the source-output should be routed to
     */
    public void moveSourceOutput(SourceOutput sourceOutput, Source source) {
        if (sourceOutput == null || source == null) {
            return;
        }
        sendRawCommand("move-sink-input " + sourceOutput.getId() + " " + source.getId());
        sourceOutput.setSource(source);
    }

    /**
     * suspend a source
     *
     * @param source the source which state should be changed
     * @param suspend suspend it or not
     */
    public void suspendSource(Source source, boolean suspend) {
        if (source == null) {
            return;
        }
        if (suspend) {
            sendRawCommand("suspend-source " + source.getId() + " 1");
            source.setState(State.SUSPENDED);
        } else {
            sendRawCommand("suspend-source " + source.getId() + " 0");
            // unsuspending the source could result in different states (RUNNING,IDLE,...)
            // update to get the new state
            update();
        }
    }

    /**
     * suspend a sink
     *
     * @param sink the sink which state should be changed
     * @param suspend suspend it or not
     */
    public void suspendSink(Sink sink, boolean suspend) {
        if (sink == null) {
            return;
        }
        if (suspend) {
            sendRawCommand("suspend-sink " + sink.getId() + " 1");
            sink.setState(State.SUSPENDED);
        } else {
            sendRawCommand("suspend-sink " + sink.getId() + " 0");
            // unsuspending the sink could result in different states (RUNNING,IDLE,...)
            // update to get the new state
            update();
        }
    }

    /**
     * changes the combined sinks slaves to the given <code>sinks</code>
     *
     * @param combinedSinkName the combined sink which slaves should be changed
     * @param sinks the list of new slaves
     */
    public void setCombinedSinkSlaves(String combinedSinkName, List<Sink> sinks) {
        if (getSink(combinedSinkName) != null) {
            return;
        }
        List<String> slaves = new ArrayList<>();
        for (Sink sink : sinks) {
            slaves.add(sink.getPaName());
        }
        // add new combined-sink with same name and all slaves
        sendRawCommand(CMD_LOAD_MODULE + " " + MODULE_COMBINE_SINK + " sink_name=" + combinedSinkName + " slaves="
                + StringUtils.join(slaves, ","));
        // update internal data structure because the combined sink is new
        update();
    }

    private void sendRawCommand(String command) {
        checkConnection();
        try {
            PrintStream out = new PrintStream(client.getOutputStream(), true);
            logger.trace("sending command {} to pa-server {}", command, host);
            out.print(command + "\r\n");
            out.close();
            client.close();
        } catch (IOException e) {
            logger.error("{}", e.getLocalizedMessage(), e);
        }
    }

    private String sendRawRequest(String command) {
        logger.trace("_sendRawRequest({})", command);
        checkConnection();
        String result = "";
        try {
            PrintStream out = new PrintStream(client.getOutputStream(), true);
            out.print(command + "\r\n");

            InputStream instr = client.getInputStream();

            try {
                byte[] buff = new byte[1024];
                int retRead = 0;
                int lc = 0;
                do {
                    retRead = instr.read(buff);
                    lc++;
                    if (retRead > 0) {
                        String line = new String(buff, 0, retRead);
                        // System.out.println("'"+line+"'");
                        if (line.endsWith(">>> ") && lc > 1) {
                            result += line.substring(0, line.length() - 4);
                            break;
                        }
                        result += line.trim();
                    }
                } while (retRead > 0);
            } catch (SocketTimeoutException e) {
                // Timeout -> as newer PA versions (>=5.0) do not send the >>> we have no chance
                // to detect the end of the answer, except by this timeout
            } catch (IOException e) {
                logger.error("Exception while reading socket: {}", e.getMessage());
            }
            instr.close();
            out.close();
            client.close();
            return result;
        } catch (IOException e) {
            logger.error("{}", e.getLocalizedMessage(), e);
        }
        return result;
    }

    private void checkConnection() {
        if (client == null || client.isClosed() || !client.isConnected()) {
            try {
                connect();
            } catch (IOException e) {
                logger.error("{}", e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Connects to the pulseaudio server (timeout 500ms)
     */
    private void connect() throws IOException {
        try {
            client = new Socket(host, port);
            client.setSoTimeout(500);
        } catch (UnknownHostException e) {
            logger.error("unknown socket host {}", host);
        } catch (SocketException e) {
            logger.error("{}", e.getLocalizedMessage(), e);
        }
    }

    /**
     * Disconnects from the pulseaudio server
     */
    public void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("{}", e.getLocalizedMessage(), e);
            }
        }
    }

}
