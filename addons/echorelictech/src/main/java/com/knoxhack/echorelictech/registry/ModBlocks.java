package com.knoxhack.echorelictech.registry;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoRelicTech.MODID);
    private static final List<DeferredBlock<Block>> BLOCK_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> RELIC_ANALYZER = tracked(BLOCKS.registerBlock("relic_analyzer", RelicAnalyzerBlock::new, defaultProps()));
    public static final DeferredBlock<Block> PROTOTYPE_WORKBENCH = tracked(BLOCKS.registerBlock("prototype_workbench", PrototypeWorkbenchBlock::new, defaultProps()));
    public static final DeferredBlock<Block> CONTAINMENT_LOCKER = tracked(BLOCKS.registerBlock("containment_locker", ContainmentLockerBlock::new, defaultProps()));
    public static final DeferredBlock<Block> NULL_BATTERY_DOCK = tracked(BLOCKS.registerBlock("null_battery_dock", NullBatteryDockBlock::new, defaultProps()));

    // Shell blocks for future expansion
    public static final DeferredBlock<Block> AI_CORE_CRADLE = tracked(BLOCKS.registerSimpleBlock("ai_core_cradle", defaultProps()));
    public static final DeferredBlock<Block> RELIC_VAULT_DOOR = tracked(BLOCKS.registerSimpleBlock("relic_vault_door", defaultProps()));
    public static final DeferredBlock<Block> RELIC_DISPLAY_STAND = tracked(BLOCKS.registerSimpleBlock("relic_display_stand", defaultProps()));
    public static final DeferredBlock<Block> RELIC_CONTAINMENT_CASE = tracked(BLOCKS.registerSimpleBlock("relic_containment_case", defaultProps()));
    public static final DeferredBlock<Block> NULL_SHIELDED_VAULT = tracked(BLOCKS.registerSimpleBlock("null_shielded_vault", defaultProps()));

    private ModBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static List<DeferredBlock<Block>> blockItems() {
        return List.copyOf(BLOCK_ITEMS);
    }

    private static java.util.function.UnaryOperator<BlockBehaviour.Properties> defaultProps() {
        return p -> p.mapColor(MapColor.COLOR_GRAY).strength(2.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static DeferredBlock<Block> tracked(DeferredBlock<Block> block) {
        BLOCK_ITEMS.add(block);
        return block;
    }
}
