package com.knoxhack.echopowergrid.registry;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.GeneratorType;
import com.knoxhack.echopowergrid.block.BatteryBlock;
import com.knoxhack.echopowergrid.block.BreakerBlock;
import com.knoxhack.echopowergrid.block.CableBlock;
import com.knoxhack.echopowergrid.block.GeneratorBlock;
import com.knoxhack.echopowergrid.block.MeterBlock;
import com.knoxhack.echopowergrid.block.SubstationBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoPowerGrid.MODID);
    private static final List<DeferredBlock<Block>> BLOCK_ITEMS = new ArrayList<>();

    // Generators
    public static final DeferredBlock<Block> HAND_CRANK_GENERATOR = registerGenerator("hand_crank_generator", 5, 100, GeneratorType.HAND_CRANK);
    public static final DeferredBlock<Block> SCRAP_BURNER_GENERATOR = registerGenerator("scrap_burner_generator", 40, 2000, GeneratorType.FUEL_BURNER);
    public static final DeferredBlock<Block> SOLAR_PANEL = registerGenerator("solar_panel", 10, 200, GeneratorType.SOLAR);
    public static final DeferredBlock<Block> CREATIVE_POWER_SOURCE = registerGenerator("creative_power_source", Long.MAX_VALUE / 4, Long.MAX_VALUE / 4, GeneratorType.CREATIVE);

    // Storage
    public static final DeferredBlock<Block> SMALL_BATTERY_BANK = registerStorage("small_battery_bank", 20000, 100, 100);

    // Cables
    public static final DeferredBlock<Block> LOW_VOLTAGE_CABLE = registerCable("low_voltage_cable", 100);
    public static final DeferredBlock<Block> INDUSTRIAL_CABLE = registerCable("industrial_cable", 500);

    // Control
    public static final DeferredBlock<Block> OUTPOST_SUBSTATION = registerSubstation("outpost_substation");
    public static final DeferredBlock<Block> EMERGENCY_BREAKER = registerBreaker("emergency_breaker");
    public static final DeferredBlock<Block> POWER_METER = registerMeter("power_meter");

    // Creative/Test
    public static final DeferredBlock<Block> CREATIVE_POWER_SINK = registerConsumer("creative_power_sink", Long.MAX_VALUE / 4);
    public static final DeferredBlock<Block> TEST_POWER_CONSUMER = registerConsumer("test_power_consumer", 20);

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static List<DeferredBlock<Block>> blockItems() {
        return List.copyOf(BLOCK_ITEMS);
    }

    public static boolean isPowerNode(BlockState state) {
        return state.getBlock() instanceof GeneratorBlock
            || state.getBlock() instanceof BatteryBlock
            || state.getBlock() instanceof CableBlock
            || state.getBlock() instanceof SubstationBlock
            || state.getBlock() instanceof BreakerBlock
            || state.getBlock() instanceof MeterBlock
            || state.getBlock() instanceof com.knoxhack.echopowergrid.block.ConsumerBlock;
    }

    public static long getTransferLimit(BlockState state) {
        if (state.getBlock() instanceof CableBlock cable) {
            return cable.getTransferLimit();
        }
        if (state.getBlock() instanceof SubstationBlock) return 500;
        if (state.getBlock() instanceof BreakerBlock breaker && !breaker.isTripped(state)) return 1000;
        return Long.MAX_VALUE; // Generators, batteries, meters have no cable-like transfer limit
    }

    private static DeferredBlock<Block> registerGenerator(String name, long genRate, long buffer, GeneratorType type) {
        return tracked(BLOCKS.registerBlock(name, p -> new GeneratorBlock(genRate, buffer, type, p), defaultProps()));
    }

    private static DeferredBlock<Block> registerStorage(String name, long capacity, long maxIn, long maxOut) {
        return tracked(BLOCKS.registerBlock(name, p -> new BatteryBlock(capacity, maxIn, maxOut, p), defaultProps()));
    }

    private static DeferredBlock<Block> registerCable(String name, long transferLimit) {
        return tracked(BLOCKS.registerBlock(name, p -> new CableBlock(transferLimit, p), cableProps()));
    }

    private static DeferredBlock<Block> registerSubstation(String name) {
        return tracked(BLOCKS.registerBlock(name, p -> new SubstationBlock(p), defaultProps()));
    }

    private static DeferredBlock<Block> registerBreaker(String name) {
        return tracked(BLOCKS.registerBlock(name, p -> new BreakerBlock(p), defaultProps()));
    }

    private static DeferredBlock<Block> registerMeter(String name) {
        return tracked(BLOCKS.registerBlock(name, p -> new MeterBlock(p), defaultProps()));
    }

    private static DeferredBlock<Block> registerConsumer(String name, long demand) {
        return tracked(BLOCKS.registerBlock(name, p -> new com.knoxhack.echopowergrid.block.ConsumerBlock(demand, p), defaultProps()));
    }

    private static java.util.function.UnaryOperator<BlockBehaviour.Properties> defaultProps() {
        return p -> p.mapColor(MapColor.COLOR_GRAY).strength(2.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static java.util.function.UnaryOperator<BlockBehaviour.Properties> cableProps() {
        return p -> p.mapColor(MapColor.COLOR_GRAY).strength(0.8F, 2.0F).sound(SoundType.COPPER).noOcclusion().dynamicShape();
    }

    private static DeferredBlock<Block> tracked(DeferredBlock<Block> block) {
        BLOCK_ITEMS.add(block);
        return block;
    }
}
