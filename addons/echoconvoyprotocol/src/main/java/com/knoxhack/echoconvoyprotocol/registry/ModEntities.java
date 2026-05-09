package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Entities;

public final class ModEntities {
   public static final Entities ENTITIES = DeferredRegister.createEntities(EchoConvoyProtocol.MODID);

   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> SCRAP_BIKE =
      vehicle(ConvoyVehicleKind.SCRAP_BIKE, 0.85F, 0.8F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> WASTELAND_ROVER =
      vehicle(ConvoyVehicleKind.WASTELAND_ROVER, 1.55F, 1.2F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> CARGO_CRAWLER =
      vehicle(ConvoyVehicleKind.CARGO_CRAWLER, 2.3F, 1.45F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> ARMORED_RELAY_TRUCK =
      vehicle(ConvoyVehicleKind.ARMORED_RELAY_TRUCK, 2.0F, 1.55F);

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static EntityType<ConvoyVehicleEntity> typeFor(ConvoyVehicleKind kind) {
      return switch (kind) {
         case SCRAP_BIKE -> SCRAP_BIKE.get();
         case WASTELAND_ROVER -> WASTELAND_ROVER.get();
         case CARGO_CRAWLER -> CARGO_CRAWLER.get();
         case ARMORED_RELAY_TRUCK -> ARMORED_RELAY_TRUCK.get();
      };
   }

   private static DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> vehicle(ConvoyVehicleKind kind, float width, float height) {
      return ENTITIES.registerEntityType(kind.getSerializedName(),
         (type, level) -> ConvoyVehicleEntity.create(type, level, kind),
         MobCategory.MISC,
         builder -> builder.sized(width, height).clientTrackingRange(12));
   }
}
