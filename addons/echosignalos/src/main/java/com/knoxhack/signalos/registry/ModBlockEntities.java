package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SignalOS.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SignalOsTerminalBlockEntity>> TERMINAL =
            BLOCK_ENTITIES.register("terminal",
                    () -> new BlockEntityType<>(SignalOsTerminalBlockEntity::new, Set.of(ModBlocks.TERMINAL.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
