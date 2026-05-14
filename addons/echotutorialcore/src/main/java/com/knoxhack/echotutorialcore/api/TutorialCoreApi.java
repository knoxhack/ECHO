package com.knoxhack.echotutorialcore.api;

import com.knoxhack.echotutorialcore.api.card.TutorialCard;
import com.knoxhack.echotutorialcore.api.hint.TutorialHint;
import com.knoxhack.echotutorialcore.api.trigger.TutorialFlow;
import com.knoxhack.echotutorialcore.data.TutorialCoreRegistries;
import com.knoxhack.echotutorialcore.network.TutorialNetworking;
import com.knoxhack.echotutorialcore.server.TutorialCardManager;
import com.knoxhack.echotutorialcore.server.TutorialFlowManager;
import com.knoxhack.echotutorialcore.server.TutorialHintManager;
import com.knoxhack.echotutorialcore.server.TutorialMistakeDetector;
import com.knoxhack.echotutorialcore.server.TutorialProgressManager;
import com.knoxhack.echotutorialcore.server.TutorialRequirementResolver;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TutorialCoreApi {
    private TutorialCoreApi() {}

    // Registration
    public static void registerCard(TutorialCard card) {
        TutorialCoreRegistries.registerCard(card);
    }

    public static void registerHint(TutorialHint hint) {
        TutorialCoreRegistries.registerHint(hint);
    }

    public static void registerFlow(TutorialFlow flow) {
        TutorialCoreRegistries.registerFlow(flow);
    }

    // Card operations
    public static void unlockCard(ServerPlayer player, Identifier cardId) {
        TutorialCardManager.unlockCard(player, cardId);
        TutorialNetworking.sendUnlockCard(player, cardId);
    }

    public static void showCard(ServerPlayer player, Identifier cardId) {
        TutorialNetworking.sendShowCard(player, cardId);
    }

    // Hint operations
    public static void showHint(ServerPlayer player, Identifier hintId) {
        TutorialHintManager.showHint(player, hintId);
    }

    public static void showHint(ServerPlayer player, TutorialHint hint) {
        TutorialHintManager.showHint(player, hint);
    }

    // Progress
    public static void markProgress(ServerPlayer player, Identifier progressId) {
        TutorialProgressManager.markProgress(player, progressId);
    }

    public static boolean hasProgress(ServerPlayer player, Identifier progressId) {
        return TutorialProgressManager.hasProgress(player, progressId);
    }

    // Guide mode
    public static void setGuideMode(ServerPlayer player, TutorialGuideMode mode) {
        TutorialProgressManager.setGuideMode(player, mode);
    }

    public static TutorialGuideMode getGuideMode(ServerPlayer player) {
        return TutorialProgressManager.getGuideMode(player);
    }

    // Mistake reports
    public static void reportMistake(ServerPlayer player, Identifier mistakeId) {
        TutorialMistakeDetector.reportNoPower(player); // generic fallback
    }

    public static void reportMissingRequirement(ServerPlayer player, Identifier requirementId) {
        TutorialRequirementResolver.showRequirementHint(player, requirementId);
    }

    public static List<String> getRecommendedNextSteps(ServerPlayer player) {
        return List.of(
                "Build a Water Purifier",
                "Build a Micro Generator",
                "Use your scanner to locate a Signal Lead",
                "Open HoloMap to review the route",
                "Claim your reward",
                "Research your schematic fragments",
                "Prepare filters before entering toxic air",
                "Turn in Guardian Datacore"
        );
    }

    // Convenience reports
    public static void reportNoPower(Player player, BlockPos pos) {
        TutorialMistakeDetector.reportNoPower(player);
    }

    public static void reportMissingFilter(Player player) {
        TutorialMistakeDetector.reportMissingFilter(player);
    }

    public static void reportRecipeLocked(Player player) {
        TutorialMistakeDetector.reportRecipeLocked(player);
    }

    public static void reportHazardUnprepared(Player player) {
        TutorialMistakeDetector.reportHazardUnprepared(player);
    }

    public static void reportRewardAvailable(Player player) {
        TutorialMistakeDetector.reportUnclaimedReward(player);
    }

    public static void reportSignalDetected(Player player) {
        TutorialProgressManager.markProgress(player, Identifier.fromNamespaceAndPath("echotutorialcore", "detected_first_signal"));
    }

    public static void reportGuardianLocated(Player player) {
        TutorialProgressManager.markProgress(player, Identifier.fromNamespaceAndPath("echotutorialcore", "located_first_guardian"));
    }

    public static void reportFactionContact(Player player) {
        TutorialProgressManager.markProgress(player, Identifier.fromNamespaceAndPath("echotutorialcore", "faction_contact"));
    }
}
