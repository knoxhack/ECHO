package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.block.entity.IndustrialFluxDuctBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialFluidPipeBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialItemDuctBlockEntity;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMachineBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
   private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "echoindustrialnexus");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialMachineBlockEntity>> INDUSTRIAL_MACHINE = BLOCK_ENTITIES.register(
      "industrial_machine", () -> new BlockEntityType(IndustrialMachineBlockEntity::new, machineBlocks())
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialFluxDuctBlockEntity>> FLUX_DUCT = BLOCK_ENTITIES.register(
      "flux_duct", () -> new BlockEntityType(IndustrialFluxDuctBlockEntity::new, fluxDuctBlocks())
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialItemDuctBlockEntity>> ITEM_DUCT = BLOCK_ENTITIES.register(
      "item_duct", () -> new BlockEntityType(IndustrialItemDuctBlockEntity::new, itemDuctBlocks())
   );
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialFluidPipeBlockEntity>> FLUID_PIPE = BLOCK_ENTITIES.register(
      "fluid_pipe", () -> new BlockEntityType(IndustrialFluidPipeBlockEntity::new, fluidPipeBlocks())
   );

   private ModBlockEntities() {
   }

   public static void register(IEventBus eventBus) {
      BLOCK_ENTITIES.register(eventBus);
   }

   private static Set<Block> machineBlocks() {
      return Set.of(
         (Block)ModBlocks.SCRAP_DYNAMO.get(),
         (Block)ModBlocks.THERMAL_ARRAY.get(),
         (Block)ModBlocks.GEOTHERMAL_PUMP.get(),
         (Block)ModBlocks.REACTOR_HEAT_EXCHANGER.get(),
         (Block)ModBlocks.SOLAR_CONCENTRATOR.get(),
         (Block)ModBlocks.STATIC_HEAT_EXCHANGER.get(),
         (Block)ModBlocks.FURNACE_WARDEN_CORE.get(),
         (Block)ModBlocks.ORE_GRINDER.get(),
         (Block)ModBlocks.SALVAGE_SHREDDER.get(),
         (Block)ModBlocks.ALLOY_KILN.get(),
         (Block)ModBlocks.SUBSTRATE_GRINDER.get(),
         (Block)ModBlocks.FLUID_REFINER.get(),
         (Block)ModBlocks.WATER_PURIFIER.get(),
         (Block)ModBlocks.FILTER_PRESS.get(),
         (Block)ModBlocks.COMPONENT_ASSEMBLER.get(),
         (Block)ModBlocks.INDUSTRIAL_RECYCLER.get(),
         (Block)ModBlocks.CORRUPTION_SAFE_RECYCLER.get(),
         (Block)ModBlocks.NEXUS_THERMAL_INJECTOR.get(),
         (Block)ModBlocks.REALITY_FURNACE.get(),
         (Block)ModBlocks.FACTORY_CONTROLLER.get(),
         (Block)ModBlocks.FLUX_CAPACITOR_BANK.get(),
         (Block)ModBlocks.REINFORCED_CAPACITOR.get(),
         (Block)ModBlocks.STABILIZED_FLUX_BANK.get(),
         (Block)ModBlocks.HYBRID_FLUX_BANK.get(),
         (Block)ModBlocks.CORE_FLUX_BANK.get(),
         (Block)ModBlocks.INDUSTRIAL_SCRUBBER.get()
      );
   }


   private static Set<Block> itemDuctBlocks() {
      return Set.of(
         (Block)ModBlocks.SCRAP_DUCT.get(),
         (Block)ModBlocks.REINFORCED_DUCT.get(),
         (Block)ModBlocks.SMART_DUCT.get(),
         (Block)ModBlocks.VACUUM_DUCT.get(),
         (Block)ModBlocks.NEXUS_SAFE_DUCT.get()
      );
   }

   private static Set<Block> fluxDuctBlocks() {
      return Set.of(
         (Block)ModBlocks.COPPER_FLUX_DUCT.get(),
         (Block)ModBlocks.REINFORCED_FLUX_DUCT.get(),
         (Block)ModBlocks.STABILIZED_FLUX_DUCT.get(),
         (Block)ModBlocks.HYBRID_FLUX_DUCT.get(),
         (Block)ModBlocks.CORE_FLUX_DUCT.get()
      );
   }

   private static Set<Block> fluidPipeBlocks() {
      return Set.of(
         (Block)ModBlocks.RUSTED_PIPE.get(),
         (Block)ModBlocks.REINFORCED_PIPE.get(),
         (Block)ModBlocks.PRESSURIZED_PIPE.get(),
         (Block)ModBlocks.SHIELDED_PIPE.get(),
         (Block)ModBlocks.STATIC_PIPE.get()
      );
   }
}
