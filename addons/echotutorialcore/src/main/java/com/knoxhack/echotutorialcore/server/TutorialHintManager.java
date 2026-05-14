package com.knoxhack.echotutorialcore.server;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import com.knoxhack.echotutorialcore.api.hint.TutorialHint;
import com.knoxhack.echotutorialcore.config.TutorialConfig;
import com.knoxhack.echotutorialcore.data.TutorialCoreRegistries;
import com.knoxhack.echotutorialcore.data.TutorialPlayerData;
import com.knoxhack.echotutorialcore.network.TutorialNetworking;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TutorialHintManager {
    private TutorialHintManager() {}

    public static void showHint(Player player, Identifier hintId) {
        if (player == null || hintId == null) return;
        TutorialHint hint = TutorialCoreRegistries.getHint(hintId).orElse(null);
        if (hint == null) return;
        showHint(player, hint);
    }

    public static void showHint(Player player, TutorialHint hint) {
        if (player == null || hint == null) return;
        TutorialGuideMode mode = TutorialProgressManager.getGuideMode(player);
        if (!hint.allowedInMode(mode)) return;
        if (mode == TutorialGuideMode.OFF) return;

        TutorialPlayerData data = TutorialPlayerData.get(player);
        if (data.isHintDismissed(hint.id()) && hint.dismissible()) return;

        long now = player.level().getGameTime();
        long lastTime = data.getLastHintTime(hint.id());
        if (now - lastTime < hint.cooldownTicks()) return;

        int maxPerMinute = TutorialConfig.MAX_HINTS_PER_MINUTE.get();
        if (maxPerMinute >= 0 && data.popupCountThisSession() >= maxPerMinute * 20) {
            return;
        }

        int maxPopups = TutorialConfig.MAX_POPUPS_PER_SESSION.get();
        if (maxPopups >= 0 && data.popupCountThisSession() >= maxPopups) {
            return;
        }

        data.recordHintTime(hint.id(), now);
        data.incrementPopupCount();
        TutorialPlayerData.save(player, data);

        if (player instanceof ServerPlayer sp) {
            TutorialNetworking.sendShowHint(sp, hint);
        } else {
            player.sendSystemMessage(Component.literal("[ECHO-7] " + hint.title() + ": " + hint.message()));
        }
    }

    public static void dismissHint(Player player, Identifier hintId) {
        if (player == null || hintId == null) return;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        data.dismissHint(hintId);
        TutorialPlayerData.save(player, data);
    }

    public static void evaluateHints(Player player) {
        if (player == null || !(player instanceof ServerPlayer)) return;
        TutorialGuideMode mode = TutorialProgressManager.getGuideMode(player);
        if (mode == TutorialGuideMode.OFF) return;

        for (TutorialHint hint : TutorialCoreRegistries.allHints()) {
            if (!hint.allowedInMode(mode)) continue;
            TutorialPlayerData data = TutorialPlayerData.get(player);
            if (data.isHintDismissed(hint.id())) continue;
            long now = player.level().getGameTime();
            if (now - data.getLastHintTime(hint.id()) < hint.cooldownTicks()) continue;
            // Contextual evaluation is intentionally lightweight.
            // Full condition evaluation (inventory checks, region checks, etc.)
            // can be expanded in integration-specific evaluators.
        }
    }

    public static void sendChatFallback(Player player, String title, String message) {
        if (player == null) return;
        String prefix = title != null && !title.isBlank() ? "[ECHO-7] " + title + ": " : "[ECHO-7] ";
        player.sendSystemMessage(Component.literal(prefix + message));
    }
}
