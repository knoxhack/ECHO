package com.knoxhack.echoworldcore.event;

import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.Config;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EchoWorldCore.MODID)
public final class WorldCoreEvents {
    private WorldCoreEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.tickCount % Config.playerScanInterval() == 0) {
            WorldRegionService.INSTANCE.tickPlayer(player);
        }
    }
}
