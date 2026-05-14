package com.knoxhack.echoplayercore.event;

import com.knoxhack.echoplayercore.data.TeleportLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerDeathLocationStoredEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final TeleportLocation location;

    public PlayerDeathLocationStoredEvent(ServerPlayer player, TeleportLocation location) {
        this.player = player;
        this.location = location;
    }

    public ServerPlayer player() {
        return player;
    }

    public TeleportLocation location() {
        return location;
    }
}
