package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.block.entity.NexusMachineBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "echonexusprotocol");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NexusMachineBlockEntity>> NEXUS_MACHINE = BLOCK_ENTITIES.register(
      "nexus_machine", () -> new BlockEntityType(NexusMachineBlockEntity::new, machineBlocks())
   );

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> machineBlocks() {
      return Set.copyOf(ModBlocks.MACHINE_BLOCKS.stream().map(DeferredHolder::get).toList());
   }
}
