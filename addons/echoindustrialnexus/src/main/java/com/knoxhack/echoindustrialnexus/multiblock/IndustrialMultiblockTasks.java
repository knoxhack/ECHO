package com.knoxhack.echoindustrialnexus.multiblock;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import net.minecraft.resources.Identifier;

public final class IndustrialMultiblockTasks {
   public static final Identifier PROCESS_SCRAP_INTO_SCRAP_PLATE = EchoIndustrialNexus.id("process_scrap_into_scrap_plate");
   public static final Identifier PRESS_SCRAP_PLATE_INTO_REFINED_PLATE = EchoIndustrialNexus.id("press_scrap_plate_into_refined_plate");
   public static final Identifier WELD_REINFORCED_MACHINE_FRAME = EchoIndustrialNexus.id("weld_reinforced_machine_frame");
   public static final Identifier ASSEMBLE_PRECISION_CIRCUIT = EchoIndustrialNexus.id("assemble_precision_circuit");
   public static final Identifier ENCODE_RECIPE_MATRIX_SHARD = EchoIndustrialNexus.id("encode_recipe_matrix_shard");

   private IndustrialMultiblockTasks() {
   }

   public static void register() {
      EchoIndustrialNexus.LOGGER.debug("Industrial Nexus multiblock automation tasks are data-driven through MultiblockCore automation recipes.");
   }
}
