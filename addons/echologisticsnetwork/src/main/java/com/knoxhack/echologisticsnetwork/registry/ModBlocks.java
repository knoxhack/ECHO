package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.block.LogisticsBlock;
import com.knoxhack.echologisticsnetwork.block.LogisticsBlock.LogisticsKind;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks(EchoLogisticsNetwork.MODID);

   public static final DeferredBlock<Block> LOGISTICS_TERMINAL = logistics(LogisticsKind.LOGISTICS_TERMINAL, MapColor.COLOR_CYAN, 4.0F);
   public static final DeferredBlock<Block> SUPPLY_CRATE = logistics(LogisticsKind.SUPPLY_CRATE, MapColor.WOOD, 2.5F);
   public static final DeferredBlock<Block> SMART_STORAGE_LABEL = logistics(LogisticsKind.SMART_STORAGE_LABEL, MapColor.COLOR_LIGHT_BLUE, 1.0F);
   public static final DeferredBlock<Block> DRONE_DELIVERY_DOCK = logistics(LogisticsKind.DRONE_DELIVERY_DOCK, MapColor.METAL, 4.0F);
   public static final DeferredBlock<Block> ROUTE_REQUESTER = logistics(LogisticsKind.ROUTE_REQUESTER, MapColor.COLOR_GRAY, 3.0F);
   public static final DeferredBlock<Block> LOADOUT_LOCKER = logistics(LogisticsKind.LOADOUT_LOCKER, MapColor.COLOR_GREEN, 3.0F);
   public static final DeferredBlock<Block> FACTION_TRADE_DEPOT = logistics(LogisticsKind.FACTION_TRADE_DEPOT, MapColor.GOLD, 3.5F);
   public static final DeferredBlock<Block> REMOTE_REWARD_RELAY = logistics(LogisticsKind.REMOTE_REWARD_RELAY, MapColor.COLOR_PURPLE, 3.0F);
   public static final DeferredBlock<Block> AUTO_RESTOCK_STATION = logistics(LogisticsKind.AUTO_RESTOCK_STATION, MapColor.COLOR_ORANGE, 3.0F);

   public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
      LOGISTICS_TERMINAL,
      SUPPLY_CRATE,
      SMART_STORAGE_LABEL,
      DRONE_DELIVERY_DOCK,
      ROUTE_REQUESTER,
      LOADOUT_LOCKER,
      FACTION_TRADE_DEPOT,
      REMOTE_REWARD_RELAY,
      AUTO_RESTOCK_STATION
   );

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   private static DeferredBlock<Block> logistics(LogisticsKind kind, MapColor color, float strength) {
      return BLOCKS.registerBlock(
         kind.getSerializedName(),
         properties -> new LogisticsBlock(kind, properties),
         p -> p.mapColor(color).strength(strength, strength * 2.0F).sound(SoundType.METAL).noOcclusion()
      );
   }
}
