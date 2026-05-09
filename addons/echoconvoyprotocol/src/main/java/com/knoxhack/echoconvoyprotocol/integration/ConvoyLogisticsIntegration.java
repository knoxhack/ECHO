package com.knoxhack.echoconvoyprotocol.integration;

import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleEntity;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public final class ConvoyLogisticsIntegration {
   private static final String SERVICE = "com.knoxhack.echologisticsnetwork.service.LogisticsNetworkService";

   private ConvoyLogisticsIntegration() {
   }

   public static boolean depositVehicleCargo(Level level, BlockPos anchorPos, ConvoyVehicleEntity vehicle) {
      if (!ModList.get().isLoaded("echologisticsnetwork") || vehicle == null) {
         return false;
      }
      List<ItemStack> cargo = vehicle.cargoStacks();
      if (cargo.isEmpty()) {
         return false;
      }
      try {
         Class<?> service = Class.forName(SERVICE);
         Method deliverPayload = service.getMethod("deliverPayload", Level.class, BlockPos.class, List.class);
         Object delivered = deliverPayload.invoke(null, level, anchorPos, cargo);
         if (Boolean.TRUE.equals(delivered)) {
            vehicle.clearCargo();
            return true;
         }
      } catch (ReflectiveOperationException | LinkageError ignored) {
         return false;
      }
      return false;
   }
}
