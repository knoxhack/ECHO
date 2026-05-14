package com.knoxhack.echoblockworks;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class Config {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue PROCEDURAL_SCATTER_ENABLED;
   public static final IntValue SCATTER_SPACING_CHUNKS;
   public static final IntValue SCATTER_SEARCH_RADIUS;
   public static final IntValue SCATTER_MAX_PIECES;

   private Config() {
   }

   public static void registerEchoConfig() {
      EchoConfigRegistry.register(EchoConfigProvider.of(EchoBlockworks.MODID, () -> new EchoConfigModule(
         EchoBlockworks.MODID,
         "ECHO Blockworks",
         List.of(new EchoConfigCategory("worldgen", "Worldgen", List.of(
            EchoConfigEntry.booleanSpec("procedural_scatter", "Procedural Scatter",
               "Generate rare Blockworks rubble and debris scatter during new chunk loads.",
               EchoConfigSide.COMMON, PROCEDURAL_SCATTER_ENABLED, true, true, true),
            EchoConfigEntry.intSpec("scatter_spacing_chunks", "Scatter Spacing Chunks",
               "Base chunk spacing for Blockworks procedural scatter attempts.",
               EchoConfigSide.COMMON, SCATTER_SPACING_CHUNKS, 12, 160, true, true, true),
            EchoConfigEntry.intSpec("scatter_search_radius", "Scatter Search Radius",
               "Horizontal radius used when placing small Blockworks scatter fragments.",
               EchoConfigSide.COMMON, SCATTER_SEARCH_RADIUS, 2, 32, true, true, true),
            EchoConfigEntry.intSpec("scatter_max_pieces", "Scatter Max Pieces",
               "Maximum individual blocks or detail pieces placed by one scatter cluster.",
               EchoConfigSide.COMMON, SCATTER_MAX_PIECES, 1, 16, true, true, true)))))));
   }

   static {
      Builder builder = new Builder();
      builder.push("worldgen");
      PROCEDURAL_SCATTER_ENABLED = builder.comment("Generate rare Blockworks rubble and debris scatter during new chunk loads.").define("proceduralScatterEnabled", true);
      SCATTER_SPACING_CHUNKS = builder.comment("Base chunk spacing for Blockworks procedural scatter attempts.").defineInRange("scatterSpacingChunks", 32, 12, 160);
      SCATTER_SEARCH_RADIUS = builder.comment("Horizontal radius used when placing small Blockworks scatter fragments.").defineInRange("scatterSearchRadius", 10, 2, 32);
      SCATTER_MAX_PIECES = builder.comment("Maximum individual blocks or detail pieces placed by one scatter cluster.").defineInRange("scatterMaxPieces", 7, 1, 16);
      builder.pop();
      SPEC = builder.build();
   }
}
