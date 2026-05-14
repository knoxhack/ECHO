package com.knoxhack.echoplayercore.event;

import com.knoxhack.echoplayercore.data.WarpLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerWarpTeleportEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final WarpLocation warp;

    public PlayerWarpTeleportEvent(ServerPlayer player, WarpLocation warp) {
        this.player = player;
        this.warp = warp;
    }

    public ServerPlayer player() {
        return player;
    }

    public WarpLocation warp() {
        return warp;
    }
}
