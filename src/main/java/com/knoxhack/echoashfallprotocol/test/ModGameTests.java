package com.knoxhack.echoashfallprotocol.test;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiscoveryCategory;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.google.gson.JsonElement;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.NexusCoreBlock;
import com.knoxhack.echoashfallprotocol.block.PowerNodeBlock;
import com.knoxhack.echoashfallprotocol.block.WorkshopBlock;
import com.knoxhack.echoashfallprotocol.block.entity.BatteryBankBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.NexusCoreBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.PowerCableBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.PowerNodeBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.WaterPurifierBlockEntity;
import com.knoxhack.echoashfallprotocol.boss.BossHudProfile;
import com.knoxhack.echoashfallprotocol.boss.BossHudProfiles;
import com.knoxhack.echoashfallprotocol.boss.BossHudTargetResolver;
import com.knoxhack.echoashfallprotocol.echo.EchoIntel;
import com.knoxhack.echoashfallprotocol.echo.EndgameMissionProgress;
import com.knoxhack.echoashfallprotocol.echo.Mission;
import com.knoxhack.echoashfallprotocol.echo.MissionGuideRegistry;
import com.knoxhack.echoashfallprotocol.echo.MissionRegistry;
import com.knoxhack.echoashfallprotocol.echo.MissionUxSummary;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.entity.ScoutDrone;
import com.knoxhack.echoashfallprotocol.entity.boss.BiomeBossEntity;
import com.knoxhack.echoashfallprotocol.entity.boss.NexusFinalBossEntity;
import com.knoxhack.echoashfallprotocol.entity.boss.WardenBossEntity;
import com.knoxhack.echoashfallprotocol.endgame.NexusAccessRules;
import com.knoxhack.echoashfallprotocol.endgame.NexusChoiceService;
import com.knoxhack.echoashfallprotocol.endgame.NexusFinalBossProfile;
import com.knoxhack.echoashfallprotocol.endgame.NexusFinalBossProfiles;
import com.knoxhack.echoashfallprotocol.endgame.NexusPressureMobProfiles;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelayProfile;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelayProfiles;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelayState;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelaySiteService;
import com.knoxhack.echoashfallprotocol.endgame.NexusRelayType;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.endgame.PrefallArchivesArenaService;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventProfiles;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventStatus;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventType;
import com.knoxhack.echoashfallprotocol.event.ModStructuresCommand;
import com.knoxhack.echoashfallprotocol.event.NexusCommandHandler;
import com.knoxhack.echoashfallprotocol.event.PostNexusEventHandler;
import com.knoxhack.echoashfallprotocol.event.StructureGenCommand;
import com.knoxhack.echoashfallprotocol.fasttravel.RadioNetwork;
import com.knoxhack.echoashfallprotocol.faction.AshfallBiomeFactions;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionContracts;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionMap;
import com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfile;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfiles;
import com.knoxhack.echoashfallprotocol.integration.AshfallDiscoveryProvider;
import com.knoxhack.echoashfallprotocol.integration.AshfallTerminalCommonIntegration;
import com.knoxhack.echoashfallprotocol.item.RareTechSchematicItem;
import com.knoxhack.echoashfallprotocol.item.SchematicFragmentItem;
import com.knoxhack.echoashfallprotocol.machine.MachineWearData;
import com.knoxhack.echoashfallprotocol.machine.MachineWearSavedData;
import com.knoxhack.echoashfallprotocol.network.BossNavigationPacket;
import com.knoxhack.echoashfallprotocol.network.DroneCommandPacket;
import com.knoxhack.echoashfallprotocol.network.ModNetwork;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.Perk;
import com.knoxhack.echoashfallprotocol.research.PerkRegistry;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.BiomeGuardianSiteData;
import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import com.knoxhack.echoashfallprotocol.world.NexusCampaignData;
import com.knoxhack.echoashfallprotocol.world.NexusWorldData;
import com.knoxhack.echoashfallprotocol.world.StartingDropPodData;
import com.knoxhack.echoashfallprotocol.worldgen.ProceduralStructureGenerator;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalNavigationProfiles;
import com.knoxhack.echoterminal.api.TerminalNavigationSection;
import com.knoxhack.echoterminal.api.TerminalTabRegistry;
import com.knoxhack.echoterminal.discovery.TerminalDiscoveryProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoAshfallProtocol.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENTITY_ATTRIBUTE_HARDENING =
            TEST_FUNCTIONS.register("entity_attribute_hardening", () -> ModGameTests::entityAttributeHardening);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRONE_COMMAND_HARDENING =
            TEST_FUNCTIONS.register("drone_command_hardening", () -> ModGameTests::droneCommandHardening);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GUARDIAN_PROFILE_COVERAGE =
            TEST_FUNCTIONS.register("guardian_profile_coverage", () -> ModGameTests::guardianProfileCoverage);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GUARDIAN_BOSS_SMOKE =
            TEST_FUNCTIONS.register("guardian_boss_smoke", () -> ModGameTests::guardianBossSmoke);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GUARDIAN_SITE_STATE =
            TEST_FUNCTIONS.register("guardian_site_state", () -> ModGameTests::guardianSiteState);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOSS_HUD_NAVIGATION =
            TEST_FUNCTIONS.register("boss_hud_navigation", () -> ModGameTests::bossHudNavigation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_ACCESS_RULES =
            TEST_FUNCTIONS.register("nexus_access_rules", () -> ModGameTests::nexusAccessRules);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_CAMPAIGN_DATA =
            TEST_FUNCTIONS.register("nexus_campaign_data", () -> ModGameTests::nexusCampaignData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_WARFRONT_CONTENT =
            TEST_FUNCTIONS.register("nexus_warfront_content", () -> ModGameTests::nexusWarfrontContent);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WARDEN_ARENA_SERVICE =
            TEST_FUNCTIONS.register("warden_arena_service", () -> ModGameTests::wardenArenaService);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RARE_SCHEMATIC_RESEARCH =
            TEST_FUNCTIONS.register("rare_schematic_research", () -> ModGameTests::rareSchematicResearch);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RESEARCH_PERK_GRAPH =
            TEST_FUNCTIONS.register("research_perk_graph", () -> ModGameTests::researchPerkGraph);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRUCTURE_EXPORT_PATHS =
            TEST_FUNCTIONS.register("structure_export_paths", () -> ModGameTests::structureExportPaths);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STARTER_DROP_POD_TEMPLATE =
            TEST_FUNCTIONS.register("starter_drop_pod_template", () -> ModGameTests::starterDropPodTemplate);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STARTER_DROP_POD_CORRUPTION_GUARD =
            TEST_FUNCTIONS.register("starter_drop_pod_corruption_guard", () -> ModGameTests::starterDropPodCorruptionGuard);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STARTING_DROP_POD_DATA_LENIENT_LOAD =
            TEST_FUNCTIONS.register("starting_drop_pod_data_lenient_load", () -> ModGameTests::startingDropPodDataLenientLoad);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHIVE_READ_STATE =
            TEST_FUNCTIONS.register("archive_read_state", () -> ModGameTests::archiveReadState);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SUBSTRATE_GRINDER_RECIPES =
            TEST_FUNCTIONS.register("substrate_grinder_recipes", () -> ModGameTests::substrateGrinderRecipes);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENVIRONMENTAL_EVENT_PROFILES =
            TEST_FUNCTIONS.register("environmental_event_profiles", () -> ModGameTests::environmentalEventProfiles);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_UX_SUMMARY =
            TEST_FUNCTIONS.register("mission_ux_summary", () -> ModGameTests::missionUxSummary);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENDGAME_ROUTE_PROGRESS =
            TEST_FUNCTIONS.register("endgame_route_progress", () -> ModGameTests::endgameRouteProgress);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_LORE_TAXONOMY =
            TEST_FUNCTIONS.register("terminal_lore_taxonomy", () -> ModGameTests::terminalLoreTaxonomy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_COMMAND_DECK_OWNERSHIP =
            TEST_FUNCTIONS.register("terminal_command_deck_ownership", () -> ModGameTests::terminalCommandDeckOwnership);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_COMMON_REGISTRATION =
            TEST_FUNCTIONS.register("terminal_common_registration", () -> ModGameTests::terminalCommonRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_GUIDE_COVERAGE =
            TEST_FUNCTIONS.register("mission_guide_coverage", () -> ModGameTests::missionGuideCoverage);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIRST_NIGHT_ROUTE_SAFETY =
            TEST_FUNCTIONS.register("first_night_route_safety", () -> ModGameTests::firstNightRouteSafety);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPLORATION_SITE_PROFILES =
            TEST_FUNCTIONS.register("exploration_site_profiles", () -> ModGameTests::explorationSiteProfiles);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ASHFALL_DISCOVERY_PROVIDER =
            TEST_FUNCTIONS.register("ashfall_discovery_provider", () -> ModGameTests::ashfallDiscoveryProvider);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTION_CONTRACT_BALANCE =
            TEST_FUNCTIONS.register("faction_contract_balance", () -> ModGameTests::factionContractBalance);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRICT_FACTION_ENTITY_IDS =
            TEST_FUNCTIONS.register("strict_faction_entity_ids", () -> ModGameTests::strictFactionEntityIds);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MACHINE_WEAR_SAVED_DATA =
            TEST_FUNCTIONS.register("machine_wear_saved_data", () -> ModGameTests::machineWearSavedData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEBUG_COMMAND_PERMISSION_GATES =
            TEST_FUNCTIONS.register("debug_command_permission_gates", () -> ModGameTests::debugCommandPermissionGates);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_UPGRADE_DATA_PATH =
            TEST_FUNCTIONS.register("nexus_upgrade_data_path", () -> ModGameTests::nexusUpgradeDataPath);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WATER_PURIFIER_NETWORK_POWER =
            TEST_FUNCTIONS.register("water_purifier_network_power", () -> ModGameTests::waterPurifierNetworkPower);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORKSHOP_STATUS_COPY =
            TEST_FUNCTIONS.register("workshop_status_copy", () -> ModGameTests::workshopStatusCopy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NEXUS_COMMAND_STATUS_ONLY =
            TEST_FUNCTIONS.register("nexus_command_status_only", () -> ModGameTests::nexusCommandStatusOnly);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RADIO_DYNAMIC_STATION_PERSISTENCE =
            TEST_FUNCTIONS.register("radio_dynamic_station_persistence", () -> ModGameTests::radioDynamicStationPersistence);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DRONE_INTEL_TARGETING =
            TEST_FUNCTIONS.register("drone_intel_targeting", () -> ModGameTests::droneIntelTargeting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> QUEST_REWARD_STACK_PERSISTENCE =
            TEST_FUNCTIONS.register("quest_reward_stack_persistence", () -> ModGameTests::questRewardStackPersistence);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("release_hardening"));
        register(event, environment, "entity_attribute_hardening", ENTITY_ATTRIBUTE_HARDENING.getId());
        register(event, environment, "drone_command_hardening", DRONE_COMMAND_HARDENING.getId());
        register(event, environment, "guardian_profile_coverage", GUARDIAN_PROFILE_COVERAGE.getId());
        register(event, environment, "guardian_boss_smoke", GUARDIAN_BOSS_SMOKE.getId());
        register(event, environment, "guardian_site_state", GUARDIAN_SITE_STATE.getId());
        register(event, environment, "boss_hud_navigation", BOSS_HUD_NAVIGATION.getId());
        register(event, environment, "nexus_access_rules", NEXUS_ACCESS_RULES.getId());
        register(event, environment, "nexus_campaign_data", NEXUS_CAMPAIGN_DATA.getId());
        register(event, environment, "nexus_warfront_content", NEXUS_WARFRONT_CONTENT.getId());
        register(event, environment, "warden_arena_service", WARDEN_ARENA_SERVICE.getId());
        register(event, environment, "rare_schematic_research", RARE_SCHEMATIC_RESEARCH.getId());
        register(event, environment, "research_perk_graph", RESEARCH_PERK_GRAPH.getId());
        register(event, environment, "structure_export_paths", STRUCTURE_EXPORT_PATHS.getId());
        register(event, environment, "starter_drop_pod_template", STARTER_DROP_POD_TEMPLATE.getId());
        register(event, environment, "starter_drop_pod_corruption_guard", STARTER_DROP_POD_CORRUPTION_GUARD.getId());
        register(event, environment, "starting_drop_pod_data_lenient_load", STARTING_DROP_POD_DATA_LENIENT_LOAD.getId());
        register(event, environment, "archive_read_state", ARCHIVE_READ_STATE.getId());
        register(event, environment, "substrate_grinder_recipes", SUBSTRATE_GRINDER_RECIPES.getId());
        register(event, environment, "environmental_event_profiles", ENVIRONMENTAL_EVENT_PROFILES.getId());
        register(event, environment, "mission_ux_summary", MISSION_UX_SUMMARY.getId());
        register(event, environment, "endgame_route_progress", ENDGAME_ROUTE_PROGRESS.getId());
        register(event, environment, "terminal_lore_taxonomy", TERMINAL_LORE_TAXONOMY.getId());
        register(event, environment, "terminal_command_deck_ownership", TERMINAL_COMMAND_DECK_OWNERSHIP.getId());
        register(event, environment, "terminal_common_registration", TERMINAL_COMMON_REGISTRATION.getId());
        register(event, environment, "mission_guide_coverage", MISSION_GUIDE_COVERAGE.getId());
        register(event, environment, "first_night_route_safety", FIRST_NIGHT_ROUTE_SAFETY.getId());
        register(event, environment, "exploration_site_profiles", EXPLORATION_SITE_PROFILES.getId());
        register(event, environment, "ashfall_discovery_provider", ASHFALL_DISCOVERY_PROVIDER.getId());
        register(event, environment, "faction_contract_balance", FACTION_CONTRACT_BALANCE.getId());
        register(event, environment, "strict_faction_entity_ids", STRICT_FACTION_ENTITY_IDS.getId());
        register(event, environment, "machine_wear_saved_data", MACHINE_WEAR_SAVED_DATA.getId());
        register(event, environment, "debug_command_permission_gates", DEBUG_COMMAND_PERMISSION_GATES.getId());
        register(event, environment, "nexus_upgrade_data_path", NEXUS_UPGRADE_DATA_PATH.getId());
        register(event, environment, "water_purifier_network_power", WATER_PURIFIER_NETWORK_POWER.getId());
        register(event, environment, "workshop_status_copy", WORKSHOP_STATUS_COPY.getId());
        register(event, environment, "nexus_command_status_only", NEXUS_COMMAND_STATUS_ONLY.getId());
        register(event, environment, "radio_dynamic_station_persistence", RADIO_DYNAMIC_STATION_PERSISTENCE.getId());
        register(event, environment, "drone_intel_targeting", DRONE_INTEL_TARGETING.getId());
        register(event, environment, "quest_reward_stack_persistence", QUEST_REWARD_STACK_PERSISTENCE.getId());
    }

    private static void entityAttributeHardening(GameTestHelper helper) {
        List<EntityType<? extends Entity>> attackingTypes = attackingTypes();
        int index = 0;

        for (EntityType<? extends Entity> type : allAshfallTypes()) {
            Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            Entity entity = type.create(helper.getLevel(), EntitySpawnReason.EVENT);
            helper.assertTrue(entity != null, "Entity should be spawnable: " + entityId);
            if (entity == null) {
                continue;
            }

            if (entity instanceof Mob mob) {
                helper.assertTrue(mob.getAttribute(Attributes.MAX_HEALTH) != null,
                        "Mob should have max health attribute: " + entityId);
                helper.assertTrue(mob.getAttribute(Attributes.MOVEMENT_SPEED) != null,
                        "Mob should have movement speed attribute: " + entityId);
                if (attackingTypes.contains(type)) {
                    helper.assertTrue(mob.getAttribute(Attributes.ATTACK_DAMAGE) != null,
                            "Attacking mob should have attack damage attribute: " + entityId);
                }
            }

            BlockPos pos = helper.absolutePos(new BlockPos(1 + index % 8, 2, 1 + index / 8));
            entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(entity);
            entity.tick();
            entity.discard();
            index++;
        }

        helper.succeed();
    }

    private static void droneCommandHardening(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);
        cleanupOwnedDrones(helper, player);

        ModNetwork.handleDroneCommand(new DroneCommandPacket("RECALL"), player);

        Player otherPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos otherPos = helper.absolutePos(new BlockPos(4, 2, 1));
        otherPlayer.setPos(otherPos.getX() + 0.5D, otherPos.getY(), otherPos.getZ() + 0.5D);
        EchoCompanionDrone wrongOwnerDrone = spawnCompanionDrone(helper, otherPlayer, new BlockPos(6, 2, 1));
        ModNetwork.handleDroneCommand(new DroneCommandPacket("RECALL"), player);
        helper.assertTrue(!player.getUUID().equals(wrongOwnerDrone.getOwnerUUID()),
                "Wrong-owner companion drone should not be claimed by commands");
        wrongOwnerDrone.discard();

        EchoCompanionDrone drone = spawnCompanionDrone(helper, player, new BlockPos(8, 2, 1));
        drone.setRepairLevel(15);
        ModNetwork.handleDroneCommand(new DroneCommandPacket("COMBAT"), player);
        helper.assertTrue(drone.getCurrentMode() != EchoCompanionDrone.DroneMode.COMBAT,
                "Locked companion combat mode should stay unavailable");

        boolean initialLight = drone.isLightEnabled();
        ModNetwork.handleDroneCommand(new DroneCommandPacket("TOGGLE_LIGHT"), player);
        helper.assertTrue(drone.isLightEnabled() != initialLight,
                "Light command should toggle companion light");

        drone.setPos(player.getX() + 24.0D, player.getY(), player.getZ() + 24.0D);
        helper.runAfterDelay(1L, () -> {
            ModNetwork.handleDroneCommand(new DroneCommandPacket("RECALL"), player);
            helper.assertTrue(drone.distanceToSqr(player) < 64.0D,
                    "Recall should move companion drone near owner");

            drone.setRepairLevel(EchoCompanionDrone.REPAIR_FULL);
            ModNetwork.handleDroneCommand(new DroneCommandPacket("SCAVENGE"), player);
            helper.assertTrue(drone.getCurrentMode() == EchoCompanionDrone.DroneMode.SCAVENGE,
                    "Unlocked companion scavenge mode should activate");

            ModNetwork.handleDroneCommand(new DroneCommandPacket("PATROL"), player);
            helper.assertTrue(drone.getCurrentMode() == EchoCompanionDrone.DroneMode.PATROL,
                    "Unlocked companion patrol mode should activate");

            ModNetwork.handleDroneCommand(new DroneCommandPacket("COMBAT"), player);
            helper.assertTrue(drone.getCurrentMode() == EchoCompanionDrone.DroneMode.COMBAT,
                    "Unlocked companion combat mode should activate");

            drone.discard();
            ScoutDrone scout = spawnScoutDrone(helper, player, new BlockPos(8, 2, 4));
            ModNetwork.handleDroneCommand(new DroneCommandPacket("SCAVENGE"), player);
            helper.assertTrue(scout.getMode() == ScoutDrone.DroneMode.SCAVENGE,
                    "Scout fallback should map SCAVENGE command to scavenge mode");

            scout.discard();
            cleanupOwnedDrones(helper, player);
            helper.succeed();
        });
    }

    private static void guardianProfileCoverage(GameTestHelper helper) {
        helper.assertTrue(BiomeGuardianProfiles.all().size() == 8, "Ashfall should expose eight active biome guardian profiles");
        helper.assertTrue(BiomeGuardianProfiles.byBiome("the_wasteland").isEmpty(),
                "The Wasteland should no longer expose an active guardian profile");
        helper.assertTrue(BiomeGuardianProfiles.byMissionId("neutralize_wasteland_sentinel").isEmpty(),
                "Wasteland Sentinel should not be an active guardian mission");
        helper.assertTrue(ProceduralStructureGenerator.getMainStructureForBiome("the_wasteland") == null,
                "The Wasteland should not generate a biome-main guardian site");
        helper.assertTrue(MissionRegistry.getMissionById("neutralize_wasteland_sentinel") == null,
                "Wasteland Sentinel mission should be removed from the active mission registry");
        Mission plainsWarlord = MissionRegistry.getMissionById("neutralize_plains_warlord");
        helper.assertTrue(plainsWarlord != null
                        && plainsWarlord.prerequisites().equals(List.of("activate_relay_station")),
                "Plains Warlord should unlock directly after the Relay Station mission");
        Set<BiomeGuardianProfile.GuardianAbility> abilities =
                EnumSet.noneOf(BiomeGuardianProfile.GuardianAbility.class);
        Set<String> missions = new HashSet<>();
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            helper.assertTrue(AshfallFactionMap.all().contains(profile.ownerFaction()),
                    "Guardian owner faction should be one of the three Ashfall factions: " + profile.bossPath());
            helper.assertTrue(abilities.add(profile.ability()), "Guardian ability should be unique: " + profile.bossPath());
            helper.assertTrue(missions.add(profile.missionId()), "Guardian mission should be unique: " + profile.missionId());
            helper.assertTrue(profile.bossType().get() != null, "Guardian boss type missing: " + profile.bossPath());
            helper.assertTrue(profile.defenderType().get() != null, "Guardian defender type missing: " + profile.bossPath());
            helper.assertFalse(profile.rewardBundle().isEmpty(), "Guardian reward bundle missing: " + profile.bossPath());
            helper.assertTrue(profile.rewardBundle().entries().stream()
                            .anyMatch(entry -> entry.item().get().asItem() == ModItems.GUARDIAN_DATACORE.get()),
                    "Guardian reward bundle should include Guardian Datacore: " + profile.bossPath());
            helper.assertTrue(profile.visual().scale() > 0.0F, "Guardian visual scale must be positive: " + profile.bossPath());
            helper.assertTrue(profile.visual().shadow() > 0.0F, "Guardian visual shadow must be positive: " + profile.bossPath());
            BiomeGuardianProfile.PolishData polish = profile.polish();
            helper.assertTrue(polish != null, "Guardian polish data missing: " + profile.bossPath());
            if (polish != null) {
                helper.assertFalse(polish.arenaSetPiece().isBlank(), "Guardian arena polish missing: " + profile.bossPath());
                helper.assertFalse(polish.phaseCue().isBlank(), "Guardian phase cue missing: " + profile.bossPath());
                helper.assertFalse(polish.counterplayObject().isBlank(), "Guardian counterplay object missing: " + profile.bossPath());
                helper.assertFalse(polish.addPressurePattern().isBlank(), "Guardian add pressure missing: " + profile.bossPath());
                helper.assertFalse(polish.rewardCategory().isBlank(), "Guardian reward category missing: " + profile.bossPath());
                helper.assertFalse(polish.codexSummary().isBlank(), "Guardian Codex summary missing: " + profile.bossPath());
                helper.assertFalse(polish.hudObjectiveLabel().isBlank(), "Guardian HUD objective label missing: " + profile.bossPath());
            }
            helper.assertTrue(ProceduralStructureGenerator.hasGuardianSiteTheme(profile),
                    "Guardian structure theme missing: " + profile.bossPath());
            helper.assertTrue(ProceduralStructureGenerator.guardianSiteLayoutContractValid(profile),
                    "Guardian structure layout contract invalid: " + profile.bossPath());
            helper.assertTrue(BiomeGuardianProfiles.byBossType(profile.bossType().get()).orElse(null) == profile,
                    "Guardian boss type lookup should round-trip: " + profile.bossPath());
            helper.assertTrue(BiomeGuardianProfiles.byMissionId(profile.missionId()).orElse(null) == profile,
                    "Guardian mission lookup should round-trip: " + profile.missionId());
        }
        helper.succeed();
    }

    private static void environmentalEventProfiles(GameTestHelper helper) {
        Set<EnvironmentalEventType> covered = EnumSet.noneOf(EnvironmentalEventType.class);
        for (var profile : EnvironmentalEventProfiles.activeProfiles()) {
            helper.assertTrue(profile.durationTicks() > 0, "Environmental event duration must be positive: " + profile.type());
            helper.assertFalse(profile.commandAlias().isBlank(), "Environmental event command alias missing: " + profile.type());
            helper.assertFalse(profile.hudLabel().isBlank(), "Environmental event HUD label missing: " + profile.type());
            helper.assertTrue(profile.particleBudget() >= 0, "Environmental event particle budget must be non-negative: " + profile.type());
            helper.assertTrue(EnvironmentalEventProfiles.byAlias(profile.commandAlias()).orElse(null) == profile.type(),
                    "Environmental event command alias should parse: " + profile.commandAlias());
            EnvironmentalEventStatus status = EnvironmentalEventStatus.fromSynced(
                    profile.type().name(),
                    profile.durationTicks(),
                    profile.durationTicks(),
                    1.0F,
                    0.0F,
                    0);
            helper.assertTrue(status.active(), "Environmental event status should be active: " + profile.type());
            helper.assertFalse(status.counterGuidance().isBlank(), "Environmental event counter guidance missing: " + profile.type());
            helper.assertFalse(status.survivalImpact().isBlank(), "Environmental event survival impact missing: " + profile.type());
            helper.assertTrue(status.weatherMode() == profile.weatherMode(), "Environmental event weather mode mismatch: " + profile.type());
            helper.assertFalse(status.centerWarningTitle().isBlank(), "Environmental event center title missing: " + profile.type());
            helper.assertFalse(status.centerWarningSubtitle().isBlank(), "Environmental event center subtitle missing: " + profile.type());
            helper.assertTrue((status.hudColor() >>> 24) > 0, "Environmental event HUD color should be visible: " + profile.type());
            helper.assertTrue(environmentalHudIconIndex(profile.type()) >= 0,
                    "Environmental event HUD icon mapping missing: " + profile.type());
            covered.add(profile.type());
        }
        for (EnvironmentalEventType type : EnvironmentalEventType.values()) {
            if (type != EnvironmentalEventType.NONE) {
                helper.assertTrue(covered.contains(type), "Environmental event profile missing: " + type);
                int iconIndex = environmentalHudIconIndex(type);
                helper.assertTrue(iconIndex >= 0 && iconIndex < 8, "Environmental event HUD icon index out of atlas range: " + type);
            }
        }
        helper.succeed();
    }

    private static int environmentalHudIconIndex(EnvironmentalEventType type) {
        return switch (type) {
            case RADIATION_STORM -> 0;
            case TOXIC_STORM -> 1;
            case BLACKOUT -> 2;
            case ASH_STORM -> 3;
            case CRYO_FRONT -> 4;
            case NEXUS_SURGE -> 5;
            default -> -1;
        };
    }

    private static void guardianBossSmoke(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(12, 2, 12));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);

        int index = 0;
        int guardianCount = Math.max(1, BiomeGuardianProfiles.all().size());
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            BiomeBossEntity boss = profile.bossType().get().create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(boss != null, "Guardian should be spawnable: " + profile.bossPath());
            if (boss == null) {
                continue;
            }
            double angle = index++ * (Math.PI * 2.0D / guardianCount);
            boss.setPos(player.getX() + Math.cos(angle) * 5.0D, player.getY(), player.getZ() + Math.sin(angle) * 5.0D);
            boss.setTarget(player);
            level.addFreshEntity(boss);
            boss.tick();
            boss.setHealth(boss.getMaxHealth() * 0.30F);
            boss.tick();
            boss.tick();
            helper.assertTrue(boss.getGuardianPhase() >= 3, "Guardian should enter phase 3: " + profile.bossPath());
        }

        for (int i = 0; i < 90; i++) {
            for (BiomeBossEntity boss : level.getEntitiesOfClass(BiomeBossEntity.class, player.getBoundingBox().inflate(18.0D))) {
                boss.tick();
            }
        }

        for (BiomeBossEntity boss : level.getEntitiesOfClass(BiomeBossEntity.class, player.getBoundingBox().inflate(18.0D))) {
            boss.discard();
        }
        helper.succeed();
    }

    private static void guardianSiteState(GameTestHelper helper) {
        BiomeGuardianProfile profile = BiomeGuardianProfiles.byBiome("ruined_plains").orElseThrow();
        BiomeGuardianSiteData data = BiomeGuardianSiteData.get(helper.getLevel());
        BlockPos entrance = helper.absolutePos(new BlockPos(4, 1, 4));
        BlockPos arena = helper.absolutePos(new BlockPos(12, 1, 12));
        data.addOrUpdate(profile, entrance, arena);
        helper.assertTrue(data.nearestActive(entrance, profile.bossPath()).isPresent(),
                "Guardian site should be active before defeat");
        helper.assertTrue(data.nearestActiveForMission(entrance, profile.missionId()).isPresent(),
                "Guardian mission scanner lookup should use active saved entrance");
        data.addOrUpdate(profile, entrance.offset(4, 0, 0), arena.offset(4, 0, 0));
        long nearbyCount = data.allSites().stream()
                .filter(entry -> entry.guardianId().equals(profile.bossPath()))
                .filter(entry -> entry.entrance().distSqr(entrance) < 128 * 128)
                .count();
        helper.assertTrue(nearbyCount == 1, "Nearby duplicate guardian sites should collapse");
        data.markDefeated(profile.bossPath(), arena);
        helper.assertFalse(data.nearestActive(entrance, profile.bossPath()).isPresent(),
                "Guardian site should stop being active after defeat");
        helper.succeed();
    }

    private static void bossHudNavigation(GameTestHelper helper) {
        helper.assertTrue(BossHudProfiles.all().size() >= 13,
                "Boss HUD profiles should cover guardians, Warden, and orbital boss-tier encounters");
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            BossHudProfile hud = BossHudProfiles.byEntityId(profile.entityId()).orElse(null);
            helper.assertTrue(hud != null, "Guardian HUD profile missing: " + profile.bossPath());
            helper.assertTrue(BossHudProfiles.byTitle(profile.title()).orElse(null) == hud,
                    "Guardian HUD title lookup should round-trip: " + profile.title());
            helper.assertTrue(hud != null && !hud.compassLabel().isBlank(),
                    "Guardian HUD compass label missing: " + profile.bossPath());
            helper.assertTrue(hud != null && (hud.accentColor() & 0x00FFFFFF) != 0,
                    "Guardian HUD accent should be visible: " + profile.bossPath());
            helper.assertTrue(hud != null && !hud.phaseWarningLabel().isBlank(),
                    "Guardian HUD phase warning missing: " + profile.bossPath());
            helper.assertTrue(hud != null && !hud.counterplayLabel().isBlank(),
                    "Guardian HUD counterplay missing: " + profile.bossPath());
            helper.assertTrue(!profile.cinematicCue().dangerVerb().isBlank(),
                    "Guardian cinematic cue missing danger verb: " + profile.bossPath());
            helper.assertTrue(!profile.rewardBundle().isEmpty(),
                    "Guardian Codex reward bundle missing: " + profile.bossPath());
            if (hud != null) {
                BiomeGuardianProfile.PolishData polish = profile.polish();
                helper.assertTrue(hud.subtitle().contains(profile.cinematicCue().dangerVerb())
                                && hud.subtitle().contains(polish.counterplayObject()),
                        "Guardian HUD subtitle should mirror profile danger/counterplay copy: " + profile.bossPath());
                helper.assertTrue(hud.phaseWarningLabel().equals(profile.cinematicCue().phaseWarningLabel()),
                        "Guardian HUD phase warning should mirror profile cue: " + profile.bossPath());
                helper.assertTrue(hud.counterplayLabel().equals(polish.counterplayObject()),
                        "Guardian HUD counterplay should mirror polish data: " + profile.bossPath());
                helper.assertTrue(hud.compassLabel().equals(profile.cinematicCue().objectiveLabel()),
                        "Guardian HUD compass label should mirror profile objective: " + profile.bossPath());
            }
        }
        helper.assertTrue(BossHudProfiles.byEntityId("echoashfallprotocol:warden_boss").isPresent(),
                "Warden HUD profile should exist");
        helper.assertTrue(BossHudProfiles.byEntityId("echoorbitalremnants:corrupted_docking_ai").isPresent(),
                "Orbital Docking AI HUD profile should exist");
        helper.assertTrue(BossHudProfiles.byTitle("ECHO-0").isPresent(), "ECHO-0 HUD title profile should exist");

        BiomeGuardianProfile guardian = BiomeGuardianProfiles.byBiome("ruined_plains").orElseThrow();
        BossHudProfile guardianHud = BossHudProfiles.byEntityId(guardian.entityId()).orElseThrow();
        BossNavigationPacket original = BossNavigationPacket.active(guardianHud, "minecraft:overworld",
                new BlockPos(4, 64, -9), 2, 0.45F, guardian.title());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        BossNavigationPacket.CODEC.encode(buffer, original);
        BossNavigationPacket decoded = BossNavigationPacket.CODEC.decode(buffer);
        helper.assertTrue(decoded.active(), "Boss navigation packet should preserve active flag");
        helper.assertTrue(decoded.bossId().equals(original.bossId()), "Boss navigation packet should preserve boss id");
        helper.assertTrue(decoded.title().equals(original.title()), "Boss navigation packet should preserve title");
        helper.assertTrue(decoded.subtitle().equals(original.subtitle()), "Boss navigation packet should preserve subtitle");
        helper.assertTrue(decoded.dimension().equals(original.dimension()), "Boss navigation packet should preserve dimension");
        helper.assertTrue(decoded.position().equals(original.position()), "Boss navigation packet should preserve position");
        helper.assertTrue(decoded.phase() == 2, "Boss navigation packet should preserve phase");
        helper.assertTrue(Math.abs(decoded.healthPercent() - original.healthPercent()) < 0.0001F,
                "Boss navigation packet should preserve health percent");
        helper.assertTrue(decoded.accentColor() == original.accentColor(),
                "Boss navigation packet should preserve accent color");
        helper.assertTrue(decoded.compassLabel().equals(original.compassLabel()),
                "Boss navigation packet should preserve compass label");
        helper.assertTrue(decoded.category().equals(original.category()),
                "Boss navigation packet should preserve category");
        helper.assertTrue("LIVE".equals(decoded.targetKind()), "Boss navigation packet should preserve live target kind");
        helper.assertTrue(guardianHud.phaseForHealth(0.65F) == 2 && guardianHud.phaseForHealth(0.32F) == 3,
                "Boss HUD phase thresholds should expose phase 2 and phase 3");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);
        setCurrentMission(QuestData.get(player), guardian.missionId());
        BiomeGuardianSiteData data = BiomeGuardianSiteData.get(helper.getLevel());
        BlockPos entrance = helper.absolutePos(new BlockPos(8, 2, 8));
        BlockPos arena = helper.absolutePos(new BlockPos(12, 2, 8));
        data.addOrUpdate(guardian, entrance, arena);
        helper.getLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(170.0D),
                entity -> BossHudProfiles.isSupportedEntityId(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()))
                .forEach(Entity::discard);

        BossNavigationPacket siteTarget = BossHudTargetResolver.resolve(player);
        helper.assertTrue(siteTarget.active(), "Active guardian mission should expose a compass target");
        helper.assertTrue(siteTarget.title().contains("Entrance"), "Guardian pre-combat target should point to the entrance");
        helper.assertTrue("ENTRANCE".equals(siteTarget.targetKind()), "Guardian pre-combat target should be marked as entrance");

        BiomeBossEntity boss = guardian.bossType().get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(boss != null, "Guardian boss should spawn for HUD resolver priority");
        if (boss != null) {
            boss.setPos(player.getX() + 3.0D, player.getY(), player.getZ());
            helper.getLevel().addFreshEntity(boss);
            boss.tick();
            BossNavigationPacket liveTarget = BossHudTargetResolver.resolve(player);
            helper.assertTrue(liveTarget.active(), "Live boss should expose a compass target");
            helper.assertTrue(!liveTarget.title().contains("Entrance"),
                    "Live boss should override the guardian entrance target");
            helper.assertTrue("LIVE".equals(liveTarget.targetKind()), "Live boss target should be marked as live");
            boss.discard();
        }

        data.markDefeated(guardian.bossPath(), arena);
        BossNavigationPacket cleared = BossHudTargetResolver.resolve(player);
        helper.assertFalse(cleared.active(), "Defeated guardian site should clear the compass target");

        WardenBossEntity warden = ModEntities.WARDEN_BOSS.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(warden != null, "Warden should spawn for archive boss target state");
        if (warden != null) {
            warden.setPos(player.getX() + 4.0D, player.getY(), player.getZ());
            helper.getLevel().addFreshEntity(warden);
            warden.tick();
            BossNavigationPacket archiveTarget = BossHudTargetResolver.resolve(player);
            helper.assertTrue(archiveTarget.active(), "Live Warden should expose an archive compass target");
            helper.assertTrue("ARCHIVE".equals(archiveTarget.targetKind()),
                    "Warden live target should be marked as archive");
            warden.discard();
        }
        helper.succeed();
    }

    private static void nexusAccessRules(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(new BlockPos(5, 2, 4));
        QuestData quest = new QuestData();
        NexusWorldData.get(level.getServer().overworld())
                .setChoice(NexusWorldData.WorldState.NORMAL, BlockPos.ZERO, "GameTest reset");
        NexusCampaignData campaign = NexusCampaignData.get(level.getServer().overworld());
        campaign.resetForTests();
        for (BlockPos nodePos : List.copyOf(NexusWorldData.get(level).getActiveNodePositions())) {
            NexusWorldData.get(level).removePowerNode(nodePos);
        }

        level.setBlock(corePos, ModBlocks.NEXUS_CORE.get().defaultBlockState(), 3);
        helper.assertTrue(level.getBlockEntity(corePos) instanceof NexusCoreBlockEntity,
                "Nexus Core block entity should be present");
        NexusCoreBlockEntity core = (NexusCoreBlockEntity) level.getBlockEntity(corePos);

        NexusAccessRules.Status missingGuardians = NexusAccessRules.evaluate(quest, level, core);
        helper.assertFalse(missingGuardians.allowed(), "Nexus should deny before guardians are defeated");
        helper.assertTrue(missingGuardians.missingGuardianCount() == 8,
                "Nexus gate should require all eight active guardians before checking nodes");

        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            quest.recordEntityKill(profile.entityId());
        }

        NexusAccessRules.Status missingNodes = NexusAccessRules.evaluate(quest, level, core);
        helper.assertFalse(missingNodes.allowed(), "Nexus should deny without five active nodes");
        helper.assertTrue(missingNodes.missingGuardianCount() == 0,
                "Guardian kills should satisfy the guardian gate");

        for (int i = 0; i < NexusCoreBlock.REQUIRED_NODES; i++) {
            BlockPos nodePos = corePos.offset(2 + i * 2, 0, 0);
            level.setBlock(nodePos,
                    ModBlocks.POWER_NODE.get().defaultBlockState().setValue(PowerNodeBlock.ACTIVE, true), 3);
            if (level.getBlockEntity(nodePos) instanceof PowerNodeBlockEntity node) {
                node.activate();
            }
            NexusWorldData.get(level).recordPowerNodeActivated(nodePos);
        }

        NexusAccessRules.Status warfrontLocked = NexusAccessRules.evaluate(quest, level, core);
        helper.assertFalse(warfrontLocked.allowed(),
                "Nexus should deny after guardians and nodes until Warfront is complete");

        campaign.awaken(corePos);
        campaign.scanRelays();
        campaign.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.STABILIZED);
        campaign.resolveRelay(NexusRelayType.CRYO, NexusRelayState.SEVERED);
        campaign.resolveRelay(NexusRelayType.BIO, NexusRelayState.OVERRIDDEN);
        campaign.markSiegeComplete();

        NexusAccessRules.Status ready = NexusAccessRules.evaluate(quest, level, core);
        helper.assertTrue(ready.allowed(),
                "Nexus should allow after guardians, five nodes, three relays, and the siege are ready");
        helper.assertTrue(ready.activatedNodes() >= NexusCoreBlock.REQUIRED_NODES,
                "Nexus status should report active node count");

        NexusWorldData worldData = NexusWorldData.get(level.getServer().overworld());
        worldData.setChoice(NexusWorldData.WorldState.RESTORED, corePos, "GameTest");
        NexusAccessRules.Status sealed = NexusAccessRules.evaluate(quest, level, core);
        helper.assertFalse(sealed.allowed(), "Resolved world state should deny stale local Core choices");
        helper.assertTrue(sealed.worldResolved(), "Resolved world denial should be visible in status");
        helper.assertTrue(sealed.worldState() == NexusWorldData.WorldState.RESTORED,
                "Resolved status should report the selected world path");

        helper.assertTrue(NexusChoiceService.parseChoice("restore") == NexusCoreBlockEntity.NexusChoice.RESTORE,
                "Restore choice should parse");
        helper.assertTrue(NexusChoiceService.parseChoice("destroyed") == NexusCoreBlockEntity.NexusChoice.DESTROY,
                "Destroy aliases should parse");
        helper.assertTrue(NexusChoiceService.parseChoice("CONTROL") == NexusCoreBlockEntity.NexusChoice.CONTROL,
                "Control choice should parse case-insensitively");

        worldData.setChoice(NexusWorldData.WorldState.NORMAL, BlockPos.ZERO, "");
        campaign.resetForTests();
        for (BlockPos nodePos : NexusWorldData.get(level).getActiveNodePositions()) {
            NexusWorldData.get(level).removePowerNode(nodePos);
        }
        helper.succeed();
    }

    private static void nexusCampaignData(GameTestHelper helper) {
        NexusCampaignData data = NexusCampaignData.get(helper.getLevel().getServer().overworld());
        data.resetForTests();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));

        helper.assertFalse(data.isAwakened(), "Campaign should start dormant after reset");
        data.awaken(pos);
        helper.assertTrue(data.isAwakened(), "Awakening should persist");
        helper.assertTrue(data.getInstability() >= 25, "Awakening should start instability pressure");

        data.scanRelays();
        helper.assertTrue(data.getScannedRelayCount() == NexusCampaignData.REQUIRED_RELAY_SCAN_COUNT,
                "Scan should reveal all Prime Relays");
        data.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.STABILIZED);
        int restoreReadiness = data.getReadinessRestore();
        helper.assertFalse(data.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.SEVERED),
                "Resolved relay outcome should be immutable");
        helper.assertTrue(data.getRelayState(NexusRelayType.REACTOR) == NexusRelayState.STABILIZED
                        && data.getReadinessRestore() == restoreReadiness,
                "Rejected relay outcome changes must not alter readiness");
        data.resolveRelay(NexusRelayType.CRYO, NexusRelayState.SEVERED);
        data.resolveRelay(NexusRelayType.BIO, NexusRelayState.OVERRIDDEN);
        helper.assertTrue(data.getResolvedRelayCount() == NexusCampaignData.REQUIRED_RELAY_RESOLUTION_COUNT,
                "Three resolved relays should satisfy relay count");
        helper.assertFalse(data.isWarfrontComplete(), "Warfront should still require the countermeasure siege");

        helper.assertTrue(data.markSiegeComplete(), "First siege credit should change campaign state");
        helper.assertFalse(data.markSiegeComplete(), "Duplicate siege credit should be ignored");
        helper.assertTrue(data.isWarfrontComplete(), "Siege completion should finish Warfront readiness");
        data.markWardenDefeated();
        data.markFinaleComplete();
        helper.assertTrue(data.isWardenDefeated() && data.isFinaleComplete(),
                "Post-choice midpoint and finale flags should persist");
        data.markFinalBossSummoned(PostNexusData.NexusPath.CONTROL);
        helper.assertTrue(data.isFinalBossSummonedFor(PostNexusData.NexusPath.CONTROL),
                "Final boss summon path should persist");
        data.clearFinalBossSummoned();
        helper.assertFalse(data.isFinalBossSummoned(), "Final boss recovery flag should clear after finale credit");

        data.resetForTests();
        helper.succeed();
    }

    private static void nexusWarfrontContent(GameTestHelper helper) {
        var level = helper.getLevel().getServer().overworld();
        NexusCampaignData data = NexusCampaignData.get(level);
        data.resetForTests();

        helper.assertTrue(BiomeGuardianProfiles.all().size() == 8,
                "Warfront content must not change the eight active biome guardian profiles");
        helper.assertTrue(NexusRelayProfiles.hasCoverage(), "Every Prime Relay type needs a content profile");
        helper.assertTrue(NexusPressureMobProfiles.registryMatchesEntities(), "Pressure mob profiles must map to registered entities");
        helper.assertTrue(NexusFinalBossProfiles.hasCoverage(), "All three Nexus paths need finale boss profiles");

        data.awaken(helper.absolutePos(new BlockPos(3, 2, 3)));
        data.scanRelays();
        helper.assertFalse(data.hasRelaySite(NexusRelayType.REACTOR),
                "Old-save Warfront data should start without relay positions");
        NexusRelaySiteService.ensureSitesAssignedAndGenerated(level, data, helper.absolutePos(new BlockPos(3, 2, 3)));
        for (NexusRelayProfile profile : NexusRelayProfiles.all()) {
            helper.assertTrue(data.hasRelaySite(profile.type()), "Relay scan should assign site: " + profile.type());
            helper.assertTrue(data.isRelayGenerated(profile.type()), "Relay scan should generate objective shell: " + profile.type());
            helper.assertTrue(NexusRelaySiteService.objectiveShellExists(level, data, profile.type()),
                    "Relay objective shell should be non-empty: " + profile.type());
            helper.assertTrue(profile.requiredPressureKills() > 0, "Relay profile needs pressure objective: " + profile.type());
            helper.assertTrue(!profile.objective().isBlank(), "Relay profile objective text missing: " + profile.type());
        }

        data.resetForTests();
        BlockPos near = helper.absolutePos(new BlockPos(8, 2, 8));
        data.awaken(near);
        data.scanRelays();
        for (NexusRelayType type : NexusRelayType.values()) {
            data.assignRelaySite(type, helper.absolutePos(new BlockPos(4 + type.ordinal() * 2, 2, 10)));
        }
        NexusRelaySiteService.ensureSitesAssignedAndGenerated(level, data, near);
        helper.assertTrue(data.firstEncounterCompleteUnresolvedRelay() == null,
                "Relay resolution queue should reject relays before encounter completion");
        NexusRelayProfile reactor = NexusRelayProfiles.byType(NexusRelayType.REACTOR).orElseThrow();
        data.markRelayEncounterStarted(NexusRelayType.REACTOR);
        for (int i = 0; i < reactor.requiredPressureKills(); i++) {
            data.incrementRelayPressureKill(NexusRelayType.REACTOR);
        }
        helper.assertFalse(data.isRelayObjectiveSatisfied(NexusRelayType.REACTOR, reactor),
                "Commander relay should still require the commander after pressure kills");
        data.markRelayCommanderDefeated(NexusRelayType.REACTOR);
        helper.assertTrue(data.isRelayObjectiveSatisfied(NexusRelayType.REACTOR, reactor),
                "Relay objective should pass after pressure kills and commander defeat");
        data.markRelayEncounterComplete(NexusRelayType.REACTOR);
        helper.assertTrue(data.firstEncounterCompleteUnresolvedRelay() == NexusRelayType.REACTOR,
                "Completed relay encounter should enter the resolution queue");
        helper.assertTrue(data.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.STABILIZED),
                "Encounter-complete relay should accept a resolved outcome in saved state");
        helper.assertFalse(data.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.SEVERED),
                "Resolved relay should reject a second outcome");
        helper.assertTrue(data.relaySummaryPayload().contains("Final Boss:")
                        && data.relaySummaryPayload().contains("Reactor Relay"),
                "Relay summary payload should include final boss and relay state text");

        Player scannerPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(NexusRelaySiteService.hasRelayScannerLens(scannerPlayer),
                "Scanner lens should not be active without the lens item");
        scannerPlayer.getInventory().add(new ItemStack(ModItems.PORTABLE_SIGNAL_SCANNER.get()));
        helper.assertFalse(NexusRelaySiteService.hasRelayScannerLens(scannerPlayer),
                "Portable scanner alone should not count as the relay lens upgrade");
        scannerPlayer.getInventory().add(new ItemStack(ModItems.RELAY_SCANNER_LENS.get()));
        helper.assertTrue(NexusRelaySiteService.hasRelayScannerLens(scannerPlayer),
                "Relay scanner lens should activate from the passive lens item");

        smokeEntity(helper, ModEntities.GRIDBOUND_HUSK.get());
        smokeEntity(helper, ModEntities.RELAY_WARDEN.get());
        smokeEntity(helper, ModEntities.SIGNAL_LEECH.get());
        smokeEntity(helper, ModEntities.NEXUS_NULLIFIER.get());
        for (NexusFinalBossProfile profile : NexusFinalBossProfiles.all()) {
            NexusFinalBossEntity boss = profile.entityType().get().create(helper.getLevel(), EntitySpawnReason.EVENT);
            helper.assertTrue(boss != null, "Finale boss should spawn: " + profile.entityPath());
            if (boss != null) {
                boss.setPos(near.getX() + 0.5D, near.getY(), near.getZ() + 0.5D);
                helper.getLevel().addFreshEntity(boss);
                boss.tick();
                helper.assertTrue(boss.path() == profile.path(), "Finale boss path should match profile: " + profile.path());
                helper.assertTrue(boss.getAttribute(Attributes.ATTACK_DAMAGE) != null,
                        "Finale boss needs attack damage: " + profile.entityPath());
                boss.discard();
            }
        }

        data.resetForTests();
        helper.succeed();
    }

    private static void wardenArenaService(GameTestHelper helper) {
        var level = helper.getLevel();
        int removed = PrefallArchivesArenaService.resetArena(level, PostNexusData.NexusPath.RESTORE, false);
        helper.assertTrue(removed >= 0, "Arena reset should report removed Warden count");
        helper.assertTrue(PrefallArchivesArenaService.inspectArena(level).ready(), "Arena shell should be ready after prepare");
        helper.assertTrue(level.getBlockState(PrefallArchivesArenaService.ARENA_CENTER.below()).is(Blocks.LAPIS_BLOCK),
                "Restore arena should use restore center block");

        helper.assertTrue(PrefallArchivesArenaService.spawnWardenIfMissing(level),
                "First Warden spawn should create one boss");
        helper.assertFalse(PrefallArchivesArenaService.spawnWardenIfMissing(level),
                "Second Warden spawn should be idempotent");
        helper.assertTrue(PrefallArchivesArenaService.getWardenCount(level) == 1,
                "Arena should contain exactly one living Warden");

        WardenBossEntity duplicate = ModEntities.WARDEN_BOSS.get().create(level, EntitySpawnReason.EVENT);
        helper.assertTrue(duplicate != null, "Duplicate Warden should be spawnable for cleanup test");
        if (duplicate != null) {
            duplicate.setPos(
                    PrefallArchivesArenaService.WARDEN_POS.getX() + 1.5D,
                    PrefallArchivesArenaService.WARDEN_POS.getY(),
                    PrefallArchivesArenaService.WARDEN_POS.getZ() + 1.5D);
            level.addFreshEntity(duplicate);
        }

        helper.assertTrue(PrefallArchivesArenaService.getWardenCount(level) >= 2,
                "Arena should see duplicate Warden before cleanup");
        int duplicateCleanup = PrefallArchivesArenaService.cleanupDuplicateWardens(level);
        helper.assertTrue(duplicateCleanup >= 1, "Duplicate cleanup should remove extra Wardens");
        helper.assertTrue(PrefallArchivesArenaService.getWardenCount(level) == 1,
                "Duplicate cleanup should leave one Warden");

        int resetCleanup = PrefallArchivesArenaService.resetArena(level, PostNexusData.NexusPath.DESTROY, true);
        helper.assertTrue(resetCleanup >= 1, "Reset should remove existing Warden before respawn");
        helper.assertTrue(PrefallArchivesArenaService.getWardenCount(level) == 1,
                "Reset with spawn should leave exactly one Warden");
        helper.assertTrue(level.getBlockState(PrefallArchivesArenaService.ARENA_CENTER.below()).is(Blocks.REDSTONE_BLOCK),
                "Destroy arena should use destroy center block");
        PrefallArchivesArenaService.removeAllWardens(level);
        helper.succeed();
    }

    private static void rareSchematicResearch(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ResearchData research = ResearchData.get(player);
        research.resetAll();

        ItemStack rare = new ItemStack(ModItems.RARE_TECH_SCHEMATIC.get());
        RareTechSchematicItem.DecodeResult decoded = RareTechSchematicItem.decodeAtResearchLab(player, rare);
        ResearchData decodedResearch = ResearchData.get(player);
        helper.assertTrue(decoded.consumed(), "Rare tech schematic should be consumed at a Research Lab");
        helper.assertTrue(rare.isEmpty(), "Rare tech schematic stack should shrink after decoding");
        helper.assertTrue(decoded.unlockedType() == SchematicFragmentItem.SchematicType.WEAPONS,
                "Rare tech schematic should unlock the first missing schematic branch in enum order");
        helper.assertTrue(decodedResearch.hasSchematic("weapons"), "Weapons schematic branch should unlock");
        helper.assertTrue(decodedResearch.getPoints() == RareTechSchematicItem.MISSING_CATEGORY_RP,
                "Rare tech schematic should award 75 RP when unlocking a missing branch");

        for (SchematicFragmentItem.SchematicType type : SchematicFragmentItem.SchematicType.values()) {
            decodedResearch.unlockSchematic(type.getDisplayName().toLowerCase(Locale.ROOT));
        }

        ItemStack duplicate = new ItemStack(ModItems.RARE_TECH_SCHEMATIC.get());
        RareTechSchematicItem.DecodeResult archived = RareTechSchematicItem.decodeAtResearchLab(player, duplicate);
        ResearchData archivedResearch = ResearchData.get(player);
        helper.assertTrue(archived.consumed(), "Duplicate rare tech schematic should be consumed");
        helper.assertTrue(archived.unlockedType() == null, "Duplicate rare tech schematic should not unlock a branch");
        helper.assertTrue(archivedResearch.getPoints()
                        == RareTechSchematicItem.MISSING_CATEGORY_RP + RareTechSchematicItem.DUPLICATE_ARCHIVE_RP,
                "Duplicate rare tech schematic should archive for 125 RP");
        helper.succeed();
    }

    private static void researchPerkGraph(GameTestHelper helper) {
        var perks = PerkRegistry.getAll();
        helper.assertFalse(perks.isEmpty(), "Research perk registry should not be empty");

        Set<String> ids = new HashSet<>();
        EnumSet<Perk.Branch> branches = EnumSet.noneOf(Perk.Branch.class);
        for (Perk perk : perks.values()) {
            helper.assertTrue(ids.add(perk.getId()), "Research perk id should be unique: " + perk.getId());
            helper.assertTrue(perk.getId().equals(perk.getId().toLowerCase(Locale.ROOT)),
                    "Research perk id should stay lowercase: " + perk.getId());
            helper.assertFalse(perk.getName().isBlank(), "Research perk name should not be blank: " + perk.getId());
            helper.assertFalse(perk.getDescription().isBlank(),
                    "Research perk description should not be blank: " + perk.getId());
            helper.assertTrue(perk.getCost() > 0, "Research perk cost should be positive: " + perk.getId());
            branches.add(perk.getBranch());

            for (String prerequisiteId : perk.getPrerequisites()) {
                Perk prerequisite = PerkRegistry.get(prerequisiteId);
                helper.assertTrue(prerequisite != null,
                        "Research prerequisite should exist: " + prerequisiteId + " for " + perk.getId());
                if (prerequisite != null) {
                    helper.assertTrue(prerequisite.getTier() < perk.getTier(),
                            "Research prerequisite should be an earlier tier: " + prerequisiteId + " -> " + perk.getId());
                }
            }
        }

        for (Perk.Branch branch : Perk.Branch.values()) {
            helper.assertTrue(branches.contains(branch), "Research branch should have perks: " + branch);
        }
        helper.succeed();
    }

    private static void structureExportPaths(GameTestHelper helper) {
        helper.assertTrue(ModStructuresCommand.isSafeStructureToken("cache_room"),
                "Lowercase export names should be accepted");
        helper.assertFalse(ModStructuresCommand.isSafeStructureToken("CacheRoom"),
                "Uppercase export names should be rejected");
        helper.assertFalse(ModStructuresCommand.isSafeStructureToken(".."),
                "Path traversal export names should be rejected");
        helper.assertFalse(ModStructuresCommand.isSafeStructureToken("bad/path"),
                "Path separator export names should be rejected");

        String globalPath = ModStructuresCommand.resolveStructureOutputPath("cache_room", "global")
                .toString()
                .replace('\\', '/');
        helper.assertTrue(globalPath.endsWith("data/echoashfallprotocol/structure/global/cache_room.nbt"),
                "Global exports should write under the singular structure resource path");
        String stalePluralPath = "data/echoashfallprotocol/" + "structures";
        helper.assertFalse(globalPath.contains(stalePluralPath),
                "Global exports should not write under the stale plural structures path");

        String biomePath = ModStructuresCommand.resolveStructureOutputPath("cache_room", "toxic_swamp")
                .toString()
                .replace('\\', '/');
        helper.assertTrue(biomePath.endsWith("data/echoashfallprotocol/structure/biomes/toxic_swamp/cache_room.nbt"),
                "Biome exports should write under the singular structure resource path");
        helper.succeed();
    }

    private static void starterDropPodTemplate(GameTestHelper helper) {
        var template = helper.getLevel().getStructureManager().get(id("drop_pod"));
        helper.assertTrue(template.isPresent(), "Starting drop pod NBT template should load");
        var size = template.orElseThrow().getSize();
        helper.assertTrue(size.getX() == 20 && size.getY() == 10 && size.getZ() == 20,
                "Starting drop pod should keep the curated 20x10x20 footprint");

        BlockPos origin = helper.absolutePos(new BlockPos(32, 4, 32));
        BlockPos spawn = ProceduralStructureGenerator.placeStartingDropPod(
                helper.getLevel(), origin, helper.getLevel().getRandom());
        helper.assertTrue(spawn != null, "Starting drop pod placement should return a safe spawn");
        if (spawn == null) {
            helper.fail("Starting drop pod placement returned null");
            return;
        }

        helper.assertTrue(helper.getLevel().getBlockState(spawn).isAir(),
                "Drop pod spawn feet block should be clear");
        helper.assertTrue(helper.getLevel().getBlockState(spawn.above()).isAir(),
                "Drop pod spawn head block should be clear");
        helper.assertFalse(helper.getLevel().getBlockState(spawn.below()).isAir(),
                "Drop pod spawn should stand on the pod floor");

        BlockPos placePos = origin.offset(-size.getX() / 2, -2, -size.getZ() / 2);
        helper.assertTrue(helper.getLevel().getBlockState(placePos.offset(5, 3, 10)).is(Blocks.WHITE_BED),
                "Curated drop pod should include the guaranteed bed foot");
        helper.assertTrue(helper.getLevel().getBlockState(placePos.offset(5, 3, 11)).is(Blocks.WHITE_BED),
                "Curated drop pod should include the guaranteed bed head");
        helper.assertTrue(countBlocks(helper, placePos, size, "echoashfallprotocol:drop_pod_hull") >= 80,
                "Curated drop pod should retain a readable hull shell");
        helper.assertTrue(countBlocks(helper, placePos, size, "minecraft:barrel") >= 4,
                "Curated drop pod should expose visible starter lockers");
        helper.assertTrue(countBlocks(helper, placePos, size, "minecraft:chest") >= 1,
                "Curated drop pod should retain starter cache storage");
        helper.assertTrue(countStarterPodProtectedPathClutter(helper, placePos) == 0,
                "Curated drop pod should keep spawn, lockers, bed route, terminal access, and ramp clear of debris clutter");
        helper.assertTrue(countStarterPodOffPathClutter(helper, placePos, size) > 0,
                "Curated drop pod should retain off-path decorative crash debris");
        helper.assertTrue(countInvalidBlockEntities(helper, placePos, size) == 0,
                "Curated drop pod placement should not leave block entities on air or non-entity blocks");
        helper.succeed();
    }

    private static void starterDropPodCorruptionGuard(GameTestHelper helper) {
        var template = helper.getLevel().getStructureManager().get(id("drop_pod"));
        helper.assertTrue(template.isPresent(), "Starting drop pod NBT template should load");
        Vec3i size = template.orElseThrow().getSize();

        BlockPos origin = helper.absolutePos(new BlockPos(64, 4, 64));
        BlockPos placePos = origin.offset(-size.getX() / 2, -2, -size.getZ() / 2);
        BlockPos staleClearPos = placePos.offset(9, 3, -1);
        helper.getLevel().setBlock(staleClearPos, Blocks.CHEST.defaultBlockState(), 3);
        helper.assertTrue(helper.getLevel().getBlockEntity(staleClearPos) != null,
                "Regression setup should create a stale-prone block entity before pod clearing");

        BlockPos spawn = ProceduralStructureGenerator.placeStartingDropPod(
                helper.getLevel(), origin, helper.getLevel().getRandom());
        helper.assertTrue(spawn != null, "Starting drop pod placement should still succeed with stale block entities nearby");
        helper.assertTrue(helper.getLevel().getBlockState(staleClearPos).isAir(),
                "Starting pod clear margin should leave stale setup block as air");
        helper.assertTrue(helper.getLevel().getBlockEntity(staleClearPos) == null,
                "Starting pod clear margin should remove stale block entity data before saving");

        helper.assertTrue(countInvalidBlockEntities(helper, placePos, size) == 0,
                "Drop pod placement should not leave block entities on air or non-entity blocks");
        helper.succeed();
    }

    private static void startingDropPodDataLenientLoad(GameTestHelper helper) {
        com.google.gson.JsonObject empty = new com.google.gson.JsonObject();
        StartingDropPodData emptyData = StartingDropPodData.CODEC.parse(JsonOps.INSTANCE, empty)
                .result()
                .orElse(null);
        helper.assertTrue(emptyData != null, "Starting drop pod data should load when pods is absent");

        com.google.gson.JsonObject malformedEntry = new com.google.gson.JsonObject();
        malformedEntry.addProperty("playerId", "not-a-uuid");
        com.google.gson.JsonArray pods = new com.google.gson.JsonArray();
        pods.add(malformedEntry);
        com.google.gson.JsonObject malformedRoot = new com.google.gson.JsonObject();
        malformedRoot.add("pods", pods);
        StartingDropPodData malformedData = StartingDropPodData.CODEC.parse(JsonOps.INSTANCE, malformedRoot)
                .result()
                .orElse(null);
        helper.assertTrue(malformedData != null,
                "Starting drop pod data should load and skip malformed player entries");
        helper.succeed();
    }

    private static void archiveReadState(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoIntel intel = EchoIntel.get(player);
        intel.discoverLore("terminal_overhaul_test", "Terminal Overhaul Test", "Readable archive content.");
        helper.assertTrue(intel.getUnreadCount() >= 1, "Discovered lore should create unread intel");
        intel.markAsRead("lore_terminal_overhaul_test");
        helper.assertTrue(intel.getAllIntel().stream()
                        .filter(entry -> entry.id.equals("lore_terminal_overhaul_test"))
                        .allMatch(entry -> entry.isRead),
                "Archive read-state clearing should mark selected intel read");
        helper.succeed();
    }

    private static void substrateGrinderRecipes(GameTestHelper helper) {
        List<Item> substrateInputs = List.of(
                Items.STONE,
                Items.COBBLESTONE,
                Items.DEEPSLATE,
                Items.COBBLED_DEEPSLATE,
                ModBlocks.WASTELAND_STONE.get().asItem(),
                ModBlocks.WASTELAND_TRACE_RUBBLE.get().asItem(),
                ModBlocks.RUBBLE.get().asItem(),
                ModBlocks.CONCRETE_RUBBLE.get().asItem(),
                ModBlocks.CONCRETE_CHUNK.get().asItem(),
                ModBlocks.INDUSTRIAL_AGGREGATE.get().asItem(),
                ModBlocks.OIL_STAINED_CONCRETE.get().asItem(),
                ModBlocks.CRASH_SLAG.get().asItem(),
                ModBlocks.ASH_STONE.get().asItem(),
                ModBlocks.DEEP_ASH.get().asItem(),
                ModBlocks.TOXIC_SLAGSTONE.get().asItem(),
                ModBlocks.IRRADIATED_SHALE.get().asItem(),
                ModBlocks.CRYOGENIC_FRACTURED_STONE.get().asItem(),
                ModBlocks.NEXUS_CRACKED_SOIL.get().asItem(),
                ModBlocks.RIFTSTONE.get().asItem()
        );
        List<Item> legacyInputs = List.of(
                ModItems.IRON_SHARD.get(),
                ModItems.COPPER_SHARD.get(),
                ModItems.COAL_DUST.get(),
                ModItems.GOLD_TRACE.get(),
                ModItems.GOLD_CLUSTER.get(),
                ModItems.URANIUM_SHARD.get()
        );

        for (Item input : substrateInputs) {
            helper.assertTrue(OreGrinderBlockEntity.hasSubstrateRecipe(new ItemStack(input)),
                    "Substrate Grinder missing recipe for " + BuiltInRegistries.ITEM.getKey(input));
        }
        for (Item input : legacyInputs) {
            helper.assertTrue(OreGrinderBlockEntity.hasSubstrateRecipe(new ItemStack(input)),
                    "Legacy grinder recipe missing for " + BuiltInRegistries.ITEM.getKey(input));
        }

        BlockPos grinderPos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlock(grinderPos, ModBlocks.ORE_GRINDER.get().defaultBlockState(), 3);
        helper.assertTrue(helper.getLevel().getBlockEntity(grinderPos) instanceof OreGrinderBlockEntity,
                "Substrate Grinder block entity should exist for insertion coverage");
        if (helper.getLevel().getBlockEntity(grinderPos) instanceof OreGrinderBlockEntity grinder) {
            helper.assertTrue(grinder.canInsertItem(OreGrinderBlockEntity.INPUT_SLOT_1,
                            new ItemStack(Items.STONE)),
                    "Hopper insertion should accept single valid substrate items before a full batch is present");
            helper.assertTrue(grinder.canInsertItem(OreGrinderBlockEntity.INPUT_SLOT_1,
                            new ItemStack(ModBlocks.WASTELAND_STONE.get())),
                    "Hopper insertion should accept biome substrate inputs");
            helper.assertFalse(grinder.canInsertItem(OreGrinderBlockEntity.OUTPUT_SLOT,
                            new ItemStack(ModBlocks.WASTELAND_STONE.get(), 3)),
                    "Hopper insertion should reject direct output-slot input");
            helper.assertTrue(grinder.canExtractItem(OreGrinderBlockEntity.BYPRODUCT_SLOT),
                    "Byproducts should be extractable only from the byproduct/output side");
            helper.assertTrue(grinder.getOutputSlots(Direction.DOWN).length == 2,
                    "Downward hopper extraction should expose output and byproduct slots");
            helper.assertTrue(grinder.getOutputSlots(Direction.NORTH).length == 0,
                    "Side hopper extraction should not pull grinder outputs");

            grinder.getInventory().setStackInSlot(OreGrinderBlockEntity.INPUT_SLOT_1,
                    new ItemStack(ModBlocks.WASTELAND_STONE.get(), 3));
            grinder.setEnergyStored(1_000);
            for (int tick = 0; tick < 120; tick++) {
                OreGrinderBlockEntity.serverTick(helper.getLevel(), grinderPos,
                        helper.getLevel().getBlockState(grinderPos), grinder);
            }
            ItemStack output = grinder.getInventory().getStackInSlot(OreGrinderBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(output.is(ModItems.IRON_SHARD.get()) && output.getCount() >= 2,
                    "Powered grinder should turn wasteland stone into iron shards");
        }

        for (OreGrinderBlockEntity.GrinderRecipe recipe : OreGrinderBlockEntity.getSubstrateRecipes().values()) {
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(recipe.output()) != null,
                    "Grinder recipe output must be registered for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
            if (recipe.byproduct() != null) {
                helper.assertTrue(BuiltInRegistries.ITEM.getKey(recipe.byproduct()) != null,
                        "Grinder recipe byproduct must be registered for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
                helper.assertTrue(recipe.byproductChance() > 0.0F && recipe.byproductChance() <= 1.0F,
                        "Grinder byproduct chance out of range for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
                helper.assertTrue(recipe.byproductCount() > 0,
                        "Grinder byproduct count must be positive for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
            }
            helper.assertTrue(recipe.inputCount() > 0 && recipe.outputCount() > 0,
                    "Grinder recipe counts must be positive for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
            helper.assertTrue(recipe.processTime() > 0 && recipe.powerPerOperation() > 0,
                    "Grinder recipe cost must be positive for " + BuiltInRegistries.ITEM.getKey(recipe.input()));
        }
        helper.succeed();
    }

    private static EchoCompanionDrone spawnCompanionDrone(GameTestHelper helper, Player owner, BlockPos relativePos) {
        EchoCompanionDrone drone = ModEntities.ECHO_COMPANION_DRONE.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(drone != null, "Companion drone should be spawnable");
        BlockPos pos = helper.absolutePos(relativePos);
        drone.setOwner(owner);
        drone.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(drone);
        return drone;
    }

    private static ScoutDrone spawnScoutDrone(GameTestHelper helper, Player owner, BlockPos relativePos) {
        ScoutDrone drone = ModEntities.SCOUT_DRONE.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(drone != null, "Scout Drone should be spawnable");
        BlockPos pos = helper.absolutePos(relativePos);
        drone.setOwner(owner);
        drone.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(drone);
        return drone;
    }

    private static void cleanupOwnedDrones(GameTestHelper helper, Player owner) {
        helper.getLevel().getEntitiesOfClass(EchoCompanionDrone.class, owner.getBoundingBox().inflate(256.0D),
                drone -> owner.getUUID().equals(drone.getOwnerUUID())).forEach(EchoCompanionDrone::discard);
        helper.getLevel().getEntitiesOfClass(ScoutDrone.class, owner.getBoundingBox().inflate(256.0D),
                drone -> owner.getUUID().equals(drone.getOwnerUUID())).forEach(ScoutDrone::discard);
    }

    private static int countItem(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countBlocks(GameTestHelper helper, BlockPos origin, Vec3i size, String blockId) {
        int count = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    Identifier id = BuiltInRegistries.BLOCK.getKey(
                            helper.getLevel().getBlockState(origin.offset(x, y, z)).getBlock());
                    if (id != null && id.toString().equals(blockId)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int countStarterPodProtectedPathClutter(GameTestHelper helper, BlockPos origin) {
        int count = 0;
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                if (!isStarterPodProtectedPathCell(x, z)) {
                    continue;
                }
                if (isStarterPodPathClutter(helper, origin.offset(x, 3, z))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countStarterPodOffPathClutter(GameTestHelper helper, BlockPos origin, Vec3i size) {
        int count = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                if (!isStarterPodProtectedPathCell(x, z)
                        && isStarterPodPathClutter(helper, origin.offset(x, 3, z))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isStarterPodProtectedPathCell(int localX, int localZ) {
        if (Math.abs(localX - 9) <= 1 && localZ >= 7 && localZ <= 12) {
            return true;
        }
        if ((localX == 6 || localX == 7 || localX == 12 || localX == 13) && localZ >= 6 && localZ <= 7) {
            return true;
        }
        if (localX >= 6 && localX <= 9 && localZ >= 10 && localZ <= 12) {
            return true;
        }
        return Math.abs(localX - 9) <= 2 && localZ >= 15 && localZ <= 19;
    }

    private static boolean isStarterPodPathClutter(GameTestHelper helper, BlockPos pos) {
        var state = helper.getLevel().getBlockState(pos);
        return state.is(ModBlocks.RUSTED_METAL_DEBRIS.get())
                || state.is(ModBlocks.CABLE_BUNDLE.get())
                || state.is(ModBlocks.TWISTED_METAL.get())
                || Identifier.fromNamespaceAndPath("minecraft", "chain")
                        .equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))
                || state.is(Blocks.STONE_BUTTON);
    }

    private static int countInvalidBlockEntities(GameTestHelper helper, BlockPos origin, Vec3i size) {
        int count = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (helper.getLevel().getBlockEntity(pos) == null) {
                        continue;
                    }
                    var state = helper.getLevel().getBlockState(pos);
                    if (state.isAir() || !state.hasBlockEntity()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static List<EntityType<? extends Entity>> allAshfallTypes() {
        return List.of(
                ModEntities.RAD_ZOMBIE.get(),
                ModEntities.SCAVENGER_BANDIT.get(),
                ModEntities.IRRADIATED_WOLF.get(),
                ModEntities.ECHO_DRONE.get(),
                ModEntities.SCOUT_DRONE.get(),
                ModEntities.ECHO_COMPANION_DRONE.get(),
                ModEntities.GLOWING_GHOUL.get(),
                ModEntities.ASH_WRAITH.get(),
                ModEntities.TOXIC_SLIME.get(),
                ModEntities.CITY_STALKER.get(),
                ModEntities.RUST_WALKER.get(),
                ModEntities.STEAM_WRAITH.get(),
                ModEntities.MUTATED_CRAWLER.get(),
                ModEntities.WILD_DOG.get(),
                ModEntities.FERAL_HUMAN.get(),
                ModEntities.CRASH_SURVIVOR.get(),
                ModEntities.FACTION_NPC.get(),
                ModEntities.GRIDBOUND_HUSK.get(),
                ModEntities.RELAY_WARDEN.get(),
                ModEntities.SIGNAL_LEECH.get(),
                ModEntities.NEXUS_NULLIFIER.get(),
                ModEntities.WARDEN_BOSS.get(),
                ModEntities.WASTELAND_SENTINEL.get(),
                ModEntities.CRASH_ZONE_COLOSSUS.get(),
                ModEntities.CRYOGENIC_OVERSEER.get(),
                ModEntities.INDUSTRIAL_JUGGERNAUT.get(),
                ModEntities.NEXUS_SCAR_AVATAR.get(),
                ModEntities.RADIATION_BEHEMOTH.get(),
                ModEntities.CITY_RUIN_STALKER.get(),
                ModEntities.PLAINS_WARLORD.get(),
                ModEntities.TOXIC_HIVE_MATRIARCH.get(),
                ModEntities.CORRUPTION_BLOOM.get(),
                ModEntities.SEVERANCE_ENGINE.get(),
                ModEntities.MIRROR_COMMAND.get()
        );
    }

    private static void smokeEntity(GameTestHelper helper, EntityType<? extends Entity> type) {
        Entity entity = type.create(helper.getLevel(), EntitySpawnReason.EVENT);
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        helper.assertTrue(entity != null, "Entity should spawn for smoke test: " + entityId);
        if (entity != null) {
            BlockPos pos = helper.absolutePos(new BlockPos(6, 2, 6));
            entity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(entity);
            entity.tick();
            if (entity instanceof Mob mob) {
                helper.assertTrue(mob.getAttribute(Attributes.MAX_HEALTH) != null,
                        "Smoke mob should have health attribute: " + entityId);
                helper.assertTrue(mob.getAttribute(Attributes.ATTACK_DAMAGE) != null,
                        "Smoke mob should have attack damage attribute: " + entityId);
            }
            entity.discard();
        }
    }

    private static List<EntityType<? extends Entity>> attackingTypes() {
        return List.of(
                ModEntities.RAD_ZOMBIE.get(),
                ModEntities.SCAVENGER_BANDIT.get(),
                ModEntities.IRRADIATED_WOLF.get(),
                ModEntities.ECHO_DRONE.get(),
                ModEntities.SCOUT_DRONE.get(),
                ModEntities.ECHO_COMPANION_DRONE.get(),
                ModEntities.GLOWING_GHOUL.get(),
                ModEntities.ASH_WRAITH.get(),
                ModEntities.TOXIC_SLIME.get(),
                ModEntities.CITY_STALKER.get(),
                ModEntities.RUST_WALKER.get(),
                ModEntities.STEAM_WRAITH.get(),
                ModEntities.MUTATED_CRAWLER.get(),
                ModEntities.WILD_DOG.get(),
                ModEntities.FERAL_HUMAN.get(),
                ModEntities.GRIDBOUND_HUSK.get(),
                ModEntities.RELAY_WARDEN.get(),
                ModEntities.SIGNAL_LEECH.get(),
                ModEntities.NEXUS_NULLIFIER.get(),
                ModEntities.WARDEN_BOSS.get(),
                ModEntities.WASTELAND_SENTINEL.get(),
                ModEntities.CRASH_ZONE_COLOSSUS.get(),
                ModEntities.CRYOGENIC_OVERSEER.get(),
                ModEntities.INDUSTRIAL_JUGGERNAUT.get(),
                ModEntities.NEXUS_SCAR_AVATAR.get(),
                ModEntities.RADIATION_BEHEMOTH.get(),
                ModEntities.CITY_RUIN_STALKER.get(),
                ModEntities.PLAINS_WARLORD.get(),
                ModEntities.TOXIC_HIVE_MATRIARCH.get(),
                ModEntities.CORRUPTION_BLOOM.get(),
                ModEntities.SEVERANCE_ENGINE.get(),
                ModEntities.MIRROR_COMMAND.get()
        );
    }

    private static void missionUxSummary(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        QuestData quest = QuestData.get(player);
        Mission first = MissionRegistry.getMission(0, 0);
        helper.assertTrue(first != null, "First mission should exist for UX summary coverage");
        if (first == null) {
            helper.succeed();
            return;
        }

        setCurrentMission(quest, first.id());
        MissionUxSummary active = MissionUxSummary.of(player, quest, first);
        helper.assertTrue(!active.shortTitle().isBlank(), "Active mission should expose a short title");
        helper.assertTrue(!active.nextStep().isBlank(), "Active mission should expose a next step");
        helper.assertTrue(!active.tags().isEmpty(), "Active mission should expose display tags");
        helper.assertTrue(!active.relatedIntelKey().isBlank(), "Active mission should expose related intel");
        helper.assertTrue("ACTIVE".equals(active.statusLabel()), "Current unlocked mission should display ACTIVE");

        int covered = 0;
        boolean sawLocked = false;
        for (int phase = 0; phase < MissionRegistry.getPhaseCount(); phase++) {
            for (Mission mission : MissionRegistry.getMissionsForPhase(phase)) {
                MissionUxSummary summary = MissionUxSummary.of(player, quest, mission);
                helper.assertTrue(!summary.shortTitle().isBlank(), "Mission UX title should not be blank: " + mission.id());
                helper.assertTrue(!summary.nextStep().isBlank(), "Mission UX next step should not be blank: " + mission.id());
                helper.assertTrue(!summary.statusLabel().isBlank(), "Mission UX status should not be blank: " + mission.id());
                sawLocked = sawLocked || "LOCKED".equals(summary.statusLabel()) || "VIEW".equals(summary.statusLabel());
                covered++;
            }
        }
        helper.assertTrue(covered == MissionRegistry.getAllMissions().size(),
                "Mission UX coverage should inspect every registered mission");
        helper.assertTrue(sawLocked, "Mission UX coverage should include a locked or view-only state");

        quest.completeMission(first.id(), List.of());
        MissionUxSummary done = MissionUxSummary.of(player, quest, first);
        helper.assertTrue("DONE".equals(done.statusLabel()), "Completed mission without pending rewards should display DONE");
        helper.succeed();
    }

    private static void endgameRouteProgress(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        QuestData quest = QuestData.get(player);
        PostNexusData post = PostNexusData.get(player);

        post.setSelectedPath(PostNexusData.NexusPath.CONTROL);
        post.addDenseAlloy(12);
        post.addNexusCrystals(49);
        post.addEnergyCells(2);
        player.getInventory().add(new ItemStack(ModItems.DENSE_ALLOY_CHUNK.get(), 50));
        player.getInventory().add(new ItemStack(ModItems.ENERGY_CELL.get(), 50));

        EndgameMissionProgress.Snapshot resources = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "control_resource_dominance"))
                .orElseThrow(() -> new IllegalStateException("Control resource progress should be exposed"));
        helper.assertTrue(resources.entries().size() == 3,
                "Control resources should expose one terminal counter per resource");
        helper.assertTrue(resources.entries().get(0).have() == PostNexusData.CONTROL_DENSE_ALLOY_REQUIRED,
                "Dense Alloy progress should use the greater held or tracked count");
        helper.assertTrue(resources.entries().get(1).have() == 49,
                "Nexus Crystal progress should keep tracked pickup count when held count is lower");
        helper.assertTrue(resources.entries().get(2).have() == PostNexusData.CONTROL_ENERGY_CELLS_REQUIRED,
                "Energy Cell progress should use held inventory fallback");
        helper.assertTrue(resources.firstOpenStep().contains("Nexus Crystals"),
                "Control resource next step should name the missing resource");

        post.setSelectedPath(PostNexusData.NexusPath.RESTORE);
        post.incrementNodesRepaired();
        post.incrementNodesRepaired();
        EndgameMissionProgress.Snapshot repairNodes = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "restore_repair_nodes"))
                .orElseThrow(() -> new IllegalStateException("Restore node progress should be exposed"));
        helper.assertTrue(repairNodes.entries().get(0).have() == 2,
                "Restore node counter should expose the tracked node count");
        helper.assertTrue(repairNodes.firstOpenStep().contains("Power Nodes"),
                "Restore node next step should point at Power Nodes");

        EndgameMissionProgress.Snapshot storm = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "destroy_survive_storms"))
                .orElseThrow(() -> new IllegalStateException("Destroy storm progress should be exposed"));
        helper.assertTrue(storm.entries().get(0).have() == 0,
                "Destroy storm counter should start at zero before credit");
        helper.assertTrue(PostNexusEventHandler.isDestroyRouteStormCreditEvent(
                        EnvironmentalEventType.RADIATION_STORM, false),
                "Destroy storm mission should credit radiation storms");
        helper.assertTrue(PostNexusEventHandler.isDestroyRouteStormCreditEvent(
                        EnvironmentalEventType.ASH_STORM, false),
                "Destroy storm mission should credit ash storms");
        helper.assertTrue(PostNexusEventHandler.isDestroyRouteStormCreditEvent(
                        EnvironmentalEventType.NEXUS_SURGE, false),
                "Destroy storm mission should credit Nexus surges");
        helper.assertTrue(PostNexusEventHandler.isDestroyRouteStormCreditEvent(
                        EnvironmentalEventType.NONE, true),
                "Destroy storm mission should credit thunder even without a custom event");
        helper.assertTrue(!PostNexusEventHandler.isDestroyRouteStormCreditEvent(
                        EnvironmentalEventType.TOXIC_STORM, false),
                "Destroy storm mission should not credit unrelated clear-weather events");

        NexusCampaignData campaign = NexusCampaignData.get(helper.getLevel().getServer().overworld());
        campaign.resetForTests();
        campaign.awaken(helper.absolutePos(new BlockPos(1, 2, 1)));
        campaign.scanRelays();
        EndgameMissionProgress.Snapshot relayScan = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "scan_prime_relays"))
                .orElseThrow(() -> new IllegalStateException("Prime Relay scan progress should be exposed"));
        helper.assertTrue(relayScan.entries().get(0).have() == NexusCampaignData.REQUIRED_RELAY_SCAN_COUNT,
                "Prime Relay scan progress should read world campaign data");

        campaign.resolveRelay(NexusRelayType.REACTOR, NexusRelayState.STABILIZED);
        campaign.resolveRelay(NexusRelayType.CRYO, NexusRelayState.SEVERED);
        EndgameMissionProgress.Snapshot relayResolve = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "resolve_prime_relays"))
                .orElseThrow(() -> new IllegalStateException("Prime Relay resolve progress should be exposed"));
        helper.assertTrue(relayResolve.entries().get(0).have() == 2,
                "Prime Relay resolution progress should expose resolved relay count");

        campaign.resolveRelay(NexusRelayType.BIO, NexusRelayState.OVERRIDDEN);
        campaign.markSiegeComplete();
        EndgameMissionProgress.Snapshot siege = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "survive_core_countermeasure"))
                .orElseThrow(() -> new IllegalStateException("Core siege progress should be exposed"));
        helper.assertTrue(siege.entries().get(0).satisfied(),
                "Core siege progress should read world campaign siege credit");

        post.incrementPathOperationsComplete();
        EndgameMissionProgress.Snapshot operation = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "control_command_lattice"))
                .orElseThrow(() -> new IllegalStateException("Path operation progress should be exposed"));
        helper.assertTrue(operation.entries().get(0).satisfied(),
                "Post-Warden path operation should expose player counter credit");
        post.setFinalBossDefeated(true);
        EndgameMissionProgress.Snapshot finale = EndgameMissionProgress
                .forMission(player, quest, requireMission(helper, "control_finale"))
                .orElseThrow(() -> new IllegalStateException("Path finale progress should be exposed"));
        helper.assertTrue(finale.entries().get(0).satisfied(),
                "Path finale should expose final boss credit");
        campaign.resetForTests();
        helper.succeed();
    }

    private static void terminalLoreTaxonomy(GameTestHelper helper) {
        try {
            Class<?> tabClass = Class.forName(
                    "com.knoxhack.echoashfallprotocol.integration.AshfallTerminalIntegration$AshfallTab");
            Method phaseTitle = tabClass.getDeclaredMethod("phaseTitle", int.class);
            phaseTitle.setAccessible(true);
            String[] expected = {
                    "PODFALL",
                    "OUTPOST SURVIVAL",
                    "LIFE SUPPORT",
                    "SIGNAL CONTACT",
                    "BIOHAZARD ADAPTATION",
                    "DEEP EXTRACTION",
                    "GRID RESTORATION",
                    "NEXUS DECISION",
                    "AFTERMATH PROTOCOL"
            };
            for (int i = 0; i < expected.length; i++) {
                Object actual = phaseTitle.invoke(null, i);
                helper.assertTrue(expected[i].equals(actual),
                        "Ashfall phase " + (i + 1) + " should render as " + expected[i]);
            }
            helper.succeed();
        } catch (ReflectiveOperationException error) {
            helper.assertTrue(false, "Ashfall terminal taxonomy reflection failed: " + error.getMessage());
        }
    }

    private static void terminalCommandDeckOwnership(GameTestHelper helper) {
        if (TerminalTabRegistry.tabs().isEmpty()) {
            helper.succeed();
            return;
        }
        Identifier commandDeck = Identifier.fromNamespaceAndPath("echoterminal", "overview");
        Identifier ashfallCommand = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "overview");
        boolean hasBuiltInCommandDeck = TerminalTabRegistry.tabs().stream()
                .anyMatch(tab -> commandDeck.equals(tab.descriptor().id())
                        && "COMMAND DECK".equals(tab.descriptor().title()));
        boolean hasAshfallCommand = TerminalTabRegistry.tabs().stream()
                .anyMatch(tab -> ashfallCommand.equals(tab.descriptor().id())
                        && "ASHFALL COMMAND".equals(tab.descriptor().title()));
        if (!hasBuiltInCommandDeck) {
            helper.succeed();
            return;
        }
        helper.assertTrue(hasBuiltInCommandDeck,
                "Ashfall must not overwrite the built-in echoterminal:overview Command Deck action hub");
        helper.assertTrue(hasAshfallCommand,
                "Ashfall active protocol overview should live on its own addon-owned tab id");
        TerminalNavigationProfiles.profile(commandDeck).ifPresent(profile ->
                helper.assertTrue(profile.section() == TerminalNavigationSection.TERMINAL,
                        "Built-in Command Deck should stay in Terminal navigation"));
        TerminalNavigationProfiles.profile(ashfallCommand).ifPresent(profile ->
                helper.assertTrue("ashfall".equals(profile.chapterId()),
                        "Ashfall command overview should be grouped under the Ashfall chapter"));
        helper.succeed();
    }

    private static void terminalCommonRegistration(GameTestHelper helper) {
        AshfallTerminalCommonIntegration.register();
        Identifier missions = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "missions");
        Identifier sideOps = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "ashfall_side_ops");
        if (ModList.get().isLoaded("echomissioncore")) {
            helper.assertTrue(TerminalMissionRegistry.provider(Identifier.fromNamespaceAndPath("echomissioncore", "missions")).isPresent(),
                    "MissionCore should own the shared mission feed when loaded");
        } else {
            helper.assertTrue(TerminalMissionRegistry.provider(Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "ashfall_protocol")).isPresent(),
                    "Ashfall common setup should register the main mission provider");
            helper.assertTrue(TerminalMissionRegistry.provider(sideOps).isPresent(),
                    "Ashfall common setup should register the side ops mission provider");
        }
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        if (player instanceof ServerPlayer serverPlayer) {
            helper.assertTrue(TerminalActionRegistry.handle(serverPlayer, missions,
                    TerminalMissionActions.MISSION_ACTION, "invalid"),
                    "Ashfall common setup should register server-side terminal mission actions");
        }
        helper.succeed();
    }

    private static void missionGuideCoverage(GameTestHelper helper) {
        int covered = 0;
        for (Mission mission : MissionRegistry.getAllMissions()) {
            helper.assertTrue(MissionGuideRegistry.hasGuide(mission.id()),
                    "Every required Ashfall mission needs a field guide: " + mission.id());
            MissionGuideRegistry.Guide guide = MissionGuideRegistry.get(mission.id());
            helper.assertTrue(!guide.title().isBlank(), "Mission guide title should not be blank: " + mission.id());
            helper.assertTrue(!guide.body().isBlank(), "Mission guide body should not be blank: " + mission.id());
            helper.assertTrue(!guide.body().contains("no field guide"),
                    "Mission guide should not expose fallback copy: " + mission.id());
            covered++;
        }
        helper.assertTrue(covered == MissionRegistry.getAllMissions().size(),
                "Mission guide coverage should inspect every required mission");
        helper.succeed();
    }

    private static void firstNightRouteSafety(GameTestHelper helper) {
        String[] phase0Order = {
                "secure_crash_outpost",
                "craft_scrap_knife",
                "drink_clean_water",
                "get_dirty_water",
                "emergency_filter_water"
        };
        List<Mission> phase0 = MissionRegistry.getMissionsForPhase(0);
        helper.assertTrue(phase0.size() >= phase0Order.length,
                "Phase 0 should retain the full first-night crash route");
        for (int i = 0; i < phase0Order.length; i++) {
            helper.assertTrue(phase0Order[i].equals(phase0.get(i).id()),
                    "Phase 0 first-night route changed at index " + i);
        }

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        QuestData quest = QuestData.get(player);
        String[] earlyRoute = {
                "secure_crash_outpost",
                "craft_scrap_knife",
                "drink_clean_water",
                "get_dirty_water",
                "emergency_filter_water",
                "forage_wasteland_food",
                "plant_mutated_sapling",
                "build_rain_collector",
                "stockpile_rations",
                "secure_sleep_shelter",
                "craft_bone_knife",
                "craft_crude_spear",
                "craft_hide_wrap",
                "find_schematic_fragment",
                "build_hand_recycler",
                "make_machine_casing",
                "build_micro_generator",
                "build_water_purifier",
                "stockpile_clean_water"
        };
        for (String missionId : earlyRoute) {
            Mission mission = requireMission(helper, missionId);
            setCurrentMission(quest, mission.id());
            MissionUxSummary summary = MissionUxSummary.of(player, quest, mission);
            MissionGuideRegistry.Guide guide = MissionGuideRegistry.get(mission.id());
            helper.assertTrue(!summary.shortTitle().isBlank(),
                    "First-night mission should expose a short title: " + mission.id());
            helper.assertTrue(!summary.nextStep().isBlank(),
                    "First-night mission should expose a next step: " + mission.id());
            helper.assertTrue(!guide.title().isBlank(),
                    "First-night mission should expose a guide title: " + mission.id());
            helper.assertTrue(!guide.body().isBlank(),
                    "First-night mission should expose guide copy: " + mission.id());
        }

        Mission first = requireMission(helper, "secure_crash_outpost");
        helper.assertTrue("Anchor Pod Outpost".equals(first.objectiveText()),
                "First mission should display the pod anchor objective");
        helper.assertTrue(requireMission(helper, "forage_wasteland_food").objectiveText().contains("Food Buffer"),
                "Food mission should display as a buffer confirmation");
        helper.assertTrue(requireMission(helper, "stockpile_rations").objectiveText().contains("Ration Buffer"),
                "Ration mission should display as a buffer confirmation");

        assertRewardAtLeast(helper, requireMission(helper, "plant_mutated_sapling"), Items.CAULDRON, 1);
        assertRewardAtLeast(helper, requireMission(helper, "plant_mutated_sapling"), ModItems.SCRAP_PLASTIC.get(), 8);
        assertRewardAtLeast(helper, requireMission(helper, "secure_sleep_shelter"), ModItems.ANIMAL_BONE.get(), 2);
        assertRewardAtLeast(helper, requireMission(helper, "secure_sleep_shelter"), ModItems.ANIMAL_HIDE.get(), 4);
        assertRewardAtLeast(helper, requireMission(helper, "craft_hide_wrap"), ModItems.SCHEMATIC_FRAGMENT.get(), 1);
        assertRewardAtLeast(helper, requireMission(helper, "find_schematic_fragment"), ModItems.MACHINE_CASING.get(), 1);
        assertRewardAtLeast(helper, requireMission(helper, "build_hand_recycler"), ModItems.SCRAP_METAL.get(), 12);
        assertRewardAtLeast(helper, requireMission(helper, "build_hand_recycler"), ModItems.SCRAP_WIRE.get(), 6);
        assertRequiredCount(helper, requireMission(helper, "build_hand_recycler"), ModItems.MACHINE_CASING.get(), 1);
        assertRequiredCount(helper, requireMission(helper, "build_hand_recycler"), ModItems.SCRAP_METAL.get(), 4);
        assertRequiredCount(helper, requireMission(helper, "build_hand_recycler"), ModItems.SCRAP_WIRE.get(), 4);
        assertRequiredCount(helper, requireMission(helper, "build_micro_generator"), ModItems.MACHINE_CASING.get(), 1);
        assertRequiredCount(helper, requireMission(helper, "build_micro_generator"), ModItems.SCRAP_WIRE.get(), 3);
        assertRequiredCount(helper, requireMission(helper, "build_water_purifier"), ModItems.MACHINE_CASING.get(), 3);
        assertRewardAtLeast(helper, requireMission(helper, "build_water_purifier"), ModItems.FILTER_CARTRIDGE_BASIC.get(), 2);
        assertRewardAtLeast(helper, requireMission(helper, "build_water_purifier"), ModItems.DIRTY_WATER_BOTTLE.get(), 2);
        helper.assertTrue(MissionGuideRegistry.get("build_water_purifier").body().contains("three machine casings"),
                "Water purifier guide should describe the first-hour casing cost");
        helper.succeed();
    }

    private static void explorationSiteProfiles(GameTestHelper helper) {
        List<String> warnings = ExplorationSiteRegistry.validationWarnings();
        helper.assertTrue(warnings.isEmpty(),
                "Exploration site registry warnings: " + String.join("; ", warnings));

        for (ExplorationSiteRegistry.SiteProfile profile : ExplorationSiteRegistry.all()) {
            helper.assertTrue(!profile.displayName().isBlank(), "POI display name missing: " + profile.id());
            helper.assertTrue(!profile.route().isBlank(), "POI route missing: " + profile.id());
            helper.assertTrue(!profile.description().isBlank(), "POI intel missing: " + profile.id());
            helper.assertTrue(!profile.prepHint().isBlank(), "POI prep hint missing: " + profile.id());
            helper.assertTrue(!profile.resourceProfile().isBlank(), "POI resource profile missing: " + profile.id());
            helper.assertTrue(!profile.objective().isBlank(), "POI objective missing: " + profile.id());
            helper.assertTrue(!profile.rewardTrack().isBlank(), "POI reward track missing: " + profile.id());
            helper.assertTrue(!profile.structureIds().isEmpty(), "POI structure ids missing: " + profile.id());
            helper.assertTrue(profile.hazardProfile() != ExplorationSiteRegistry.HazardProfile.UNKNOWN,
                    "Registered POI should not use fallback hazard: " + profile.id());
        }

        TagKey<Structure> poiStructures = TagKey.create(Registries.STRUCTURE, id("poi_structures"));
        var structures = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.STRUCTURE)
                .getOrThrow(poiStructures);
        int taggedStructures = 0;
        for (Holder<Structure> holder : structures) {
            ResourceKey<Structure> key = holder.unwrapKey().orElse(null);
            helper.assertTrue(key != null, "Tagged POI structure should have a registry key");
            if (key == null) {
                continue;
            }
            String structureId = key.identifier().getPath();
            ExplorationSiteRegistry.SiteProfile profile = ExplorationSiteRegistry.findByStructure(structureId).orElse(null);
            helper.assertTrue(profile != null,
                    "Tagged POI structure should resolve to a scanner profile: " + structureId);
            if (profile != null) {
                helper.assertTrue(profile.structureIds().contains(structureId),
                        "Tagged POI structure should be explicitly listed by its profile: " + structureId);
            }
            taggedStructures++;
        }
        helper.assertTrue(taggedStructures > 0, "POI structure tag should not be empty");
        helper.succeed();
    }

    private static void ashfallDiscoveryProvider(GameTestHelper helper) {
        if (EchoCoreServices.factionDefinitions().stream().noneMatch(definition -> AshfallFactionMap.isAshfall(definition.id()))) {
            AshfallBiomeFactions.register();
        }
        List<EchoDiscoveryEntry> entries = new AshfallDiscoveryProvider().entries(null);
        helper.assertTrue(!entries.isEmpty(), "Ashfall discovery provider should publish entries");
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.category() == EchoDiscoveryCategory.STRUCTURE),
                "Ashfall discovery provider should publish structure entries");
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.category() == EchoDiscoveryCategory.BIOME),
                "Ashfall discovery provider should publish biome entries");
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.category() == EchoDiscoveryCategory.GUARDIAN),
                "Ashfall discovery provider should publish guardian entries");
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.category() == EchoDiscoveryCategory.EVENT),
                "Ashfall discovery provider should publish event entries");
        helper.assertTrue(entries.stream().noneMatch(entry -> entry.category() == EchoDiscoveryCategory.FACTION),
                "Ashfall discovery provider should leave faction entries to the shared Terminal provider");
        List<EchoDiscoveryEntry> terminalEntries = new TerminalDiscoveryProvider().entries(null);
        Set<Identifier> discoveryIds = new HashSet<>();
        for (EchoDiscoveryEntry entry : entries) {
            helper.assertTrue(discoveryIds.add(entry.id()), "Ashfall discovery entry ids should be unique: " + entry.id());
        }
        for (EchoDiscoveryEntry entry : terminalEntries) {
            helper.assertTrue(discoveryIds.add(entry.id()),
                    "Ashfall discovery ids should not duplicate shared Terminal entries: " + entry.id());
        }
        helper.assertTrue(entries.stream().allMatch(entry -> entry.id() != null
                        && entry.chapterId() != null
                        && !entry.revealedTitle().isBlank()
                        && !entry.lockedHintTitle().isBlank()
                        && !entry.hintText().isBlank()
                        && !entry.revealedSummary().isBlank()),
                "Every Ashfall discovery entry should have stable id and nonblank spoiler-safe copy");
        helper.assertTrue(entries.stream().anyMatch(entry -> entry.id().equals(AshfallDiscoveryProvider.biomeId("the_wasteland"))),
                "The main Wasteland biome should remain discoverable as a biome entry");
        helper.succeed();
    }

    private static void factionContractBalance(GameTestHelper helper) {
        if (EchoCoreServices.factionDefinitions().stream().noneMatch(definition -> AshfallFactionMap.isAshfall(definition.id()))) {
            AshfallBiomeFactions.register();
        }
        long activeAshfallDefinitions = EchoCoreServices.factionDefinitions().stream()
                .filter(definition -> AshfallFactionMap.all().contains(definition.id()))
                .count();
        helper.assertTrue(activeAshfallDefinitions == 3,
                "Echo Core should expose exactly three active Ashfall faction definitions");

        List<EchoFactionDefinition> definitions = AshfallFactionMap.all().stream()
                .map(factionId -> EchoCoreServices.factionDefinition(factionId).orElse(null))
                .toList();
        helper.assertTrue(definitions.stream().allMatch(java.util.Objects::nonNull),
                "All three Ashfall Echo Core factions should be registered");
        helper.assertTrue(definitions.size() == 3, "Ashfall should expose exactly three Echo Core factions");

        int contractCount = 0;
        for (EchoFactionDefinition definition : definitions) {
            helper.assertTrue(definition.contracts().size() == 3,
                    "Faction should have field/trusted/aligned contracts: " + definition.id());
            helper.assertTrue(definition.contracts().stream().map(EchoFactionContract::requiredReputation).toList()
                            .equals(List.of(0, 35, 75)),
                    "Contract reputation tiers should be 0/35/75: " + definition.id());
            for (EchoFactionContract contract : definition.contracts()) {
                AshfallFactionContracts.Spec spec = AshfallFactionContracts.spec(contract.id()).orElse(null);
                helper.assertTrue(spec != null, "Ashfall contract spec should exist: " + contract.id());
                if (spec != null) {
                    helper.assertTrue(!spec.objectives().isEmpty(),
                            "Ashfall contract should have objectives: " + contract.id());
                    helper.assertTrue(spec.reputationReward() > 0,
                            "Ashfall contract should grant reputation: " + contract.id());
                    contractCount++;
                }
            }
        }
        helper.assertTrue(contractCount == 9, "Ashfall should expose nine Echo Core contracts");

        assertContractHasObjective(helper, "crashbreak_salvage_field_contract",
                AshfallFactionContracts.ObjectiveType.POI_DISCOVERY);
        assertContractHasObjective(helper, "radwarden_compact_aligned_contract",
                AshfallFactionContracts.ObjectiveType.REPAIR);
        assertContractHasObjective(helper, "sporebound_sanctum_aligned_contract",
                AshfallFactionContracts.ObjectiveType.REPAIR);

        helper.assertTrue("industrial_factory".equals(ExplorationSiteRegistry.normalize("derelict_workshop")),
                "Legacy derelict workshop alias should still resolve to industrial factory profile");
        helper.succeed();
    }

    private static void strictFactionEntityIds(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(id("faction_npc")),
                "Generic faction_npc entity id should remain registered");
        assertRetiredEntityIdAbsent(helper, "remnant", "soldier");
        assertRetiredEntityIdAbsent(helper, "salvager", "trader");
        assertRetiredEntityIdAbsent(helper, "mutant", "creature");
        helper.succeed();
    }

    private static void machineWearSavedData(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        MachineWearData wearData = new MachineWearData(helper.getLevel());
        wearData.setWear(pos, 73);
        wearData.setJammed(pos, true);

        MachineWearSavedData saved = MachineWearSavedData.get(helper.getLevel());
        JsonElement encoded = MachineWearSavedData.CODEC.encodeStart(JsonOps.INSTANCE, saved)
                .result()
                .orElseThrow(() -> new IllegalStateException("Machine wear saved data should encode"));
        MachineWearSavedData decoded = MachineWearSavedData.CODEC.parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow(() -> new IllegalStateException("Machine wear saved data should decode"));

        helper.assertTrue(decoded.getWear(pos) == 73, "Machine wear should survive saved-data serialization");
        helper.assertTrue(decoded.isJammed(pos), "Machine jam state should survive saved-data serialization");
        helper.assertTrue(new MachineWearData(helper.getLevel()).getWear(pos) == 73,
                "Machine wear API should read world-saved state");
        wearData.repair(pos, 100);
        helper.assertFalse(MachineWearSavedData.get(helper.getLevel()).isJammed(pos),
                "Repair should clear jam state when wear reaches zero");
        helper.succeed();
    }

    private static void debugCommandPermissionGates(GameTestHelper helper) {
        CommandSourceStack nonOp = commandSource(helper, LevelBasedPermissionSet.MODERATOR);
        CommandSourceStack op = commandSource(helper, LevelBasedPermissionSet.GAMEMASTER);

        helper.assertFalse(ModStructuresCommand.hasCommandPermission(nonOp),
                "Structure export command should reject non-OP command source");
        helper.assertFalse(StructureGenCommand.hasCommandPermission(nonOp),
                "POI generation command should reject non-OP command source");
        helper.assertTrue(ModStructuresCommand.hasCommandPermission(op),
                "Structure export command should allow OP/dev command source");
        helper.assertTrue(StructureGenCommand.hasCommandPermission(op),
                "POI generation command should allow OP/dev command source");
        helper.succeed();
    }

    private static CommandSourceStack commandSource(GameTestHelper helper, LevelBasedPermissionSet permissions) {
        return new CommandSourceStack(
                CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                helper.getLevel(),
                permissions,
                "gametest",
                Component.literal("gametest"),
                helper.getLevel().getServer(),
                null);
    }

    private static void nexusUpgradeDataPath(GameTestHelper helper) {
        ItemStack blade = new ItemStack(ModItems.NEXUS_BLADE.get());
        com.knoxhack.echoashfallprotocol.item.upgrade.GearUpgradeHandler.setUpgradeLevel(blade, 2);

        helper.assertTrue(com.knoxhack.echoashfallprotocol.item.GearUpgradeHandler.getUpgradeLevel(blade) == 2,
                "Right-click compatibility handler should read nexus_upgrades");
        helper.assertTrue(com.knoxhack.echoashfallprotocol.item.upgrade.GearUpgradeHandler.getBonusDamage(blade) >= 2.0F,
                "Nexus upgrade level should increase weapon damage");
        helper.assertTrue(com.knoxhack.echoashfallprotocol.item.GearUpgradeHandler.getDamageBonus(blade) >= 2.0F,
                "Legacy damage helper should mirror Nexus upgrade damage");
        helper.succeed();
    }

    private static void waterPurifierNetworkPower(GameTestHelper helper) {
        BlockPos purifierPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cablePos = purifierPos.east();
        BlockPos bankPos = cablePos.east();

        helper.getLevel().setBlock(purifierPos, ModBlocks.WATER_PURIFIER.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(cablePos, ModBlocks.POWER_CABLE.get().defaultBlockState(), 3);
        helper.getLevel().setBlock(bankPos, ModBlocks.BATTERY_BANK.get().defaultBlockState(), 3);

        helper.assertTrue(helper.getLevel().getBlockEntity(purifierPos) instanceof WaterPurifierBlockEntity,
                "Water purifier block entity should be present");
        helper.assertTrue(helper.getLevel().getBlockEntity(cablePos) instanceof PowerCableBlockEntity,
                "Power cable block entity should be present");
        helper.assertTrue(helper.getLevel().getBlockEntity(bankPos) instanceof BatteryBankBlockEntity,
                "Battery bank block entity should be present");
        if (helper.getLevel().getBlockEntity(purifierPos) instanceof WaterPurifierBlockEntity purifier
                && helper.getLevel().getBlockEntity(bankPos) instanceof BatteryBankBlockEntity bank) {
            MachineWearData wearData = new MachineWearData(helper.getLevel());
            wearData.repair(purifierPos, MachineWearData.MAX_WEAR);
            bank.setEnergyStored(2_000);
            purifier.getInventory().setStackInSlot(0, new ItemStack(ModItems.DIRTY_WATER_BOTTLE.get()));
            purifier.getInventory().setStackInSlot(1, new ItemStack(ModItems.FILTER_CARTRIDGE_BASIC.get()));

            for (int i = 0; i < 220; i++) {
                BatteryBankBlockEntity.serverTick(helper.getLevel(), bankPos,
                        helper.getLevel().getBlockState(bankPos), bank);
                if (helper.getLevel().getBlockEntity(cablePos) instanceof PowerCableBlockEntity cable) {
                    PowerCableBlockEntity.serverTick(helper.getLevel(), cablePos,
                            helper.getLevel().getBlockState(cablePos), cable);
                }
                WaterPurifierBlockEntity.serverTick(helper.getLevel(), purifierPos,
                        helper.getLevel().getBlockState(purifierPos), purifier);
            }

            int cableEnergy = helper.getLevel().getBlockEntity(cablePos) instanceof PowerCableBlockEntity cable
                    ? cable.getEnergyStored()
                    : -1;
            helper.assertTrue(purifier.getInventory().getStackInSlot(2).is(ModItems.CLEAN_WATER_BOTTLE.get()),
                    "Water purifier should produce clean water from cabled network power"
                            + " output=" + purifier.getInventory().getStackInSlot(2)
                            + " progress=" + purifier.data.get(0) + "/" + purifier.data.get(1)
                            + " hasPower=" + purifier.data.get(2)
                            + " purifierEnergy=" + purifier.getEnergyStored()
                            + " cableEnergy=" + cableEnergy
                            + " bankEnergy=" + bank.getEnergyStored()
                            + " wear=" + wearData.getWear(purifierPos)
                            + " jammed=" + wearData.isJammed(purifierPos));
            helper.assertTrue(bank.getEnergyStored() < 2_000,
                    "Cabled network source should spend energy on purification");
        }
        helper.succeed();
    }

    private static void workshopStatusCopy(GameTestHelper helper) {
        String copy = WorkshopBlock.coverageSummaryMessage().getString();
        helper.assertFalse(copy.contains("Machine links"), "Workshop status should not advertise unimplemented links");
        helper.assertTrue(copy.contains("efficiency"), "Workshop status should describe the active area bonus");
        helper.succeed();
    }

    private static void nexusCommandStatusOnly(GameTestHelper helper) {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        NexusCommandHandler.register(dispatcher);

        var nexus = dispatcher.getRoot().getChild("nexus");
        helper.assertTrue(nexus != null, "/nexus command should be registered");
        helper.assertTrue(nexus.getChild("status") != null, "/nexus status should remain public");
        for (String mutatingVerb : List.of("awaken", "scan", "encounter", "relay", "siege", "operation", "finale")) {
            helper.assertTrue(nexus.getChild(mutatingVerb) == null,
                    "/nexus should not expose public mutation verb: " + mutatingVerb);
        }
        helper.succeed();
    }

    private static void radioDynamicStationPersistence(GameTestHelper helper) {
        RadioNetwork network = new RadioNetwork();
        RadioNetwork.StationInfo dynamic = new RadioNetwork.StationInfo(
                "relay_22222_70_-3333",
                "Relay 22222, -3333",
                new BlockPos(22222, 70, -3333));
        network.activateStation(dynamic);

        RadioNetwork restored = roundTripRadioNetwork(helper, network);
        RadioNetwork.StationInfo restoredStation = restored.getStationInfo(dynamic.getId());
        helper.assertTrue(restored.isActivated(dynamic.getId()), "Dynamic relay id should remain activated");
        helper.assertTrue(restoredStation != null, "Dynamic relay metadata should be restored");
        helper.assertTrue(restoredStation != null && restoredStation.getName().equals(dynamic.getName()),
                "Dynamic relay name should survive serialization");
        helper.assertTrue(restoredStation != null && restoredStation.getPosition().equals(dynamic.getPosition()),
                "Dynamic relay position should survive serialization");
        helper.assertTrue(restored.getAvailableDestinations(BlockPos.ZERO).stream()
                        .anyMatch(station -> station.getId().equals(dynamic.getId())),
                "Dynamic relay should resolve as a destination after reload");

        TagValueOutput legacyOutput = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        legacyOutput.putInt("discoveredCount", 1);
        legacyOutput.putString("discovered_0", "relay_-10_64_20");
        legacyOutput.putInt("activatedCount", 1);
        legacyOutput.putString("activated_0", "relay_-10_64_20");
        RadioNetwork legacy = new RadioNetwork();
        legacy.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), legacyOutput.buildResult()));
        helper.assertTrue(legacy.getStationInfo("relay_-10_64_20") != null,
                "Legacy id-only dynamic relay saves should be reconstructed from relay coordinates");
        helper.succeed();
    }

    private static void droneIntelTargeting(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);

        FactionDiplomacy diplomacy = player.getData(ModAttachments.FACTION_DIPLOMACY.get());
        diplomacy.setRelation(
                FactionDiplomacy.FactionPair.fromFactions(
                        AshfallBiomeFactions.RADWARDEN_COMPACT,
                        AshfallBiomeFactions.CRASHBREAK_SALVAGE),
                -80);
        player.setData(ModAttachments.FACTION_DIPLOMACY.get(), diplomacy);

        EchoCompanionDrone drone = spawnCompanionDrone(helper, player, new BlockPos(4, 2, 2));
        drone.setRepairLevel(EchoCompanionDrone.REPAIR_FULL);
        drone.setCurrentMode(EchoCompanionDrone.DroneMode.COMBAT);

        Mob bandit = ModEntities.SCAVENGER_BANDIT.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(bandit != null, "Scavenger bandit should be spawnable");
        if (bandit != null) {
            BlockPos banditPos = helper.absolutePos(new BlockPos(6, 2, 2));
            bandit.setPos(banditPos.getX() + 0.5D, banditPos.getY(), banditPos.getZ() + 0.5D);
            helper.getLevel().addFreshEntity(bandit);
            drone.tick();
            helper.assertTrue(drone.getTarget() == bandit,
                    "Combat intel should assign an obvious hostile faction target");
            bandit.discard();
        }
        drone.discard();
        helper.succeed();
    }

    private static void questRewardStackPersistence(GameTestHelper helper) {
        QuestData original = new QuestData();
        ItemStack namedReward = new ItemStack(Items.DIAMOND_SWORD);
        namedReward.set(DataComponents.CUSTOM_NAME, Component.literal("ECHO Reward"));
        original.completeMission("custom_reward", List.of(namedReward));

        QuestData restored = roundTripQuestData(helper, original);
        List<ItemStack> rewards = restored.getPendingRewards("custom_reward");
        helper.assertTrue(rewards.size() == 1, "Custom reward stack should survive serialization");
        helper.assertTrue(rewards.get(0).is(Items.DIAMOND_SWORD), "Custom reward item should survive serialization");
        helper.assertTrue("ECHO Reward".equals(rewards.get(0).getHoverName().getString()),
                "Custom reward component data should survive serialization");

        TagValueOutput legacyOutput = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        legacyOutput.putInt("pendingRewardMissions", 1);
        legacyOutput.putString("rewardMission_0", "legacy_reward");
        legacyOutput.putInt("rewardCount_0", 1);
        legacyOutput.putString("rewardItem_0_0_id", BuiltInRegistries.ITEM.getKey(Items.APPLE).toString());
        legacyOutput.putInt("rewardItem_0_0_count", 3);
        QuestData legacy = new QuestData();
        legacy.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), legacyOutput.buildResult()));
        helper.assertTrue(legacy.getPendingRewards("legacy_reward").size() == 1
                        && legacy.getPendingRewards("legacy_reward").get(0).is(Items.APPLE)
                        && legacy.getPendingRewards("legacy_reward").get(0).getCount() == 3,
                "Legacy id/count pending reward entries should still deserialize");
        helper.succeed();
    }

    private static RadioNetwork roundTripRadioNetwork(GameTestHelper helper, RadioNetwork original) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        original.serialize(output);
        CompoundTag tag = output.buildResult();
        RadioNetwork restored = new RadioNetwork();
        restored.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        return restored;
    }

    private static QuestData roundTripQuestData(GameTestHelper helper, QuestData original) {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        original.serialize(output);
        CompoundTag tag = output.buildResult();
        QuestData restored = new QuestData();
        restored.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
        return restored;
    }

    private static void assertRetiredEntityIdAbsent(GameTestHelper helper, String first, String second) {
        Identifier entityId = id(first + "_" + second);
        helper.assertTrue(!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId),
                "Retired Ashfall faction entity id should not be registered: " + entityId);
    }

    private static void assertContractHasObjective(GameTestHelper helper, String contractPath,
            AshfallFactionContracts.ObjectiveType type) {
        AshfallFactionContracts.Spec spec = AshfallFactionContracts.spec(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, contractPath)).orElse(null);
        helper.assertTrue(spec != null, "Ashfall contract should exist: " + contractPath);
        if (spec == null) {
            return;
        }
        helper.assertTrue(spec.objectives().stream().anyMatch(objective -> objective.type() == type),
                "Ashfall contract should include " + type + " objective: " + contractPath);
    }

    private static Mission requireMission(GameTestHelper helper, String missionId) {
        Mission mission = MissionRegistry.getMissionById(missionId);
        helper.assertTrue(mission != null, "Mission should exist: " + missionId);
        if (mission == null) {
            throw new IllegalStateException("Missing mission " + missionId);
        }
        return mission;
    }

    private static void assertRewardAtLeast(GameTestHelper helper, Mission mission, Item item, int count) {
        int total = mission.rewards().stream()
                .filter(stack -> stack.getItem() == item)
                .mapToInt(ItemStack::getCount)
                .sum();
        helper.assertTrue(total >= count,
                mission.id() + " should reward at least " + count + "x " + BuiltInRegistries.ITEM.getKey(item));
    }

    private static void assertRequiredCount(GameTestHelper helper, Mission mission, Item item, int count) {
        int total = mission.requiredItems().stream()
                .filter(stack -> stack.getItem() == item)
                .mapToInt(ItemStack::getCount)
                .sum();
        helper.assertTrue(total == count,
                mission.id() + " should require exactly " + count + "x " + BuiltInRegistries.ITEM.getKey(item));
    }

    private static void setCurrentMission(QuestData quest, String missionId) {
        for (int phase = 0; phase < MissionRegistry.getPhaseCount(); phase++) {
            List<Mission> missions = MissionRegistry.getMissionsForPhase(phase);
            for (int index = 0; index < missions.size(); index++) {
                if (missions.get(index).id().equals(missionId)) {
                    quest.setCurrentPhase(phase);
                    quest.setCurrentMissionIndex(index);
                    quest.unlockMission(missionId);
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Unknown mission: " + missionId);
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment, String testName, Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                400,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                2);
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, path);
    }
}
