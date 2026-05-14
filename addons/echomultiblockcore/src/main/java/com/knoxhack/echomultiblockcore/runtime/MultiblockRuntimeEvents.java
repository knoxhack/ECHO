package com.knoxhack.echomultiblockcore.runtime;

import com.knoxhack.echomultiblockcore.network.MultiblockDefinitionSync;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class MultiblockRuntimeEvents {
    private MultiblockRuntimeEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        // Controllers tick through block entity tickers; this hook is reserved for future global sweeps.
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MultiblockDefinitionSync.sendTo(player);
        }
    }
}
