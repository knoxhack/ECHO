package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.echo.EchoIntel;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Set;

/**
 * Shared quest lifecycle and objective progress logic.
 */
public final class FactionQuestProgression {
    private FactionQuestProgression() {}

    public static boolean acceptQuest(ServerPlayer player, FactionQuest quest) {
        if (quest == null || !quest.canAccept(player)) {
            return false;
        }

        FactionQuestData data = FactionQuestData.get(player);
        if (!data.acceptQuest(quest, player.level().getGameTime())) {
            return false;
        }

        FactionQuestData.saveAndSync(player, data);
        player.sendSystemMessage(Component.literal("\u00A76[" + quest.getFaction().getDisplayName() + "]\u00A7r Quest accepted: " + quest.getTitle()));
        for (FactionQuest.Objective objective : quest.getObjectiveModels()) {
            player.sendSystemMessage(Component.literal("\u00A77 - " + objective.displayText()));
        }
        return true;
    }

    public static void completeQuest(ServerPlayer player, FactionQuest quest) {
        if (quest == null) return;

        FactionQuestData data = FactionQuestData.get(player);
        if (data.isCompleted(quest.getId())) return;

        data.completeQuest(quest, player.level().getGameTime());
        FactionQuestData.saveAndSync(player, data);

        ReputationData reputation = ReputationData.get(player);
        reputation.addReputation(quest.getFaction(), quest.getRewardReputation());
        ReputationData.saveAndSync(player, reputation);

        ResearchData research = ResearchData.get(player);
        research.addPoints(quest.getRewardResearch());
        ResearchData.saveAndSync(player, research);

        grantRewardItems(player, quest);
        FactionWorldManager.onPlayerQuestComplete(player, quest.getFaction(), quest.getRewardReputation());
        FactionProgressionHelper.syncMilestones(player, quest.getFaction());
        recordFactionTaskCompletion(player, quest.getFaction());

        EchoIntel intel = EchoIntel.get(player);
        intel.addReconIntel(
            "Faction Contract Complete",
            quest.getFaction().getDisplayName() + " contract resolved: " + quest.getTitle(),
            quest.getFaction(),
            EchoIntel.IntelPriority.MEDIUM
        );
        EchoIntel.saveAndSync(player, intel);
        player.sendSystemMessage(Component.literal("\u00A76[ECHO-7]\u00A7r " + factionBenefitLine(quest.getFaction())), true);

        player.sendSystemMessage(Component.literal("\u00A7a[Quest Complete]\u00A7r " + quest.getTitle()));
        player.sendSystemMessage(Component.literal("\u00A7b+" + quest.getRewardReputation() + " " + quest.getFaction().getDisplayName() + " reputation"));
        player.sendSystemMessage(Component.literal("\u00A7e+" + quest.getRewardResearch() + " Research Points"));
    }

    private static void recordFactionTaskCompletion(ServerPlayer player, ReputationData.Faction faction) {
        QuestData questData = QuestData.get(player);
        questData.visitLocation("special", "faction:first_task_complete");
        questData.visitLocation("special", switch (faction) {
            case REMNANTS -> "faction:remnants_task_complete";
            case SALVAGERS -> "faction:salvagers_task_complete";
            case MUTANTS -> "faction:mutants_task_complete";
        });
        QuestData.saveAndSync(player, questData);
    }

    public static boolean progress(ServerPlayer player, FactionQuest.ObjectiveType type, String targetId,
                                   ReputationData.Faction factionHint, int amount) {
        FactionQuestData data = FactionQuestData.get(player);
        boolean changed = false;

        for (Map.Entry<ReputationData.Faction, String> active : data.getActiveQuestIds().entrySet()) {
            FactionQuest quest = FactionQuestRegistry.get(active.getValue());
            if (quest == null) continue;

            for (int i = 0; i < quest.getObjectiveModels().size(); i++) {
                FactionQuest.Objective objective = quest.getObjectiveModels().get(i);
                if (!matches(objective, type, targetId, factionHint)) continue;
                int before = data.getObjectiveProgress(quest.getId(), i);
                int after = data.addObjectiveProgress(quest.getId(), i, amount, objective.requiredCount());
                if (after != before) {
                    changed = true;
                    player.sendSystemMessage(Component.literal(
                        "\u00A76[" + quest.getFaction().getDisplayName() + "]\u00A7r " + objective.displayText() +
                            " \u00A77(" + after + "/" + objective.requiredCount() + ")"
                    ), true);
                }
            }

            if (data.isQuestComplete(quest)) {
                completeQuest(player, quest);
                data = FactionQuestData.get(player);
            }
        }

        if (changed) {
            FactionQuestData.saveAndSync(player, data);
        }
        return changed;
    }

    public static boolean tryDeliverHeldItem(ServerPlayer player, ReputationData.Faction faction, InteractionHand hand) {
        FactionQuestData data = FactionQuestData.get(player);
        FactionQuest quest = data.getActiveQuest(faction);
        if (quest == null) return false;

        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return false;

        boolean delivered = false;
        for (int i = 0; i < quest.getObjectiveModels().size(); i++) {
            FactionQuest.Objective objective = quest.getObjectiveModels().get(i);
            if (objective.type() != FactionQuest.ObjectiveType.ITEM_DELIVERY) continue;
            int current = data.getObjectiveProgress(quest.getId(), i);
            int remaining = objective.requiredCount() - current;
            if (remaining <= 0 || !heldMatches(held, objective.targetId())) continue;

            int transfer = Math.min(remaining, held.getCount());
            held.shrink(transfer);
            data.addObjectiveProgress(quest.getId(), i, transfer, objective.requiredCount());
            delivered = true;
            player.sendSystemMessage(Component.literal(
                "\u00A76[" + faction.getDisplayName() + "]\u00A7r Delivered " + transfer + "x " + held.getHoverName().getString() +
                    " \u00A77(" + data.getObjectiveProgress(quest.getId(), i) + "/" + objective.requiredCount() + ")"
            ));
        }

        if (delivered) {
            if (data.isQuestComplete(quest)) {
                completeQuest(player, quest);
            } else {
                FactionQuestData.saveAndSync(player, data);
            }
        }
        return delivered;
    }

    public static String describeProgress(ServerPlayer player, FactionQuest quest) {
        return describeProgress((net.minecraft.world.entity.player.Player) player, quest);
    }

    public static String describeProgress(net.minecraft.world.entity.player.Player player, FactionQuest quest) {
        FactionQuestData data = FactionQuestData.get(player);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < quest.getObjectiveModels().size(); i++) {
            if (i > 0) builder.append(" | ");
            FactionQuest.Objective objective = quest.getObjectiveModels().get(i);
            builder.append(data.getObjectiveProgress(quest.getId(), i))
                .append("/")
                .append(objective.requiredCount())
                .append(" ")
                .append(objective.displayText());
        }
        return builder.toString();
    }

    private static boolean matches(FactionQuest.Objective objective, FactionQuest.ObjectiveType type,
                                   String targetId, ReputationData.Faction factionHint) {
        if (objective.type() != type) return false;
        if (objective.faction() != null && factionHint != null && objective.faction() != factionHint) return false;
        if (objective.targetId().isEmpty()) return true;
        if (targetId == null || targetId.isEmpty()) return false;
        String normalizedTarget = targetId.toLowerCase();
        String normalizedObjective = objective.targetId().toLowerCase();
        if (normalizedTarget.contains(normalizedObjective) || normalizedObjective.equals(normalizedTarget)) {
            return true;
        }

        Set<String> targetAliases = ExplorationSiteRegistry.aliasesFor(normalizedTarget);
        Set<String> objectiveAliases = ExplorationSiteRegistry.aliasesFor(normalizedObjective);
        for (String alias : objectiveAliases) {
            if (targetAliases.contains(alias) || normalizedTarget.contains(alias)) {
                return true;
            }
        }
        for (String alias : targetAliases) {
            if (normalizedObjective.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private static boolean heldMatches(ItemStack stack, String targetId) {
        if (targetId == null || targetId.isEmpty()) return false;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return itemId.equals(targetId) || itemId.endsWith(":" + targetId) || itemId.contains(targetId);
    }

    private static void grantRewardItems(ServerPlayer player, FactionQuest quest) {
        for (String itemId : quest.getRewardItems()) {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId)).ifPresent(item -> give(player, item));
        }
    }

    private static String factionBenefitLine(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> "Remnant trust updated: security reports, medical support, and restoration tech stock can improve.";
            case SALVAGERS -> "Salvager trust updated: trade routes, rare salvage tips, and route supplies can improve.";
            case MUTANTS -> "Mutant Front trust updated: bio-support, adaptation supplies, and hazard intel can improve.";
        };
    }

    private static void give(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        com.knoxhack.echoashfallprotocol.research.PerkEffectHandler.applyLootBonus(player, stack);
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }
}
