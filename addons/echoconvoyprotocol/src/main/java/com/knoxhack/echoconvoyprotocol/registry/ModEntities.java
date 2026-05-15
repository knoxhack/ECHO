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
      vehicle(ConvoyVehicleKind.SCRAP_BIKE, 0.95F, 1.35F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> WASTELAND_ROVER =
      vehicle(ConvoyVehicleKind.WASTELAND_ROVER, 2.25F, 1.75F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> CARGO_CRAWLER =
      vehicle(ConvoyVehicleKind.CARGO_CRAWLER, 3.05F, 1.85F);
   public static final DeferredHolder<EntityType<?>, EntityType<ConvoyVehicleEntity>> ARMORED_RELAY_TRUCK =
      vehicle(ConvoyVehicleKind.ARMORED_RELAY_TRUCK, 3.10F, 2.15F);

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
