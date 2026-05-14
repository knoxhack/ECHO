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
      vehicle(ConvoyVehicleKind.SCRAP_BIKE, 1.2F, 1.1F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> WASTELAND_ROVER =
      vehicle(ConvoyVehicleKind.WASTELAND_ROVER, 2.2F, 1.7F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> CARGO_CRAWLER =
      vehicle(ConvoyVehicleKind.CARGO_CRAWLER, 3.0F, 2.0F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> ARMORED_RELAY_TRUCK =
      vehicle(ConvoyVehicleKind.ARMORED_RELAY_TRUCK, 2.85F, 2.05F);

   private ModEntities() {
   }

   public static void register(IEventBus eventBus) {
      ENTITIES.register(eventBus);
   }

   public static EntityType<ConvoyVehicleEntity> typeFor(ConvoyVehicleKind kind) {
      if (kind == ConvoyVehicleKind.SCRAP_BIKE) {
         return SCRAP_BIKE.get();
      }
      if (kind == ConvoyVehicleKind.WASTELAND_ROVER) {
         return WASTELAND_ROVER.get();
      }
      if (kind == ConvoyVehicleKind.CARGO_CRAWLER) {
         return CARGO_CRAWLER.get();
      }
      return ARMORED_RELAY_TRUCK.get();
   }

   private static DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> vehicle(ConvoyVehicleKind kind, float width, float height) {
      return ENTITIES.registerEntityType(kind.getSerializedName(),
         (type, level) -> ConvoyVehicleEntity.create(type, level, kind),
         MobCategory.MISC,
         builder -> builder.sized(width, height).clientTrackingRange(12));
   }
}
