package com.knoxhack.echonexusprotocol.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;

public final class ModEnergyCapabilities {
   private ModEnergyCapabilities() {
   }

   public static void register(RegisterCapabilitiesEvent event) {
      event.registerBlockEntity(Energy.BLOCK, (BlockEntityType)ModBlockEntities.NEXUS_MACHINE.get(), (be, side) -> be instanceof com.knoxhack.echonexusprotocol.block.entity.NexusMachineBlockEntity machine ? machine.energyStorage() : null);
   }
}
