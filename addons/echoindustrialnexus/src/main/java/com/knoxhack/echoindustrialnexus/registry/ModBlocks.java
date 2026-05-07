package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.block.IndustrialFluxDuctBlock;
import com.knoxhack.echoindustrialnexus.block.IndustrialFluidPipeBlock;
import com.knoxhack.echoindustrialnexus.block.IndustrialItemDuctBlock;
import com.knoxhack.echoindustrialnexus.block.IndustrialMachineBlock;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;

public final class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("echoindustrialnexus");
   public static final DeferredBlock<Block> SCRAP_DYNAMO = machine("scrap_dynamo", IndustrialMachineBlock.MachineKind.SCRAP_DYNAMO, MapColor.TERRACOTTA_ORANGE);
   public static final DeferredBlock<Block> THERMAL_ARRAY = machine("thermal_array", IndustrialMachineBlock.MachineKind.THERMAL_ARRAY, MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> GEOTHERMAL_PUMP = machine(
      "geothermal_pump", IndustrialMachineBlock.MachineKind.GEOTHERMAL_PUMP, MapColor.TERRACOTTA_ORANGE
   );
   public static final DeferredBlock<Block> REACTOR_HEAT_EXCHANGER = machine(
      "reactor_heat_exchanger", IndustrialMachineBlock.MachineKind.REACTOR_HEAT_EXCHANGER, MapColor.COLOR_GREEN
   );
   public static final DeferredBlock<Block> SOLAR_CONCENTRATOR = machine(
      "solar_concentrator", IndustrialMachineBlock.MachineKind.SOLAR_CONCENTRATOR, MapColor.GOLD
   );
   public static final DeferredBlock<Block> STATIC_HEAT_EXCHANGER = machine(
      "static_heat_exchanger", IndustrialMachineBlock.MachineKind.STATIC_HEAT_EXCHANGER, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> FURNACE_WARDEN_CORE = machine(
      "furnace_warden_core", IndustrialMachineBlock.MachineKind.FURNACE_WARDEN_CORE, MapColor.TERRACOTTA_RED
   );
   public static final DeferredBlock<Block> ORE_GRINDER = machine("ore_grinder", IndustrialMachineBlock.MachineKind.ORE_GRINDER, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> SALVAGE_SHREDDER = machine(
      "salvage_shredder", IndustrialMachineBlock.MachineKind.SALVAGE_SHREDDER, MapColor.COLOR_BLACK
   );
   public static final DeferredBlock<Block> ALLOY_KILN = machine("alloy_kiln", IndustrialMachineBlock.MachineKind.ALLOY_KILN, MapColor.TERRACOTTA_RED);
   public static final DeferredBlock<Block> SUBSTRATE_GRINDER = machine(
      "substrate_grinder", IndustrialMachineBlock.MachineKind.SUBSTRATE_GRINDER, MapColor.DIRT
   );
   public static final DeferredBlock<Block> FLUID_REFINER = machine("fluid_refiner", IndustrialMachineBlock.MachineKind.FLUID_REFINER, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> WATER_PURIFIER = machine(
      "water_purifier", IndustrialMachineBlock.MachineKind.WATER_PURIFIER, MapColor.COLOR_LIGHT_BLUE
   );
   public static final DeferredBlock<Block> FILTER_PRESS = machine("filter_press", IndustrialMachineBlock.MachineKind.FILTER_PRESS, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> COMPONENT_ASSEMBLER = machine(
      "component_assembler", IndustrialMachineBlock.MachineKind.COMPONENT_ASSEMBLER, MapColor.COLOR_LIGHT_BLUE
   );
   public static final DeferredBlock<Block> INDUSTRIAL_RECYCLER = machine(
      "industrial_recycler", IndustrialMachineBlock.MachineKind.INDUSTRIAL_RECYCLER, MapColor.COLOR_GREEN
   );
   public static final DeferredBlock<Block> CORRUPTION_SAFE_RECYCLER = machine(
      "corruption_safe_recycler", IndustrialMachineBlock.MachineKind.CORRUPTION_SAFE_RECYCLER, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> NEXUS_THERMAL_INJECTOR = machine(
      "nexus_thermal_injector", IndustrialMachineBlock.MachineKind.NEXUS_THERMAL_INJECTOR, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> REALITY_FURNACE = machine(
      "reality_furnace", IndustrialMachineBlock.MachineKind.REALITY_FURNACE, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> FACTORY_CONTROLLER = machine(
      "factory_controller", IndustrialMachineBlock.MachineKind.FACTORY_CONTROLLER, MapColor.COLOR_CYAN
   );
   public static final DeferredBlock<Block> FLUX_CAPACITOR_BANK = machine(
      "flux_capacitor_bank", IndustrialMachineBlock.MachineKind.FLUX_CAPACITOR_BANK, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> REINFORCED_CAPACITOR = machine(
      "reinforced_capacitor", IndustrialMachineBlock.MachineKind.REINFORCED_CAPACITOR, MapColor.COLOR_ORANGE
   );
   public static final DeferredBlock<Block> STABILIZED_FLUX_BANK = machine(
      "stabilized_flux_bank", IndustrialMachineBlock.MachineKind.STABILIZED_FLUX_BANK, MapColor.COLOR_CYAN
   );
   public static final DeferredBlock<Block> HYBRID_FLUX_BANK = machine(
      "hybrid_flux_bank", IndustrialMachineBlock.MachineKind.HYBRID_FLUX_BANK, MapColor.COLOR_PURPLE
   );
   public static final DeferredBlock<Block> CORE_FLUX_BANK = machine("core_flux_bank", IndustrialMachineBlock.MachineKind.CORE_FLUX_BANK, MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> INDUSTRIAL_SCRUBBER = machine(
      "industrial_scrubber", IndustrialMachineBlock.MachineKind.INDUSTRIAL_SCRUBBER, MapColor.PLANT
   );
   public static final DeferredBlock<Block> COPPER_FLUX_DUCT = fluxDuct("copper_flux_duct", "Copper Flux Duct", 256, MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> REINFORCED_FLUX_DUCT = fluxDuct("reinforced_flux_duct", "Reinforced Flux Duct", 768, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> STABILIZED_FLUX_DUCT = fluxDuct("stabilized_flux_duct", "Stabilized Flux Duct", 1536, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> HYBRID_FLUX_DUCT = fluxDuct("hybrid_flux_duct", "Hybrid Flux Duct", 3072, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> CORE_FLUX_DUCT = fluxDuct("core_flux_duct", "Core Flux Duct", 8192, MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> SCRAP_DUCT = itemDuct("scrap_duct", "Scrap Duct", 24, false, false, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> REINFORCED_DUCT = itemDuct("reinforced_duct", "Reinforced Duct", 14, false, false, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> SMART_DUCT = itemDuct("smart_duct", "Smart Duct", 10, false, false, MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> VACUUM_DUCT = itemDuct("vacuum_duct", "Vacuum Duct", 12, true, false, MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> NEXUS_SAFE_DUCT = itemDuct("nexus_safe_duct", "Nexus-Safe Duct", 10, false, true, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> RUSTED_PIPE = fluidPipe("rusted_pipe", IndustrialFluidPipeBlock.PipeTier.RUSTED, MapColor.TERRACOTTA_ORANGE);
   public static final DeferredBlock<Block> REINFORCED_PIPE = fluidPipe("reinforced_pipe", IndustrialFluidPipeBlock.PipeTier.REINFORCED, MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> PRESSURIZED_PIPE = fluidPipe("pressurized_pipe", IndustrialFluidPipeBlock.PipeTier.PRESSURIZED, MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> SHIELDED_PIPE = fluidPipe("shielded_pipe", IndustrialFluidPipeBlock.PipeTier.SHIELDED, MapColor.COLOR_GREEN);
   public static final DeferredBlock<Block> STATIC_PIPE = fluidPipe("static_pipe", IndustrialFluidPipeBlock.PipeTier.STATIC, MapColor.COLOR_PURPLE);
   public static final DeferredBlock<Block> SIGNAL_DUCT = metal("signal_duct", MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> REINFORCED_FACTORY_WALL = metal("reinforced_factory_wall", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> RUSTED_CATWALK = metal("rusted_catwalk", MapColor.TERRACOTTA_ORANGE);
   public static final DeferredBlock<Block> INDUSTRIAL_GRATE = metal("industrial_grate", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> WARNING_STRIPE_BLOCK = metal("warning_stripe_block", MapColor.GOLD);
   public static final DeferredBlock<Block> ASHPROOF_GLASS = glass("ashproof_glass", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> REINFORCED_GLASS = glass("reinforced_glass", MapColor.COLOR_LIGHT_BLUE);
   public static final DeferredBlock<Block> MACHINE_CASING = metal("machine_casing", MapColor.METAL);
   public static final DeferredBlock<Block> THERMAL_COIL_BLOCK = metal("thermal_coil_block", MapColor.COLOR_ORANGE);
   public static final DeferredBlock<Block> VENT_BLOCK = metal("vent_block", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> PIPE_WALL = metal("pipe_wall", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> CONTROL_PANEL = metal("control_panel", MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> ECHO_MONITOR = glass("echo_monitor", MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> BROKEN_MONITOR = glass("broken_monitor", MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> FACTORY_LIGHT = glass("factory_light", MapColor.GOLD);
   public static final DeferredBlock<Block> EMERGENCY_LIGHT = glass("emergency_light", MapColor.TERRACOTTA_RED);
   public static final DeferredBlock<Block> HAZARD_DOOR = metal("hazard_door", MapColor.GOLD);
   public static final DeferredBlock<Block> MAINTENANCE_HATCH = metal("maintenance_hatch", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> CONVEYOR_FLOOR = metal("conveyor_floor", MapColor.COLOR_BLACK);
   public static final DeferredBlock<Block> SMOKE_VENT = metal("smoke_vent", MapColor.COLOR_GRAY);
   public static final DeferredBlock<Block> PRESSURE_GAUGE_BLOCK = metal("pressure_gauge_block", MapColor.COLOR_CYAN);
   public static final DeferredBlock<Block> COOLING_FAN_BLOCK = metal("cooling_fan_block", MapColor.COLOR_GRAY);
   public static final List<DeferredBlock<Block>> ALL_BLOCKS = List.of(
      SCRAP_DYNAMO,
      THERMAL_ARRAY,
      GEOTHERMAL_PUMP,
      REACTOR_HEAT_EXCHANGER,
      SOLAR_CONCENTRATOR,
      STATIC_HEAT_EXCHANGER,
      FURNACE_WARDEN_CORE,
      ORE_GRINDER,
      SALVAGE_SHREDDER,
      ALLOY_KILN,
      SUBSTRATE_GRINDER,
      FLUID_REFINER,
      WATER_PURIFIER,
      FILTER_PRESS,
      COMPONENT_ASSEMBLER,
      INDUSTRIAL_RECYCLER,
      CORRUPTION_SAFE_RECYCLER,
      NEXUS_THERMAL_INJECTOR,
      REALITY_FURNACE,
      FACTORY_CONTROLLER,
      FLUX_CAPACITOR_BANK,
      REINFORCED_CAPACITOR,
      STABILIZED_FLUX_BANK,
      HYBRID_FLUX_BANK,
      CORE_FLUX_BANK,
      INDUSTRIAL_SCRUBBER,
      COPPER_FLUX_DUCT,
      REINFORCED_FLUX_DUCT,
      STABILIZED_FLUX_DUCT,
      HYBRID_FLUX_DUCT,
      CORE_FLUX_DUCT,
      SCRAP_DUCT,
      REINFORCED_DUCT,
      SMART_DUCT,
      VACUUM_DUCT,
      NEXUS_SAFE_DUCT,
      RUSTED_PIPE,
      REINFORCED_PIPE,
      PRESSURIZED_PIPE,
      SHIELDED_PIPE,
      STATIC_PIPE,
      SIGNAL_DUCT,
      REINFORCED_FACTORY_WALL,
      RUSTED_CATWALK,
      INDUSTRIAL_GRATE,
      WARNING_STRIPE_BLOCK,
      ASHPROOF_GLASS,
      REINFORCED_GLASS,
      MACHINE_CASING,
      THERMAL_COIL_BLOCK,
      VENT_BLOCK,
      PIPE_WALL,
      CONTROL_PANEL,
      ECHO_MONITOR,
      BROKEN_MONITOR,
      FACTORY_LIGHT,
      EMERGENCY_LIGHT,
      HAZARD_DOOR,
      MAINTENANCE_HATCH,
      CONVEYOR_FLOOR,
      SMOKE_VENT,
      PRESSURE_GAUGE_BLOCK,
      COOLING_FAN_BLOCK
   );

   private ModBlocks() {
   }

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
   }

   private static DeferredBlock<Block> machine(String name, IndustrialMachineBlock.MachineKind kind, MapColor color) {
      return BLOCKS.registerBlock(
         name, properties -> new IndustrialMachineBlock(kind, properties), p -> p.mapColor(color).strength(4.0F, 8.0F).sound(SoundType.METAL)
      );
   }

   private static DeferredBlock<Block> fluxDuct(String name, String displayName, int transferLimit, MapColor color) {
      return BLOCKS.registerBlock(
         name,
         properties -> new IndustrialFluxDuctBlock(displayName, transferLimit, properties),
         p -> p.mapColor(color).strength(2.0F, 5.0F).sound(SoundType.COPPER).noOcclusion()
      );
   }


   private static DeferredBlock<Block> itemDuct(String name, String displayName, int transferInterval, boolean vacuum, boolean nexusSafe, MapColor color) {
      return BLOCKS.registerBlock(
         name,
         properties -> new IndustrialItemDuctBlock(displayName, transferInterval, vacuum, nexusSafe, properties),
         p -> p.mapColor(color).strength(2.0F, 5.0F).sound(SoundType.COPPER).noOcclusion()
      );
   }

   private static DeferredBlock<Block> fluidPipe(String name, IndustrialFluidPipeBlock.PipeTier tier, MapColor color) {
      return BLOCKS.registerBlock(
         name,
         properties -> new IndustrialFluidPipeBlock(tier, properties),
         p -> p.mapColor(color).strength(2.0F, 5.0F).sound(SoundType.COPPER).noOcclusion()
      );
   }

   private static DeferredBlock<Block> metal(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(name, p -> p.mapColor(color).strength(4.0F, 8.0F).sound(SoundType.METAL));
   }

   private static DeferredBlock<Block> glass(String name, MapColor color) {
      return BLOCKS.registerSimpleBlock(
         name, p -> p.mapColor(color).strength(0.8F, 1.5F).sound(SoundType.GLASS).noOcclusion().isValidSpawn((state, level, pos, entityType) -> false)
      );
   }
}
