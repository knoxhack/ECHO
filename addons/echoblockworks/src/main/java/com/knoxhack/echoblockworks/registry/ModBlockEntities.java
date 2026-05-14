package com.knoxhack.echoblockworks.registry;

import com.knoxhack.echoblockworks.EchoBlockworks;
import com.knoxhack.echoblockworks.block.entity.BlockworksTableBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoBlockworks.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockworksTableBlockEntity>> BLOCKWORKS_TABLE =
      BLOCK_ENTITIES.register("blockworks_table",
         () -> new BlockEntityType<>(BlockworksTableBlockEntity::new, Set.of(ModBlocks.BLOCKWORKS_TABLE.get())));

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }
}
