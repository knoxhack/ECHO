package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.block.HydroponicTrayBlock;
import com.knoxhack.echoagriculturereclamation.block.ReclamationCropBlock;
import com.knoxhack.echoagriculturereclamation.block.ReclamationMachineBlock;
import com.knoxhack.echoagriculturereclamation.block.ReclamationSoilBlock;
import com.knoxhack.echoagriculturereclamation.content.CropSpec;
import com.knoxhack.echoagriculturereclamation.content.SoilState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks(EchoAgricultureReclamation.MODID);
   private static final Map<String, DeferredBlock<ReclamationCropBlock>> CROPS = new LinkedHashMap<>();
   private static final List<DeferredBlock<Block>> BLOCK_ITEMS = new ArrayList<>();

   public static final DeferredBlock<Block> DEAD_SOIL = soil("dead_soil", SoilState.DEAD, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> CONTAMINATED_SOIL = soil("contaminated_soil", SoilState.CONTAMINATED, MapColor.TERRACOTTA_PURPLE);
   public static final DeferredBlock<Block> IRRADIATED_SOIL = soil("irradiated_soil", SoilState.IRRADIATED, MapColor.TERRACOTTA_GREEN);
   public static final DeferredBlock<Block> TOXIC_MUD = soil("toxic_mud", SoilState.TOXIC_MUD, MapColor.TERRACOTTA_LIGHT_GREEN);
   public static final DeferredBlock<Block> PURIFIED_SOIL = soil("purified_soil", SoilState.PURIFIED, MapColor.DIRT);
   public static final DeferredBlock<Block> STABILIZED_SOIL = soil("stabilized_soil", SoilState.STABILIZED, MapColor.COLOR_GREEN);
   public static final DeferredBlock<Block> RESTORED_SOIL = soil("restored_soil", SoilState.RESTORED, MapColor.GRASS);

   public static final DeferredBlock<Block> HYDROPONIC_TRAY = tracked(BLOCKS.registerBlock("hydroponic_tray", HydroponicTrayBlock::new,
      p -> p.mapColor(MapColor.COLOR_CYAN).strength(2.0F, 5.0F).sound(SoundType.COPPER).noOcclusion()));
   public static final DeferredBlock<Block> SEED_VAULT_TERMINAL = machine("seed_vault_terminal", ReclamationMachineBlock.MachineKind.SEED_VAULT_TERMINAL, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> SOIL_PURIFIER = machine("soil_purifier", ReclamationMachineBlock.MachineKind.SOIL_PURIFIER, MapColor.COLOR_GREEN);
   public static final DeferredBlock<Block> GENE_STABILIZER = machine("gene_stabilizer", ReclamationMachineBlock.MachineKind.GENE_STABILIZER, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> BIO_REACTOR = machine("bio_reactor", ReclamationMachineBlock.MachineKind.BIO_REACTOR, MapColor.PLANT);
   public static final DeferredBlock<Block> GREENHOUSE_CONTROLLER = machine("greenhouse_controller", ReclamationMachineBlock.MachineKind.GREENHOUSE_CONTROLLER, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> POLLINATOR_DRONE_DOCK = machine("pollinator_drone_dock", ReclamationMachineBlock.MachineKind.POLLINATOR_DRONE_DOCK, MapColor.GOLD);
   public static final DeferredBlock<Block> SPORE_FILTER = machine("spore_filter", ReclamationMachineBlock.MachineKind.SPORE_FILTER, MapColor.COLOR_LIGHT_GREEN);
   public static final DeferredBlock<Block> COMPOST_RECYCLER = machine("compost_recycler", ReclamationMachineBlock.MachineKind.COMPOST_RECYCLER, MapColor.DIRT);
   public static final DeferredBlock<Block> ECOLOGY_SCANNER = machine("ecology_scanner", ReclamationMachineBlock.MachineKind.ECOLOGY_SCANNER, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> GREENHOUSE_GLASS = tracked(BLOCKS.registerSimpleBlock("greenhouse_glass",
      p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.8F, 1.5F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, entityType) -> false)));

   public static final DeferredBlock<ReclamationCropBlock> ASH_WHEAT_CROP = crop(CropSpec.byPath("ash_wheat"));
   public static final DeferredBlock<ReclamationCropBlock> HARDROOT_CROP = crop(CropSpec.byPath("hardroot"));
   public static final DeferredBlock<ReclamationCropBlock> GLOW_BEANS_CROP = crop(CropSpec.byPath("glow_beans"));
   public static final DeferredBlock<ReclamationCropBlock> RADLEAF_CROP = crop(CropSpec.byPath("radleaf"));
   public static final DeferredBlock<ReclamationCropBlock> MUTANT_BERRIES_CROP = crop(CropSpec.byPath("mutant_berries"));
   public static final DeferredBlock<ReclamationCropBlock> CRYO_MOSS_CROP = crop(CropSpec.byPath("cryo_moss"));
   public static final DeferredBlock<ReclamationCropBlock> CLEAN_CORN_CROP = crop(CropSpec.byPath("clean_corn"));
   public static final DeferredBlock<ReclamationCropBlock> MEDICINAL_ALOE_CROP = crop(CropSpec.byPath("medicinal_aloe"));
   public static final DeferredBlock<ReclamationCropBlock> FILTER_REED_CROP = crop(CropSpec.byPath("filter_reed"));
   public static final DeferredBlock<ReclamationCropBlock> NEXUS_ORCHID_CROP = crop(CropSpec.byPath("nexus_orchid"));
   public static final DeferredBlock<ReclamationCropBlock> SIGNAL_FUNGUS_CROP = crop(CropSpec.byPath("signal_fungus"));

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   public static List<DeferredBlock<Block>> blockItems() {
      return List.copyOf(BLOCK_ITEMS);
   }

   public static List<DeferredBlock<ReclamationCropBlock>> cropBlocks() {
      return List.copyOf(CROPS.values());
   }

   public static ReclamationCropBlock cropBlock(CropSpec spec) {
      return CROPS.get(spec.path()).get();
   }

   public static Block blockFor(SoilState state) {
      return switch (state) {
         case DEAD -> DEAD_SOIL.get();
         case CONTAMINATED -> CONTAMINATED_SOIL.get();
         case IRRADIATED -> IRRADIATED_SOIL.get();
         case TOXIC_MUD -> TOXIC_MUD.get();
         case PURIFIED -> PURIFIED_SOIL.get();
         case STABILIZED -> STABILIZED_SOIL.get();
         case RESTORED -> RESTORED_SOIL.get();
      };
   }

   private static DeferredBlock<Block> soil(String name, SoilState state, MapColor color) {
      return tracked(BLOCKS.registerBlock(name, p -> new ReclamationSoilBlock(state, p), p -> p.mapColor(color).strength(0.6F).sound(SoundType.GRAVEL).randomTicks()));
   }

   private static DeferredBlock<Block> machine(String name, ReclamationMachineBlock.MachineKind kind, MapColor color) {
      return tracked(BLOCKS.registerBlock(name, p -> new ReclamationMachineBlock(kind, p), p -> p.mapColor(color).strength(3.0F, 7.0F).sound(SoundType.METAL)));
   }

   private static DeferredBlock<ReclamationCropBlock> crop(CropSpec spec) {
      DeferredBlock<ReclamationCropBlock> crop = BLOCKS.registerBlock(spec.path() + "_crop",
         p -> new ReclamationCropBlock(spec, p),
         p -> p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP));
      CROPS.put(spec.path(), crop);
      return crop;
   }

   private static DeferredBlock<Block> tracked(DeferredBlock<Block> block) {
      BLOCK_ITEMS.add(block);
      return block;
   }
}
