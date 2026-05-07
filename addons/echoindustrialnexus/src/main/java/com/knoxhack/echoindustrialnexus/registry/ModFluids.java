package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMachineBlockEntity;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class ModFluids {
   private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, EchoIndustrialNexus.MODID);
   private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, EchoIndustrialNexus.MODID);
   public static final IndustrialFluid DIRTY_WATER = register("dirty_water", IndustrialMachineBlockEntity.FLUID_DIRTY_WATER, 285, 1200, Rarity.COMMON);
   public static final IndustrialFluid CLEAN_WATER = register("clean_water", IndustrialMachineBlockEntity.FLUID_CLEAN_WATER, 285, 1000, Rarity.COMMON);
   public static final IndustrialFluid TOXIC_SLUDGE = register("toxic_sludge", IndustrialMachineBlockEntity.FLUID_TOXIC_SLUDGE, 330, 3600, Rarity.UNCOMMON);
   public static final IndustrialFluid STATIC_FLUID = register("static_fluid", IndustrialMachineBlockEntity.FLUID_STATIC, 220, 1800, Rarity.RARE);
   public static final IndustrialFluid CRYO_GEL = register("cryo_gel", IndustrialMachineBlockEntity.FLUID_CRYO_GEL, 140, 2400, Rarity.UNCOMMON);
   public static final IndustrialFluid COOLANT = register("coolant", IndustrialMachineBlockEntity.FLUID_COOLANT, 175, 900, Rarity.UNCOMMON);
   public static final IndustrialFluid CHEMICAL_SOLVENT = register("chemical_solvent", IndustrialMachineBlockEntity.FLUID_SOLVENT, 315, 1300, Rarity.UNCOMMON);
   public static final IndustrialFluid NEXUS_GEL = register("nexus_gel", IndustrialMachineBlockEntity.FLUID_NEXUS_GEL, 260, 2100, Rarity.RARE);
   public static final IndustrialFluid OIL_RESIDUE = register("oil_residue", IndustrialMachineBlockEntity.FLUID_OIL_RESIDUE, 310, 4200, Rarity.COMMON);
   public static final List<IndustrialFluid> ALL = List.of(
      DIRTY_WATER,
      CLEAN_WATER,
      TOXIC_SLUDGE,
      STATIC_FLUID,
      CRYO_GEL,
      COOLANT,
      CHEMICAL_SOLVENT,
      NEXUS_GEL,
      OIL_RESIDUE
   );

   private ModFluids() {
   }

   public static void register(IEventBus eventBus) {
      FLUID_TYPES.register(eventBus);
      FLUIDS.register(eventBus);
   }

   public static FluidResource resourceFor(int fluidId) {
      IndustrialFluid fluid = byId(fluidId);
      return fluid == null ? FluidResource.EMPTY : FluidResource.of(fluid.source().get());
   }

   public static int idFor(FluidResource resource) {
      if (resource == null || resource.isEmpty()) {
         return IndustrialMachineBlockEntity.FLUID_NONE;
      }
      Fluid fluid = resource.getFluid();
      for (IndustrialFluid industrialFluid : ALL) {
         if (industrialFluid.source().get() == fluid || industrialFluid.flowing().get() == fluid) {
            return industrialFluid.id();
         }
      }
      return IndustrialMachineBlockEntity.FLUID_NONE;
   }

   public static IndustrialFluid byId(int id) {
      for (IndustrialFluid fluid : ALL) {
         if (fluid.id() == id) {
            return fluid;
         }
      }
      return null;
   }

   public static boolean isNexusFluid(int id) {
      return id == IndustrialMachineBlockEntity.FLUID_STATIC || id == IndustrialMachineBlockEntity.FLUID_NEXUS_GEL;
   }

   public static boolean isHazardousFluid(int id) {
      return id == IndustrialMachineBlockEntity.FLUID_TOXIC_SLUDGE
         || id == IndustrialMachineBlockEntity.FLUID_STATIC
         || id == IndustrialMachineBlockEntity.FLUID_NEXUS_GEL
         || id == IndustrialMachineBlockEntity.FLUID_OIL_RESIDUE;
   }

   public static boolean isPressurizedSafe(int id) {
      return id == IndustrialMachineBlockEntity.FLUID_CLEAN_WATER
         || id == IndustrialMachineBlockEntity.FLUID_COOLANT
         || id == IndustrialMachineBlockEntity.FLUID_CRYO_GEL;
   }

   private static IndustrialFluid register(String name, int id, int temperature, int viscosity, Rarity rarity) {
      DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register(
         name,
         () -> new FluidType(
            FluidType.Properties.create()
               .descriptionId("fluid." + EchoIndustrialNexus.MODID + "." + name)
               .temperature(temperature)
               .viscosity(viscosity)
               .rarity(rarity)
               .canSwim(false)
               .canDrown(false)
         )
      );
      AtomicReference<DeferredHolder<Fluid, ? extends Fluid>> sourceRef = new AtomicReference<>();
      AtomicReference<DeferredHolder<Fluid, ? extends Fluid>> flowingRef = new AtomicReference<>();
      DeferredHolder<Fluid, BaseFlowingFluid.Source> source = FLUIDS.register(
         name,
         () -> new BaseFlowingFluid.Source(new BaseFlowingFluid.Properties(type::get, () -> sourceRef.get().get(), () -> flowingRef.get().get()))
      );
      DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing = FLUIDS.register(
         "flowing_" + name,
         () -> new BaseFlowingFluid.Flowing(new BaseFlowingFluid.Properties(type::get, () -> sourceRef.get().get(), () -> flowingRef.get().get()))
      );
      sourceRef.set(source);
      flowingRef.set(flowing);
      return new IndustrialFluid(id, name, type, source, flowing);
   }

   public record IndustrialFluid(
      int id,
      String name,
      DeferredHolder<FluidType, FluidType> type,
      DeferredHolder<Fluid, BaseFlowingFluid.Source> source,
      DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing
   ) {
   }
}
