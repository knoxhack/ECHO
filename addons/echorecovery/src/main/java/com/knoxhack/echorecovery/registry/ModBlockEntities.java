package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.block.entity.GraveBlockEntity;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoRecovery.MODID);

    @SuppressWarnings("unchecked")
    public static final Supplier<BlockEntityType<GraveBlockEntity>> GRAVE = BLOCK_ENTITIES.register("grave",
        () -> new BlockEntityType<>(GraveBlockEntity::new, Set.copyOf(ModBlocks.blockItems().stream().map(b -> (Block) b.get()).toList())));

    private ModBlockEntities() {}

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
