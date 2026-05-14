package com.knoxhack.echoplayercore.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerHomeDeletedEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final String homeName;

    public PlayerHomeDeletedEvent(ServerPlayer player, String homeName) {
        this.player = player;
        this.homeName = homeName;
    }

    public ServerPlayer player() {
        return player;
    }

    public String homeName() {
        return homeName;
    }
}
