package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.Random;

/**
 * Event handlers for faction reputation changes and fragment drops.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class FactionEvents {
    
    private static final Random random = new Random();
    
    /**
     * Award reputation and research points for completing missions
     * (Called from mission completion system)
     */
    public static void onMissionComplete(Player player, String missionId, int difficulty) {
        // Award research points based on mission difficulty
        ResearchData research = ResearchData.get(player);
        int points = 10 + (difficulty * 5);
        research.addPoints(points);
        
        // Award faction reputation based on mission type
        ReputationData reputation = ReputationData.get(player);
        ReputationData.Faction helpedFaction = null;
        
        // Parse faction from mission ID (e.g., "remnant_patrol", "salvager_trade")
        if (missionId.contains("remnant")) {
            reputation.addReputation(ReputationData.Faction.REMNANTS, 5 + difficulty);
            helpedFaction = ReputationData.Faction.REMNANTS;
        } else if (missionId.contains("salvager")) {
            reputation.addReputation(ReputationData.Faction.SALVAGERS, 5 + difficulty);
            helpedFaction = ReputationData.Faction.SALVAGERS;
        } else if (missionId.contains("mutant")) {
            reputation.addReputation(ReputationData.Faction.MUTANTS, 5 + difficulty);
            helpedFaction = ReputationData.Faction.MUTANTS;
        }
        
        // Diplomatic consequences: helping one faction affects relations with their enemies
        if (helpedFaction != null && player instanceof ServerPlayer serverPlayer) {
            applyDiplomaticConsequences(serverPlayer, helpedFaction, 5 + difficulty);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ResearchData.saveAndSync(serverPlayer, research);
            FactionProgressionHelper.syncMilestones(serverPlayer);
        }
    }
    
    /**
     * Apply diplomatic consequences when player helps a faction.
     * Enemies of the helped faction lose relation with player.
     */
    private static void applyDiplomaticConsequences(ServerPlayer player, ReputationData.Faction helpedFaction, int amount) {
        FactionDiplomacy diplomacy = player.getData(com.knoxhack.echoashfallprotocol.registry.ModAttachments.FACTION_DIPLOMACY.get());
        ReputationData reputation = ReputationData.get(player);
        
        for (ReputationData.Faction other : ReputationData.Faction.values()) {
            if (other == helpedFaction) continue;
            
            FactionDiplomacy.FactionPair pair = FactionDiplomacy.FactionPair.fromFactions(helpedFaction, other);
            if (pair == null) continue;
            
            int currentRelation = diplomacy.getRelation(pair);
            
            // If factions are hostile (negative relation), helping one hurts reputation with the other
            if (currentRelation < 0) {
                // Reduce reputation with the enemy faction
                int reputationLoss = Math.max(1, amount / 3); // 1/3 of the gain is lost with enemies
                reputation.addReputation(other, -reputationLoss);
                
                // Also worsen diplomatic relations between the factions
                diplomacy.modifyRelation(pair, -1);
                
                // Notify player if significant
                if (reputationLoss >= 3) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A78[ECHO-7]\u00A7r " + other.getDisplayName() + " disapproves of your aid to " + 
                        helpedFaction.getDisplayName() + "."
                    ));
                }
            }
        }
        
        // Save updated data
        FactionDiplomacy.saveAndSync(player, diplomacy);
        ReputationData.saveAndSync(player, reputation);
    }
    
    /**
     * Award research points for POI discovery
     */
    public static void onPOIDiscovered(Player player, String poiId) {
        ExplorationSiteRegistry.SiteProfile profile = ExplorationSiteRegistry.getOrFallback(poiId);
        String normalizedId = profile.id();
        ResearchData research = ResearchData.get(player);
        int points = switch (normalizedId) {
            case "drop_pod" -> 5;
            case "train_yard", "survivor_cache", "crash_zone_wasteland" -> 10;
            case "bio_lab", "data_center_ruin", "ruined_cityscape" -> 15;
            case "military_vault" -> 20;
            case "reactor_ruin" -> 25;
            case "industrial_factory", "cryogenic_ruins", "subway_station", "toxic_swamp", "radiation_zone" -> 18;
            default -> Math.max(10, profile.researchPoints() / 2);
        };
        research.addPoints(points);

        ReputationData reputation = ReputationData.get(player);
        ReputationData.Faction discoveredFaction = profile.faction();
        if (discoveredFaction == null && (normalizedId.contains("military") || normalizedId.contains("reactor") || normalizedId.contains("radiation"))) {
            discoveredFaction = ReputationData.Faction.REMNANTS;
            reputation.addReputation(discoveredFaction, 2);
        } else if (discoveredFaction == null && (normalizedId.contains("factory") || normalizedId.contains("workshop") || normalizedId.contains("data_center") || normalizedId.contains("salvager"))) {
            discoveredFaction = ReputationData.Faction.SALVAGERS;
            reputation.addReputation(discoveredFaction, 2);
        } else if (discoveredFaction == null && (normalizedId.contains("bio") || normalizedId.contains("cryo") || normalizedId.contains("subway") || normalizedId.contains("toxic"))) {
            discoveredFaction = ReputationData.Faction.MUTANTS;
            reputation.addReputation(discoveredFaction, 2);
        } else if (discoveredFaction != null) {
            reputation.addReputation(discoveredFaction, 2);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            FactionQuestProgression.progress(serverPlayer, FactionQuest.ObjectiveType.POI_DISCOVERY, normalizedId, discoveredFaction, 1);
            if (discoveredFaction != null) {
                FactionQuestProgression.progress(serverPlayer, FactionQuest.ObjectiveType.RECON, "faction_hub", discoveredFaction, 1);
            }
            ReputationData.saveAndSync(serverPlayer, reputation);
            ResearchData.saveAndSync(serverPlayer, research);
            FactionProgressionHelper.syncMilestones(serverPlayer);
        }
    }
    
    /**
     * Handle entity kills for reputation and fragment drops
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource() == null || event.getSource().getEntity() == null) return;
        
        net.minecraft.world.entity.Entity killer = event.getSource().getEntity();
        if (!(killer instanceof Player player)) return;
        
        // Hostile faction mobs affect reputation when killed
        String entityName = event.getEntity().getType().getDescriptionId();
        ReputationData reputation = ReputationData.get(player);
        
        // Example: Killing feral humans (mutant faction) reduces mutant reputation
        if (entityName.contains("feral_human") || entityName.contains("mutant")) {
            reputation.addReputation(ReputationData.Faction.MUTANTS, -2);
        }
        
        // Rare fragment drops from faction-affiliated mobs
        if (entityName.contains("military") || entityName.contains("soldier")) {
            // Small chance to drop weapon/armor fragments
            if (random.nextFloat() < 0.05f) {
                ItemStack fragment = new ItemStack(
                    random.nextBoolean() ? ModItems.SCHEMATIC_FRAGMENT_WEAPONS.get() : ModItems.SCHEMATIC_FRAGMENT_ARMOR.get()
                );
                // Drop item at entity location
                var entity = event.getEntity();
                var itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ(), fragment);
                entity.level().addFreshEntity(itemEntity);
            }
            // Killing military units reduces remnant reputation
            reputation.addReputation(ReputationData.Faction.REMNANTS, -1);
        }
        
        if (entityName.contains("scavenger") || entityName.contains("bandit")) {
            // Small chance to drop machine/medical fragments
            if (random.nextFloat() < 0.05f) {
                ItemStack fragment = new ItemStack(
                    random.nextBoolean() ? ModItems.SCHEMATIC_FRAGMENT_MACHINES.get() : ModItems.SCHEMATIC_FRAGMENT_MEDICAL.get()
                );
                // Drop item at entity location
                var entity = event.getEntity();
                var itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ(), fragment);
                entity.level().addFreshEntity(itemEntity);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            FactionQuestProgression.progress(serverPlayer, FactionQuest.ObjectiveType.KILL, entityName, identifyFaction(entityName), 1);
            ReputationData.saveAndSync(serverPlayer, reputation);
            FactionProgressionHelper.syncMilestones(serverPlayer);
        }
    }

    private static ReputationData.Faction identifyFaction(String entityName) {
        if (entityName.contains("remnant") || entityName.contains("soldier") || entityName.contains("military")) {
            return ReputationData.Faction.REMNANTS;
        }
        if (entityName.contains("salvager") || entityName.contains("scavenger") || entityName.contains("bandit")) {
            return ReputationData.Faction.SALVAGERS;
        }
        if (entityName.contains("mutant") || entityName.contains("feral") || entityName.contains("ghoul")
            || entityName.contains("zombie") || entityName.contains("toxic")) {
            return ReputationData.Faction.MUTANTS;
        }
        return null;
    }
    
    /**
     * Handle block breaks for research points
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        
        // Research Lab blocks give small research points when used
        if (event.getState().getBlock() instanceof com.knoxhack.echoashfallprotocol.block.ResearchLabBlock) {
            // Only on server side
            if (!player.level().isClientSide()) {
                // Small passive gain: 1 point per 10 minutes of active use
                // (This would need a timer system for proper implementation)
                // ResearchData research = ResearchData.get(player);
            }
        }
    }
    
    /**
     * Copy faction data when player clones (death/respawn)
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Reputation and research data are marked with copyOnDeath() in ModAttachments
        // This event is handled automatically by NeoForge attachment system
    }
}
