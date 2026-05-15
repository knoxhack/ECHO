package com.knoxhack.echomultiblockcore.registry;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.block.MultiblockControllerBlock;
import com.knoxhack.echomultiblockcore.block.MultiblockCrateBlock;
import com.knoxhack.echomultiblockcore.block.RoboticArmBlock;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoMultiblockCore.MODID);

    public static final DeferredBlock<Block> MULTIBLOCK_CONTROLLER = BLOCKS.registerBlock(
            "multiblock_controller",
            properties -> new MultiblockControllerBlock(EchoMultiblockCore.id("industrial_assembly_line"), properties),
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(4.0F, 8.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> SIGNAL_TOWER_CORE = BLOCKS.registerBlock(
            "signal_tower_core",
            properties -> new MultiblockControllerBlock(EchoMultiblockCore.id("signal_tower_tier_1"), properties),
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(4.0F, 8.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> REINFORCED_FRAME = metal("reinforced_frame", MapColor.COLOR_GRAY);
    public static final DeferredBlock<Block> SIGNAL_CONDUIT = metal("signal_conduit", MapColor.COLOR_CYAN);
    public static final DeferredBlock<Block> POWER_BUS = metal("power_bus", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> DATA_BUS = metal("data_bus", MapColor.COLOR_LIGHT_BLUE);
    public static final DeferredBlock<Block> INPUT_CRATE = BLOCKS.registerBlock(
            "input_crate",
            properties -> new MultiblockCrateBlock(MultiblockCrateBlock.CrateKind.INPUT, properties),
            p -> p.mapColor(MapColor.WOOD).strength(2.5F, 4.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<Block> OUTPUT_CRATE = BLOCKS.registerBlock(
            "output_crate",
            properties -> new MultiblockCrateBlock(MultiblockCrateBlock.CrateKind.OUTPUT, properties),
            p -> p.mapColor(MapColor.WOOD).strength(2.5F, 4.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<Block> ROBOTIC_ARM = BLOCKS.registerBlock(
            "robotic_arm",
            RoboticArmBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY).strength(3.5F, 8.0F).sound(SoundType.METAL).noOcclusion());
    public static final DeferredBlock<Block> AUTO_BUILDER = metal("auto_builder", MapColor.COLOR_GREEN);
    public static final DeferredBlock<Block> REINFORCED_MACHINE_FRAME = metal("reinforced_machine_frame", MapColor.METAL);

    public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
            MULTIBLOCK_CONTROLLER,
            SIGNAL_TOWER_CORE,
            REINFORCED_FRAME,
            SIGNAL_CONDUIT,
            POWER_BUS,
            DATA_BUS,
            INPUT_CRATE,
            OUTPUT_CRATE,
            ROBOTIC_ARM,
            AUTO_BUILDER,
            REINFORCED_MACHINE_FRAME);

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static boolean isController(Block block) {
        return block == MULTIBLOCK_CONTROLLER.get() || block == SIGNAL_TOWER_CORE.get();
    }

    public static Identifier definitionFor(Block block) {
        if (block instanceof MultiblockControllerBlock controller) {
            return controller.defaultDefinitionId();
        }
        return EchoMultiblockCore.id("industrial_assembly_line");
    }

    private static DeferredBlock<Block> metal(String name, MapColor color) {
        return BLOCKS.registerSimpleBlock(name, p -> p.mapColor(color).strength(4.0F, 8.0F).sound(SoundType.METAL));
    }
}
