package com.knoxhack.echoplayercore.event;

import com.knoxhack.echoplayercore.data.TeleportLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerSpawnTeleportEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final TeleportLocation target;

    public PlayerSpawnTeleportEvent(ServerPlayer player, TeleportLocation target) {
        this.player = player;
        this.target = target;
    }

    public ServerPlayer player() {
        return player;
    }

    public TeleportLocation target() {
        return target;
    }
}
