package com.knoxhack.echoindustrialnexus;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class Config {
   public static final ModConfigSpec SPEC;
   public static final IntValue SCRAP_DYNAMO_OUTPUT;
   public static final IntValue THERMAL_ARRAY_OUTPUT;
   public static final IntValue PROCESSOR_FLUX_CAPACITY;
   public static final IntValue CAPACITOR_FLUX_CAPACITY;
   public static final IntValue DUCT_TRANSFER_RATE;
   public static final IntValue SCRUBBER_FLUX_PER_TICK;
   public static final BooleanValue PROCEDURAL_POIS_ENABLED;
   public static final IntValue POI_SPACING_CHUNKS;
   public static final IntValue POI_SEARCH_RADIUS;
   public static final IntValue THERMAL_PLANT_SPACING_BONUS;
   public static final IntValue FACTORY_SPACING_BONUS;
   public static final IntValue GEOTHERMAL_SPACING_BONUS;
   public static final IntValue REACTOR_SPACING_BONUS;
   public static final IntValue NEXUS_EXCHANGER_SPACING_BONUS;

   private Config() {
   }

   static {
      Builder builder = new Builder();
      builder.push("thermal_flux");
      SCRAP_DYNAMO_OUTPUT = builder.comment("Thermal Flux generated per tick while the Scrap Dynamo is burning fuel.").defineInRange("scrapDynamoOutput", 18, 1, 4096);
      THERMAL_ARRAY_OUTPUT = builder.comment("Thermal Flux generated per tick while the Thermal Array is burning a heat source.").defineInRange("thermalArrayOutput", 80, 1, 8192);
      PROCESSOR_FLUX_CAPACITY = builder.comment("Internal Thermal Flux buffer for Industrial Nexus processors.").defineInRange("processorFluxCapacity", 10000, 100, 1000000);
      CAPACITOR_FLUX_CAPACITY = builder.comment("Thermal Flux stored by the basic Flux Capacitor Bank.").defineInRange("capacitorFluxCapacity", 180000, 1000, 10000000);
      DUCT_TRANSFER_RATE = builder.comment("Thermal Flux a Copper Flux Duct network can move per pull.").defineInRange("ductTransferRate", 448, 1, 8192);
      SCRUBBER_FLUX_PER_TICK = builder.comment("Thermal Flux consumed per tick by an active Industrial Scrubber.").defineInRange("scrubberFluxPerTick", 6, 1, 1024);
      builder.pop();
      builder.push("worldgen");
      PROCEDURAL_POIS_ENABLED = builder.comment("Generate compact Industrial Nexus procedural POIs during new chunk loads.").define("proceduralPoisEnabled", true);
      POI_SPACING_CHUNKS = builder.comment("Base chunk spacing for Industrial Nexus procedural POI attempts.").defineInRange("poiSpacingChunks", 48, 12, 160);
      POI_SEARCH_RADIUS = builder.comment("Surface search radius used when placing Industrial Nexus POIs.").defineInRange("poiSearchRadius", 16, 2, 32);
      THERMAL_PLANT_SPACING_BONUS = builder.comment("Extra chunks between Abandoned Thermal Plant attempts.").defineInRange("thermalPlantSpacingBonus", 7, 0, 120);
      FACTORY_SPACING_BONUS = builder.comment("Extra chunks between Rusted Factory Complex attempts.").defineInRange("factorySpacingBonus", 0, 0, 120);
      GEOTHERMAL_SPACING_BONUS = builder.comment("Extra chunks between Geothermal Drill Site attempts.").defineInRange("geothermalSpacingBonus", 4, 0, 120);
      REACTOR_SPACING_BONUS = builder.comment("Extra chunks between Reactor Cooling Station attempts.").defineInRange("reactorSpacingBonus", 12, 0, 120);
      NEXUS_EXCHANGER_SPACING_BONUS = builder.comment("Extra chunks between Nexus Heat Exchanger Ruins attempts.").defineInRange("nexusExchangerSpacingBonus", 16, 0, 120);
      builder.pop();
      SPEC = builder.build();
   }
}
