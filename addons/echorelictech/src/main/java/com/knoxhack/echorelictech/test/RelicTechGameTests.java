package com.knoxhack.echorelictech.test;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.registry.ModBlocks;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.registry.ModItems;
import com.knoxhack.echorelictech.server.RelicInstabilitySavedData;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RelicTechGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoRelicTech.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RELIC_ANALYZER_PLACES =
            TEST_FUNCTIONS.register("relic_analyzer_places", () -> RelicTechGameTests::relicAnalyzerPlaces);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONTAINMENT_LOCKER_PLACES =
            TEST_FUNCTIONS.register("containment_locker_places", () -> RelicTechGameTests::containmentLockerPlaces);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PHASE_ANCHOR_HAS_DATA =
            TEST_FUNCTIONS.register("phase_anchor_has_data", () -> RelicTechGameTests::phaseAnchorHasData);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NULL_BATTERY_STORES_CHARGE =
            TEST_FUNCTIONS.register("null_battery_stores_charge", () -> RelicTechGameTests::nullBatteryStoresCharge);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INSTABILITY_SAVED_DATA =
            TEST_FUNCTIONS.register("instability_saved_data", () -> RelicTechGameTests::instabilitySavedData);

    private RelicTechGameTests() {}

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("relictech_hardening"));
        register(event, environment, "relic_analyzer_places", RELIC_ANALYZER_PLACES.getId());
        register(event, environment, "containment_locker_places", CONTAINMENT_LOCKER_PLACES.getId());
        register(event, environment, "phase_anchor_has_data", PHASE_ANCHOR_HAS_DATA.getId());
        register(event, environment, "null_battery_stores_charge", NULL_BATTERY_STORES_CHARGE.getId());
        register(event, environment, "instability_saved_data", INSTABILITY_SAVED_DATA.getId());
    }

    private static void relicAnalyzerPlaces(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 0, 0);
        helper.setBlock(pos, ModBlocks.RELIC_ANALYZER.get().defaultBlockState());
        helper.assertBlock(pos, b -> b == ModBlocks.RELIC_ANALYZER.get(), b -> net.minecraft.network.chat.Component.literal("Relic Analyzer should place"));
        helper.assertTrue(helper.getBlockEntity(pos, com.knoxhack.echorelictech.block.entity.RelicAnalyzerBlockEntity.class) != null, "Relic Analyzer should have block entity");
        helper.succeed();
    }

    private static void containmentLockerPlaces(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 0, 0);
        helper.setBlock(pos, ModBlocks.CONTAINMENT_LOCKER.get().defaultBlockState());
        helper.assertBlock(pos, b -> b == ModBlocks.CONTAINMENT_LOCKER.get(), b -> net.minecraft.network.chat.Component.literal("Containment Locker should place"));
        helper.succeed();
    }

    private static void phaseAnchorHasData(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.PHASE_ANCHOR.get());
        stack.set(ModDataComponents.RELIC_DATA.get(), new RelicInstanceData(
            Identifier.fromNamespaceAndPath(EchoRelicTech.MODID, "phase_anchor"),
            RelicCondition.DAMAGED, 0, BlockPos.ZERO, "", 0, false, false, false, false, 0));
        helper.assertTrue(stack.has(ModDataComponents.RELIC_DATA.get()), "Phase Anchor should have relic data component");
        RelicInstanceData data = stack.get(ModDataComponents.RELIC_DATA.get());
        helper.assertTrue(data != null && data.relicId().getPath().equals("phase_anchor"), "Relic ID should be phase_anchor");
        helper.succeed();
    }

    private static void nullBatteryStoresCharge(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.NULL_BATTERY.get());
        stack.set(ModDataComponents.NULL_CHARGE.get(), 5);
        helper.assertTrue(stack.getOrDefault(ModDataComponents.NULL_CHARGE.get(), 0) == 5, "Null Battery should store charge 5");
        helper.succeed();
    }

    private static void instabilitySavedData(GameTestHelper helper) {
        var data = RelicInstabilitySavedData.get(helper.getLevel());
        var inst = data.get(java.util.UUID.randomUUID());
        helper.assertTrue(inst.value == 0, "Default instability should be 0");
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
        return Identifier.fromNamespaceAndPath(EchoRelicTech.MODID, path);
    }
}
