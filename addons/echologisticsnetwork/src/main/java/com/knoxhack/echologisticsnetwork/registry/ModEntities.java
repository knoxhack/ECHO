package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.entity.CourierDroneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities(EchoLogisticsNetwork.MODID);

   public static final DeferredHolder<EntityType<?>, EntityType<CourierDroneEntity>> COURIER_DRONE =
      ENTITIES.registerEntityType("courier_drone", CourierDroneEntity::new, MobCategory.MISC,
         builder -> builder.sized(0.6F, 0.6F).clientTrackingRange(10));

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(COURIER_DRONE.get(), CourierDroneEntity.createAttributes().build());
   }
}
