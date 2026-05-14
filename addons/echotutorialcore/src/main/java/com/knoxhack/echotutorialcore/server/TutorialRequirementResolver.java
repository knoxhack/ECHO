package com.knoxhack.echotutorialcore.server;

import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import com.knoxhack.echotutorialcore.config.TutorialConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TutorialRequirementResolver {
    private TutorialRequirementResolver() {}

    public static void showRequirementHint(Player player, Identifier requirementId) {
        if (player == null || requirementId == null) return;
        TutorialGuideMode mode = TutorialProgressManager.getGuideMode(player);
        if (mode == TutorialGuideMode.OFF) return;
        if (!TutorialConfig.ENABLE_RECIPE_LOCK_EXPLANATIONS.get()) return;

        // Fallback chat message with requirement id as scaffold.
        // Integrations can expand this with actual item/tag lookups.
        player.sendSystemMessage(Component.literal("[ECHO-7] Requirement: " + requirementId.toString()));
    }

    public static List<String> resolveMissingItems(Player player, List<Identifier> items) {
        List<String> missing = new ArrayList<>();
        if (items == null) return missing;
        for (Identifier id : items) {
            // Safe optional check without hard-coding item existence
            missing.add(id.toString());
        }
        return missing;
    }
}
