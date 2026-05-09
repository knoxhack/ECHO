package com.knoxhack.echocore.test;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiagnosticBlocker;
import com.knoxhack.echocore.api.EchoDiscoveryCategory;
import com.knoxhack.echocore.api.EchoDiscoveryEntry;
import com.knoxhack.echocore.api.EchoDiscoveryProvider;
import com.knoxhack.echocore.api.EchoDiscoveryState;
import com.knoxhack.echocore.api.EchoDialogueTree;
import com.knoxhack.echocore.api.EchoFactionAction;
import com.knoxhack.echocore.api.EchoFactionActionHandlerService;
import com.knoxhack.echocore.api.EchoFactionActionResult;
import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echocore.api.EchoFactionPoiAffinity;
import com.knoxhack.echocore.api.EchoFactionRegistry;
import com.knoxhack.echocore.api.EchoFactionStanding;
import com.knoxhack.echocore.api.EchoHazardTelemetry;
import com.knoxhack.echocore.api.EchoHandoffs;
import com.knoxhack.echocore.api.EchoProfile;
import com.knoxhack.echocore.api.EchoProfileService;
import com.knoxhack.echocore.api.EchoNpcRole;
import com.knoxhack.echocore.api.EchoPackMode;
import com.knoxhack.echocore.api.EchoProgressLedger;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.NexusCampaignService;
import com.knoxhack.echocore.api.TerminalPlacementService;
import com.knoxhack.echocore.api.TerminalRewardService;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoCore.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_SERVICE_NOOPS =
            TEST_FUNCTIONS.register("core_service_noops", () -> ModGameTests::coreServiceNoops);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_PLATFORM_CONTRACTS =
            TEST_FUNCTIONS.register("core_platform_contracts", () -> ModGameTests::corePlatformContracts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_BETA_SERVICE_CONTRACTS =
            TEST_FUNCTIONS.register("core_beta_service_contracts", () -> ModGameTests::coreBetaServiceContracts);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_FACTION_DATA =
            TEST_FUNCTIONS.register("core_faction_data", () -> ModGameTests::coreFactionData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_DISCOVERY_GRID =
            TEST_FUNCTIONS.register("core_discovery_grid", () -> ModGameTests::coreDiscoveryGrid);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("core_release"));
        register(event, environment, "core_service_noops", CORE_SERVICE_NOOPS.getId());
        register(event, environment, "core_platform_contracts", CORE_PLATFORM_CONTRACTS.getId());
        register(event, environment, "core_beta_service_contracts", CORE_BETA_SERVICE_CONTRACTS.getId());
        register(event, environment, "core_faction_data", CORE_FACTION_DATA.getId());
        register(event, environment, "core_discovery_grid", CORE_DISCOVERY_GRID.getId());
    }

    private static void coreServiceNoops(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            helper.assertFalse(EchoCoreServices.placeTerminal(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), null),
                    "Terminal placement should no-op without Terminal installed");
            helper.assertTrue(EchoCoreServices.terminalStructureBlockState().is(Blocks.AIR),
                    "Terminal structure fallback should be air");
        });
        helper.succeed();
    }

    private static void corePlatformContracts(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            EchoCoreServices.clearPlatformServicesForTests();
            EchoCoreServices.registerPackModeService(player -> EchoPackMode.FULL_SAGA);
            helper.assertTrue(EchoCoreServices.packMode(null) == EchoPackMode.FULL_SAGA,
                    "Pack mode service overrides should resolve stable modes");
            EchoProgressLedger ledger = EchoProgressLedger.empty()
                    .withMilestone("ashfall:nexus")
                    .withMilestone("ashfall:nexus")
                    .withMilestone("stationfall.blackbox_retrieved")
                    .withMilestone(EchoHandoffs.STATIONFALL_BLACKBOX_RECOVERED)
                    .withFlag("path", "restore")
                    .withActiveObjective("orbital:launch");
            helper.assertTrue(ledger.milestones().size() == 2, "Ledger milestones should de-duplicate ids and handoff aliases");
            helper.assertTrue(ledger.hasMilestone("stationfall:blackbox_recovered"),
                    "Ledger milestone lookup should accept Stationfall handoff aliases");
            helper.assertTrue("restore".equals(ledger.flag("path")), "Ledger flags should be readable");

            EchoHazardTelemetry telemetry = new EchoHazardTelemetry(80, 20, 0, 100, 100, 0, 0, 0, "")
                    .merge(new EchoHazardTelemetry(60, 70, 20, 42, 90, 0, 0, 10, "Radiation elevated."));
            helper.assertTrue(telemetry.hydration() == 60 && telemetry.radiation() == 70 && telemetry.oxygen() == 42,
                    "Hazard telemetry should merge by most dangerous values");

            EchoCoreServices.registerHazardTelemetryService(player -> {
                throw new IllegalStateException("test hazard provider failure");
            });
            helper.assertTrue(EchoCoreServices.hazardTelemetry(null).hydration() == 100,
                    "Failing hazard providers should be ignored");

            EchoCoreServices.registerRouteRecordService(player -> {
                List<EchoRouteRecord> records = new java.util.ArrayList<>();
                records.add(new EchoRouteRecord(id("z_route"), "echocore", "Z Route", "Route", "Overworld", "ACTIVE", "Test route.", false));
                records.add(new EchoRouteRecord(id("a_route"), "echocore", "A Route", "Route", "Overworld", "ACTIVE", "Test route.", false));
                records.add(new EchoRouteRecord(id("a_route"), "echocore", "Duplicate Route", "Route", "Overworld", "ACTIVE", "Ignored.", false));
                records.add(null);
                return records;
            });
            EchoCoreServices.registerRouteRecordService(player -> {
                throw new IllegalStateException("test route provider failure");
            });
            List<EchoRouteRecord> routes = EchoCoreServices.routeRecords(null);
            helper.assertTrue(routes.size() == 2, "Route record services should de-duplicate ids and ignore invalid records");
            helper.assertTrue(routes.get(0).id().equals(id("a_route")),
                    "Route record services should sort records by stable id/title order");

            EchoCoreServices.registerDiagnosticService(player -> {
                List<EchoDiagnosticBlocker> blockers = new java.util.ArrayList<>();
                blockers.add(new EchoDiagnosticBlocker(id("info_blocker"), "echocore", EchoDiagnosticBlocker.Severity.INFO,
                        "Info", "Info detail.", "Read it."));
                blockers.add(new EchoDiagnosticBlocker(id("critical_blocker"), "echocore", EchoDiagnosticBlocker.Severity.CRITICAL,
                        "Critical", "Critical detail.", "Fix it."));
                blockers.add(new EchoDiagnosticBlocker(id("critical_blocker"), "echocore", EchoDiagnosticBlocker.Severity.WARNING,
                        "Duplicate", "Ignored.", "Ignored."));
                blockers.add(null);
                return blockers;
            });
            EchoCoreServices.registerDiagnosticService(player -> {
                throw new IllegalStateException("test diagnostic provider failure");
            });
            List<EchoDiagnosticBlocker> diagnostics = EchoCoreServices.diagnostics(null);
            helper.assertTrue(diagnostics.size() == 2,
                    "Diagnostic services should de-duplicate ids and ignore invalid records");
            helper.assertTrue(diagnostics.get(0).severity() == EchoDiagnosticBlocker.Severity.CRITICAL,
                    "Diagnostics should sort critical blockers first");

            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            EchoCoreServices.registerProfileService(new EchoProfileService() {
                @Override
                public EchoProfile profile(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test profile provider failure");
                }

                @Override
                public void saveProfile(net.minecraft.server.level.ServerPlayer player, EchoProfile profile) {
                    throw new IllegalStateException("test profile save failure");
                }

                @Override
                public EchoProgressLedger progressLedger(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test ledger provider failure");
                }

                @Override
                public void saveProgressLedger(net.minecraft.server.level.ServerPlayer player,
                        EchoProgressLedger ledger) {
                    throw new IllegalStateException("test ledger save failure");
                }
            });
            helper.assertTrue(EchoCoreServices.profile(player).callsign().equals("ECHO Operator"),
                    "Failing profile service should return the empty profile fallback");
            helper.assertTrue(EchoCoreServices.progressLedger(player).milestones().isEmpty(),
                    "Failing ledger service should return an empty ledger fallback");
            EchoCoreServices.saveProfile(null, EchoProfile.empty());
            EchoCoreServices.saveProgressLedger(null, EchoProgressLedger.empty());

            EchoCoreServices.registerTerminalPlacementService(new TerminalPlacementService() {
                @Override
                public boolean placeTerminal(net.minecraft.world.level.Level level, BlockPos pos,
                        net.minecraft.world.entity.player.Player owner) {
                    throw new IllegalStateException("test terminal placement failure");
                }

                @Override
                public net.minecraft.world.level.block.state.BlockState structureBlockState() {
                    throw new IllegalStateException("test terminal block state failure");
                }

                @Override
                public boolean isTerminalBlock(net.minecraft.world.level.block.state.BlockState state) {
                    throw new IllegalStateException("test terminal block check failure");
                }
            });
            helper.assertFalse(EchoCoreServices.placeTerminal(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)), player),
                    "Failing terminal placement service should return false");
            helper.assertTrue(EchoCoreServices.terminalStructureBlockState().is(Blocks.AIR),
                    "Failing terminal block state should return air");
            helper.assertFalse(EchoCoreServices.isTerminalBlock(Blocks.STONE.defaultBlockState()),
                    "Failing terminal block check should return false");

            EchoCoreServices.registerTerminalRewardService(new TerminalRewardService() {
                @Override
                public boolean storeRewards(net.minecraft.server.level.ServerPlayer player, String missionId,
                        List<net.minecraft.world.item.ItemStack> rewards) {
                    throw new IllegalStateException("test reward store failure");
                }

                @Override
                public boolean claimRewards(net.minecraft.server.level.ServerPlayer player) {
                    throw new IllegalStateException("test reward claim failure");
                }

                @Override
                public int pendingRewardCount(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test reward count failure");
                }
            });
            helper.assertFalse(EchoCoreServices.storeTerminalRewards(null, "test", List.of()),
                    "Failing reward store should return false");
            helper.assertFalse(EchoCoreServices.claimTerminalRewards(null),
                    "Failing reward claim should return false");
            helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(player) == 0,
                    "Failing reward count should return zero");

            EchoCoreServices.registerNexusPathService(target -> {
                throw new IllegalStateException("test nexus path failure");
            });
            helper.assertFalse(EchoCoreServices.hasPostNexusChoice(player),
                    "Failing Nexus path provider should return false");
            EchoCoreServices.registerNexusCampaignService(new NexusCampaignService() {
                @Override
                public String pathId(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test campaign path failure");
                }

                @Override
                public int instability(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test campaign instability failure");
                }

                @Override
                public boolean isWarfrontComplete(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test campaign warfront failure");
                }

                @Override
                public boolean isFinalProtocolComplete(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test campaign final failure");
                }

                @Override
                public String statusLine(net.minecraft.world.entity.player.Player player) {
                    throw new IllegalStateException("test campaign status failure");
                }
            });
            helper.assertTrue(EchoCoreServices.nexusCampaignPathId(player).isBlank(),
                    "Failing campaign path should return blank");
            helper.assertTrue(EchoCoreServices.nexusInstability(player) == 0,
                    "Failing campaign instability should return zero");
            helper.assertFalse(EchoCoreServices.isNexusWarfrontComplete(player),
                    "Failing campaign warfront should return false");
            helper.assertFalse(EchoCoreServices.isNexusFinalProtocolComplete(player),
                    "Failing campaign final protocol should return false");
            helper.assertTrue(!EchoCoreServices.nexusCampaignStatusLine(player).isBlank(),
                    "Failing campaign status should return a fallback line");

            EchoCoreServices.registerIntelMirrorService((target, sourceModId, recordId, title, content) -> {
                throw new IllegalStateException("test intel mirror failure");
            });
            EchoCoreServices.mirrorIntel(null, EchoCore.MODID, "test", "Test", "Test content");
            EchoCoreServices.clearPlatformServicesForTests();
        });
        helper.succeed();
    }

    private static void coreDiscoveryGrid(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            EchoCoreServices.clearPlatformServicesForTests();
            EchoDiscoveryEntry alpha = discoveryEntry("alpha", EchoDiscoveryCategory.STRUCTURE, "Alpha Signal", 10);
            EchoDiscoveryEntry duplicateFirst = discoveryEntry("duplicate", EchoDiscoveryCategory.EVENT, "First Duplicate", 20);
            EchoDiscoveryEntry duplicateSecond = discoveryEntry("duplicate", EchoDiscoveryCategory.EVENT, "Second Duplicate", 30);
            EchoDiscoveryEntry checked = discoveryEntry("checked", EchoDiscoveryCategory.GUARDIAN, "Checked Signal", 40);
            EchoCoreServices.registerDiscoveryProvider(new com.knoxhack.echocore.api.EchoDiscoveryProvider() {
                @Override
                public List<EchoDiscoveryEntry> entries(net.minecraft.world.entity.player.Player player) {
                    return List.of(alpha, duplicateFirst, checked);
                }

                @Override
                public EchoDiscoveryState state(net.minecraft.world.entity.player.Player player, EchoDiscoveryEntry entry) {
                    return checked.id().equals(entry.id()) ? EchoDiscoveryState.CHECKED : EchoDiscoveryState.LOCKED;
                }
            });
            EchoCoreServices.registerDiscoveryProvider(player -> List.of(duplicateSecond));

            List<EchoDiscoveryEntry> entries = EchoCoreServices.discoveryEntries(null);
            helper.assertTrue(entries.size() == 3, "Discovery registry should de-duplicate feature ids");
            EchoDiscoveryEntry duplicate = EchoCoreServices.discoveryEntry(null, id("duplicate")).orElse(null);
            helper.assertTrue(duplicate != null && "First Duplicate".equals(duplicate.revealedTitle()),
                    "Discovery registry should keep the first duplicate id");

            var player = (ServerPlayer) helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(EchoCoreServices.discoveryState(player, alpha) == EchoDiscoveryState.LOCKED,
                    "Undiscovered provider-locked features should remain locked");
            helper.assertTrue(EchoCoreServices.discoverFeature(player, alpha.id()),
                    "First discovery should report a newly recorded id");
            helper.assertFalse(EchoCoreServices.discoverFeature(player, alpha.id()),
                    "Duplicate discovery should not record or notify again");
            helper.assertTrue(EchoCoreServices.hasDiscoveredFeature(player, alpha.id()),
                    "Discovery data should persist the feature id");
            helper.assertTrue(EchoCoreServices.discoveryState(player, alpha) == EchoDiscoveryState.DISCOVERED,
                    "Stored discovery should lift a provider-locked entry to discovered");
            helper.assertTrue(EchoCoreServices.discoveryState(player, checked) == EchoDiscoveryState.CHECKED,
                    "Provider live state should resolve checked entries");

            EchoCoreServices.clearPlatformServicesForTests();
            EchoDiscoveryEntry playerScoped = discoveryEntry("player_scoped", EchoDiscoveryCategory.STRUCTURE,
                    "Player Scoped Signal", 50);
            EchoCoreServices.registerDiscoveryProvider(new EchoDiscoveryProvider() {
                @Override
                public List<EchoDiscoveryEntry> entries(net.minecraft.world.entity.player.Player player) {
                    if (player == null) {
                        throw new IllegalStateException("Player-scoped discovery entries need player context.");
                    }
                    return List.of(playerScoped);
                }

                @Override
                public EchoDiscoveryState state(net.minecraft.world.entity.player.Player player, EchoDiscoveryEntry entry) {
                    return EchoDiscoveryState.CHECKED;
                }
            });
            helper.assertTrue(EchoCoreServices.platformProviderSummary().contains("discoveryProviders=1"),
                    "Platform provider summary should count discovery providers without listing entries");
            EchoDiscoveryEntry scoped = EchoCoreServices.discoveryEntries(player).stream()
                    .filter(entry -> entry.id().equals(playerScoped.id()))
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(scoped != null
                            && EchoCoreServices.discoveryState(player, scoped) == EchoDiscoveryState.CHECKED,
                    "Discovery state should resolve with player-scoped provider ownership");

            EchoCoreServices.clearPlatformServicesForTests();
            Identifier routeId = id("active_route");
            EchoCoreServices.registerRouteRecordService(routePlayer -> List.of(new EchoRouteRecord(
                    routeId,
                    "echocore",
                    "Active Route",
                    "Route",
                    "Overworld",
                    routePlayer == null ? "LOCKED" : "ACTIVE",
                    "Player-visible route record.",
                    false)));
            EchoCoreServices.registerDiscoveryProvider(routePlayer -> {
                if (routePlayer == null) {
                    return List.of();
                }
                return EchoCoreServices.routeRecords(routePlayer).stream()
                        .map(record -> new EchoDiscoveryEntry(
                                EchoCoreServices.routeDiscoveryId(record.id()),
                                id("test_chapter"),
                                EchoDiscoveryCategory.STRUCTURE,
                                record.title(),
                                "Unknown Route",
                                "Find the route in the field.",
                                record.summary(),
                                null,
                                null,
                                0xFF66E8FF,
                                record.id(),
                                60))
                        .toList();
            });
            Identifier routeDiscoveryId = EchoCoreServices.routeDiscoveryId(routeId);
            helper.assertTrue(EchoCoreServices.discoverVisibleRouteRecords(player) == 1,
                    "Visible route discovery should record a newly visible route");
            helper.assertTrue(EchoCoreServices.hasDiscoveredFeature(player, routeDiscoveryId),
                    "Visible route discovery should persist the route feature id");
            helper.assertTrue(EchoCoreServices.discoverVisibleRouteRecords(player) == 0,
                    "Visible route discovery should be once-only for already recorded routes");
        });
        helper.succeed();
    }

    private static void coreBetaServiceContracts(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            EchoCoreServices.clearPlatformServicesForTests();

            EchoProgressLedger ledger = new EchoProgressLedger(
                    new java.util.LinkedHashSet<>(List.of("orbital:launch_ready", "", "ashfall:drop_pod_ready", "nexus:path:restore")),
                    java.util.Map.of("beta.route", "ashfall_to_orbital", "", "ignored"),
                    new java.util.LinkedHashSet<>(List.of("orbital:scan_launch_site", "ashfall:repair_terminal")));
            helper.assertTrue(List.copyOf(ledger.milestones()).equals(List.of("ashfall:drop_pod_ready", EchoHandoffs.NEXUS_PROTOCOL_COMPLETE, "orbital:launch_ready")),
                    "Progress ledger ids should normalize to sorted, non-blank beta milestone ids");
            helper.assertTrue(ledger.hasMilestone("nexus:path:merge"),
                    "Progress ledger should treat legacy Nexus path milestones as Nexus completion aliases");
            helper.assertTrue("ashfall_to_orbital".equals(ledger.flag("beta.route")),
                    "Progress ledger flags should preserve canonical beta route handoff ids");
            boolean immutableLedger = false;
            try {
                ledger.milestones().add("mutation");
            } catch (UnsupportedOperationException expected) {
                immutableLedger = true;
            }
            helper.assertTrue(immutableLedger, "Progress ledger snapshots must be immutable API values");

            EchoCoreServices.registerRouteRecordService(player -> List.of(
                    new EchoRouteRecord(id("orbital_launch_chain"), "orbital_remnants", "Launch Chain",
                            "Launch", "Overworld", "IN PROGRESS", "Beta route handoff.", false),
                    new EchoRouteRecord(id("ashfall_recovery_route"), "ashfall", "Recovery Route",
                            "Survival", "Overworld", "ACTIVE", "Ashfall beta route.", false)));
            List<EchoRouteRecord> routes = EchoCoreServices.routeRecords(null);
            helper.assertTrue(routes.size() == 2, "Route record contract should expose both beta route records");
            helper.assertTrue("ashfall".equals(routes.get(0).chapterId()),
                    "Route record contract should sort by stable chapter id before title");
            helper.assertTrue(routes.stream().anyMatch(route -> route.id().equals(id("orbital_launch_chain"))),
                    "Route record contract should keep canonical Orbital handoff route id");

            java.util.concurrent.atomic.AtomicBoolean stored = new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicBoolean claimed = new java.util.concurrent.atomic.AtomicBoolean(false);
            EchoCoreServices.registerTerminalRewardService(new TerminalRewardService() {
                @Override
                public boolean storeRewards(net.minecraft.server.level.ServerPlayer player, String missionId,
                        List<net.minecraft.world.item.ItemStack> rewards) {
                    stored.set("ashfall:repair_terminal".equals(missionId) && rewards.isEmpty());
                    return true;
                }

                @Override
                public boolean claimRewards(net.minecraft.server.level.ServerPlayer player) {
                    claimed.set(true);
                    return true;
                }

                @Override
                public int pendingRewardCount(net.minecraft.world.entity.player.Player player) {
                    return -4;
                }
            });
            helper.assertTrue(EchoCoreServices.storeTerminalRewards(null, "ashfall:repair_terminal", null),
                    "Terminal reward service should accept sanitized reward lists");
            helper.assertTrue(stored.get(), "Terminal reward service should receive empty rewards instead of null");
            helper.assertTrue(EchoCoreServices.claimTerminalRewards(null),
                    "Terminal reward service should route claim calls through the registered provider");
            helper.assertTrue(claimed.get(), "Terminal reward provider should receive claim calls");
            helper.assertTrue(EchoCoreServices.pendingTerminalRewardCount(null) == 0,
                    "Terminal reward pending count should clamp provider underflow to zero");

            EchoCoreServices.clearPlatformServicesForTests();
        });
        helper.succeed();
    }

    private static void coreFactionData(GameTestHelper helper) {
        EchoFactionRegistry.withClearedForTests(() -> {
            EchoCoreServices.clearPlatformServicesForTests();
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            Identifier factionId = id("test_faction");
            Identifier contractId = id("test_contract");
            EchoCoreServices.registerFaction(new EchoFactionDefinition(
                    factionId,
                    "Test Faction",
                    "Test",
                    "Core Test",
                    "Verifies portable faction state.",
                    "None",
                    "Bring assertions.",
                    "Debug services",
                    0x72A7FF,
                    false,
                    List.of(new EchoNpcRole("quartermaster", "Quartermaster", "Supplies test state.")),
                    List.of(new EchoFactionAction(id("test_action"), "Request Supplies", "No-op test action.", 0, true)),
                    List.of(new EchoFactionContract(
                            contractId,
                            "Prove Contact",
                            "Complete the test handshake.",
                            0,
                            40,
                            "Accept and complete a core contract.",
                            "Standing",
                            "Core")),
                    List.of(new EchoFactionPoiAffinity("test_profile", "hub", 1, true)),
                    new EchoDialogueTree("Hello, operator.", List.of("Standing", "Contracts"), "Signal clear.")));
            boolean conflictingDuplicateRejected = false;
            try {
                EchoCoreServices.registerFaction(new EchoFactionDefinition(
                        factionId,
                        "Conflicting Faction",
                        "Conflict",
                        "Core Test",
                        "Conflicting duplicate.",
                        "None",
                        "Bring assertions.",
                        "Debug services",
                        0x72A7FF,
                        false,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        EchoDialogueTree.EMPTY));
            } catch (IllegalStateException expected) {
                conflictingDuplicateRejected = true;
            }
            helper.assertTrue(conflictingDuplicateRejected,
                    "Faction registry should reject conflicting duplicate definitions");

            boolean duplicateContractRejected = false;
            try {
                new EchoFactionDefinition(
                        id("duplicate_contract_faction"),
                        "Duplicate Contract Faction",
                        "Dup",
                        "Core Test",
                        "Verifies duplicate contract validation.",
                        "None",
                        "Bring assertions.",
                        "Debug services",
                        0x72A7FF,
                        false,
                        List.of(),
                        List.of(),
                        List.of(
                                new EchoFactionContract(contractId, "One", "", 0, 1, "", "", ""),
                                new EchoFactionContract(contractId, "Two", "", 0, 1, "", "", "")),
                        List.of(),
                        EchoDialogueTree.EMPTY);
            } catch (IllegalArgumentException expected) {
                duplicateContractRejected = true;
            }
            helper.assertTrue(duplicateContractRejected,
                    "Faction definitions should reject duplicate contract ids");
            EchoCoreServices.registerFactionActionHandler(new EchoFactionActionHandlerService() {
                @Override
                public boolean supports(Identifier id) {
                    return factionId.equals(id);
                }

                @Override
                public List<EchoFactionAction> actions(net.minecraft.world.entity.player.Player player,
                        com.knoxhack.echocore.api.EchoFactionProfile profile, String roleId) {
                    return List.of(new EchoFactionAction(id("test_handler_action"),
                            "Handler Action", "Exercises addon action handlers.", 0, false));
                }

                @Override
                public String localContext(net.minecraft.world.entity.player.Player player,
                        com.knoxhack.echocore.api.EchoFactionProfile profile, String roleId) {
                    return "Mock route context.";
                }

                @Override
                public EchoFactionActionResult handle(net.minecraft.server.level.ServerPlayer player,
                        Identifier id, Identifier actionId, String roleId, Identifier targetId) {
                    if (id("test_handler_action").equals(actionId)) {
                        return EchoFactionActionResult.success("Handled", "Addon action result persisted.", 5);
                    }
                    return EchoFactionActionResult.failure("Unhandled", "Unexpected action.");
                }
            });

            helper.assertTrue(EchoCoreServices.factionDefinitions().size() == 1, "Faction registry should expose definitions");
            EchoCoreServices.recordFactionInteraction(player, factionId, "quartermaster", 42L);
            var snapshot = EchoCoreServices.factionInteractionSnapshot(player, factionId, "quartermaster").orElseThrow();
            helper.assertTrue("Quartermaster".equals(snapshot.roleName()), "Snapshot should resolve NPC role names");
            helper.assertTrue(snapshot.actions().stream().anyMatch(action -> action.id().equals(id("test_handler_action"))),
                    "Snapshot should include addon-provided actions");
            helper.assertTrue("Mock route context.".equals(snapshot.localContext()),
                    "Snapshot should include addon local context");
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                EchoFactionActionResult result = EchoCoreServices.performFactionAction(
                        serverPlayer, factionId, id("test_handler_action"), "quartermaster", null);
                helper.assertTrue(result.success(), "Action handler result should report success");
                helper.assertTrue(EchoCoreServices.factionProfile(player, factionId).orElseThrow().reputation() == 5,
                        "Action handler reputation delta should persist");
            }
            helper.assertTrue(EchoCoreServices.acceptFactionContract(player, factionId, contractId),
                    "Player should be able to accept an available faction contract");
            helper.assertTrue(EchoCoreServices.completeFactionContract(player, factionId, contractId),
                    "Player should be able to complete the active faction contract");
            var profile = EchoCoreServices.factionProfile(player, factionId).orElseThrow();
            helper.assertTrue(profile.completedContracts() == 1, "Completed contracts should persist");
            helper.assertTrue(profile.standing() == EchoFactionStanding.TRUSTED, "Contract reward should raise standing");
            helper.assertTrue(EchoCoreServices.factionStandingLines(player, factionId.toString()).stream()
                            .anyMatch(line -> line.contains("Test Faction")),
                    "Compatibility standing lines should include Echo Core profile summaries");
            EchoCoreServices.clearPlatformServicesForTests();
        });
        helper.succeed();
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            String testName, Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                100,
                0,
                true,
                net.minecraft.world.level.block.Rotation.NONE,
                false,
                1,
                1,
                false,
                2);
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static EchoDiscoveryEntry discoveryEntry(
            String path, EchoDiscoveryCategory category, String title, int sortOrder) {
        return new EchoDiscoveryEntry(
                id(path),
                id("test_chapter"),
                category,
                title,
                "Unknown Signal",
                "A test hint is present.",
                "A test summary is present.",
                null,
                null,
                0xFF66E8FF,
                null,
                sortOrder);
    }

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoCore.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoCore.MODID, path);
    }
}
