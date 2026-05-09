package com.knoxhack.echoterminal.mission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class VanillaJourneyProgression {
    private VanillaJourneyProgression() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(VanillaJourneyProgression::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(VanillaJourneyProgression::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(VanillaJourneyProgression::onPlayerRespawned);
        NeoForge.EVENT_BUS.addListener(VanillaJourneyProgression::onPlayerChangedDimension);
    }

    private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (event == null || !VanillaJourneyProvider.INSTANCE.tracksAdvancement(event.getAdvancement().id())) {
            return;
        }
        sync(event.getEntity());
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    private static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.getEntity());
    }

    static boolean sync(Player player) {
        return player instanceof ServerPlayer serverPlayer
                && VanillaJourneyProvider.INSTANCE.refreshIfChanged(serverPlayer);
    }
}
