package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.ConvoyBlock;
import com.knoxhack.echoconvoyprotocol.block.ConvoyBlock.ConvoyBlockKind;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks(EchoConvoyProtocol.MODID);

   public static final DeferredBlock<Block> VEHICLE_WORKBENCH = convoy(ConvoyBlockKind.VEHICLE_WORKBENCH, MapColor.METAL, 3.5F);
   public static final DeferredBlock<Block> FUEL_STILL = convoy(ConvoyBlockKind.FUEL_STILL, MapColor.COLOR_ORANGE, 3.0F);
   public static final DeferredBlock<Block> BATTERY_CHARGING_PAD = convoy(ConvoyBlockKind.BATTERY_CHARGING_PAD, MapColor.COLOR_LIGHT_BLUE, 2.5F);
   public static final DeferredBlock<Block> VEHICLE_DOCK = convoy(ConvoyBlockKind.VEHICLE_DOCK, MapColor.COLOR_GRAY, 4.0F);
   public static final DeferredBlock<Block> CONVOY_BEACON = convoy(ConvoyBlockKind.CONVOY_BEACON, MapColor.COLOR_CYAN, 3.0F);
   public static final DeferredBlock<Block> ROADSIDE_SIGNAL_MARKER = convoy(ConvoyBlockKind.ROADSIDE_SIGNAL_MARKER, MapColor.COLOR_YELLOW, 1.5F);
   public static final DeferredBlock<Block> CARGO_ANCHOR = convoy(ConvoyBlockKind.CARGO_ANCHOR, MapColor.WOOD, 2.5F);
   public static final DeferredBlock<Block> FIELD_REPAIR_STATION = convoy(ConvoyBlockKind.FIELD_REPAIR_STATION, MapColor.METAL, 3.0F);

   public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
      VEHICLE_WORKBENCH,
      FUEL_STILL,
      BATTERY_CHARGING_PAD,
      VEHICLE_DOCK,
      CONVOY_BEACON,
      ROADSIDE_SIGNAL_MARKER,
      CARGO_ANCHOR,
      FIELD_REPAIR_STATION
   );

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   private static DeferredBlock<Block> convoy(ConvoyBlockKind kind, MapColor color, float strength) {
      return BLOCKS.registerBlock(
         kind.getSerializedName(),
         properties -> new ConvoyBlock(kind, properties),
         properties -> properties.mapColor(color).strength(strength, strength * 2.0F).sound(SoundType.METAL).noOcclusion()
      );
   }
}
