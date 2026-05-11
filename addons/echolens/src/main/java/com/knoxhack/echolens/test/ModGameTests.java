package com.knoxhack.echolens.test;

import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.ILensInspectionService;
import com.knoxhack.echolens.api.LensAccessPolicy;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoProvider;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensReport;
import com.knoxhack.echolens.api.LensScanMode;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import com.knoxhack.echolens.config.LensConfig;
import com.knoxhack.echolens.integration.LensCoreIntegration;
import com.knoxhack.echolens.provider.BlockStatsProvider;
import com.knoxhack.echolens.provider.EntityStatsProvider;
import com.knoxhack.echolens.provider.IntegrationStatusProvider;
import com.knoxhack.echolens.provider.SafeInventoryProvider;
import com.knoxhack.echolens.registry.LensInspectionService;
import com.knoxhack.echolens.registry.LensProviderRegistry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
