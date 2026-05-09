package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.block.entity.HydroponicTrayBlockEntity;
import com.knoxhack.echoagriculturereclamation.block.entity.ReclamationCropBlockEntity;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoAgricultureReclamation.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HydroponicTrayBlockEntity>> HYDROPONIC_TRAY =
      BLOCK_ENTITIES.register("hydroponic_tray", () -> new BlockEntityType<>(HydroponicTrayBlockEntity::new, Set.of((Block)ModBlocks.HYDROPONIC_TRAY.get())));

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReclamationCropBlockEntity>> CROP =
      BLOCK_ENTITIES.register("crop", () -> new BlockEntityType<>(ReclamationCropBlockEntity::new, cropBlocks()));

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> cropBlocks() {
      return ModBlocks.cropBlocks().stream().map(block -> (Block)block.get()).collect(Collectors.toUnmodifiableSet());
   }
}
