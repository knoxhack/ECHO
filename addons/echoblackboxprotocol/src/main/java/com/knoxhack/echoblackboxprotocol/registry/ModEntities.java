package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.entity.BlackboxBossEntity;
import com.knoxhack.echoblackboxprotocol.entity.BlackboxMobEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities("echoblackboxprotocol");
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> ARCHIVE_HUSK = mob("archive_husk", 0.6F, 1.95F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> SECURITY_ECHO = mob("security_echo", 0.6F, 1.95F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> MEMORY_PARASITE = mob("memory_parasite", 0.55F, 0.9F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> FALSE_ECHO_MINION = mob("false_echo_minion", 0.6F, 1.95F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> COMMAND_REMNANT_MINION = mob("command_remnant_minion", 0.6F, 1.95F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> BLACKBOX_SENTINEL = mob("blackbox_sentinel", 0.9F, 2.4F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxBossEntity>> FALSE_ECHO = boss("false_echo", 0.8F, 2.2F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxBossEntity>> COMMAND_REMNANT = boss("command_remnant", 0.9F, 2.4F);
   public static final DeferredHolder<EntityType<?>, EntityType<BlackboxBossEntity>> NEXUS_GUARDIAN = boss("nexus_guardian", 1.1F, 2.8F);

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static void registerAttributes(EntityAttributeCreationEvent event) {
      event.put((EntityType)ARCHIVE_HUSK.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)SECURITY_ECHO.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)MEMORY_PARASITE.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)FALSE_ECHO_MINION.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)COMMAND_REMNANT_MINION.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)BLACKBOX_SENTINEL.get(), BlackboxMobEntity.createAttributes().build());
      event.put((EntityType)FALSE_ECHO.get(), BlackboxBossEntity.createAttributes().build());
      event.put((EntityType)COMMAND_REMNANT.get(), BlackboxBossEntity.createAttributes().build());
      event.put((EntityType)NEXUS_GUARDIAN.get(), BlackboxBossEntity.createAttributes().build());
   }

   private static DeferredHolder<EntityType<?>, EntityType<BlackboxMobEntity>> mob(String name, float width, float height) {
      return ENTITIES.registerEntityType(name, BlackboxMobEntity::new, MobCategory.MONSTER, builder -> builder.sized(width, height).clientTrackingRange(8));
   }

   private static DeferredHolder<EntityType<?>, EntityType<BlackboxBossEntity>> boss(String name, float width, float height) {
      return ENTITIES.registerEntityType(name, BlackboxBossEntity::new, MobCategory.MONSTER, builder -> builder.sized(width, height).clientTrackingRange(12));
   }
}
