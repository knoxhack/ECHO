package com.knoxhack.echolens.test;

import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echocore.api.mission.InMemoryMissionRegistry;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.ILensInspectionService;
import com.knoxhack.echolens.api.LensAccessPolicy;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoProvider;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensProviderDiagnostic;
import com.knoxhack.echolens.api.LensReport;
import com.knoxhack.echolens.api.LensScanMode;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import com.knoxhack.echolens.api.ServerLensProvider;
import com.knoxhack.echolens.client.LensHudLayout;
import com.knoxhack.echolens.client.LensTheme;
import com.knoxhack.echolens.config.LensConfig;
import com.knoxhack.echolens.integration.LensCoreIntegration;
import com.knoxhack.echolens.integration.LensMissionCoreIntegration;
import com.knoxhack.echolens.network.LensScanRequestPacket;
import com.knoxhack.echolens.network.LensScanResponsePacket;
import com.knoxhack.echolens.network.LensServerScanStatus;
import com.knoxhack.echolens.provider.BlockStatsProvider;
import com.knoxhack.echolens.provider.EntityStatsProvider;
import com.knoxhack.echolens.provider.IntegrationStatusProvider;
import com.knoxhack.echolens.provider.ServerPrivacyProvider;
import com.knoxhack.echolens.provider.SafeInventoryProvider;
import com.knoxhack.echolens.registry.LensInspectionService;
import com.knoxhack.echolens.registry.LensProviderRegistry;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoLens.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REGISTRY_ORDERING =
            TEST_FUNCTIONS.register("provider_registry_ordering", () -> ModGameTests::providerRegistryOrdering);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DUPLICATE_REJECTION =
            TEST_FUNCTIONS.register("duplicate_provider_rejection", () -> ModGameTests::duplicateProviderRejection);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FAILURE_ISOLATION =
            TEST_FUNCTIONS.register("failing_provider_isolation", () -> ModGameTests::failingProviderIsolation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PRIVACY_FILTERING =
            TEST_FUNCTIONS.register("privacy_filtering", () -> ModGameTests::privacyFiltering);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BLOCK_SECTIONS =
            TEST_FUNCTIONS.register("block_section_generation", () -> ModGameTests::blockSectionGeneration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENTITY_SECTIONS =
            TEST_FUNCTIONS.register("entity_section_generation", () -> ModGameTests::entitySectionGeneration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CORE_REGISTRATION =
            TEST_FUNCTIONS.register("core_chapter_and_service_registration", () -> ModGameTests::coreRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OPTIONAL_NOOP =
            TEST_FUNCTIONS.register("optional_integration_noop", () -> ModGameTests::optionalIntegrationNoop);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NOOP_SERVICE =
            TEST_FUNCTIONS.register("noop_service_empty_report", () -> ModGameTests::noopServiceEmptyReport);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONFIG_REGISTRY =
            TEST_FUNCTIONS.register("config_registry_publication", () -> ModGameTests::configRegistryPublication);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REGISTER_ALL =
            TEST_FUNCTIONS.register("provider_register_all", () -> ModGameTests::providerRegisterAll);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PROVIDER_DIAGNOSTICS =
            TEST_FUNCTIONS.register("provider_diagnostics", () -> ModGameTests::providerDiagnostics);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> HUD_LAYOUT_BOUNDS =
            TEST_FUNCTIONS.register("hud_layout_bounds", () -> ModGameTests::hudLayoutBounds);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PACKET_CODEC =
            TEST_FUNCTIONS.register("server_scan_packet_codec", () -> ModGameTests::serverScanPacketCodec);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVER_PROVIDER_ORDERING =
            TEST_FUNCTIONS.register("server_provider_ordering", () -> ModGameTests::serverProviderOrdering);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVER_RESPONSE_STATUS =
            TEST_FUNCTIONS.register("server_response_status", () -> ModGameTests::serverResponseStatus);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_CORE_CONTENT =
            TEST_FUNCTIONS.register("missioncore_content_registration", () -> ModGameTests::missionCoreContentRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> THEME_COLOR_FALLBACK =
            TEST_FUNCTIONS.register("theme_color_fallback", () -> ModGameTests::themeColorFallback);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        register(event, "provider_registry_ordering", REGISTRY_ORDERING.getId());
        register(event, "duplicate_provider_rejection", DUPLICATE_REJECTION.getId());
        register(event, "failing_provider_isolation", FAILURE_ISOLATION.getId());
        register(event, "privacy_filtering", PRIVACY_FILTERING.getId());
        register(event, "block_section_generation", BLOCK_SECTIONS.getId());
        register(event, "entity_section_generation", ENTITY_SECTIONS.getId());
        register(event, "core_chapter_and_service_registration", CORE_REGISTRATION.getId());
        register(event, "optional_integration_noop", OPTIONAL_NOOP.getId());
        register(event, "noop_service_empty_report", NOOP_SERVICE.getId());
        register(event, "config_registry_publication", CONFIG_REGISTRY.getId());
        register(event, "provider_register_all", REGISTER_ALL.getId());
        register(event, "provider_diagnostics", PROVIDER_DIAGNOSTICS.getId());
        register(event, "hud_layout_bounds", HUD_LAYOUT_BOUNDS.getId());
        register(event, "server_scan_packet_codec", PACKET_CODEC.getId());
        register(event, "server_provider_ordering", SERVER_PROVIDER_ORDERING.getId());
        register(event, "server_response_status", SERVER_RESPONSE_STATUS.getId());
        register(event, "missioncore_content_registration", MISSION_CORE_CONTENT.getId());
        register(event, "theme_color_fallback", THEME_COLOR_FALLBACK.getId());
    }

    private static void providerRegistryOrdering(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(provider("second", 20));
            LensProviderRegistry.register(provider("first", 5));
            helper.assertTrue(LensProviderRegistry.providers().get(0).id().equals(id("first")),
                    "Providers should be sorted by priority.");
        });
        helper.succeed();
    }

    private static void themeColorFallback(GameTestHelper helper) {
        LensTheme theme = new LensTheme(
                0xE8071017, 0xF00B1720, 0x7738DFF4, 0x884CCBFF,
                0xFFEAF8FF, 0xFF8FA7B0, 0xFFA6E22E, 0xFFFFD166, 0xFFFF5A6E, 0xFF66D9EF);
        helper.assertTrue(theme.tone(null) == theme.text(), "Lens neutral tone should fall back to text color.");
        helper.assertTrue(theme.tone(LensTone.WARNING) == theme.warning(), "Lens warning tone should resolve warning color.");
        helper.assertTrue(theme.alpha(theme.echo(), 0.5F) == 0x8066D9EF, "Lens alpha helper should preserve RGB while scaling alpha.");
        helper.succeed();
    }

    private static void duplicateProviderRejection(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(provider("duplicate", 1));
            try {
                LensProviderRegistry.register(provider("duplicate", 2));
                throw new AssertionError("Duplicate provider id should be rejected.");
            } catch (IllegalArgumentException expected) {
                helper.assertTrue(true, "Duplicate provider id rejected.");
            }
        });
        helper.succeed();
    }

    private static void failingProviderIsolation(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(provider("good", 1));
            LensProviderRegistry.register(new LensInfoProvider() {
                @Override
                public Identifier id() {
                    return ModGameTests.id("failing");
                }

                @Override
                public int priority() {
                    return 2;
                }

                @Override
                public List<LensInfoSection> inspect(LensContext context) {
                    throw new IllegalStateException("intentional test failure");
                }
            });
            LensReport report = LensInspectionService.INSTANCE.inspect(blockContext(helper, Blocks.STONE.defaultBlockState()));
            helper.assertTrue(report.sections().stream().anyMatch(section -> section.id().equals(id("section/good"))),
                    "Good provider output should survive failing provider isolation.");
        });
        helper.succeed();
    }

    private static void privacyFiltering(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(SafeInventoryProvider.INSTANCE);
            BlockPos local = new BlockPos(1, 1, 1);
            helper.setBlock(local, Blocks.CHEST);
            LensContext context = LensContext.block(helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel(),
                    helper.absolutePos(local), Blocks.CHEST.defaultBlockState(), Blocks.CHEST.defaultBlockState().getFluidState(),
                    LensScanMode.DEEP, LensAccessPolicy.PUBLIC_ONLY);
            LensReport report = LensInspectionService.INSTANCE.inspect(context);
            helper.assertTrue(report.sections().stream()
                    .filter(section -> section.category() == LensDataCategory.INVENTORY)
                    .flatMap(section -> section.rows().stream())
                    .noneMatch(row -> row.label().getString().equals("Capacity")),
                    "Public-only inventory policy should not expose slot capacity.");
        });
        helper.succeed();
    }

    private static void blockSectionGeneration(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(BlockStatsProvider.INSTANCE);
            LensReport report = LensInspectionService.INSTANCE.inspect(blockContext(helper, Blocks.STONE.defaultBlockState()));
            helper.assertTrue(report.sections().stream().anyMatch(section -> section.id().equals(id("section/block"))),
                    "Stone should produce a block stats section.");
        });
        helper.succeed();
    }

    private static void entitySectionGeneration(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(EntityStatsProvider.INSTANCE);
            LensContext context = LensContext.entity(helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel(),
                    helper.makeMockPlayer(GameType.SURVIVAL), LensScanMode.EXPANDED, LensAccessPolicy.PUBLIC_ONLY);
            LensReport report = LensInspectionService.INSTANCE.inspect(context);
            helper.assertTrue(report.sections().stream().anyMatch(section -> section.id().equals(id("section/entity"))),
                    "Living entities should produce an entity stats section.");
        });
        helper.succeed();
    }

    private static void coreRegistration(GameTestHelper helper) {
        LensCoreIntegration.register();
        helper.assertTrue(EchoAddonRegistry.isRegistered("lens"), "Lens should register an ECHO addon chapter.");
        helper.assertTrue(EchoServiceRegistry.find(ILensInspectionService.class).isPresent(),
                "Lens inspection service should be registered in ECHO Core.");
        helper.succeed();
    }

    private static void optionalIntegrationNoop(GameTestHelper helper) {
        List<LensInfoSection> sections = IntegrationStatusProvider.INSTANCE.inspect(
                blockContext(helper, Blocks.CRAFTING_TABLE.defaultBlockState()));
        helper.assertFalse(sections.isEmpty(), "Integration provider should return safe status rows.");
        helper.succeed();
    }

    private static void noopServiceEmptyReport(GameTestHelper helper) {
        LensReport report = ILensInspectionService.NOOP.inspect(blockContext(helper, Blocks.CRAFTING_TABLE.defaultBlockState()));
        helper.assertTrue(report.isEmpty(), "No-op Lens service should return an empty report.");
        helper.succeed();
    }

    private static void configRegistryPublication(GameTestHelper helper) {
        LensConfig.registerEchoConfig();
        helper.assertTrue(EchoConfigRegistry.snapshot(EchoLens.MODID, EchoConfigSide.COMMON)
                        .filter(snapshot -> snapshot.hasEntries()).isPresent(),
                "Lens should publish common config through ECHO Core config registry.");
        helper.assertTrue(EchoConfigRegistry.snapshot(EchoLens.MODID, EchoConfigSide.CLIENT)
                        .filter(snapshot -> snapshot.hasEntries()).isPresent(),
                "Lens should publish client config through ECHO Core config registry.");
        helper.succeed();
    }

    private static void providerRegisterAll(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.registerAll(List.of(provider("bulk_second", 20), provider("bulk_first", 5)));
            helper.assertTrue(LensProviderRegistry.providers().size() == 2,
                    "registerAll should register every provider.");
            helper.assertTrue(LensProviderRegistry.providers().get(0).id().equals(id("bulk_first")),
                    "registerAll should preserve registry priority sorting.");
        });
        helper.succeed();
    }

    private static void missionCoreContentRegistration(GameTestHelper helper) {
        InMemoryMissionRegistry registry = new InMemoryMissionRegistry();
        LensMissionCoreIntegration.registerContent(registry);
        helper.assertTrue(registry.chapter(id("lens")).isPresent(), "Lens MissionCore chapter should be owned by Lens.");
        assertMission(helper, registry, "verified_deep_scan", "scan", MissionObjectiveType.SCAN_BLOCK);
        assertMission(helper, registry, "machine_diagnostic", "diagnostic", MissionObjectiveType.SCAN_BLOCK);
        assertMission(helper, registry, "index_shortcut", "shortcut", MissionObjectiveType.UNLOCK_RESEARCH);
        helper.succeed();
    }

    private static void assertMission(
            GameTestHelper helper,
            InMemoryMissionRegistry registry,
            String missionPath,
            String objectiveKey,
            MissionObjectiveType type) {
        Identifier missionId = id(missionPath);
        MissionDefinition mission = registry.missionDefinition(missionId)
                .orElseThrow(() -> new AssertionError("Missing MissionCore mission: " + missionId));
        helper.assertTrue(mission.kind() == MissionKind.SIDE_OP, "Lens MissionCore missions should be side ops.");
        helper.assertTrue(!mission.rewards().isEmpty(), "Lens MissionCore mission should have a claimable reward: " + missionId);
        helper.assertTrue(mission.objectives().size() == 1, "Lens MissionCore mission should have one direct objective: " + missionId);
        helper.assertTrue(mission.objectives().getFirst().type() == type, "Lens objective type should stay stable: " + missionId);
        String target = mission.objectives().getFirst().criteria().get("target");
        helper.assertTrue(MissionHookTargets.objectiveTarget(EchoLens.MODID, missionId, objectiveKey).toString().equals(target),
                "Lens MissionCore objective target should use MissionHookTargets: " + missionId);
    }

    private static void providerDiagnostics(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.register(provider("diagnostic", 7));
            List<LensProviderDiagnostic> diagnostics = LensProviderRegistry.diagnostics();
            helper.assertTrue(diagnostics.size() == 1, "Diagnostics should include registered providers.");
            LensProviderDiagnostic diagnostic = diagnostics.get(0);
            helper.assertTrue(diagnostic.id().equals(id("diagnostic")), "Diagnostics should expose provider id.");
            helper.assertTrue(diagnostic.priority() == 7, "Diagnostics should expose provider priority.");
            helper.assertTrue(diagnostic.loaded(), "Registered provider diagnostics should report loaded.");
            helper.assertTrue(!diagnostic.providerClass().isBlank(), "Diagnostics should expose provider class.");
        });
        helper.succeed();
    }

    private static void hudLayoutBounds(GameTestHelper helper) {
        LensHudLayout.Bounds bounds = LensHudLayout.clampPanel(-200, 600, 160, 80, 320, 180);
        helper.assertTrue(bounds.x() >= LensHudLayout.SCREEN_PADDING,
                "HUD layout should clamp negative x into the screen.");
        helper.assertTrue(bounds.y() <= 180 - 80 - LensHudLayout.SCREEN_PADDING,
                "HUD layout should clamp y to the lower screen edge.");
        LensHudLayout.ActionStrip strip = LensHudLayout.actionStrip(120, new int[]{80, 80, 80}, 12, 5, 8);
        helper.assertTrue(strip.chips().size() == 3, "Action strip should keep all actions.");
        helper.assertTrue(strip.height() > 12, "Action strip should wrap overflowing actions.");
        helper.assertTrue(strip.chips().stream().allMatch(chip -> chip.x() + chip.width() <= 120 - 8),
                "Action chips should stay inside panel padding.");
        helper.succeed();
    }

    private static void serverScanPacketCodec(GameTestHelper helper) {
        LensScanRequestPacket request = new LensScanRequestPacket(42, LensScanMode.DEEP,
                com.knoxhack.echolens.api.LensTargetKind.BLOCK, new BlockPos(3, 4, 5), -1,
                Identifier.withDefaultNamespace("stone"));
        FriendlyByteBuf requestBuffer = new FriendlyByteBuf(Unpooled.buffer());
        LensScanRequestPacket.CODEC.encode(requestBuffer, request);
        LensScanRequestPacket decodedRequest = LensScanRequestPacket.CODEC.decode(requestBuffer);
        helper.assertTrue(decodedRequest.requestId() == 42, "Request id should survive packet codec.");
        helper.assertTrue(decodedRequest.blockPos().equals(new BlockPos(3, 4, 5)),
                "Request block position should survive packet codec.");

        LensInfoSection section = LensInfoSection.of(id("section/server_test"), LensDataCategory.INTEGRATION,
                "Server", "S", LensTone.GOOD, LensVisibility.DEEP,
                List.of(LensInfoRow.of("Status", "Verified", "S", LensTone.GOOD, LensVisibility.DEEP)));
        LensScanResponsePacket response = LensScanResponsePacket.of(42, LensServerScanStatus.VERIFIED,
                "BLOCK:(3,4,5):minecraft:stone", List.of(section), "ok");
        FriendlyByteBuf responseBuffer = new FriendlyByteBuf(Unpooled.buffer());
        LensScanResponsePacket.CODEC.encode(responseBuffer, response);
        LensScanResponsePacket decodedResponse = LensScanResponsePacket.CODEC.decode(responseBuffer);
        helper.assertTrue(decodedResponse.status() == LensServerScanStatus.VERIFIED,
                "Response status should survive packet codec.");
        helper.assertTrue(decodedResponse.toSections().size() == 1,
                "Response sections should survive packet codec.");
        helper.succeed();
    }

    private static void serverProviderOrdering(GameTestHelper helper) {
        LensProviderRegistry.withClearedForTests(() -> {
            LensProviderRegistry.registerAll(List.of(serverProvider("server_second", 40), provider("client", 1),
                    serverProvider("server_first", 10)));
            List<ServerLensProvider> providers = LensProviderRegistry.serverProviders();
            helper.assertTrue(providers.size() == 2, "Only ServerLensProvider instances should be returned.");
            helper.assertTrue(providers.get(0).id().equals(id("server_first")),
                    "Server providers should preserve priority order.");
        });
        helper.succeed();
    }

    private static void serverResponseStatus(GameTestHelper helper) {
        LensScanResponsePacket response = LensScanResponsePacket.of(7, LensServerScanStatus.REDACTED,
                "BLOCK:(1,1,1):minecraft:chest",
                ServerPrivacyProvider.INSTANCE.inspect(blockContext(helper, Blocks.CHEST.defaultBlockState())),
                "redacted");
        helper.assertTrue(response.status() == LensServerScanStatus.REDACTED,
                "Redacted server scan status should be represented explicitly.");
        helper.assertTrue(response.toSections().stream().flatMap(section -> section.rows().stream())
                        .noneMatch(row -> row.value().getString().toLowerCase(java.util.Locale.ROOT).contains("diamond")),
                "Redacted response should not expose inventory contents.");
        helper.succeed();
    }

    private static LensContext blockContext(GameTestHelper helper, net.minecraft.world.level.block.state.BlockState state) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        return LensContext.block(helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel(), pos, state,
                state.getFluidState(), LensScanMode.DEEP, LensAccessPolicy.PUBLIC_ONLY);
    }

    private static LensInfoProvider provider(String path, int priority) {
        return new LensInfoProvider() {
            @Override
            public Identifier id() {
                return ModGameTests.id(path);
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<LensInfoSection> inspect(LensContext context) {
                return List.of(LensInfoSection.of(ModGameTests.id("section/" + path), LensDataCategory.IDENTITY,
                        path, "*", LensTone.INFO, LensVisibility.COMPACT,
                        List.of(LensInfoRow.of("Provider", path, "*", LensTone.INFO, LensVisibility.COMPACT))));
            }
        };
    }

    private static ServerLensProvider serverProvider(String path, int priority) {
        return new ServerLensProvider() {
            @Override
            public Identifier id() {
                return ModGameTests.id(path);
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<LensInfoSection> inspect(LensContext context) {
                return List.of(LensInfoSection.of(ModGameTests.id("section/" + path),
                        LensDataCategory.INTEGRATION, path, "S", LensTone.INFO, LensVisibility.DEEP,
                        List.of(LensInfoRow.of("Provider", path, "S", LensTone.INFO, LensVisibility.DEEP))));
            }
        };
    }

    private static void register(RegisterGameTestsEvent event, String testName, Identifier functionId) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("lens_" + testName));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1,
                false, 16);
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoLens.MODID, path);
    }

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoLens.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }
}
