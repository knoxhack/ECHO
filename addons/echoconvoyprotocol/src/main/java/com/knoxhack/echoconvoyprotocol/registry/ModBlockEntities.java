package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyStationBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoConvoyProtocol.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConvoyStationBlockEntity>> CONVOY_STATION =
      BLOCK_ENTITIES.register("convoy_station", () -> new BlockEntityType(ConvoyStationBlockEntity::new, convoyBlocks()));

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> convoyBlocks() {
      return Set.of(
         ModBlocks.VEHICLE_WORKBENCH.get(),
         ModBlocks.FUEL_STILL.get(),
         ModBlocks.BATTERY_CHARGING_PAD.get(),
         ModBlocks.VEHICLE_DOCK.get(),
         ModBlocks.CONVOY_BEACON.get(),
         ModBlocks.ROADSIDE_SIGNAL_MARKER.get(),
         ModBlocks.CARGO_ANCHOR.get(),
         ModBlocks.FIELD_REPAIR_STATION.get()
      );
   }
}
