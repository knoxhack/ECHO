package com.knoxhack.echoplayercore.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerTpaTeleportEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final ServerPlayer target;
    private final boolean here;

    public PlayerTpaTeleportEvent(ServerPlayer player, ServerPlayer target, boolean here) {
        this.player = player;
        this.target = target;
        this.here = here;
    }

    public ServerPlayer player() {
        return player;
    }

    public ServerPlayer target() {
        return target;
    }

    public boolean here() {
        return here;
    }
}
