package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.entity.PollinatorDroneEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities(EchoAgricultureReclamation.MODID);

   public static final DeferredHolder<EntityType<?>, EntityType<PollinatorDroneEntity>> POLLINATOR_DRONE =
      ENTITIES.registerEntityType("pollinator_drone", PollinatorDroneEntity::new, MobCategory.MISC,
         builder -> builder.sized(0.6F, 0.6F).clientTrackingRange(10));

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put(POLLINATOR_DRONE.get(), PollinatorDroneEntity.createAttributes().build());
   }
}
