package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.POIRegistry;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Event handlers for exploration, faction, and research progression.
 * Handles reputation gains, research point awards, and schematic fragment drops.
 */
public class ExpansionEvents {
    
    public ExpansionEvents() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    /**
     * Award research points and reputation for killing hostile entities
     */
    @SubscribeEvent
    public void onEntityKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        
        var entity = event.getEntity();
        String entityName = entity.getType().getDescriptionId();
        
        // Award research points for killing certain entities
        int researchPoints = 0;
        int repGain = 0;
        ReputationData.Faction faction = null;
        
        // Check for faction-aligned kills
        if (entityName.contains("rad_zombie") || entityName.contains("feral_human") || entityName.contains("ash_wraith")) {
            // Killing threats helps Mutants (reduces hostile wildlife)
            researchPoints = 2;
            repGain = 1;
            faction = ReputationData.Faction.MUTANTS;
        } else if (entityName.contains("city_stalker") || entityName.contains("scavenger_bandit")) {
            // Killing bandits helps Salvagers (removes competition)
            researchPoints = 3;
            repGain = 1;
            faction = ReputationData.Faction.SALVAGERS;
        } else if (entityName.contains("irradiated_wolf") || entityName.contains("rust_walker")) {
            // Killing mutants helps Remnants (cleansing)
            researchPoints = 2;
            repGain = 1;
            faction = ReputationData.Faction.REMNANTS;
        }
        
        if (researchPoints > 0) {
            ResearchData research = ResearchData.get(player);
            research.addPoints(researchPoints);
            ResearchData.saveAndSync(player, research);
        }
        
        if (faction != null && repGain > 0) {
            ReputationData.get(player).addReputation(faction, repGain);
        }
        
        // Chance to drop schematic fragments from certain enemies
        if (entityName.contains("city_stalker") || entityName.contains("scavenger_bandit")) {
            if (player.getRandom().nextFloat() < 0.05f) { // 5% chance
                // Would drop schematic fragment item
                // Implementation would use ItemEntity spawn
            }
        }
    }
    
    /**
     * Award research points for discovering new structures/biomes
     */
    public void onPOIDiscovery(Player player, String poiId) {
        if (player instanceof ServerPlayer serverPlayer) {
            // Award research points
            ResearchData research = ResearchData.get(serverPlayer);
            research.addPoints(10);
            ResearchData.saveAndSync(serverPlayer, research);
            
            // Award reputation based on POI faction
            var poi = POIRegistry.get(poiId);
            if (poi != null && poi.getAssociatedFaction() != null) {
                ReputationData.get(serverPlayer).addReputation(poi.getAssociatedFaction(), 5);
            }
            
            // Send discovery message
            String displayName = poi != null ? poi.getName() : com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry.getOrFallback(poiId).displayName();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Discovered: " + displayName).withStyle(net.minecraft.ChatFormatting.GREEN));
        }
    }
    
    /**
     * Award reputation and research for completing faction quests
     */
    public void onQuestComplete(ServerPlayer player, com.knoxhack.echoashfallprotocol.faction.FactionQuest quest) {
        // Award reputation
        ReputationData.get(player).addReputation(quest.getFaction(), quest.getDifficulty().getReputationReward());
        
        // Award research points
        ResearchData research = ResearchData.get(player);
        research.addPoints(quest.getDifficulty().getResearchPoints());
        ResearchData.saveAndSync(player, research);
        
        // Award schematic fragment on hard/extreme quests
        if (quest.getDifficulty() == com.knoxhack.echoashfallprotocol.faction.FactionQuest.Difficulty.HARD ||
            quest.getDifficulty() == com.knoxhack.echoashfallprotocol.faction.FactionQuest.Difficulty.EXTREME) {
            // Would give schematic fragment reward
        }
    }
    
    /**
     * Award research for activating relay stations
     */
    public void onStationActivate(ServerPlayer player) {
        ResearchData research = ResearchData.get(player);
        research.addPoints(15);
        ResearchData.saveAndSync(player, research);
        
        // Also grant reputation with Salvagers (they maintain the network)
        ReputationData.get(player).addReputation(ReputationData.Faction.SALVAGERS, 3);
    }
    
    /**
     * Check if player is in Cryogenic Ruins biome for cold damage
     */
    public void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer)) return;
        
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        
        // Check if in Cryogenic Ruins biome
        if (com.knoxhack.echoashfallprotocol.world.CryogenicRuinsBiome.isInBiome(level, pos)) {
            var coldData = com.knoxhack.echoashfallprotocol.survival.ColdData.get(player);
            coldData.update(player);
        }
    }
}
