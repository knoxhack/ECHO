package com.knoxhack.echoashfallprotocol.endgame;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Storm survival is now centralized in PostNexusEventHandler so the DESTROY path
 * cannot double-count the same storm from two event subscribers.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class StormSurvivalTracker {
    private StormSurvivalTracker() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Intentionally no-op.
    }
}
