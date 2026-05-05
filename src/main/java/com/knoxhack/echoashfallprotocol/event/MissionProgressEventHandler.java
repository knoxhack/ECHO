package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelaySiteService;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfiles;
import com.knoxhack.echoashfallprotocol.world.BiomeGuardianSiteData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.Set;

/**
 * Global quest progress hooks shared by mission requirements.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class MissionProgressEventHandler {
    private static final Set<String> BIOME_BOSS_IDS = Set.of(
            "echoashfallprotocol:wasteland_sentinel",
            "echoashfallprotocol:crash_zone_colossus",
            "echoashfallprotocol:cryogenic_overseer",
            "echoashfallprotocol:industrial_juggernaut",
            "echoashfallprotocol:nexus_scar_avatar",
            "echoashfallprotocol:radiation_behemoth",
            "echoashfallprotocol:city_ruin_stalker",
            "echoashfallprotocol:plains_warlord",
            "echoashfallprotocol:toxic_hive_matriarch"
    );

    private static final Map<String, String> BOSS_NAMES = Map.of(
            "echoashfallprotocol:wasteland_sentinel", "Wasteland Sentinel",
            "echoashfallprotocol:crash_zone_colossus", "Crash Zone Colossus",
            "echoashfallprotocol:cryogenic_overseer", "Cryogenic Overseer",
            "echoashfallprotocol:industrial_juggernaut", "Industrial Juggernaut",
            "echoashfallprotocol:nexus_scar_avatar", "Nexus Scar Avatar",
            "echoashfallprotocol:radiation_behemoth", "Radiation Behemoth",
            "echoashfallprotocol:city_ruin_stalker", "City Ruin Stalker",
            "echoashfallprotocol:plains_warlord", "Plains Warlord",
            "echoashfallprotocol:toxic_hive_matriarch", "Toxic Hive Matriarch"
    );

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LivingEntity killed = event.getEntity();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType()).toString();
        QuestData quest = QuestData.get(player);
        quest.recordEntityKill(entityId);
        NexusRelaySiteService.recordPressureMobKill(player, killed);

        var guardianProfile = BiomeGuardianProfiles.byEntityId(entityId);
        if (BIOME_BOSS_IDS.contains(entityId) || guardianProfile.isPresent()) {
            String bossName = guardianProfile.map(profile -> profile.title()).orElse(BOSS_NAMES.getOrDefault(entityId, entityId));
            quest.visitLocation("special", "boss:" + entityId);
            quest.discoverPOI("boss:" + entityId);
            quest.recordPOIState("boss:" + entityId, QuestData.POIObjectiveState.BOSS_DEFEATED);
            guardianProfile.ifPresent(profile -> quest.visitLocation("guardian", profile.bossPath()));
            guardianProfile.ifPresent(profile ->
                    quest.recordPOIState("guardian_" + profile.bossPath(), QuestData.POIObjectiveState.BOSS_DEFEATED));
            quest.addToArchive("[GUARDIAN] " + bossName + " neutralized. Underground Gridfall node archived.");
            if (killed.level() instanceof ServerLevel serverLevel) {
                guardianProfile.ifPresent(profile ->
                        BiomeGuardianSiteData.get(serverLevel).markDefeated(profile.bossPath(), killed.blockPosition()));
            }
            player.sendSystemMessage(Component.literal("\u00A76[ECHO-7]\u00A7r Guardian dossier updated: " + bossName + " neutralized."));
        }

        QuestData.saveAndSync(player, quest);
    }
}
