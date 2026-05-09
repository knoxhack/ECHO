package com.knoxhack.echoconvoyprotocol.item;

import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
import com.knoxhack.echoconvoyprotocol.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VehicleKitItem extends Item {
   private final ConvoyVehicleKind kind;

   public VehicleKitItem(ConvoyVehicleKind kind, Properties properties) {
      super(properties);
      this.kind = kind;
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (!(level instanceof ServerLevel serverLevel)) {
         return InteractionResult.SUCCESS;
      }
      EntityType<ConvoyVehicleEntity> type = ModEntities.typeFor(kind);
      ConvoyVehicleEntity vehicle = type.create(serverLevel, EntitySpawnReason.EVENT);
      if (vehicle == null) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Vehicle registry offline."));
         return InteractionResult.CONSUME;
      }
      vehicle.setPos(player.getX(), player.getY(), player.getZ());
      vehicle.setYRot(player.getYRot());
      vehicle.setOldPosAndRot();
      if (!serverLevel.noBlockCollision(vehicle, vehicle.getBoundingBox()) || !serverLevel.noBorderCollision(vehicle, vehicle.getBoundingBox())) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Clear a flat staging area before deploying the " + kind.displayName() + "."));
         return InteractionResult.CONSUME;
      }
      if (!serverLevel.addFreshEntity(vehicle)) {
         player.sendSystemMessage(Component.literal("ECHO CONVOY // Vehicle deployment failed in this loaded area."));
         return InteractionResult.CONSUME;
      }
      ItemStack stack = player.getItemInHand(hand);
      if (!player.getAbilities().instabuild) {
         stack.shrink(1);
      }
      player.sendSystemMessage(Component.literal("ECHO CONVOY // " + kind.displayName() + " deployed. Right-click to claim and drive."));
      return InteractionResult.SUCCESS_SERVER;
   }
}
