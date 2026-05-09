package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.block.entity.LogisticsBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoLogisticsNetwork.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogisticsBlockEntity>> LOGISTICS =
      BLOCK_ENTITIES.register("logistics", () -> new BlockEntityType(LogisticsBlockEntity::new, logisticsBlocks()));

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> logisticsBlocks() {
      return Set.of(
         ModBlocks.LOGISTICS_TERMINAL.get(),
         ModBlocks.SUPPLY_CRATE.get(),
         ModBlocks.SMART_STORAGE_LABEL.get(),
         ModBlocks.DRONE_DELIVERY_DOCK.get(),
         ModBlocks.ROUTE_REQUESTER.get(),
         ModBlocks.LOADOUT_LOCKER.get(),
         ModBlocks.FACTION_TRADE_DEPOT.get(),
         ModBlocks.REMOTE_REWARD_RELAY.get(),
         ModBlocks.AUTO_RESTOCK_STATION.get()
      );
   }
}
