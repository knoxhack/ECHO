package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.content.SeedProfile;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
   public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoAgricultureReclamation.MODID);

   public static final DeferredHolder<DataComponentType<?>, DataComponentType<SeedProfile>> SEED_PROFILE =
      DATA_COMPONENT_TYPES.register(
         "seed_profile",
         () -> DataComponentType.<SeedProfile>builder()
            .persistent(SeedProfile.CODEC)
            .networkSynchronized(SeedProfile.STREAM_CODEC)
            .build()
      );

   private ModDataComponents() {
   }

   public static void register(IEventBus eventBus) {
      DATA_COMPONENT_TYPES.register(eventBus);
   }
}
