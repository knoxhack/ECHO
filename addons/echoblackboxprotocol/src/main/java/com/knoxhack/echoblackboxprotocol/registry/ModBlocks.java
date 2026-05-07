package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.block.BlackboxMachineBlock;
import com.knoxhack.echoblackboxprotocol.block.BlackboxMonolithBlock;
import com.knoxhack.echoblackboxprotocol.progression.BlackboxDungeon;
import com.knoxhack.echoblackboxprotocol.progression.BlackboxMachineKind;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("echoblackboxprotocol");
   public static final DeferredBlock<Block> CORE_BRICK = stone("core_brick", MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> SIGNAL_GLASS = glass("signal_glass", MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> BLACK_METAL_BLOCK = metal("black_metal_block", MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> CORRUPTED_FERRITE_BLOCK = metal("corrupted_ferrite_block", MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> BLACKBOX_DECODER = machine("blackbox_decoder", BlackboxMachineKind.BLACKBOX_DECODER, MapColor.COLOR_BLUE);
   public static final DeferredBlock<Block> MEMORY_PROJECTOR = machine("memory_projector", BlackboxMachineKind.MEMORY_PROJECTOR, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> ARCHIVE_TERMINAL = machine("archive_terminal", BlackboxMachineKind.ARCHIVE_TERMINAL, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> CORE_KEY_ASSEMBLER = machine("core_key_assembler", BlackboxMachineKind.CORE_KEY_ASSEMBLER, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> TRUTH_ENGINE = machine("truth_engine", BlackboxMachineKind.TRUTH_ENGINE, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> MEMORY_STABILIZER = machine("memory_stabilizer", BlackboxMachineKind.MEMORY_STABILIZER, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> PROTOCOL_EXTRACTOR = machine("protocol_extractor", BlackboxMachineKind.PROTOCOL_EXTRACTOR, MapColor.COLOR_RED);
   public static final DeferredBlock<Block> VAULT_MONOLITH = monolith("vault_monolith", BlackboxDungeon.VAULT);
   public static final DeferredBlock<Block> BUNKER_MONOLITH = monolith("bunker_monolith", BlackboxDungeon.BUNKER);
   public static final DeferredBlock<Block> LABYRINTH_MONOLITH = monolith("labyrinth_monolith", BlackboxDungeon.LABYRINTH);
   public static final DeferredBlock<Block> TEMPLE_MONOLITH = monolith("temple_monolith", BlackboxDungeon.TEMPLE);
   public static final DeferredBlock<Block> CORE_CHAMBER_MONOLITH = monolith("core_chamber_monolith", BlackboxDungeon.CORE_CHAMBER);
   public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
      CORE_BRICK,
      SIGNAL_GLASS,
      BLACK_METAL_BLOCK,
      CORRUPTED_FERRITE_BLOCK,
      BLACKBOX_DECODER,
      MEMORY_PROJECTOR,
      ARCHIVE_TERMINAL,
      CORE_KEY_ASSEMBLER,
      TRUTH_ENGINE,
      MEMORY_STABILIZER,
      PROTOCOL_EXTRACTOR,
      VAULT_MONOLITH,
      BUNKER_MONOLITH,
      LABYRINTH_MONOLITH,
      TEMPLE_MONOLITH,
      CORE_CHAMBER_MONOLITH
   );

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   private static DeferredBlock<Block> machine(String name, BlackboxMachineKind kind, MapColor color) {
      return BLOCKS.registerBlock(
         name,
         properties -> new BlackboxMachineBlock(kind, properties),
         p -> p.mapColor(color).strength(5.0F, 10.0F).sound(SoundType.METAL).lightLevel(state -> 3)
      );
   }

   private static DeferredBlock<Block> monolith(String name, BlackboxDungeon dungeon) {
      return BLOCKS.registerBlock(
         name,
         properties -> new BlackboxMonolithBlock(dungeon, properties),
         p -> p.mapColor(MapColor.COLOR_BLACK).strength(8.0F, 20.0F).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 5)
      );
   }

   private static DeferredBlock<Block> metal(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(name, p -> p.mapColor(color).strength(4.5F, 9.0F).sound(SoundType.METAL));
   }

   private static DeferredBlock<Block> stone(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(name, p -> p.mapColor(color).strength(3.0F, 8.0F).sound(SoundType.DEEPSLATE));
   }

   private static DeferredBlock<Block> glass(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(
         name, p -> p.mapColor(color).strength(0.8F, 1.5F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, entityType) -> false)
      );
   }
}
