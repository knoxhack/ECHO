package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyMultiblockControllerBlockEntity;
import com.knoxhack.echoconvoyprotocol.block.entity.ConvoyMultiblockCrateBlockEntity;
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
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConvoyMultiblockControllerBlockEntity>> CONVOY_MULTIBLOCK_CONTROLLER =
      BLOCK_ENTITIES.register("convoy_multiblock_controller", () -> new BlockEntityType<>(
         ConvoyMultiblockControllerBlockEntity::new,
         convoyMultiblockControllerBlocks()
      ));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConvoyMultiblockCrateBlockEntity>> CONVOY_MULTIBLOCK_CRATE =
      BLOCK_ENTITIES.register("convoy_multiblock_crate", () -> new BlockEntityType<>(
         ConvoyMultiblockCrateBlockEntity::new,
         convoyMultiblockCrateBlocks()
      ));

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
         ModBlocks.VEHICLE_UPGRADE_BAY.get(),
         ModBlocks.CONVOY_BEACON.get(),
         ModBlocks.ROADSIDE_SIGNAL_MARKER.get(),
         ModBlocks.CARGO_ANCHOR.get(),
         ModBlocks.FIELD_REPAIR_STATION.get()
      );
   }

   private static Set<Block> convoyMultiblockControllerBlocks() {
      return Set.of(
         ModBlocks.CONVOY_DEPOT_CONTROLLER.get(),
         ModBlocks.VEHICLE_REPAIR_GANTRY_CONTROLLER.get(),
         ModBlocks.CARGO_LOADING_BAY_CONTROLLER.get(),
         ModBlocks.FUEL_REFINERY_PAD_CONTROLLER.get(),
         ModBlocks.ROUTE_DISPATCH_TOWER_CONTROLLER.get(),
         ModBlocks.MOBILE_COMMAND_GARAGE_CONTROLLER.get(),
         ModBlocks.CONVOY_RECOVERY_BEACON_CONTROLLER.get()
      );
   }

   private static Set<Block> convoyMultiblockCrateBlocks() {
      return Set.of(
         ModBlocks.CARGO_INPUT_CRATE.get(),
         ModBlocks.CARGO_OUTPUT_CRATE.get(),
         ModBlocks.FUEL_INPUT_TANK.get(),
         ModBlocks.FUEL_OUTPUT_TANK.get()
      );
   }
}
