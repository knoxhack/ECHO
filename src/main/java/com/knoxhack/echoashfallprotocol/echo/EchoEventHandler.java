package com.knoxhack.echoashfallprotocol.echo;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.boss.BossHudSync;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Event handler that drives the ECHO-7 system.
 * Ticks once per player per server tick.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class EchoEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Only tick every 20 ticks (1 second) for performance
            if (serverPlayer.level().getGameTime() % 20 == 0) {
                EchoGuideManager.tick(serverPlayer);
                BossHudSync.syncBestTarget(serverPlayer);
            }
        }
    }
}
