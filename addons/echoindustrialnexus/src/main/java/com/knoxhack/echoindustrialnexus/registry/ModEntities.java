package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.entity.FurnaceDroneEntity;
import com.knoxhack.echoindustrialnexus.entity.FurnaceWardenEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities("echoindustrialnexus");
   public static final DeferredHolder<EntityType<?>, EntityType<FurnaceWardenEntity>> FURNACE_WARDEN = ENTITIES.registerEntityType(
      "furnace_warden", FurnaceWardenEntity::new, MobCategory.MONSTER, builder -> builder.sized(1.2F, 2.8F).clientTrackingRange(12)
   );
   public static final DeferredHolder<EntityType<?>, EntityType<FurnaceDroneEntity>> FURNACE_DRONE = ENTITIES.registerEntityType(
      "furnace_drone", FurnaceDroneEntity::new, MobCategory.MONSTER, builder -> builder.sized(0.75F, 1.65F).clientTrackingRange(8)
   );

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)FURNACE_WARDEN.get(), FurnaceWardenEntity.createAttributes().build());
      event.put((EntityType)FURNACE_DRONE.get(), FurnaceDroneEntity.createAttributes().build());
   }
}
