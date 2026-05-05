package com.knoxhack.echoashfallprotocol.test;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.NexusCoreBlock;
import com.knoxhack.echoashfallprotocol.block.PowerNodeBlock;
import com.knoxhack.echoashfallprotocol.block.entity.NexusCoreBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity;
import com.knoxhack.echoashfallprotocol.block.entity.PowerNodeBlockEntity;
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
import com.knoxhack.echoashfallprotocol.entity.boss.WardenBossEntity;
import com.knoxhack.echoashfallprotocol.endgame.NexusAccessRules;
import com.knoxhack.echoashfallprotocol.endgame.NexusChoiceService;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.endgame.PrefallArchivesArenaService;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventProfiles;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventStatus;
import com.knoxhack.echoashfallprotocol.event.EnvironmentalEventType;
import com.knoxhack.echoashfallprotocol.event.ModStructuresCommand;
import com.knoxhack.echoashfallprotocol.event.PostNexusEventHandler;
import com.knoxhack.echoashfallprotocol.faction.AshfallBiomeFactions;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionContracts;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionMap;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfile;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfiles;
import com.knoxhack.echoashfallprotocol.item.RareTechSchematicItem;
import com.knoxhack.echoashfallprotocol.item.SchematicFragmentItem;
import com.knoxhack.echoashfallprotocol.network.BossNavigationPacket;
import com.knoxhack.echoashfallprotocol.network.DroneCommandPacket;
import com.knoxhack.echoashfallprotocol.network.ModNetwork;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.Perk;
import com.knoxhack.echoashfallprotocol.research.PerkRegistry;
import com.knoxhack.echoashfallprotocol.research.ResearchData;
import com.knoxhack.echoashfallprotocol.world.BiomeGuardianSiteData;
import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import com.knoxhack.echoashfallprotocol.world.NexusWorldData;
import com.knoxhack.echoashfallprotocol.worldgen.ProceduralStructureGenerator;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
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
import net.neoforged.bus.api.IEventBus;
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
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_GUIDE_COVERAGE =
            TEST_FUNCTIONS.register("mission_guide_coverage", () -> ModGameTests::missionGuideCoverage);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIRST_NIGHT_ROUTE_SAFETY =
            TEST_FUNCTIONS.register("first_night_route_safety", () -> ModGameTests::firstNightRouteSafety);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EXPLORATION_SITE_PROFILES =
            TEST_FUNCTIONS.register("exploration_site_profiles", () -> ModGameTests::explorationSiteProfiles);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTION_CONTRACT_BALANCE =
            TEST_FUNCTIONS.register("faction_contract_balance", () -> ModGameTests::factionContractBalance);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STRICT_FACTION_ENTITY_IDS =
            TEST_FUNCTIONS.register("strict_faction_entity_ids", () -> ModGameTests::strictFactionEntityIds);

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
        register(event, environment, "warden_arena_service", WARDEN_ARENA_SERVICE.getId());
        register(event, environment, "rare_schematic_research", RARE_SCHEMATIC_RESEARCH.getId());
        register(event, environment, "research_perk_graph", RESEARCH_PERK_GRAPH.getId());
        register(event, environment, "structure_export_paths", STRUCTURE_EXPORT_PATHS.getId());
        register(event, environment, "starter_drop_pod_template", STARTER_DROP_POD_TEMPLATE.getId());
        register(event, environment, "archive_read_state", ARCHIVE_READ_STATE.getId());
        register(event, environment, "substrate_grinder_recipes", SUBSTRATE_GRINDER_RECIPES.getId());
        register(event, environment, "environmental_event_profiles", ENVIRONMENTAL_EVENT_PROFILES.getId());
        register(event, environment, "mission_ux_summary", MISSION_UX_SUMMARY.getId());
        register(event, environment, "endgame_route_progress", ENDGAME_ROUTE_PROGRESS.getId());
        register(event, environment, "terminal_lore_taxonomy", TERMINAL_LORE_TAXONOMY.getId());
        register(event, environment, "mission_guide_coverage", MISSION_GUIDE_COVERAGE.getId());
        register(event, environment, "first_night_route_safety", FIRST_NIGHT_ROUTE_SAFETY.getId());
        register(event, environment, "exploration_site_profiles", EXPLORATION_SITE_PROFILES.getId());
        register(event, environment, "faction_contract_balance", FACTION_CONTRACT_BALANCE.getId());
        register(event, environment, "strict_faction_entity_ids", STRICT_FACTION_ENTITY_IDS.getId());
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
        helper.assertTrue(BiomeGuardianProfiles.all().size() == 9, "All nine Ashfall biome guardians need profiles");
        Set<BiomeGuardianProfile.GuardianAbility> abilities =
                EnumSet.noneOf(BiomeGuardianProfile.GuardianAbility.class);
        Set<String> missions = new HashSet<>();
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
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
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            BiomeBossEntity boss = profile.bossType().get().create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(boss != null, "Guardian should be spawnable: " + profile.bossPath());
            if (boss == null) {
                continue;
            }
            double angle = index++ * (Math.PI * 2.0D / 9.0D);
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
        BiomeGuardianProfile profile = BiomeGuardianProfiles.byBiome("the_wasteland").orElseThrow();
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
        helper.assertTrue(BossHudProfiles.all().size() >= 14,
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

        BossHudProfile sentinelHud = BossHudProfiles.byEntityId("echoashfallprotocol:wasteland_sentinel").orElseThrow();
        BossNavigationPacket original = BossNavigationPacket.active(sentinelHud, "minecraft:overworld",
                new BlockPos(4, 64, -9), 2, 0.45F, "Wasteland Sentinel");
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
        helper.assertTrue(sentinelHud.phaseForHealth(0.65F) == 2 && sentinelHud.phaseForHealth(0.32F) == 3,
                "Boss HUD phase thresholds should expose phase 2 and phase 3");

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);
        BiomeGuardianProfile guardian = BiomeGuardianProfiles.byBiome("the_wasteland").orElseThrow();
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
        for (BlockPos nodePos : List.copyOf(NexusWorldData.get(level).getActiveNodePositions())) {
            NexusWorldData.get(level).removePowerNode(nodePos);
        }

        level.setBlock(corePos, ModBlocks.NEXUS_CORE.get().defaultBlockState(), 3);
        helper.assertTrue(level.getBlockEntity(corePos) instanceof NexusCoreBlockEntity,
                "Nexus Core block entity should be present");
        NexusCoreBlockEntity core = (NexusCoreBlockEntity) level.getBlockEntity(corePos);

        NexusAccessRules.Status missingGuardians = NexusAccessRules.evaluate(quest, level, core);
        helper.assertFalse(missingGuardians.allowed(), "Nexus should deny before guardians are defeated");
        helper.assertTrue(missingGuardians.missingGuardianCount() == 9,
                "Nexus gate should require all nine guardians before checking nodes");

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

        NexusAccessRules.Status ready = NexusAccessRules.evaluate(quest, level, core);
        helper.assertTrue(ready.allowed(), "Nexus should allow after guardians and five nodes are ready");
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
        for (BlockPos nodePos : NexusWorldData.get(level).getActiveNodePositions()) {
            NexusWorldData.get(level).removePowerNode(nodePos);
        }
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

    private static int countBlocks(GameTestHelper helper, BlockPos origin, net.minecraft.core.Vec3i size, String blockId) {
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
                ModEntities.WARDEN_BOSS.get(),
                ModEntities.WASTELAND_SENTINEL.get(),
                ModEntities.CRASH_ZONE_COLOSSUS.get(),
                ModEntities.CRYOGENIC_OVERSEER.get(),
                ModEntities.INDUSTRIAL_JUGGERNAUT.get(),
                ModEntities.NEXUS_SCAR_AVATAR.get(),
                ModEntities.RADIATION_BEHEMOTH.get(),
                ModEntities.CITY_RUIN_STALKER.get(),
                ModEntities.PLAINS_WARLORD.get(),
                ModEntities.TOXIC_HIVE_MATRIARCH.get()
        );
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
                ModEntities.WARDEN_BOSS.get(),
                ModEntities.WASTELAND_SENTINEL.get(),
                ModEntities.CRASH_ZONE_COLOSSUS.get(),
                ModEntities.CRYOGENIC_OVERSEER.get(),
                ModEntities.INDUSTRIAL_JUGGERNAUT.get(),
                ModEntities.NEXUS_SCAR_AVATAR.get(),
                ModEntities.RADIATION_BEHEMOTH.get(),
                ModEntities.CITY_RUIN_STALKER.get(),
                ModEntities.PLAINS_WARLORD.get(),
                ModEntities.TOXIC_HIVE_MATRIARCH.get()
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

    private static void factionContractBalance(GameTestHelper helper) {
        if (EchoCoreServices.factionDefinitions().stream().noneMatch(definition -> AshfallFactionMap.isAshfall(definition.id()))) {
            AshfallBiomeFactions.register();
        }

        List<EchoFactionDefinition> definitions = AshfallFactionMap.all().stream()
                .map(factionId -> EchoCoreServices.factionDefinition(factionId).orElse(null))
                .toList();
        helper.assertTrue(definitions.stream().allMatch(java.util.Objects::nonNull),
                "All 10 Ashfall Echo Core factions should be registered");
        helper.assertTrue(definitions.size() == 10, "Ashfall should expose exactly 10 Echo Core factions");

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
        helper.assertTrue(contractCount == 30, "Ashfall should expose 30 Echo Core contracts");

        assertContractHasObjective(helper, "crashbreak_salvage_field_contract",
                AshfallFactionContracts.ObjectiveType.POI_DISCOVERY);
        assertContractHasObjective(helper, "radwarden_compact_aligned_contract",
                AshfallFactionContracts.ObjectiveType.REPAIR);
        assertContractHasObjective(helper, "sporebound_sanctum_aligned_contract",
                AshfallFactionContracts.ObjectiveType.KILL);

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
