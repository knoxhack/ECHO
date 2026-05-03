package com.knoxhack.echoterminal.registry;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoTerminal.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EchoTerminalBlockEntity>> ECHO_TERMINAL =
            BLOCK_ENTITIES.register("echo_terminal",
                    () -> new BlockEntityType<>(EchoTerminalBlockEntity::new, Set.of(ModBlocks.ECHO_TERMINAL_BLOCK.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
