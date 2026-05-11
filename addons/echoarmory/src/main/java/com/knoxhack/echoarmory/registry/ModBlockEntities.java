package com.knoxhack.echoarmory.registry;

import com.knoxhack.echoarmory.EchoArmory;
import com.knoxhack.echoarmory.block.entity.ArmoryStationBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoArmory.MODID);

   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArmoryStationBlockEntity>> ARMORY_STATION =
      BLOCK_ENTITIES.register("armory_station", () -> new BlockEntityType(ArmoryStationBlockEntity::new, armoryBlocks()));

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> armoryBlocks() {
      return Set.of(
         ModBlocks.ARMORY_BENCH.get(),
         ModBlocks.WEAPON_FORGE.get(),
         ModBlocks.ARMOR_FORGE.get(),
         ModBlocks.ENERGY_CORE_CHARGING_STATION.get(),
         ModBlocks.MODULE_UPGRADE_TABLE.get(),
         ModBlocks.SIGIL_ENGRAVER.get(),
         ModBlocks.LOADOUT_TERMINAL.get(),
         ModBlocks.WEAPON_RACK.get(),
         ModBlocks.ARMOR_STAND.get(),
         ModBlocks.VEIL_INFUSER.get(),
         ModBlocks.CONSTRUCT_DOCK.get()
      );
   }
}
