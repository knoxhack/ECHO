package com.knoxhack.echoorbitalremnants.test;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.block.entity.OrbitalMachineBlockEntity;
import com.knoxhack.echoorbitalremnants.entity.AbandonedCaptainEntity;
import com.knoxhack.echoorbitalremnants.entity.CorruptedDockingAiEntity;
import com.knoxhack.echoorbitalremnants.entity.EchoZeroEntity;
import com.knoxhack.echoorbitalremnants.entity.EuropaCryoWardenEntity;
import com.knoxhack.echoorbitalremnants.integration.OrbitalMissionProvider;
import com.knoxhack.echoorbitalremnants.item.FactionPledgeItem;
import com.knoxhack.echoorbitalremnants.menu.OrbitalMachineMenu;
import com.knoxhack.echoorbitalremnants.network.EchoTerminalSnapshot;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.FactionStanding;
import com.knoxhack.echoorbitalremnants.progression.LaunchReadiness;
import com.knoxhack.echoorbitalremnants.progression.ModAdvancements;
import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.suit.SuitState;
import com.knoxhack.echoorbitalremnants.world.EuropaCryoOcean;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySite;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySiteType;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySites;
import com.knoxhack.echoorbitalremnants.world.LunarScarZone;
import com.knoxhack.echoorbitalremnants.world.MarsAshBasin;
import com.knoxhack.echoorbitalremnants.world.NexusAnomalyBelt;
import com.knoxhack.echoorbitalremnants.world.OrbitalDebrisField;
import com.knoxhack.echoorbitalremnants.world.RouteTerrainGenerator;
import com.knoxhack.echoterminal.api.TerminalTab;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.gametest.framework.GameTestEnvironments;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoOrbitalRemnants.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MACHINE_PROCESSING =
            TEST_FUNCTIONS.register("machine_processing", () -> ModGameTests::machineProcessing);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LAUNCH_READINESS =
            TEST_FUNCTIONS.register("launch_readiness", () -> ModGameTests::launchReadiness);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROCKET_ASSEMBLY =
            TEST_FUNCTIONS.register("rocket_assembly", () -> ModGameTests::rocketAssembly);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_PROGRESS =
            TEST_FUNCTIONS.register("terminal_progress", () -> ModGameTests::terminalProgress);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_GATES =
            TEST_FUNCTIONS.register("route_gates", () -> ModGameTests::routeGates);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SUIT_STATE =
            TEST_FUNCTIONS.register("suit_state", () -> ModGameTests::suitState);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FACTION_REWARD =
            TEST_FUNCTIONS.register("faction_reward", () -> ModGameTests::factionReward);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FINAL_PROTOCOL =
            TEST_FUNCTIONS.register("final_protocol", () -> ModGameTests::finalProtocol);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_CACHES =
            TEST_FUNCTIONS.register("route_caches", () -> ModGameTests::routeCaches);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_TERRAIN =
            TEST_FUNCTIONS.register("route_terrain", () -> ModGameTests::routeTerrain);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SURVEY_PROGRESS =
            TEST_FUNCTIONS.register("survey_progress", () -> ModGameTests::surveyProgress);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SURVEY_HARDENING =
            TEST_FUNCTIONS.register("survey_hardening", () -> ModGameTests::surveyHardening);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROUTE_SITE_FAMILIES =
            TEST_FUNCTIONS.register("route_site_families", () -> ModGameTests::routeSiteFamilies);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_SURVIVAL_CHAIN =
            TEST_FUNCTIONS.register("beta_survival_chain", () -> ModGameTests::betaSurvivalChain);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_TERMINAL_GUIDANCE =
            TEST_FUNCTIONS.register("beta_terminal_guidance", () -> ModGameTests::betaTerminalGuidance);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_CACHE_SUPPORT =
            TEST_FUNCTIONS.register("beta_cache_support", () -> ModGameTests::betaCacheSupport);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EARLY_GROUND_RECOVERY =
            TEST_FUNCTIONS.register("early_ground_recovery", () -> ModGameTests::earlyGroundRecovery);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EARLY_LAUNCH_TO_ORBIT =
            TEST_FUNCTIONS.register("early_launch_to_orbit", () -> ModGameTests::earlyLaunchToOrbit);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_FACTION_CONTRACTS =
            TEST_FUNCTIONS.register("beta_faction_contracts", () -> ModGameTests::betaFactionContracts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_ORBITAL_REMNANT_CONTRACT =
            TEST_FUNCTIONS.register("beta_orbital_remnant_contract", () -> ModGameTests::betaOrbitalRemnantContract);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_VOID_SALVAGER_CONTRACT =
            TEST_FUNCTIONS.register("beta_void_salvager_contract", () -> ModGameTests::betaVoidSalvagerContract);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BETA_NEXUS_CHOIR_CONTRACT =
            TEST_FUNCTIONS.register("beta_nexus_choir_contract", () -> ModGameTests::betaNexusChoirContract);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MID_GAME_OBJECTIVE_CHAIN =
            TEST_FUNCTIONS.register("mid_game_objective_chain", () -> ModGameTests::midGameObjectiveChain);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MID_GAME_ROUTE_GATES =
            TEST_FUNCTIONS.register("mid_game_route_gates", () -> ModGameTests::midGameRouteGates);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MID_GAME_RECIPES_AND_SITES =
            TEST_FUNCTIONS.register("mid_game_recipes_and_sites", () -> ModGameTests::midGameRecipesAndSites);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EUROPA_CRYO_WARDEN =
            TEST_FUNCTIONS.register("europa_cryo_warden", () -> ModGameTests::europaCryoWarden);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOSS_IDENTITY =
            TEST_FUNCTIONS.register("boss_identity", () -> ModGameTests::bossIdentity);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_CACHE_STATE =
            TEST_FUNCTIONS.register("terminal_mission_cache_state", () -> ModGameTests::terminalMissionCacheState);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_MISSION_INTEGRATION =
            TEST_FUNCTIONS.register("terminal_mission_integration", () -> ModGameTests::terminalMissionIntegration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_LORE_TAXONOMY =
            TEST_FUNCTIONS.register("terminal_lore_taxonomy", () -> ModGameTests::terminalLoreTaxonomy);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("release_v1"));
        register(event, environment, "machine_processing", MACHINE_PROCESSING.getId());
        register(event, environment, "launch_readiness", LAUNCH_READINESS.getId());
        register(event, environment, "rocket_assembly", ROCKET_ASSEMBLY.getId());
        register(event, environment, "terminal_progress", TERMINAL_PROGRESS.getId());
        register(event, environment, "route_gates", ROUTE_GATES.getId());
        register(event, environment, "suit_state", SUIT_STATE.getId());
        register(event, environment, "faction_reward", FACTION_REWARD.getId());
        register(event, environment, "final_protocol", FINAL_PROTOCOL.getId());
        register(event, environment, "route_caches", ROUTE_CACHES.getId());
        register(event, environment, "route_terrain", ROUTE_TERRAIN.getId());
        register(event, environment, "survey_progress", SURVEY_PROGRESS.getId());
        register(event, environment, "survey_hardening", SURVEY_HARDENING.getId());
        register(event, environment, "route_site_families", ROUTE_SITE_FAMILIES.getId());
        register(event, environment, "beta_survival_chain", BETA_SURVIVAL_CHAIN.getId());
        register(event, environment, "beta_terminal_guidance", BETA_TERMINAL_GUIDANCE.getId());
        register(event, environment, "beta_cache_support", BETA_CACHE_SUPPORT.getId());
        register(event, environment, "early_ground_recovery", EARLY_GROUND_RECOVERY.getId());
        register(event, environment, "early_launch_to_orbit", EARLY_LAUNCH_TO_ORBIT.getId());
        register(event, environment, "beta_faction_contracts", BETA_FACTION_CONTRACTS.getId());
        register(event, environment, "beta_orbital_remnant_contract", BETA_ORBITAL_REMNANT_CONTRACT.getId());
        register(event, environment, "beta_void_salvager_contract", BETA_VOID_SALVAGER_CONTRACT.getId());
        register(event, environment, "beta_nexus_choir_contract", BETA_NEXUS_CHOIR_CONTRACT.getId());
        register(event, environment, "mid_game_objective_chain", MID_GAME_OBJECTIVE_CHAIN.getId());
        register(event, environment, "mid_game_route_gates", MID_GAME_ROUTE_GATES.getId());
        register(event, environment, "mid_game_recipes_and_sites", MID_GAME_RECIPES_AND_SITES.getId());
        register(event, environment, "europa_cryo_warden", EUROPA_CRYO_WARDEN.getId());
        register(event, environment, "boss_identity", BOSS_IDENTITY.getId());
        register(event, environment, "terminal_mission_cache_state", TERMINAL_MISSION_CACHE_STATE.getId());
        register(event, environment, "terminal_mission_integration", TERMINAL_MISSION_INTEGRATION.getId());
        register(event, environment, "terminal_lore_taxonomy", TERMINAL_LORE_TAXONOMY.getId());
    }

    private static void machineProcessing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.OXYGEN_COMPRESSOR.get());
        OrbitalMachineBlockEntity machine = helper.getBlockEntity(pos, OrbitalMachineBlockEntity.class);
        machine.setItem(OrbitalMachineBlockEntity.INPUT_SLOT, new ItemStack(Items.GLASS_BOTTLE));
        for (int i = 0; i < 140; i++) {
            OrbitalMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        }
        helper.assertTrue(machine.getItem(OrbitalMachineBlockEntity.OUTPUT_SLOT).is(ModItems.EMERGENCY_OXYGEN_CELL.get()),
                "Oxygen compressor should process glass bottles into emergency oxygen cells");

        BlockPos reclaimerPos = new BlockPos(3, 1, 1);
        helper.setBlock(reclaimerPos, ModBlocks.SOLAR_RECLAIMER.get());
        OrbitalMachineBlockEntity reclaimer = helper.getBlockEntity(reclaimerPos, OrbitalMachineBlockEntity.class);
        reclaimer.setItem(OrbitalMachineBlockEntity.INPUT_SLOT, new ItemStack(ModItems.VACUUM_CIRCUIT.get()));
        for (int i = 0; i < 180; i++) {
            OrbitalMachineBlockEntity.tick(helper.getLevel(), reclaimer.getBlockPos(), reclaimer.getBlockState(), reclaimer);
        }
        helper.assertTrue(reclaimer.getItem(OrbitalMachineBlockEntity.OUTPUT_SLOT).is(ModItems.NAVIGATION_CHIP.get()),
                "Solar Reclaimer should process Vacuum Circuits into Navigation Chips for late-route crafting");

        BlockPos analyzerPos = new BlockPos(5, 1, 1);
        helper.setBlock(analyzerPos, ModBlocks.SIGNAL_ANALYZER.get());
        OrbitalMachineBlockEntity analyzer = helper.getBlockEntity(analyzerPos, OrbitalMachineBlockEntity.class);
        analyzer.setItem(OrbitalMachineBlockEntity.INPUT_SLOT, new ItemStack(ModBlocks.BROKEN_SOLAR_PANEL.get()));
        for (int i = 0; i < 220; i++) {
            OrbitalMachineBlockEntity.tick(helper.getLevel(), analyzer.getBlockPos(), analyzer.getBlockState(), analyzer);
        }
        helper.assertTrue(analyzer.getItem(OrbitalMachineBlockEntity.OUTPUT_SLOT).is(ModItems.ORBIT_SURVEY_DATA.get()),
                "Signal Analyzer should process salvage into Orbit Survey Data");
        helper.succeed();
    }

    private static void launchReadiness(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(LaunchReadiness.evaluateForLaunch(player).ready(), "Launch readiness should reject an unprepared player");
        placeLaunchComplex(helper, player, new BlockPos(6, 1, 6));
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.PRESSURIZED_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.PRESSURIZED_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.PRESSURIZED_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.MAGNETIC_BOOTS.get()));
        player.getInventory().add(new ItemStack(ModItems.OXYGEN_TANK.get()));
        helper.assertTrue(LaunchReadiness.evaluateForLaunch(player).ready(), "Launch readiness should accept suit, oxygen, and infrastructure");
        helper.succeed();
    }

    private static void rocketAssembly(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos framePos = placeLaunchComplex(helper, player, new BlockPos(6, 1, 6));
        giveAssemblyParts(player);
        OrbitalMachineBlockEntity frame = helper.getBlockEntity(framePos, OrbitalMachineBlockEntity.class);
        OrbitalMachineMenu menu = new OrbitalMachineMenu(1, player.getInventory(), frame, frame.data());
        menu.broadcastChanges();
        helper.assertTrue(frame.getItem(OrbitalMachineBlockEntity.OUTPUT_SLOT).is(ModItems.EMERGENCY_ROCKET.get()),
                "Rocket Assembly Frame should offer an Emergency Rocket when all parts are ready");
        menu.quickMoveStack(player, OrbitalMachineBlockEntity.OUTPUT_SLOT);
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.EMERGENCY_ROCKET.get())),
                "Taking the assembly output should grant an Emergency Rocket");
        helper.assertTrue(count(player.getInventory(), ModItems.FUEL_TANK.get()) == 0,
                "Taking the rocket should consume the required parts");
        helper.succeed();
    }

    private static void terminalProgress(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.markOrbitalContact(player);
        progress.markLowOrbitReached(player);
        player.getInventory().add(new ItemStack(ModBlocks.STATION_LIFE_SUPPORT_CORE.get()));
        player.setPos(1.5D, Config.ORBITAL_ALTITUDE.get(), 1.5D);
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        EchoTerminalSnapshot snapshot = EchoTerminalSnapshot.from(player);
        progress.unlockMarsRoute(player);
        progress.unlockEuropaRoute(player);
        progress.unlockDeepSpaceProtocol(player);
        progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.lowOrbitReached(), "Low orbit flag should persist");
        helper.assertTrue(progress.lunarSignalUnlocked(), "Terminal scan should detect a nearby Station Life Support Core");
        helper.assertTrue(snapshot.scanReport().contains("Station life support"), "Terminal snapshot should expose scan feedback");
        helper.assertTrue(!snapshot.scanRequirement().isBlank(), "Terminal snapshot should expose the next scan requirement");
        helper.assertTrue(progress.marsRouteUnlocked(), "Mars route should persist");
        helper.assertTrue(progress.europaRouteUnlocked(), "Europa route should persist");
        helper.assertTrue(progress.deepSpaceProtocolUnlocked(), "Deep Space Protocol should persist");
        helper.succeed();
    }

    private static void routeGates(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MARS_TRANSFER_WINDOW.get()));
        InteractionResult locked = ModItems.MARS_TRANSFER_WINDOW.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(locked == InteractionResult.CONSUME, "Mars route item should be blocked before telemetry unlock");
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        helper.assertFalse(progress.marsAshBasinVisited(), "Locked route use should not mark Mars visited");
        progress.unlockMarsRoute(player);
        helper.assertTrue(progress.marsRouteUnlocked(), "Terminal progress should expose the Mars route gate flag");
        InteractionResult wrongState = ModItems.MARS_TRANSFER_WINDOW.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(wrongState == InteractionResult.CONSUME, "Unlocked Mars route should still require orbital staging");
        helper.assertFalse(progress.marsAshBasinVisited(), "Wrong-state route use should not mark Mars visited");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMERGENCY_ROCKET.get()));
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        InteractionResult noReturn = ModItems.EMERGENCY_ROCKET.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(noReturn == InteractionResult.CONSUME, "Emergency Rocket should reject orbital re-entry without an Earth return vector");
        helper.succeed();
    }

    private static void suitState(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        SuitState state = SuitState.get(player);
        state.tickVacuum(player, false, false, true, false);
        state.compromisePressure(20);
        state.addRadiation(false);
        helper.assertTrue(state.oxygen() < 100, "Vacuum exposure without a suit should drain oxygen");
        helper.assertTrue(state.pressure() < 100, "Pressure compromise should reduce pressure");
        helper.assertTrue(state.radiation() > 0, "Radiation should increase");
        helper.succeed();
    }

    private static void factionReward(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pledge = new ItemStack(ModItems.ORBITAL_REMNANT_BADGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pledge);
        ModItems.ORBITAL_REMNANT_BADGE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.orbitalRemnantStanding() == FactionStanding.ALIGNED,
                "Orbital Remnant pledge should persist aligned standing");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.OXYGEN_BOOSTER.get())),
                "Orbital Remnant pledge should grant oxygen support");
        int boosters = count(player.getInventory(), ModItems.OXYGEN_BOOSTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ORBITAL_REMNANT_BADGE.get()));
        ModItems.ORBITAL_REMNANT_BADGE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(count(player.getInventory(), ModItems.OXYGEN_BOOSTER.get()) == boosters,
                "Repeating the same pledge should not double-grant pledge rewards");
        helper.succeed();
    }

    private static void finalProtocol(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.markAnomalyBeltEntered(player);
        progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.anomalyBeltEntered(), "Entering Nexus should mark the anomaly route");
        helper.assertFalse(progress.echoZeroEncountered(), "Entering Nexus should not complete ECHO-0");
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            helper.assertFalse(ModAdvancements.hasEchoZeroResolved(serverPlayer), "Nexus entry should not grant the final advancement");
        }
        helper.assertTrue(EchoZeroEntity.completeEncounter(player), "Completing ECHO-0 should report a new final protocol");
        progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.echoZeroEncountered(), "ECHO-0 completion should persist");
        helper.assertTrue(progress.echoZeroRewardClaimed(), "ECHO-0 reward claim state should persist");
        helper.assertTrue(progress.lastTerminalReport().contains("ECHO-0 resolved"), "Terminal report should describe final completion");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.NEXUS_DRIVE_CORE.get())),
                "Final completion should grant the baseline Nexus reward");
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            helper.assertTrue(ModAdvancements.hasEchoZeroResolved(serverPlayer), "ECHO-0 completion should grant the final advancement");
        }
        int nexusCoreCount = count(player.getInventory(), ModItems.NEXUS_DRIVE_CORE.get());
        helper.assertFalse(EchoZeroEntity.completeEncounter(player), "ECHO-0 final reward should only be granted once");
        helper.assertTrue(count(player.getInventory(), ModItems.NEXUS_DRIVE_CORE.get()) == nexusCoreCount,
                "Repeated ECHO-0 completion checks should not duplicate final rewards");
        helper.succeed();
    }

    private static void routeCaches(GameTestHelper helper) {
        var level = helper.getLevel();
        OrbitalDebrisField.seedArrivalField(level, new BlockPos(20, 10, 20));
        LunarScarZone.seedLandingSite(level, new BlockPos(60, 10, 20));
        MarsAshBasin.seedLandingSite(level, new BlockPos(100, 10, 20));
        EuropaCryoOcean.seedLandingSite(level, new BlockPos(140, 10, 20));
        NexusAnomalyBelt.seedEntrySite(level, new BlockPos(180, 10, 20));

        helper.assertTrue(hasCacheWith(level, new BlockPos(20, 10, 20), ModItems.VACUUM_CIRCUIT.get()),
                "Orbital arrival should place a station salvage cache");
        helper.assertTrue(hasCacheWith(level, new BlockPos(60, 10, 20), ModItems.HELIUM_3_CELL.get()),
                "Lunar arrival should place a Helium-3 cache");
        helper.assertTrue(hasCacheWith(level, new BlockPos(100, 10, 20), ModItems.MARTIAN_SILICA.get()),
                "Mars arrival should place a Martian Silica cache");
        helper.assertTrue(hasCacheWith(level, new BlockPos(140, 10, 20), ModItems.CRYO_CRYSTAL.get()),
                "Europa arrival should place a Cryo resource cache");
        helper.assertTrue(hasCacheWith(level, new BlockPos(180, 10, 20), ModItems.LUNAR_CORE_FRAGMENT.get()),
                "Nexus arrival should place a final anomaly cache");
        helper.succeed();
    }

    private static void routeTerrain(GameTestHelper helper) {
        helper.assertTrue(RouteTerrainGenerator.topHeight(RouteTerrainGenerator.Route.ORBIT, 0, 0) > 58,
                "Orbit route terrain should generate debris bands");
        helper.assertTrue(RouteTerrainGenerator.topHeight(RouteTerrainGenerator.Route.MOON, 0, 0) > 58,
                "Moon route terrain should generate a cratered surface");
        helper.assertTrue(RouteTerrainGenerator.topHeight(RouteTerrainGenerator.Route.MARS, 0, 0) > 58,
                "Mars route terrain should generate ash basin terrain");
        helper.assertTrue(RouteTerrainGenerator.topHeight(RouteTerrainGenerator.Route.EUROPA, 0, 0) > 58,
                "Europa route terrain should generate ice shelves");
        helper.assertTrue(RouteTerrainGenerator.topHeight(RouteTerrainGenerator.Route.NEXUS, 23, 23) > 58,
                "Nexus route terrain should generate anomaly islands");
        helper.assertTrue(RouteTerrainGenerator.landmarkBlock(RouteTerrainGenerator.Route.NEXUS).is(ModBlocks.NEXUS_ANCHOR.get()),
                "Nexus terrain landmarks should expose anchor objective blocks");
        helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(RouteTerrainGenerator.Route.ORBIT).is(ModBlocks.STATION_RELAY_NODE.get()),
                "Orbit terrain should expose station relay repair nodes");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.ORBIT).stream().anyMatch(stack -> stack.is(ModItems.ORBIT_SURVEY_DATA.get())),
                "Orbit repeatable landmarks should include route survey data caches");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.NEXUS).stream().anyMatch(stack -> stack.is(ModItems.NEXUS_STABILIZER_SHARD.get())),
                "Nexus repeatable landmarks should include stabilizer shard caches");
        helper.assertTrue(RouteTerrainGenerator.hasDeepSite(RouteTerrainGenerator.Route.ORBIT, 0, 0)
                        || RouteTerrainGenerator.hasDeepSite(RouteTerrainGenerator.Route.ORBIT, 1, 0)
                        || RouteTerrainGenerator.hasDeepSite(RouteTerrainGenerator.Route.ORBIT, 0, 1),
                "Route terrain should deterministically place deep sites near common test chunks");
        helper.succeed();
    }

    private static void surveyProgress(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.recordOrbitSurvey(player, "orbit:site:a");
        EchoTerminalProgress.SurveyResult duplicate = EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:site:a");
        helper.assertTrue(duplicate.duplicate(), "Scanning the same orbit site twice should be rejected");
        EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:site:b");
        helper.assertFalse(EchoTerminalProgress.get(player).orbitSurveyComplete(), "Two unique orbit survey scans should not complete the route");
        EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:site:c");
        helper.assertTrue(EchoTerminalProgress.get(player).orbitSurveyComplete(), "Three orbit survey scans should complete the route");

        EchoTerminalProgress.SurveyResult locked = EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:site:a");
        helper.assertFalse(EchoTerminalProgress.get(player).nexusStabilized(), "Nexus stabilization should stay locked before ECHO-0");
        helper.assertFalse(locked.newlyComplete(), "Locked Nexus stabilization should not report completion");
        EchoTerminalProgress.get(player).markEchoZeroEncountered(player);
        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:site:a");
        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:site:b");
        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:site:c");
        helper.assertTrue(EchoTerminalProgress.get(player).nexusStabilized(), "Nexus stabilization should complete after ECHO-0 and three anchor scans");
        helper.succeed();
    }

    private static void surveyHardening(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        EchoTerminalSnapshot snapshot = EchoTerminalSnapshot.from(player);
        helper.assertTrue(snapshot.surveyLines().size() >= 9, "Terminal snapshot should expose route objectives plus all five survey rows");
        helper.assertTrue(snapshot.surveyLines().stream().anyMatch(line -> line.contains("Orbit") && line.contains("Signal Relay")),
                "Terminal survey rows should name scan hooks");
        helper.assertTrue(snapshot.surveyLines().stream().anyMatch(line -> line.contains("Station Relay Node")),
                "Terminal survey rows should name mid-game repair hooks");
        boolean analyzerRecipe = helper.getLevel().recipeAccess().getRecipes().stream()
                .anyMatch(holder -> holder.id().identifier().equals(id("signal_analyzer")));
        helper.assertTrue(analyzerRecipe, "Signal Analyzer should have a survival crafting recipe");
        helper.succeed();
    }

    private static void routeSiteFamilies(GameTestHelper helper) {
        for (RouteTerrainGenerator.Route route : RouteTerrainGenerator.Route.values()) {
            int variants = 0;
            for (int chunkX = -12; chunkX <= 12; chunkX++) {
                for (int chunkZ = -12; chunkZ <= 12; chunkZ++) {
                    int variant = RouteTerrainGenerator.siteVariant(route, chunkX, chunkZ);
                    if (variant >= 0) {
                        variants |= 1 << variant;
                    }
                }
            }
            helper.assertTrue(variants == 0b111, route.getSerializedName() + " should expose all three deep-site variants");
            for (int variant = 0; variant < 3; variant++) {
                String siteName = RouteTerrainGenerator.siteName(route, variant);
                helper.assertTrue(siteName.equals(expectedSiteName(route, variant)),
                        route.getSerializedName() + " variant should keep its documented site name");
                helper.assertFalse(RouteTerrainGenerator.hazardBlock(route, variant).isAir(),
                        route.getSerializedName() + " variant should expose a hazard block");
                helper.assertTrue(RouteTerrainGenerator.cacheItems(route, variant).size() >= 3,
                        route.getSerializedName() + " variant should expose a conservative route cache");
                helper.assertTrue(RouteTerrainGenerator.landmarkBlock(route).getBlock() != Blocks.AIR,
                        route.getSerializedName() + " should expose an objective block");
                helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(route).getBlock() != Blocks.AIR,
                        route.getSerializedName() + " should expose a repair objective block");
            }
        }

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos featurePos = new BlockPos(1, 1, 1);
        helper.setBlock(featurePos, ModBlocks.BROKEN_SOLAR_PANEL.get());
        BlockPos absoluteFeaturePos = helper.absolutePos(featurePos);
        player.setPos(absoluteFeaturePos.getX() + 0.5D, absoluteFeaturePos.getY(), absoluteFeaturePos.getZ() + 0.5D);
        helper.assertTrue(SuitEvents.isFeatureThreatZone(player), "Feature blocks should mark ambient threat zones");
        String hazardText = EchoTerminalProgress.get(player).localHazardText(player);
        helper.assertTrue(hazardText.contains("Deep-site signal"), "Terminal hazard text should name nearby deep route features");
        helper.succeed();
    }

    private static void betaSurvivalChain(GameTestHelper helper) {
        assertRecipe(helper, "echo_terminal");
        assertRecipe(helper, "echo_terminal_from_transponder");
        assertRecipe(helper, "signal_analyzer");
        assertRecipe(helper, "fuel_refinery");
        assertRecipe(helper, "oxygen_compressor");
        assertRecipe(helper, "rocket_assembly_frame");
        assertRecipe(helper, "launch_platform");
        assertRecipe(helper, "pressurized_helmet");
        assertRecipe(helper, "pressurized_helmet_from_fragments");
        assertRecipe(helper, "pressurized_chestplate");
        assertRecipe(helper, "pressurized_chestplate_from_fragments");
        assertRecipe(helper, "pressurized_leggings");
        assertRecipe(helper, "pressurized_leggings_from_fragments");
        assertRecipe(helper, "magnetic_boots");
        assertRecipe(helper, "magnetic_boots_from_fragments");
        assertRecipe(helper, "oxygen_tank");
        assertRecipe(helper, "navigation_computer_from_chip");
        assertRecipe(helper, "echo_flight_core_from_transponder");
        assertRecipe(helper, "orbital_shuttle");
        assertRecipe(helper, "mars_transfer_window");
        assertRecipe(helper, "europa_transfer_window");
        assertRecipe(helper, "nexus_drive_vessel");
        assertRecipe(helper, "machine_orbit_survey_data");
        assertRecipe(helper, "machine_lunar_core_sample");
        assertRecipe(helper, "machine_martian_pressure_valve");
        assertRecipe(helper, "machine_europa_thermal_probe");
        assertRecipe(helper, "machine_nexus_stabilizer_shard");
        assertRecipe(helper, "machine_station_relay_fuse");
        assertRecipe(helper, "machine_station_power_matrix");
        assertRecipe(helper, "machine_helium_extractor_core");
        assertRecipe(helper, "machine_lunar_pressure_map");
        assertRecipe(helper, "machine_pressure_regulator");
        assertRecipe(helper, "machine_martian_habitat_key");
        assertRecipe(helper, "machine_europa_probe_array");
        assertRecipe(helper, "machine_thermal_stabilizer");
        assertNoRecipe(helper, "emergency_rocket");

        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.ORBIT).stream().anyMatch(stack -> stack.is(ModItems.ORBIT_SURVEY_DATA.get())),
                "Orbit survey data should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.ORBIT).stream().anyMatch(stack -> stack.is(ModItems.STATION_RELAY_FUSE.get())),
                "Station relay fuses should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.MOON).stream().anyMatch(stack -> stack.is(ModItems.LUNAR_CORE_SAMPLE.get())),
                "Lunar core samples should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.MOON).stream().anyMatch(stack -> stack.is(ModItems.HELIUM_EXTRACTOR_CORE.get())),
                "Helium extractor cores should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.MARS).stream().anyMatch(stack -> stack.is(ModItems.MARTIAN_PRESSURE_VALVE.get())),
                "Martian pressure valves should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.MARS).stream().anyMatch(stack -> stack.is(ModItems.PRESSURE_REGULATOR.get())),
                "Pressure regulators should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.EUROPA).stream().anyMatch(stack -> stack.is(ModItems.EUROPA_THERMAL_PROBE.get())),
                "Europa thermal probes should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.EUROPA).stream().anyMatch(stack -> stack.is(ModItems.EUROPA_PROBE_ARRAY.get())),
                "Europa probe arrays should have generated-cache recovery");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.NEXUS).stream().anyMatch(stack -> stack.is(ModItems.NEXUS_STABILIZER_SHARD.get())),
                "Nexus stabilizer shards should have generated-cache recovery");
        helper.succeed();
    }

    private static void betaTerminalGuidance(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress.reset(player);
        markAshfallNexusChoice(helper, player);
        EchoTerminalSnapshot fresh = EchoTerminalSnapshot.from(player);
        helper.assertTrue(fresh.nextObjective().contains("Next Step"), "Fresh terminal snapshot should expose a compact next step");
        helper.assertTrue(fresh.nextObjective().contains("calibrate"), "Fresh terminal should direct Earth calibration");

        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.markOrbitalContact(player);
        EchoTerminalSnapshot launchBlocked = EchoTerminalSnapshot.from(player);
        helper.assertTrue(launchBlocked.nextObjective().contains("Missing"), "Blocked launch guidance should summarize missing prep");
        helper.assertFalse(launchBlocked.launchMissing().isEmpty(), "Launch tab should expose missing systems");

        progress.markLowOrbitReached(player);
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        String report = EchoTerminalProgress.get(player).lastTerminalReport();
        helper.assertTrue(report.contains("Station Life Support Core"), "Bad orbit scan should name the missing station hook");

        EchoTerminalProgress.get(player).restoreStationLifeSupport(player);
        EchoTerminalSnapshot midGameBlocked = EchoTerminalSnapshot.from(player);
        helper.assertTrue(midGameBlocked.scanRequirement().contains("(0/3)"),
                "Mid-game terminal guidance should expose the current repair count");
        helper.assertTrue(midGameBlocked.missionHelp().contains("ECHO NOTE"),
                "Mid-game terminal help should use consistent ECHO NOTE wording");

        EchoTerminalProgress.get(player).markLunarSignalInvestigated(player);
        EchoTerminalProgress.get(player).unlockMarsRoute(player);
        EchoTerminalProgress.get(player).markMarsAshBasinVisited(player);
        EchoTerminalProgress.get(player).unlockEuropaRoute(player);
        EchoTerminalProgress.get(player).markEuropaCryoOceanVisited(player);
        EchoTerminalProgress.get(player).unlockDeepSpaceProtocol(player);
        EchoTerminalProgress.get(player).markAnomalyBeltEntered(player);
        EchoTerminalSnapshot beforeEchoZero = EchoTerminalSnapshot.from(player);
        helper.assertTrue(beforeEchoZero.nextObjective().contains("ECHO-0"),
                "Finale guidance should name ECHO-0 before Nexus stabilization can begin");
        helper.assertTrue(beforeEchoZero.scanRequirement().contains("ECHO-0"),
                "Scan guidance should name ECHO-0 as the pre-stabilization blocker");

        EchoTerminalProgress.get(player).markEchoZeroEncountered(player);
        EchoTerminalSnapshot afterEchoZero = EchoTerminalSnapshot.from(player);
        helper.assertTrue(afterEchoZero.nextObjective().contains("0/3"),
                "Post-ECHO guidance should expose Nexus stabilization progress");
        helper.assertTrue(afterEchoZero.scanRequirement().contains("Anchor/Growth"),
                "Post-ECHO scan guidance should name Nexus Anchor/Growth sites");
        helper.assertTrue(afterEchoZero.missionHelp().contains("Signal Analyzer"),
                "Post-ECHO help should describe shard recovery when landmarks are hard to find");

        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:a");
        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:b");
        EchoTerminalSnapshot partialNexus = EchoTerminalSnapshot.from(player);
        helper.assertTrue(partialNexus.nextObjective().contains("2/3"),
                "Partial Nexus stabilization should report exact progress");

        EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:a");
        EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:b");
        EchoTerminalProgress.get(player).recordOrbitSurvey(player, "orbit:c");
        EchoTerminalProgress.get(player).recordMoonSurvey(player, "moon:a");
        EchoTerminalProgress.get(player).recordMoonSurvey(player, "moon:b");
        EchoTerminalProgress.get(player).recordMoonSurvey(player, "moon:c");
        EchoTerminalProgress.get(player).recordMarsSurvey(player, "mars:a");
        EchoTerminalProgress.get(player).recordMarsSurvey(player, "mars:b");
        EchoTerminalProgress.get(player).recordMarsSurvey(player, "mars:c");
        EchoTerminalProgress.get(player).recordEuropaSurvey(player, "europa:a");
        EchoTerminalProgress.get(player).recordEuropaSurvey(player, "europa:b");
        EchoTerminalProgress.get(player).recordEuropaSurvey(player, "europa:c");
        EchoTerminalProgress.get(player).recordNexusStabilization(player, "nexus:c");
        EchoTerminalSnapshot needsContract = EchoTerminalSnapshot.from(player);
        helper.assertTrue(needsContract.nextObjective().contains("faction contract"),
                "Complete surveys should point to the required faction contract");
        helper.assertTrue(needsContract.scanRequirement().contains("pledge"),
                "Missing faction pledge should remain explicit before the final seal");

        EchoTerminalProgress.get(player).alignFaction(player, FactionPledgeItem.Faction.VOID_SALVAGERS);
        EchoTerminalSnapshot activeContract = EchoTerminalSnapshot.from(player);
        helper.assertTrue(activeContract.nextObjective().contains("Orbital Alloy"),
                "Active faction contract guidance should name the missing proof");
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        player.getInventory().add(new ItemStack(ModItems.ORBITAL_ALLOY.get()));
        player.getInventory().add(new ItemStack(ModItems.VACUUM_CIRCUIT.get()));
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        EchoTerminalSnapshot finalSnapshot = EchoTerminalSnapshot.from(player);
        helper.assertTrue(finalSnapshot.nextObjective().contains("Orbital Remnants arc complete"),
                "Final terminal state should clearly name completed surveys and a faction contract");
        helper.assertTrue(finalSnapshot.missionHelp().contains("Orbital Remnants arc complete"),
                "Final terminal help should name the completed orbital arc");
        helper.assertTrue(finalSnapshot.scanReport().contains("Orbital Remnants arc complete"),
                "The scan that completes the faction contract should emit one final completion report");
        helper.assertTrue(finalSnapshot.finalComplete(),
                "Final terminal state should only set the final-complete flag after the network is sealed");
        int stabilizedCores = count(player.getInventory(), ModItems.STABILIZED_ECHO_CORE.get());
        int nexusDust = count(player.getInventory(), ModItems.NEXUS_DUST.get());
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(count(player.getInventory(), ModItems.STABILIZED_ECHO_CORE.get()) == stabilizedCores,
                "Repeated final scans should not duplicate final seal core rewards");
        helper.assertTrue(count(player.getInventory(), ModItems.NEXUS_DUST.get()) == nexusDust,
                "Repeated final scans should not duplicate final seal dust rewards");
        helper.assertTrue(EchoTerminalProgress.get(player).lastTerminalReport().contains("Orbital Remnants arc complete"),
                "Repeated final scans should refresh completion guidance instead of faction cooldown noise");
        helper.succeed();
    }

    private static void terminalMissionCacheState(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress.reset(player);
        EchoTerminalProgress fresh = EchoTerminalProgress.get(player);
        helper.assertTrue(fresh.claimedTerminalMissionCaches().isBlank(),
                "Old saves should default to no claimed terminal mission caches");
        helper.assertTrue(fresh.markTerminalMissionCacheClaimed(player, "earth_calibration"),
                "First terminal mission cache claim should persist");
        helper.assertFalse(EchoTerminalProgress.get(player).markTerminalMissionCacheClaimed(player, "earth_calibration"),
                "Terminal mission cache claims should be once-only");
        helper.assertTrue(EchoTerminalProgress.get(player).hasTerminalMissionCacheClaimed("earth_calibration"),
                "Claimed terminal mission cache ids should round-trip through Orbital progress");
        helper.succeed();
    }

    private static void terminalMissionIntegration(GameTestHelper helper) {
        try {
            Class<?> commonIntegration = Class.forName(
                    "com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalCommonIntegration");
            commonIntegration.getMethod("register").invoke(null);

            Class<?> providerClass = Class.forName(
                    "com.knoxhack.echoorbitalremnants.integration.OrbitalMissionProvider");
            Object provider = providerClass.getField("INSTANCE").get(null);
            java.lang.reflect.Method missionsMethod = providerClass.getMethod(
                    "missions", net.minecraft.world.entity.player.Player.class);
            java.lang.reflect.Method snapshotMethod = providerClass.getMethod(
                    "snapshot", net.minecraft.world.entity.player.Player.class, Identifier.class);

            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            EchoTerminalProgress.reset(player);
            markAshfallNexusChoice(helper, player);
            Identifier earthCalibration = id("earth_calibration");
            List<?> missions = (List<?>) missionsMethod.invoke(provider, player);
            helper.assertTrue(missions.size() == 12, "Orbital Terminal provider should expose all planned mission records");
            Object freshSnapshot = snapshotMethod.invoke(provider, player, earthCalibration);
            helper.assertTrue("UNLOCKED".equals(snapshotStatus(freshSnapshot)),
                    "Earth calibration mission should be active before contact");

            EchoTerminalProgress.get(player).markOrbitalContact(player);
            Object readySnapshot = snapshotMethod.invoke(provider, player, earthCalibration);
            helper.assertTrue("CLAIMABLE".equals(snapshotStatus(readySnapshot)),
                    "Completed Orbital mission records should expose a claimable cache");

            EchoTerminalProgress.get(player).markTerminalMissionCacheClaimed(player, "earth_calibration");
            Object claimedSnapshot = snapshotMethod.invoke(provider, player, earthCalibration);
            helper.assertTrue("CLAIMED".equals(snapshotStatus(claimedSnapshot)),
                    "Claimed mission cache status should be visible in the Terminal mission snapshot");

            Class<?> missionRegistry = Class.forName("com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry");
            Object registeredProvider = missionRegistry.getMethod("provider", Identifier.class)
                    .invoke(null, id("orbital_remnants"));
            helper.assertTrue((Boolean) registeredProvider.getClass().getMethod("isPresent").invoke(registeredProvider),
                    "Orbital common integration should register its Terminal mission provider");
            Class<?> actionRegistry = Class.forName("com.knoxhack.echoterminal.api.TerminalActionRegistry");
            java.lang.reflect.Field handlersField = actionRegistry.getDeclaredField("HANDLERS");
            handlersField.setAccessible(true);
            Map<?, ?> handlers = (Map<?, ?>) handlersField.get(null);
            helper.assertTrue(hasTerminalActionHandler(handlers, id("orbital"), id("scan")),
                    "Orbital command tab should register a server-side SCAN action");
            helper.assertTrue(hasTerminalActionHandler(handlers, id("orbital_survey"), id("scan")),
                    "Orbital survey tab should register a server-side SCAN action");
            helper.assertTrue(hasTerminalActionHandler(handlers, id("orbital_echo"),
                            Identifier.fromNamespaceAndPath("echoterminal", "mission_action")),
                    "Orbital ECHO tab should register the shared Terminal mission action");
            helper.succeed();
        } catch (ClassNotFoundException missingTerminal) {
            helper.succeed();
        } catch (ReflectiveOperationException | RuntimeException error) {
            helper.assertTrue(false, "Terminal mission integration reflection failed: " + error.getMessage());
        }
    }

    private static void terminalLoreTaxonomy(GameTestHelper helper) {
        helper.assertTrue("ECHO-0 ROUTE CHAIN".equals(OrbitalMissionProvider.INSTANCE.chapter().title()),
                "Orbital mission chapter should render as the ECHO-0 route chain");
        assertOrbitalTabChrome(helper,
                "com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalIntegration$OrbitalCommandTab",
                "Orbital Command");
        assertOrbitalTabChrome(helper,
                "com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalIntegration$OrbitalSurveyTab",
                "Route Survey");
        assertOrbitalTabChrome(helper,
                "com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalIntegration$OrbitalEchoTab",
                "ECHO-0 Records");
        helper.succeed();
    }

    private static void assertOrbitalTabChrome(GameTestHelper helper, String className, String title) {
        try {
            Class<?> tabClass = Class.forName(className);
            Constructor<?> constructor = tabClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            TerminalTab tab = (TerminalTab) constructor.newInstance();
            TerminalTabChrome chrome = tab.chrome();
            helper.assertTrue(title.equals(chrome.shortTitle()),
                    className + " should render as " + title);
            helper.assertTrue(TerminalTabChrome.GROUP_ORBITAL.equals(chrome.group()),
                    title + " should live in the ORBITAL group");
        } catch (ReflectiveOperationException | RuntimeException error) {
            helper.assertTrue(false, "Orbital terminal taxonomy reflection failed: " + error.getMessage());
        }
    }

    private static void betaCacheSupport(GameTestHelper helper) {
        var level = helper.getLevel();
        List<GroundRecoverySite> sites = GroundRecoverySites.seedStarterSites(level, new BlockPos(20, 10, 70));
        helper.assertTrue(sites.size() == 5, "Starter calibration should seed five critical Earth recovery sites");
        helper.assertTrue(hasCacheWith(level, sitePos(sites, GroundRecoverySiteType.ABANDONED_LAUNCH_PAD), ModItems.ROCKET_NOSE_CONE.get()),
                "Starter launch site should place a rocket-part support cache");
        helper.assertTrue(hasCacheWith(level, sitePos(sites, GroundRecoverySiteType.CRASHED_SATELLITE_FIELD), ModItems.ORBITAL_TRANSPONDER.get()),
                "Starter satellite field should place transponder support");
        helper.assertTrue(hasCacheWith(level, sitePos(sites, GroundRecoverySiteType.ORBITAL_COMMS_ARRAY), ModItems.NAVIGATION_CHIP.get()),
                "Starter comms site should place a navigation support cache");
        helper.assertTrue(hasCacheWith(level, sitePos(sites, GroundRecoverySiteType.CRYO_CREW_BUNKER), ModItems.SEALED_SUIT_FRAGMENT.get()),
                "Starter cryo bunker should place sealed suit fragments");
        helper.assertTrue(hasCacheWith(level, sitePos(sites, GroundRecoverySiteType.FALLEN_ESCAPE_POD), ModItems.SALVAGED_ENGINE.get()),
                "Starter escape pod should place engine salvage");

        OrbitalDebrisField.seedArrivalField(level, new BlockPos(80, 10, 70));
        helper.assertTrue(hasCacheCount(level, new BlockPos(80, 10, 70), ModItems.EMERGENCY_OXYGEN_CELL.get(), 6),
                "Arrival support multiplier should make orbital emergency oxygen recoverable");
        MarsAshBasin.seedLandingSite(level, new BlockPos(120, 10, 70));
        helper.assertTrue(hasCacheWith(level, new BlockPos(120, 10, 70), ModItems.SUIT_SEALANT_PATCH.get()),
                "Mars arrival should include pressure recovery support");
        helper.succeed();
    }

    private static void earlyGroundRecovery(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress.reset(player);
        markAshfallNexusChoice(helper, player);
        BlockPos scanOrigin = helper.absolutePos(new BlockPos(20, 10, 70));
        player.setPos(scanOrigin.getX() + 0.5D, scanOrigin.getY(), scanOrigin.getZ() + 0.5D);
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);

        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.hasGroundRecoverySites(), "First terminal scan should seed Earth recovery site records");
        helper.assertTrue(progress.groundRecoverySites().size() == 5, "First terminal scan should track all five critical recovery sites");
        helper.assertFalse(EchoTerminalSnapshot.from(player).groundSiteLines().isEmpty(), "Terminal snapshot should expose ground site lines");

        for (GroundRecoverySite site : progress.groundRecoverySites()) {
            player.setPos(site.pos().getX() + 0.5D, site.pos().getY(), site.pos().getZ() + 0.5D);
            com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        }

        progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.allGroundRecoverySitesComplete(), "Scanning near each tracked landmark should complete Earth recovery");
        helper.assertTrue(EchoTerminalSnapshot.from(player).nextObjective().contains("Build launch"),
                "Completed ground recovery should move terminal guidance into launch prep");
        helper.succeed();
    }

    private static void earlyLaunchToOrbit(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        placeLaunchComplex(helper, player, new BlockPos(6, 1, 6));
        giveAssemblyParts(player);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.PRESSURIZED_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.PRESSURIZED_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.PRESSURIZED_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.MAGNETIC_BOOTS.get()));
        player.getInventory().add(new ItemStack(ModItems.OXYGEN_TANK.get()));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.EMERGENCY_ROCKET.get()));

        InteractionResult result = ModItems.EMERGENCY_ROCKET.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        helper.assertTrue(result == InteractionResult.SUCCESS_SERVER, "Prepared Emergency Rocket launch should succeed");
        helper.assertTrue(progress.lowOrbitReached(), "Emergency Rocket launch should mark Low Earth Orbit reached");
        helper.assertTrue(progress.hasEarthReturnPoint(), "Emergency Rocket launch should save an Earth return vector");
        helper.succeed();
    }

    private static void betaFactionContracts(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.VOID_SALVAGER_MARKER.get()));
        ModItems.VOID_SALVAGER_MARKER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.voidSalvagerStanding() == FactionStanding.ALIGNED,
                "Void Salvager pledge should persist aligned standing");
        helper.assertTrue(progress.factionContractStatus().contains("Void Salvager"),
                "Aligned faction should expose an active terminal contract");

        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        player.getInventory().add(new ItemStack(ModItems.ORBITAL_ALLOY.get()));
        player.getInventory().add(new ItemStack(ModItems.VACUUM_CIRCUIT.get()));
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        progress = EchoTerminalProgress.get(player);
        helper.assertTrue(progress.completedFactionContractCount() == 1,
                "Completing a faction contract should persist exactly one completion");
        helper.assertTrue(progress.lastTerminalReport().contains("Void Salvager Manifest complete"),
                "Terminal report should describe the completed faction contract");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.NAVIGATION_CHIP.get())),
                "Faction contract should grant a useful mission reward");

        int completed = progress.completedFactionContractCount();
        int chips = count(player.getInventory(), ModItems.NAVIGATION_CHIP.get());
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).completedFactionContractCount() == completed,
                "Cooldown scan should not double-complete faction contracts");
        helper.assertTrue(count(player.getInventory(), ModItems.NAVIGATION_CHIP.get()) == chips,
                "Cooldown scan should not double-grant faction contract rewards");
        helper.assertTrue(EchoTerminalSnapshot.from(player).factionContract().contains("cooling down")
                        || EchoTerminalSnapshot.from(player).factionContract().contains("Void Salvager"),
                "Terminal snapshot should expose faction contract state");
        helper.succeed();
    }

    private static void betaOrbitalRemnantContract(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ORBITAL_REMNANT_BADGE.get()));
        ModItems.ORBITAL_REMNANT_BADGE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).lastTerminalReport().contains("wrong dimension"),
                "Orbital Remnant contract should explain wrong-dimension blocking");
        player.getInventory().add(new ItemStack(ModItems.ORBIT_SURVEY_DATA.get()));
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).completedFactionContractCount() == 1,
                "Orbital Remnant contract should complete from Orbit Survey Data proof");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.OXYGEN_CANISTER.get())),
                "Orbital Remnant contract should grant oxygen support rewards");
        helper.succeed();
    }

    private static void betaVoidSalvagerContract(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.VOID_SALVAGER_MARKER.get()));
        ModItems.VOID_SALVAGER_MARKER.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.getInventory().clearContent();
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).lastTerminalReport().contains("Orbital Alloy"),
                "Void Salvager contract should name missing proof items");
        player.getInventory().add(new ItemStack(ModItems.ORBITAL_ALLOY.get()));
        player.getInventory().add(new ItemStack(ModItems.VACUUM_CIRCUIT.get()));
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).completedFactionContractCount() == 1,
                "Void Salvager contract should complete from salvage proof items");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.NAVIGATION_CHIP.get())),
                "Void Salvager contract should grant navigation support rewards");
        helper.succeed();
    }

    private static void betaNexusChoirContract(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NEXUS_CHOIR_SIGIL.get()));
        ModItems.NEXUS_CHOIR_SIGIL.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setPos(player.getX(), Config.ORBITAL_ALTITUDE.get(), player.getZ());
        player.getInventory().add(new ItemStack(ModItems.NEXUS_STABILIZER_SHARD.get()));
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).lastTerminalReport().contains("ECHO-0"),
                "Nexus Choir contract should stay locked before ECHO-0");
        helper.assertTrue(EchoTerminalProgress.get(player).completedFactionContractCount() == 0,
                "Nexus Choir contract should not complete before ECHO-0");
        EchoTerminalProgress.get(player).markEchoZeroEncountered(player);
        com.knoxhack.echoorbitalremnants.item.EchoTerminalItem.performScan(player);
        helper.assertTrue(EchoTerminalProgress.get(player).completedFactionContractCount() == 1,
                "Nexus Choir contract should complete from stabilizer shard proof after ECHO-0");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.CRYO_BATTERY.get())),
                "Nexus Choir contract should grant late-route support rewards");
        helper.succeed();
    }

    private static void midGameObjectiveChain(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.repairStationRelay(player, "orbit:relay:a");
        EchoTerminalProgress.RouteObjectiveResult duplicate = EchoTerminalProgress.get(player).repairStationRelay(player, "orbit:relay:a");
        helper.assertTrue(duplicate.duplicate(), "Station relay repairs should reject duplicate site ids");
        EchoTerminalProgress.get(player).repairStationRelay(player, "orbit:relay:b");
        helper.assertFalse(EchoTerminalProgress.get(player).stationNetworkRestored(), "Two station relay repairs should not restore the network");
        EchoTerminalProgress.get(player).repairStationRelay(player, "orbit:relay:c");
        helper.assertTrue(EchoTerminalProgress.get(player).stationNetworkRestored(), "Three station relay repairs should restore the network");

        EchoTerminalProgress.get(player).repairLunarExtractor(player, "moon:extractor:a");
        EchoTerminalProgress.get(player).repairLunarExtractor(player, "moon:extractor:b");
        EchoTerminalProgress.get(player).repairLunarExtractor(player, "moon:extractor:c");
        helper.assertTrue(EchoTerminalProgress.get(player).lunarExtractorOnline(), "Three lunar extractor repairs should bring the network online");
        helper.assertTrue(EchoTerminalProgress.get(player).marsRouteUnlocked(), "Lunar extractor completion should unlock Mars reliability");

        EchoTerminalProgress.get(player).repairMarsPressureConsole(player, "mars:console:a");
        EchoTerminalProgress.get(player).repairMarsPressureConsole(player, "mars:console:b");
        EchoTerminalProgress.get(player).repairMarsPressureConsole(player, "mars:console:c");
        helper.assertTrue(EchoTerminalProgress.get(player).marsHabitatsPressurized(), "Three Mars pressure repairs should pressurize habitats");
        helper.assertTrue(EchoTerminalProgress.get(player).europaRouteUnlocked(), "Mars habitat completion should unlock Europa prep");

        EchoTerminalProgress.get(player).repairEuropaThermalArray(player, "europa:array:a");
        EchoTerminalProgress.get(player).repairEuropaThermalArray(player, "europa:array:b");
        EchoTerminalProgress.get(player).repairEuropaThermalArray(player, "europa:array:c");
        helper.assertTrue(EchoTerminalProgress.get(player).europaArrayCalibrated(), "Three Europa array repairs should calibrate Deep Space prep");
        helper.assertTrue(EchoTerminalProgress.get(player).deepSpaceProtocolUnlocked(), "Europa array completion should unlock Deep Space Protocol");
        helper.assertTrue(EchoTerminalProgress.get(player).allMidGameObjectivesComplete(), "All four mid-game route chains should complete together");
        helper.succeed();
    }

    private static void midGameRouteGates(GameTestHelper helper) {
        var shuttlePlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        shuttlePlayer.setPos(shuttlePlayer.getX(), Config.ORBITAL_ALTITUDE.get(), shuttlePlayer.getZ());
        EchoTerminalProgress shuttleProgress = EchoTerminalProgress.get(shuttlePlayer);
        shuttleProgress.restoreStationLifeSupport(shuttlePlayer);
        shuttlePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ORBITAL_SHUTTLE.get()));
        InteractionResult lockedShuttle = ModItems.ORBITAL_SHUTTLE.get().use(helper.getLevel(), shuttlePlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(lockedShuttle == InteractionResult.CONSUME, "Orbital Shuttle should require Station Network restoration");
        EchoTerminalProgress.get(shuttlePlayer).repairStationRelay(shuttlePlayer, "orbit:a");
        EchoTerminalProgress.get(shuttlePlayer).repairStationRelay(shuttlePlayer, "orbit:b");
        EchoTerminalProgress.get(shuttlePlayer).repairStationRelay(shuttlePlayer, "orbit:c");
        InteractionResult openShuttle = ModItems.ORBITAL_SHUTTLE.get().use(helper.getLevel(), shuttlePlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(openShuttle == InteractionResult.SUCCESS_SERVER, "Restored Station Network should open the Lunar route gate");

        var marsPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        marsPlayer.setPos(marsPlayer.getX(), Config.ORBITAL_ALTITUDE.get(), marsPlayer.getZ());
        EchoTerminalProgress.get(marsPlayer).unlockMarsRoute(marsPlayer);
        marsPlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MARS_TRANSFER_WINDOW.get()));
        InteractionResult lockedMars = ModItems.MARS_TRANSFER_WINDOW.get().use(helper.getLevel(), marsPlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(lockedMars == InteractionResult.CONSUME, "Mars route should require Helium Extractor restoration when mid-game objectives are enabled");
        EchoTerminalProgress.get(marsPlayer).repairLunarExtractor(marsPlayer, "moon:a");
        EchoTerminalProgress.get(marsPlayer).repairLunarExtractor(marsPlayer, "moon:b");
        EchoTerminalProgress.get(marsPlayer).repairLunarExtractor(marsPlayer, "moon:c");
        InteractionResult openMars = ModItems.MARS_TRANSFER_WINDOW.get().use(helper.getLevel(), marsPlayer, InteractionHand.MAIN_HAND);
        helper.assertTrue(openMars == InteractionResult.SUCCESS_SERVER, "Restored Helium Extractors should open Mars transfer");
        helper.succeed();
    }

    private static void midGameRecipesAndSites(GameTestHelper helper) {
        assertRecipe(helper, "machine_station_relay_fuse");
        assertRecipe(helper, "machine_helium_extractor_core");
        assertRecipe(helper, "machine_pressure_regulator");
        assertRecipe(helper, "machine_europa_probe_array");
        helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(RouteTerrainGenerator.Route.ORBIT).is(ModBlocks.STATION_RELAY_NODE.get()),
                "Orbit deep sites should use Station Relay Nodes");
        helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(RouteTerrainGenerator.Route.MOON).is(ModBlocks.HELIUM_EXTRACTOR_NODE.get()),
                "Moon deep sites should use Helium Extractor Nodes");
        helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(RouteTerrainGenerator.Route.MARS).is(ModBlocks.MARS_PRESSURE_CONSOLE.get()),
                "Mars deep sites should use pressure consoles");
        helper.assertTrue(RouteTerrainGenerator.routeObjectiveBlock(RouteTerrainGenerator.Route.EUROPA).is(ModBlocks.EUROPA_THERMAL_ARRAY.get()),
                "Europa deep sites should use thermal arrays");
        helper.assertTrue(RouteTerrainGenerator.cacheItems(RouteTerrainGenerator.Route.MOON, 1).stream().anyMatch(stack -> stack.is(ModItems.HELIUM_EXTRACTOR_CORE.get())),
                "Moon cache variants should include mid-game repair resources");
        helper.succeed();
    }

    private static void europaCryoWarden(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var warden = ModEntities.EUROPA_CRYO_WARDEN.get().create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(warden != null, "Europa Cryo Warden should be registered and spawnable");
        if (warden != null) {
            warden.die(player.damageSources().playerAttack(player));
        }
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.THERMAL_STABILIZER.get())),
                "Europa Cryo Warden should grant Thermal Stabilizer reward once through its defeat hook");
        helper.assertFalse(EchoTerminalProgress.get(player).echoZeroEncountered(),
                "Europa Cryo Warden defeat should not affect ECHO-0 completion");
        helper.succeed();
    }

    private static void bossIdentity(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos playerPos = helper.absolutePos(new BlockPos(8, 2, 8));
        player.setPos(playerPos.getX() + 0.5D, playerPos.getY(), playerPos.getZ() + 0.5D);

        CorruptedDockingAiEntity dockingAi = ModEntities.CORRUPTED_DOCKING_AI.get()
                .create(helper.getLevel(), EntitySpawnReason.EVENT);
        AbandonedCaptainEntity captain = ModEntities.ABANDONED_CAPTAIN.get()
                .create(helper.getLevel(), EntitySpawnReason.EVENT);
        EuropaCryoWardenEntity warden = ModEntities.EUROPA_CRYO_WARDEN.get()
                .create(helper.getLevel(), EntitySpawnReason.EVENT);
        EchoZeroEntity echoZero = ModEntities.ECHO_ZERO.get()
                .create(helper.getLevel(), EntitySpawnReason.EVENT);
        helper.assertTrue(dockingAi != null, "Corrupted Docking AI should be spawnable");
        helper.assertTrue(captain != null, "Abandoned Captain should be spawnable");
        helper.assertTrue(warden != null, "Europa Cryo Warden should be spawnable");
        helper.assertTrue(echoZero != null, "ECHO-0 should be spawnable");

        if (dockingAi == null || captain == null || warden == null || echoZero == null) {
            helper.assertTrue(false, "Boss identity test requires all orbital boss entities");
            return;
        }

        placeBossForIdentityTest(dockingAi, player, 2.0D, 0.0D);
        placeBossForIdentityTest(captain, player, -2.0D, 0.0D);
        placeBossForIdentityTest(warden, player, 0.0D, 2.0D);
        placeBossForIdentityTest(echoZero, player, 0.0D, -2.0D);
        helper.getLevel().addFreshEntity(dockingAi);
        helper.getLevel().addFreshEntity(captain);
        helper.getLevel().addFreshEntity(warden);
        helper.getLevel().addFreshEntity(echoZero);

        dockingAi.setHealth(dockingAi.getMaxHealth() * 0.30F);
        captain.setHealth(captain.getMaxHealth() * 0.30F);
        warden.setHealth(warden.getMaxHealth() * 0.30F);
        echoZero.setHealth(echoZero.getMaxHealth() * 0.30F);
        for (int i = 0; i < 150; i++) {
            dockingAi.tick();
            captain.tick();
            warden.tick();
            echoZero.tick();
        }

        helper.assertTrue(dockingAi.getEncounterPhase() >= 3, "Docking AI should expose later-phase airlock pressure");
        helper.assertTrue(captain.getEncounterPhase() >= 3, "Abandoned Captain should expose later-phase crew pressure");
        helper.assertTrue(warden.getEncounterPhase() >= 3, "Europa Cryo Warden should expose later-phase thermal pressure");
        helper.assertTrue(echoZero.getEncounterPhase() >= 3, "ECHO-0 should expose later-phase quarantine pressure");
        SuitState state = SuitState.get(player);
        helper.assertTrue(state.oxygen() < 100 || state.pressure() < 100 || state.radiation() > 0,
                "Orbital boss identity pulses should pressure at least one suit system");

        dockingAi.die(player.damageSources().playerAttack(player));
        captain.die(player.damageSources().playerAttack(player));
        warden.die(player.damageSources().playerAttack(player));
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.NAVIGATION_CHIP.get())),
                "Corrupted Docking AI should reward navigation recovery");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.MARTIAN_SILICA.get())),
                "Abandoned Captain should reward Mars transfer lore/materials");
        helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.THERMAL_STABILIZER.get())),
                "Europa Cryo Warden should reward thermal stabilization");
        int blackBoxCount = count(player.getInventory(), ModItems.ORBITAL_BLACK_BOX.get());
        helper.assertTrue(blackBoxCount == 1,
                "Orbital boss rewards should grant exactly one duplicate-safe Orbital Black Box");

        int nexusCoreCount = count(player.getInventory(), ModItems.NEXUS_DRIVE_CORE.get());
        helper.assertTrue(EchoZeroEntity.completeEncounter(player), "ECHO-0 should complete the final orbital protocol once");
        helper.assertFalse(EchoZeroEntity.completeEncounter(player), "ECHO-0 should not duplicate the final protocol reward");
        helper.assertTrue(count(player.getInventory(), ModItems.NEXUS_DRIVE_CORE.get()) == nexusCoreCount + 1,
                "ECHO-0 should grant exactly one Nexus Drive Core");
        helper.assertTrue(count(player.getInventory(), ModItems.ORBITAL_BLACK_BOX.get()) == blackBoxCount,
                "ECHO-0 completion should not duplicate an existing Orbital Black Box");
        echoZero.discard();
        helper.succeed();
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
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, path);
    }

    private static void markAshfallNexusChoice(GameTestHelper helper, net.minecraft.world.entity.player.Player player) {
        if (!net.neoforged.fml.ModList.get().isLoaded("echoashfallprotocol")) {
            return;
        }
        try {
            Class<?> postClass = Class.forName("com.knoxhack.echoashfallprotocol.endgame.PostNexusData");
            Class<?> pathClass = Class.forName("com.knoxhack.echoashfallprotocol.endgame.PostNexusData$NexusPath");
            Object restorePath = enumValue(pathClass, "RESTORE");
            Object postNexus = postClass.getMethod("get", net.minecraft.world.entity.player.Player.class)
                    .invoke(null, player);
            postClass.getMethod("setSelectedPath", pathClass).invoke(postNexus, restorePath);
            setAshfallPostNexusAttachment(player, postNexus);
            if (player instanceof ServerPlayer serverPlayer) {
                postClass.getMethod("saveAndSync", ServerPlayer.class, postClass).invoke(null, serverPlayer, postNexus);
            }

            Class<?> worldClass = Class.forName("com.knoxhack.echoashfallprotocol.world.NexusWorldData");
            Class<?> stateClass = Class.forName("com.knoxhack.echoashfallprotocol.world.NexusWorldData$WorldState");
            Object restoredState = enumValue(stateClass, "RESTORED");
            Object worldData = worldClass.getMethod("get", net.minecraft.server.level.ServerLevel.class)
                    .invoke(null, helper.getLevel().getServer().overworld());
            worldClass.getMethod("setChoice", stateClass, BlockPos.class, String.class)
                    .invoke(worldData, restoredState, helper.absolutePos(BlockPos.ZERO), "GameTest");
        } catch (ReflectiveOperationException | RuntimeException error) {
            helper.assertTrue(false, "GameTest could not seed Ashfall Nexus choice: " + error.getMessage());
        }
    }

    private static void setAshfallPostNexusAttachment(net.minecraft.world.entity.player.Player player, Object postNexus)
            throws ReflectiveOperationException {
        Class<?> attachmentsClass = Class.forName("com.knoxhack.echoashfallprotocol.registry.ModAttachments");
        Object supplier = attachmentsClass.getField("POST_NEXUS_DATA").get(null);
        Object attachmentType = supplier.getClass().getMethod("get").invoke(supplier);
        for (Method method : player.getClass().getMethods()) {
            if (!method.getName().equals("setData") || method.getParameterCount() != 2) {
                continue;
            }
            if (method.getParameterTypes()[0].isAssignableFrom(attachmentType.getClass())) {
                method.invoke(player, attachmentType, postNexus);
                return;
            }
        }
        throw new NoSuchMethodException("Player#setData for Ashfall post-Nexus data");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object enumValue(Class<?> enumClass, String name) {
        return Enum.valueOf(enumClass.asSubclass(Enum.class), name);
    }

    private static String snapshotStatus(Object snapshot) throws ReflectiveOperationException {
        Object status = snapshot.getClass().getMethod("status").invoke(snapshot);
        return status == null ? "" : status.toString();
    }

    private static boolean hasTerminalActionHandler(Map<?, ?> handlers, Identifier tabId, Identifier actionId)
            throws ReflectiveOperationException {
        for (Object key : handlers.keySet()) {
            Identifier keyTabId = terminalActionKeyId(key, "tabId");
            Identifier keyActionId = terminalActionKeyId(key, "actionId");
            if (tabId.equals(keyTabId) && actionId.equals(keyActionId)) {
                return true;
            }
        }
        return false;
    }

    private static Identifier terminalActionKeyId(Object key, String methodName) throws ReflectiveOperationException {
        Method method = key.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (Identifier) method.invoke(key);
    }

    private static void assertRecipe(GameTestHelper helper, String path) {
        Identifier recipeId = id(path);
        boolean exists = helper.getLevel().recipeAccess().getRecipes().stream()
                .anyMatch(holder -> holder.id().identifier().equals(recipeId));
        helper.assertTrue(exists, "Required survival recipe missing: " + recipeId);
    }

    private static void assertNoRecipe(GameTestHelper helper, String path) {
        Identifier recipeId = id(path);
        boolean exists = helper.getLevel().recipeAccess().getRecipes().stream()
                .anyMatch(holder -> holder.id().identifier().equals(recipeId));
        helper.assertFalse(exists, "Recipe should not be available through normal survival crafting: " + recipeId);
    }

    private static String expectedSiteName(RouteTerrainGenerator.Route route, int variant) {
        int normalized = Math.floorMod(variant, 3);
        return switch (route) {
            case ORBIT -> switch (normalized) {
                case 1 -> "docking rib corridor";
                case 2 -> "solar breaker yard";
                default -> "station relay spine";
            };
            case MOON -> switch (normalized) {
                case 1 -> "scar drill cairn";
                case 2 -> "Nexus impact survey pit";
                default -> "helium extractor camp";
            };
            case MARS -> switch (normalized) {
                case 1 -> "pressure pipe yard";
                case 2 -> "dust-shield pylon";
                default -> "buried habitat wing";
            };
            case EUROPA -> switch (normalized) {
                case 1 -> "frozen cable substation";
                case 2 -> "cryo vault vent";
                default -> "thermal array lab";
            };
            case NEXUS -> switch (normalized) {
                case 1 -> "folded station bridge";
                case 2 -> "Nexus growth cluster";
                default -> "Nexus anchor island";
            };
        };
    }

    private static BlockPos placeLaunchComplex(GameTestHelper helper, net.minecraft.world.entity.player.Player player, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                helper.setBlock(center.offset(x, 0, z), ModBlocks.LAUNCH_PLATFORM.get());
            }
        }
        BlockPos framePos = center.above();
        helper.setBlock(framePos, ModBlocks.ROCKET_ASSEMBLY_FRAME.get());
        helper.setBlock(center.offset(3, 1, 0), ModBlocks.FUEL_REFINERY.get());
        helper.setBlock(center.offset(-3, 1, 0), ModBlocks.OXYGEN_COMPRESSOR.get());
        helper.setBlock(center.offset(0, 1, 3), ModBlocks.NAVIGATION_CONSOLE.get());
        BlockPos absolute = helper.absolutePos(center);
        player.setPos(absolute.getX() + 0.5D, absolute.getY() + 2.0D, absolute.getZ() + 0.5D);
        return framePos;
    }

    private static void giveAssemblyParts(net.minecraft.world.entity.player.Player player) {
        player.getInventory().add(new ItemStack(ModItems.ROCKET_NOSE_CONE.get()));
        player.getInventory().add(new ItemStack(ModItems.FUEL_TANK.get()));
        player.getInventory().add(new ItemStack(ModItems.SALVAGED_ENGINE.get()));
        player.getInventory().add(new ItemStack(ModItems.LANDING_GEAR.get()));
        player.getInventory().add(new ItemStack(ModItems.ECHO_FLIGHT_CORE.get()));
        player.getInventory().add(new ItemStack(ModItems.NAVIGATION_COMPUTER.get()));
    }

    private static void placeBossForIdentityTest(Mob boss, net.minecraft.world.entity.player.Player player, double xOffset, double zOffset) {
        boss.setPos(player.getX() + xOffset, player.getY(), player.getZ() + zOffset);
        boss.setTarget(player);
    }

    private static int count(net.minecraft.world.entity.player.Inventory inventory, Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static BlockPos sitePos(List<GroundRecoverySite> sites, GroundRecoverySiteType type) {
        return sites.stream()
                .filter(site -> site.type() == type)
                .findFirst()
                .map(GroundRecoverySite::pos)
                .orElse(BlockPos.ZERO);
    }

    private static boolean hasCacheWith(net.minecraft.server.level.ServerLevel level, BlockPos center, Item item) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-18, -8, -18), center.offset(18, 8, 18))) {
            if (level.getBlockState(pos).is(Blocks.CHEST) && level.getBlockEntity(pos) instanceof Container container
                    && container.hasAnyMatching(stack -> stack.is(item))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCacheCount(net.minecraft.server.level.ServerLevel level, BlockPos center, Item item, int minCount) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-18, -8, -18), center.offset(18, 8, 18))) {
            if (level.getBlockState(pos).is(Blocks.CHEST) && level.getBlockEntity(pos) instanceof Container container) {
                int total = 0;
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    ItemStack stack = container.getItem(slot);
                    if (stack.is(item)) {
                        total += stack.getCount();
                    }
                }
                if (total >= minCount) {
                    return true;
                }
            }
        }
        return false;
    }
}
