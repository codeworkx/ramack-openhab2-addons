/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.minecraft.internal.MinecraftBindingConstants;
import org.openhab.binding.minecraft.internal.config.PlayerConfig;
import org.openhab.binding.minecraft.internal.message.OHMessage;
import org.openhab.binding.minecraft.internal.message.data.PlayerData;
import org.openhab.binding.minecraft.internal.message.data.commands.PlayerCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import rx.Observable;
import rx.Subscription;

/**
 * The {@link MinecraftPlayerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class MinecraftPlayerHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MinecraftPlayerHandler.class);

    private Subscription playerSubscription;
    private MinecraftServerHandler bridgeHandler;
    private PlayerConfig config;
    private Gson gson = new GsonBuilder().create();

    public MinecraftPlayerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        this.bridgeHandler = getBridgeHandler();
        this.config = getThing().getConfiguration().as(PlayerConfig.class);

        if (bridgeHandler == null || getThing().getBridgeUID() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge configured");

            return;
        }

        updateStatus(ThingStatus.ONLINE);
        hookupListeners(bridgeHandler);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!playerSubscription.isUnsubscribed()) {
            playerSubscription.unsubscribe();
        }
    }

    private void hookupListeners(MinecraftServerHandler bridgeHandler) {
        playerSubscription = bridgeHandler.getPlayerRx().doOnNext(players -> {
            boolean playerOnline = false;
            for (PlayerData player : players) {
                if (config.getName().equals(player.getName())) {
                    playerOnline = true;
                    break;
                }
            }
            State onlineState = playerOnline ? OnOffType.ON : OnOffType.OFF;
            updateState(MinecraftBindingConstants.CHANNEL_PLAYER_ONLINE, onlineState);
        }).flatMap(players -> Observable.from(players)).filter(player -> config.getName().equals(player.getName()))
                .subscribe(player -> updatePlayerState(player));
    }

    /**
     * Updates the state of player
     *
     * @param player the player to update
     */
    private void updatePlayerState(PlayerData player) {
        State playerLevel = new DecimalType(player.getLevel());
        State playerLevelPercentage = new DecimalType(player.getExperience());
        State playerTotalExperience = new DecimalType(player.getTotalExperience());
        State playerHealth = new DecimalType(player.getHealth());
        State playerWalkSpeed = new DecimalType(player.getWalkSpeed());
        DecimalType longitude = new DecimalType(player.getLocation().getX());
        DecimalType latitude = new DecimalType(player.getLocation().getY());
        DecimalType altitude = new DecimalType(player.getLocation().getY());
        State playerLocation = new PointType(latitude, longitude, altitude);
        State playerGameMode = new StringType(player.getGameMode());

        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_LEVEL_PERCENTAGE, playerLevelPercentage);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_TOTAL_EXPERIENCE, playerTotalExperience);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_LEVEL, playerLevel);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_HEALTH, playerHealth);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_WALK_SPEED, playerWalkSpeed);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_LOCATION, playerLocation);
        updateState(MinecraftBindingConstants.CHANNEL_PLAYER_GAME_MODE, playerGameMode);
    }

    private String getPlayerName() {
        return config.getName();
    }

    private synchronized MinecraftServerHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.");
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized MinecraftServerHandler getBridgeHandler(Bridge bridge) {
        MinecraftServerHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof MinecraftServerHandler) {
            bridgeHandler = (MinecraftServerHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
            bridgeHandler = null;
        }
        return bridgeHandler;
    }

    @Override
    public void updateState(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateState(channelUID, state);
    }

    /**
     * Send a player command to server.
     *
     * @param type the type of command to send
     * @param playerName the name of the player to target
     * @param value the related to command
     */
    private void sendPlayerCommand(String type, String playerName, String value) {
        PlayerCommandData playerCommand = new PlayerCommandData(type, playerName, value);
        JsonElement serializedCommand = gson.toJsonTree(playerCommand);
        logger.debug("Command: {}", serializedCommand);
        bridgeHandler.sendMessage(new OHMessage(OHMessage.MESSAGE_TYPE_PLAYER_COMMANDS, serializedCommand));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case MinecraftBindingConstants.CHANNEL_PLAYER_HEALTH:
                sendPlayerCommand(PlayerCommandData.COMMAND_PLAYER_HEALTH, getPlayerName(), command.toString());
                break;
            case MinecraftBindingConstants.CHANNEL_PLAYER_LEVEL:
                sendPlayerCommand(PlayerCommandData.COMMAND_PLAYER_LEVEL, getPlayerName(), command.toString());
                break;
            case MinecraftBindingConstants.CHANNEL_PLAYER_WALK_SPEED:
                sendPlayerCommand(PlayerCommandData.COMMAND_PLAYER_WALK_SPEED, getPlayerName(), command.toString());
                break;
            case MinecraftBindingConstants.CHANNEL_PLAYER_GAME_MODE:
                sendPlayerCommand(PlayerCommandData.COMMAND_PLAYER_GAME_MODE, getPlayerName(), command.toString());
                break;
            case MinecraftBindingConstants.CHANNEL_PLAYER_LOCATION:
                sendPlayerCommand(PlayerCommandData.COMMAND_PLAYER_LOCATION, getPlayerName(), command.toString());
                break;
            default:
                break;
        }
    }
}
