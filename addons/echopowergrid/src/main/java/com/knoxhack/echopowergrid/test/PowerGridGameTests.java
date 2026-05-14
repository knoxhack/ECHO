package com.knoxhack.echopowergrid.test;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.api.PowerGridSnapshot;
import com.knoxhack.echopowergrid.block.entity.GeneratorBlockEntity;
import com.knoxhack.echopowergrid.block.entity.BatteryBlockEntity;
import com.knoxhack.echopowergrid.block.entity.PowerConsumerBlockEntity;
import com.knoxhack.echopowergrid.grid.PowerNetworkManager;
import com.knoxhack.echopowergrid.registry.ModBlocks;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PowerGridGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoPowerGrid.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GENERATOR_CREATES_ENERGY =
            TEST_FUNCTIONS.register("generator_creates_energy", () -> PowerGridGameTests::generatorCreatesEnergy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CABLE_CONNECTS_BLOCKS =
            TEST_FUNCTIONS.register("cable_connects_blocks", () -> PowerGridGameTests::cableConnectsBlocks);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BATTERY_STORES_ENERGY =
            TEST_FUNCTIONS.register("battery_stores_energy", () -> PowerGridGameTests::batteryStoresEnergy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONSUMER_DRAWS_ENERGY =
            TEST_FUNCTIONS.register("consumer_draws_energy", () -> PowerGridGameTests::consumerDrawsEnergy);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NETWORK_REBUILDS_ON_PLACE =
            TEST_FUNCTIONS.register("network_rebuilds_on_place", () -> PowerGridGameTests::networkRebuildsOnPlace);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GENERATION_NOT_DUPLICATED_UNDER_DEFICIT =
            TEST_FUNCTIONS.register("generation_not_duplicated_under_deficit", () -> PowerGridGameTests::generationNotDuplicatedUnderDeficit);

    private PowerGridGameTests() {}

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("powergrid_hardening"));
        register(event, environment, "generator_creates_energy", GENERATOR_CREATES_ENERGY.getId());
        register(event, environment, "cable_connects_blocks", CABLE_CONNECTS_BLOCKS.getId());
        register(event, environment, "battery_stores_energy", BATTERY_STORES_ENERGY.getId());
        register(event, environment, "consumer_draws_energy", CONSUMER_DRAWS_ENERGY.getId());
        register(event, environment, "network_rebuilds_on_place", NETWORK_REBUILDS_ON_PLACE.getId());
        register(event, environment, "generation_not_duplicated_under_deficit", GENERATION_NOT_DUPLICATED_UNDER_DEFICIT.getId());
    }

    private static void generatorCreatesEnergy(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.setBlock(pos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        helper.runAfterDelay(1, () -> {
            BlockEntity be = helper.getLevel().getBlockEntity(pos);
            helper.assertTrue(be instanceof GeneratorBlockEntity, "Block entity should be GeneratorBlockEntity");
            if (be instanceof GeneratorBlockEntity gen) {
                helper.assertTrue(gen.getStoredEnergy() > 0 || gen.getGenerationRate() > 0,
                        "Creative generator should have generation rate or stored energy");
            }
            helper.succeed();
        });
    }

    private static void cableConnectsBlocks(GameTestHelper helper) {
        BlockPos genPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cablePos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos batPos = helper.absolutePos(new BlockPos(3, 2, 1));

        helper.setBlock(genPos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        helper.setBlock(cablePos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
        helper.setBlock(batPos, ModBlocks.SMALL_BATTERY_BANK.get().defaultBlockState());

        helper.runAfterDelay(2, () -> {
            BlockState cableState = helper.getBlockState(cablePos);
            boolean eastConnected = cableState.getValue(com.knoxhack.echopowergrid.block.CableBlock.EAST);
            boolean westConnected = cableState.getValue(com.knoxhack.echopowergrid.block.CableBlock.WEST);
            helper.assertTrue(eastConnected && westConnected,
                    "Cable should connect to both generator and battery: east=" + eastConnected + " west=" + westConnected);
            helper.succeed();
        });
    }

    private static void batteryStoresEnergy(GameTestHelper helper) {
        BlockPos genPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cablePos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos batPos = helper.absolutePos(new BlockPos(3, 2, 1));

        helper.setBlock(genPos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        helper.setBlock(cablePos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
        helper.setBlock(batPos, ModBlocks.SMALL_BATTERY_BANK.get().defaultBlockState());

        helper.runAfterDelay(45, () -> {
            BlockEntity be = helper.getLevel().getBlockEntity(batPos);
            helper.assertTrue(be instanceof BatteryBlockEntity, "Block entity should be BatteryBlockEntity");
            if (be instanceof BatteryBlockEntity bat) {
                helper.assertTrue(bat.getStoredEnergy() > 0,
                        "Battery should store energy after ticks: stored=" + bat.getStoredEnergy());
            }
            helper.succeed();
        });
    }

    private static void consumerDrawsEnergy(GameTestHelper helper) {
        BlockPos genPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cablePos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos conPos = helper.absolutePos(new BlockPos(3, 2, 1));

        helper.setBlock(genPos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        helper.setBlock(cablePos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
        helper.setBlock(conPos, ModBlocks.TEST_POWER_CONSUMER.get().defaultBlockState());

        helper.runAfterDelay(45, () -> {
            BlockEntity be = helper.getLevel().getBlockEntity(conPos);
            helper.assertTrue(be instanceof PowerConsumerBlockEntity, "Block entity should be PowerConsumerBlockEntity");
            if (be instanceof PowerConsumerBlockEntity con) {
                PowerGridSnapshot snap = EchoPowerGridApi.getSnapshot(helper.getLevel(), conPos);
                helper.assertTrue(snap.totalDemand() > 0 && con.isOnline(),
                        "Consumer should register demand and remain powered between network updates");
            }
            helper.succeed();
        });
    }

    private static void networkRebuildsOnPlace(GameTestHelper helper) {
        BlockPos genPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cablePos = helper.absolutePos(new BlockPos(2, 2, 1));

        helper.setBlock(genPos, ModBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        helper.runAfterDelay(1, () -> {
            PowerNetworkManager mgr = PowerNetworkManager.get(helper.getLevel());
            int before = mgr.getNetworkCount();
            helper.setBlock(cablePos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
            helper.runAfterDelay(2, () -> {
                int after = mgr.getNetworkCount();
                helper.assertTrue(after >= before, "Network count should be stable or grow after placing cable");
                helper.succeed();
            });
        });
    }

    private static void generationNotDuplicatedUnderDeficit(GameTestHelper helper) {
        BlockPos genPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos cableOnePos = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos batteryPos = helper.absolutePos(new BlockPos(3, 2, 1));
        BlockPos cableTwoPos = helper.absolutePos(new BlockPos(4, 2, 1));
        BlockPos consumerPos = helper.absolutePos(new BlockPos(5, 2, 1));

        helper.setBlock(genPos, ModBlocks.SOLAR_PANEL.get().defaultBlockState());
        helper.setBlock(cableOnePos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
        helper.setBlock(batteryPos, ModBlocks.SMALL_BATTERY_BANK.get().defaultBlockState());
        helper.setBlock(cableTwoPos, ModBlocks.LOW_VOLTAGE_CABLE.get().defaultBlockState());
        helper.setBlock(consumerPos, ModBlocks.TEST_POWER_CONSUMER.get().defaultBlockState());

        helper.runAfterDelay(45, () -> {
            BlockEntity battery = helper.getLevel().getBlockEntity(batteryPos);
            BlockEntity consumer = helper.getLevel().getBlockEntity(consumerPos);
            helper.assertTrue(battery instanceof BatteryBlockEntity, "Battery block entity should exist");
            helper.assertTrue(consumer instanceof PowerConsumerBlockEntity, "Consumer block entity should exist");
            if (battery instanceof BatteryBlockEntity bat && consumer instanceof PowerConsumerBlockEntity con) {
                helper.assertTrue(!con.isOnline(), "Undersupplied consumer should brown out instead of receiving duplicated power");
                helper.assertTrue(bat.getStoredEnergy() == 0,
                        "Battery should not charge while a higher-priority consumer is under deficit; stored=" + bat.getStoredEnergy());
            }
            helper.succeed();
        });
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
        return Identifier.fromNamespaceAndPath(EchoPowerGrid.MODID, path);
    }
}
