package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Random;

/**
 * Handles quest assignment from faction villager quest-givers.
 * Quest-giver villagers offer faction quests based on player reputation.
 * 
 * NOTE: The current NeoForge line significantly changed the villager profession API.
 * This implementation uses a simplified approach compatible with the new API.
 * Full profession detection would require checking job site blocks directly.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class VillagerQuestHandler {
    
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onProfessionBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BlockState state = player.level().getBlockState(event.getPos());
        ReputationData.Faction faction = getFactionForBlock(state.getBlock());
        if (faction == null || event.getHand() != InteractionHand.MAIN_HAND) return;

        recordFactionContact(player, faction);

        if (FactionQuestProgression.tryDeliverHeldItem(player, faction, event.getHand())) {
            event.setCanceled(true);
            return;
        }

        offerFactionQuests(player, faction);
        event.setCanceled(true);
    }

    private static ReputationData.Faction getFactionForBlock(Block block) {
        if (block == ModBlocks.WEAPON_RACK.get() || block == ModBlocks.SUPPLY_CRATE.get()) {
            return ReputationData.Faction.REMNANTS;
        }
        if (block == ModBlocks.TRADE_COUNTER.get() || block == ModBlocks.MAP_TABLE.get()) {
            return ReputationData.Faction.SALVAGERS;
        }
        if (block == ModBlocks.BIO_PROCESSING_STATION.get() || block == ModBlocks.SPORE_GARDEN.get()) {
            return ReputationData.Faction.MUTANTS;
        }
        return null;
    }

    private static void recordFactionContact(ServerPlayer player, ReputationData.Faction faction) {
        QuestData quest = QuestData.get(player);
        String marker = switch (faction) {
            case REMNANTS -> "faction_contact:remnants";
            case SALVAGERS -> "faction_contact:salvagers";
            case MUTANTS -> "faction_contact:mutants";
        };

        if (!quest.hasVisitedLocation("special", marker)) {
            quest.visitLocation("special", "faction_contact:any");
            quest.visitLocation("special", marker);
            QuestData.saveAndSync(player, quest);
        }
    }
    
    /**
     * Maps faction quest-giver types
     */
    public enum QuestGiverType {
        REMNANT_SOLDIER(ReputationData.Faction.REMNANTS, FactionQuest.QuestType.ELIMINATION, "Remnant Drill Sergeant"),
        REMNANT_QUARTERMASTER(ReputationData.Faction.REMNANTS, FactionQuest.QuestType.DELIVERY, "Remnant Quartermaster"),
        SALVAGER_MERCHANT(ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.DELIVERY, "Salvager Merchant"),
        SALVAGER_SCOUT(ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.RECON, "Salvager Scout"),
        MUTANT_HEALER(ReputationData.Faction.MUTANTS, FactionQuest.QuestType.REPAIR, "Mutant Healer"),
        MUTANT_ELDER(ReputationData.Faction.MUTANTS, FactionQuest.QuestType.RETRIEVAL, "Mutant Elder");
        
        private final ReputationData.Faction faction;
        private final FactionQuest.QuestType preferredQuestType;
        private final String title;
        
        QuestGiverType(ReputationData.Faction faction, FactionQuest.QuestType preferredQuestType, String title) {
            this.faction = faction;
            this.preferredQuestType = preferredQuestType;
            this.title = title;
        }
        
        public ReputationData.Faction getFaction() { return faction; }
        public FactionQuest.QuestType getPreferredQuestType() { return preferredQuestType; }
        public String getTitle() { return title; }
        
        public static QuestGiverType forFaction(ReputationData.Faction faction) {
            for (QuestGiverType type : values()) {
                if (type.faction == faction) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * Offer quests to player based on faction reputation.
     * Called when player interacts with a profession block.
     */
    public static void offerFactionQuests(Player player, ReputationData.Faction faction) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        QuestGiverType questGiver = QuestGiverType.forFaction(faction);
        if (questGiver == null) return;
        
        ReputationData rep = ReputationData.get(player);
        int reputation = rep.getReputation(faction);
        FactionQuestData questData = FactionQuestData.get(player);
        FactionQuest activeQuest = questData.getActiveQuest(faction);

        if (activeQuest != null) {
            serverPlayer.sendSystemMessage(Component.literal("\u00A76[" + questGiver.getTitle() + "]\u00A7r Active task: \u00A7f" + activeQuest.getTitle()));
            serverPlayer.sendSystemMessage(Component.literal("\u00A77" + FactionQuestProgression.describeProgress(serverPlayer, activeQuest)));
            if (questData.isQuestComplete(activeQuest)) {
                FactionQuestProgression.completeQuest(serverPlayer, activeQuest);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("\u00A77Return when the objectives are complete."));
            }
            return;
        }
        
        // Get available quests for this faction
        List<FactionQuest> available = FactionQuestRegistry.getAvailableForPlayer(player, faction);
        
        if (available.isEmpty()) {
            // No quests available
            if (reputation < 0) {
                serverPlayer.sendSystemMessage(Component.literal("\u00A7c[Quest Giver]\u00A7r You are not welcome here."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("\u00A7a[Quest Giver]\u00A7r No tasks available at the moment."));
            }
            return;
        }
        
        // Filter by preferred quest type
        List<FactionQuest> preferred = available.stream()
            .filter(q -> q.getType() == questGiver.getPreferredQuestType())
            .toList();
        
        List<FactionQuest> selectionPool = preferred.isEmpty() ? available : preferred;
        FactionQuest quest = selectionPool.get(RANDOM.nextInt(selectionPool.size()));
        
        // Send quest offer
        String title = questGiver.getTitle();
        serverPlayer.sendSystemMessage(Component.literal("\u00A76[" + title + "]\u00A7r Greetings, survivor."));
        serverPlayer.sendSystemMessage(Component.literal("\u00A77Quest Available: \u00A7r\u00A7f" + quest.getTitle()));
        serverPlayer.sendSystemMessage(Component.literal("\u00A77" + quest.getDescription()));
        serverPlayer.sendSystemMessage(Component.literal("\u00A7aReward: \u00A7r" + quest.getDifficulty().getReputationReward() + " reputation"));
        
        FactionQuestProgression.acceptQuest(serverPlayer, quest);
    }
    
    /**
     * Check if a quest can be accepted by the player.
     */
    public static boolean canAcceptQuest(Player player, FactionQuest quest) {
        ReputationData rep = ReputationData.get(player);
        return rep.getReputation(quest.getFaction()) >= quest.getRequiredReputation();
    }
}
