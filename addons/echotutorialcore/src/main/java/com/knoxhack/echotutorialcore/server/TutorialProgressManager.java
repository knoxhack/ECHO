package com.knoxhack.echotutorialcore.server;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import com.knoxhack.echotutorialcore.config.TutorialConfig;
import com.knoxhack.echotutorialcore.data.TutorialPlayerData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TutorialProgressManager {
    private TutorialProgressManager() {}

    public static void markProgress(Player player, Identifier progressId) {
        if (player == null || progressId == null) return;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        if (data.hasProgress(progressId)) return;
        data.markProgress(progressId);
        TutorialPlayerData.save(player, data);
        EchoTutorialCore.LOGGER.debug("Tutorial progress marked for {}: {}", player.getScoreboardName(), progressId);
    }

    public static boolean hasProgress(Player player, Identifier progressId) {
        if (player == null || progressId == null) return false;
        return TutorialPlayerData.get(player).hasProgress(progressId);
    }

    public static TutorialGuideMode getGuideMode(Player player) {
        if (player == null) return TutorialGuideMode.NORMAL;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        TutorialGuideMode forced = TutorialConfig.FORCE_GUIDE_MODE.get();
        if (forced != null && forced != TutorialGuideMode.OFF) {
            return forced;
        }
        return data.guideMode();
    }

    public static void setGuideMode(Player player, TutorialGuideMode mode) {
        if (player == null || mode == null) return;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        data.setGuideMode(mode);
        TutorialPlayerData.save(player, data);
        if (player instanceof ServerPlayer sp) {
            TutorialPlayerData.saveAndSync(sp, data);
        }
    }

    public static void resetPlayer(Player player) {
        if (player == null) return;
        TutorialPlayerData data = TutorialPlayerData.get(player);
        data.resetAll();
        TutorialPlayerData.save(player, data);
        if (player instanceof ServerPlayer sp) {
            TutorialPlayerData.saveAndSync(sp, data);
        }
    }
}
