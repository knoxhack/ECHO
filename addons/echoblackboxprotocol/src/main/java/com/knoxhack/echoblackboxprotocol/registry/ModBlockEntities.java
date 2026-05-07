package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocol;
import com.knoxhack.echoblackboxprotocol.block.entity.BlackboxMachineBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoBlackboxProtocol.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlackboxMachineBlockEntity>> BLACKBOX_MACHINE = BLOCK_ENTITIES.register(
      "blackbox_machine", () -> new BlockEntityType<>(BlackboxMachineBlockEntity::new, machineBlocks())
   );

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> machineBlocks() {
      return Set.of(
         ModBlocks.BLACKBOX_DECODER.get(),
         ModBlocks.MEMORY_PROJECTOR.get(),
         ModBlocks.ARCHIVE_TERMINAL.get(),
         ModBlocks.CORE_KEY_ASSEMBLER.get(),
         ModBlocks.TRUTH_ENGINE.get(),
         ModBlocks.MEMORY_STABILIZER.get(),
         ModBlocks.PROTOCOL_EXTRACTOR.get()
      );
   }
}
