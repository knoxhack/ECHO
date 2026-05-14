package com.knoxhack.echotutorialcore.server;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import com.knoxhack.echotutorialcore.config.TutorialConfig;
import com.knoxhack.echotutorialcore.data.TutorialPlayerData;
import com.knoxhack.echotutorialcore.network.TutorialNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = EchoTutorialCore.MODID)
public final class TutorialEventHandler {
    private TutorialEventHandler() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TutorialProgressManager.markProgress(player, Identifier.fromNamespaceAndPath(EchoTutorialCore.MODID, "entered_world"));
            TutorialNetworking.sendSyncProgress(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TutorialPlayerData data = TutorialPlayerData.get(player);
            data.resetPopupCount();
            TutorialPlayerData.save(player, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DamageSource source = event.getSource();
        String causeKey = source == null ? "unknown" : source.getMsgId();
        long time = player.level().getGameTime();

        TutorialPlayerData data = TutorialPlayerData.get(player);
        data.recordDeath(causeKey, time);
        TutorialPlayerData.save(player, data);

        int threshold = TutorialConfig.REPEATED_DEATH_THRESHOLD.get();
        if (data.repeatedDeathCount() >= threshold) {
            TutorialMistakeDetector.reportRepeatedFailure(player);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Scaffold: mark generic first machine placed progress.
        // Specific machine tracking can be added by machine addons calling TutorialCoreApi.
        TutorialProgressManager.markProgress(player, Identifier.fromNamespaceAndPath(EchoTutorialCore.MODID, "placed_first_machine"));
    }
}
