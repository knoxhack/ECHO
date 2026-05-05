package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Event handlers that feed Echo Core Ashfall faction state.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class FactionEvents {
    private static final Random RANDOM = new Random();

    public static void onMissionComplete(Player player, String missionId, int difficulty) {
        ResearchData research = ResearchData.get(player);
        research.addPoints(10 + (difficulty * 5));

        Identifier helpedFaction = AshfallFactionMap.fromLegacyKey(missionId);
        if (player instanceof ServerPlayer serverPlayer) {
            int reputation = 5 + difficulty;
            EchoCoreServices.addFactionReputation(serverPlayer, helpedFaction, reputation);
            applyDiplomaticConsequences(serverPlayer, helpedFaction, reputation);
            ResearchData.saveAndSync(serverPlayer, research);
            FactionProgressionHelper.syncMilestones(serverPlayer);
        }
    }

    private static void applyDiplomaticConsequences(ServerPlayer player, Identifier helpedFaction, int amount) {
        FactionDiplomacy diplomacy = player.getData(
                com.knoxhack.echoashfallprotocol.registry.ModAttachments.FACTION_DIPLOMACY.get());
        for (Identifier other : AshfallFactionMap.all()) {
            if (other.equals(helpedFaction)) {
                continue;
            }
            FactionDiplomacy.FactionPair pair = FactionDiplomacy.FactionPair.fromFactions(helpedFaction, other);
            if (pair == null || diplomacy.getRelation(pair) >= 0) {
                continue;
            }
            int reputationLoss = Math.max(1, amount / 3);
            EchoCoreServices.addFactionReputation(player, other, -reputationLoss);
            diplomacy.modifyRelation(pair, -1);
            if (reputationLoss >= 3) {
                player.sendSystemMessage(Component.literal("\u00A78[ECHO-7]\u00A7r "
                        + AshfallFactionMap.displayName(other) + " disapproves of your aid to "
                        + AshfallFactionMap.displayName(helpedFaction) + "."));
            }
        }
        FactionDiplomacy.saveAndSync(player, diplomacy);
    }

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

        Identifier discoveredFaction = profile.faction() == null
                ? AshfallFactionMap.forPoi(normalizedId)
                : profile.faction();

        if (player instanceof ServerPlayer serverPlayer) {
            EchoCoreServices.addFactionReputation(serverPlayer, discoveredFaction, 2);
            EchoCoreServices.markFactionContacted(serverPlayer, discoveredFaction);
            AshfallFactionContractProgression.progressPoi(serverPlayer, normalizedId);
            FactionNpcPopulationHandler.onPoiDiscovered(serverPlayer, normalizedId);
            ResearchData.saveAndSync(serverPlayer, research);
            FactionProgressionHelper.syncMilestones(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String entityName = event.getEntity().getType().getDescriptionId();
        Identifier affectedFaction = AshfallFactionMap.forEntity(entityName);

        if (entityName.contains("feral_human") || entityName.contains("mutant")) {
            EchoCoreServices.addFactionReputation(player, AshfallBiomeFactions.SPOREBOUND_SANCTUM, -2);
        }

        if (entityName.contains("military") || entityName.contains("soldier")) {
            maybeDropFragment(event, ModItems.SCHEMATIC_FRAGMENT_WEAPONS.get(), ModItems.SCHEMATIC_FRAGMENT_ARMOR.get());
            EchoCoreServices.addFactionReputation(player, AshfallBiomeFactions.RADWARDEN_COMPACT, -1);
        }

        if (entityName.contains("scavenger") || entityName.contains("bandit")) {
            maybeDropFragment(event, ModItems.SCHEMATIC_FRAGMENT_MACHINES.get(), ModItems.SCHEMATIC_FRAGMENT_MEDICAL.get());
        }

        AshfallFactionContractProgression.progressKill(player, entityName);
        if (affectedFaction != null) {
            FactionProgressionHelper.syncMilestones(player, affectedFaction);
        }
    }

    private static void maybeDropFragment(LivingDeathEvent event, net.minecraft.world.item.Item first,
            net.minecraft.world.item.Item second) {
        if (RANDOM.nextFloat() >= 0.05F) {
            return;
        }
        ItemStack fragment = new ItemStack(RANDOM.nextBoolean() ? first : second);
        var entity = event.getEntity();
        var itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                entity.level(), entity.getX(), entity.getY(), entity.getZ(), fragment);
        entity.level().addFreshEntity(itemEntity);
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (event.getState().getBlock() instanceof com.knoxhack.echoashfallprotocol.block.ResearchLabBlock
                && !player.level().isClientSide()) {
            // Research lab passive-use hooks live in the research system; this event keeps the extension point active.
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // NeoForge attachment copy rules handle copied Ashfall faction data.
    }
}
