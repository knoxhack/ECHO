package com.knoxhack.echoindustrialnexus.registry;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
   private ModCapabilities() {
   }

   public static void register(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(
         Capabilities.Fluid.BLOCK,
         ModBlockEntities.INDUSTRIAL_MACHINE.get(),
         (machine, direction) -> machine.fluidHandler(direction)
      );
      event.registerBlockEntity(
         Capabilities.Fluid.BLOCK,
         ModBlockEntities.FLUID_PIPE.get(),
         (pipe, direction) -> pipe.fluidHandler(direction)
      );
   }
}
