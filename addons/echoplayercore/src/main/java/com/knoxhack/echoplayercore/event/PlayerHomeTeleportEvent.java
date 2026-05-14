package com.knoxhack.echoplayercore.event;

import com.knoxhack.echoplayercore.data.HomeLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerHomeTeleportEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final HomeLocation home;

    public PlayerHomeTeleportEvent(ServerPlayer player, HomeLocation home) {
        this.player = player;
        this.home = home;
    }

    public ServerPlayer player() {
        return player;
    }

    public HomeLocation home() {
        return home;
    }
}
