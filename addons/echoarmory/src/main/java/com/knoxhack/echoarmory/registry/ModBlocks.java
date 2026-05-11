package com.knoxhack.echoarmory.registry;

import com.knoxhack.echoarmory.EchoArmory;
import com.knoxhack.echoarmory.block.ArmoryStationBlock;
import com.knoxhack.echoarmory.block.ArmoryStationBlock.StationKind;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks(EchoArmory.MODID);

   public static final DeferredBlock<Block> ARMORY_BENCH = station(StationKind.ARMORY_BENCH, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> WEAPON_FORGE = station(StationKind.WEAPON_FORGE, MapColor.TERRACOTTA_RED);
   public static final DeferredBlock<Block> ARMOR_FORGE = station(StationKind.ARMOR_FORGE, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> ENERGY_CORE_CHARGING_STATION = station(StationKind.ENERGY_CORE_CHARGING_STATION, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> MODULE_UPGRADE_TABLE = station(StationKind.MODULE_UPGRADE_TABLE, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> SIGIL_ENGRAVER = station(StationKind.SIGIL_ENGRAVER, MapColor.GOLD);
   public static final DeferredBlock<Block> LOADOUT_TERMINAL = station(StationKind.LOADOUT_TERMINAL, MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> WEAPON_RACK = station(StationKind.WEAPON_RACK, MapColor.WOOD);
   public static final DeferredBlock<Block> ARMOR_STAND = station(StationKind.ARMOR_STAND, MapColor.METAL);
   public static final DeferredBlock<Block> VEIL_INFUSER = station(StationKind.VEIL_INFUSER, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> CONSTRUCT_DOCK = station(StationKind.CONSTRUCT_DOCK, MapColor.COLOR_GREEN);

   public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
      ARMORY_BENCH,
      WEAPON_FORGE,
      ARMOR_FORGE,
      ENERGY_CORE_CHARGING_STATION,
      MODULE_UPGRADE_TABLE,
      SIGIL_ENGRAVER,
      LOADOUT_TERMINAL,
      WEAPON_RACK,
      ARMOR_STAND,
      VEIL_INFUSER,
      CONSTRUCT_DOCK
   );

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   private static DeferredBlock<Block> station(StationKind kind, MapColor color) {
      return BLOCKS.registerBlock(
         kind.getSerializedName(),
         properties -> new ArmoryStationBlock(kind, properties),
         p -> p.mapColor(color).strength(3.5F, 7.0F).sound(SoundType.METAL).noOcclusion()
      );
   }
}
