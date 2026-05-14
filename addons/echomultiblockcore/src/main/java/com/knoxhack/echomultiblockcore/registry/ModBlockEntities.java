package com.knoxhack.echomultiblockcore.registry;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.block.entity.MultiblockControllerBlockEntity;
import com.knoxhack.echomultiblockcore.block.entity.MultiblockCrateBlockEntity;
import com.knoxhack.echomultiblockcore.block.entity.RoboticArmBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoMultiblockCore.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockControllerBlockEntity>> CONTROLLER =
            BLOCK_ENTITIES.register("controller", () -> new BlockEntityType<>(
                    MultiblockControllerBlockEntity::new,
                    Set.of(ModBlocks.MULTIBLOCK_CONTROLLER.get(), ModBlocks.SIGNAL_TOWER_CORE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockCrateBlockEntity>> CRATE =
            BLOCK_ENTITIES.register("crate", () -> new BlockEntityType<>(
                    MultiblockCrateBlockEntity::new,
                    Set.of((Block) ModBlocks.INPUT_CRATE.get(), (Block) ModBlocks.OUTPUT_CRATE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RoboticArmBlockEntity>> ROBOTIC_ARM =
            BLOCK_ENTITIES.register("robotic_arm", () -> new BlockEntityType<>(
                    RoboticArmBlockEntity::new,
                    Set.of((Block) ModBlocks.ROBOTIC_ARM.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
