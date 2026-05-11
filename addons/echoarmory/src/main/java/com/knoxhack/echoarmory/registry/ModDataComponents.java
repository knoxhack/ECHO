package com.knoxhack.echoarmory.registry;

import com.knoxhack.echoarmory.EchoArmory;
import com.knoxhack.echoarmory.data.ArmoryLoadout;
import com.knoxhack.echoarmory.data.ArmoryStance;
import com.knoxhack.echoarmory.data.CosmeticTrim;
import com.knoxhack.echoarmory.data.EnergyState;
import com.knoxhack.echoarmory.data.EquipmentTier;
import com.knoxhack.echoarmory.data.InstalledModules;
import com.knoxhack.echoarmory.data.InstabilityState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
   private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoArmory.MODID);

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmoryLoadout>> ARMORY_LOADOUT =
      DATA_COMPONENT_TYPES.register("armory_loadout", () -> DataComponentType.<ArmoryLoadout>builder()
         .persistent(ArmoryLoadout.CODEC)
         .networkSynchronized(ArmoryLoadout.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<InstalledModules>> INSTALLED_MODULES =
      DATA_COMPONENT_TYPES.register("installed_modules", () -> DataComponentType.<InstalledModules>builder()
         .persistent(InstalledModules.CODEC)
         .networkSynchronized(InstalledModules.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyState>> ENERGY_STATE =
      DATA_COMPONENT_TYPES.register("energy_state", () -> DataComponentType.<EnergyState>builder()
         .persistent(EnergyState.CODEC)
         .networkSynchronized(EnergyState.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<EquipmentTier>> EQUIPMENT_TIER =
      DATA_COMPONENT_TYPES.register("equipment_tier", () -> DataComponentType.<EquipmentTier>builder()
         .persistent(EquipmentTier.CODEC)
         .networkSynchronized(EquipmentTier.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<ArmoryStance>> STANCE =
      DATA_COMPONENT_TYPES.register("stance", () -> DataComponentType.<ArmoryStance>builder()
         .persistent(ArmoryStance.CODEC)
         .networkSynchronized(ArmoryStance.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<CosmeticTrim>> COSMETIC_TRIM =
      DATA_COMPONENT_TYPES.register("cosmetic_trim", () -> DataComponentType.<CosmeticTrim>builder()
         .persistent(CosmeticTrim.CODEC)
         .networkSynchronized(CosmeticTrim.STREAM_CODEC)
         .build());

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<InstabilityState>> INSTABILITY_STATE =
      DATA_COMPONENT_TYPES.register("instability_state", () -> DataComponentType.<InstabilityState>builder()
         .persistent(InstabilityState.CODEC)
         .networkSynchronized(InstabilityState.STREAM_CODEC)
         .build());

   private ModDataComponents() {
   }

   public static void register(IEventBus eventBus) {
      DATA_COMPONENT_TYPES.register(eventBus);
   }
}
