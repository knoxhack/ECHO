package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echoblackboxprotocol.integration.BlackboxIndustrialCompat;
import com.knoxhack.echoindustrialnexus.block.entity.IndustrialMachineBlockEntity;
import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echonexusprotocol.integration.NexusIndustrialCompat;
import com.knoxhack.echostationfall.integration.StationfallIndustrialCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;

public final class IndustrialCompat {
   private IndustrialCompat() {
   }

   public static void applyScrubber(ServerLevel level, BlockPos pos, String mode) {
      if (mode == null) {
         mode = "Air Mode";
      }
      if ("Radiation Mode".equals(mode)) {
         invokeStatic("echoashfallprotocol", "com.knoxhack.echoashfallprotocol.hazard.RadiationHelper", "reduceRadiationAround", level, pos, 16, 2);
      } else if ("Blight Mode".equals(mode)) {
         applyNexusBlightScrubber(level, pos, 16, 2);
      } else if ("Station Mode".equals(mode)) {
         applyStationScrubber(level, pos, 16, 2);
      } else if ("Air Mode".equals(mode)) {
         invokeStatic("echoashfallprotocol", "com.knoxhack.echoashfallprotocol.hazard.ToxicAirHelper", "cleanAirAround", level, pos, 16, 2);
      }
   }

   public static void recordNexusThermalPressure(ServerLevel level, BlockPos pos, int intensity) {
      if (!ModList.get().isLoaded("echonexusprotocol")) {
         return;
      }
      try {
         NexusIndustrialCompat.recordThermalPressure(level, pos, Math.max(1, intensity));
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Nexus Industrial typed compat unavailable.", exception);
      }
   }

   public static void recordStaticFluidLeak(ServerLevel level, BlockPos pos, int fluidId, int amount) {
      if (fluidId != IndustrialMachineBlockEntity.FLUID_STATIC && fluidId != IndustrialMachineBlockEntity.FLUID_NEXUS_GEL) {
         return;
      }
      if (ModList.get().isLoaded("echonexusprotocol")) {
         try {
            NexusIndustrialCompat.recordStaticFluidLeak(level, pos, Math.max(1, amount));
         } catch (LinkageError exception) {
            EchoIndustrialNexus.LOGGER.debug("Nexus static leak compat unavailable.", exception);
         }
      }
   }

   public static void recordIndustrialOutput(Level level, BlockPos pos, ItemStack output) {
      if (!(level instanceof ServerLevel serverLevel) || output.isEmpty()) {
         return;
      }
      String itemId = BuiltInRegistries.ITEM.getKey(output.getItem()).getPath();
      for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(20.0D))) {
         recordOrbitalComponent(player, itemId);
         recordStationfallComponent(player, itemId);
         recordBlackboxComponent(player, itemId);
         if ("emergency_oxygen_filter".equals(itemId)) {
            supportOrbitalSuit(player, 35, 0, 0);
            supportStationfallSuit(player, 35, 0, 0);
         } else if ("pressure_seal_kit".equals(itemId) || "hull_repair_foam".equals(itemId)) {
            supportOrbitalSuit(player, 0, 25, 0);
            supportStationfallSuit(player, 0, 35, 0);
         } else if ("signal_panic_dampener".equals(itemId)) {
            supportStationfallSuit(player, 0, 0, 30);
         } else if ("memory_stabilizer_casing".equals(itemId) || "blackbox_decoder_cooling_system".equals(itemId)) {
            stabilizeBlackboxMemory(player, 8, 1);
         }
      }
   }

   public static boolean isLoaded(String modId) {
      return ModList.get().isLoaded(modId);
   }

   private static void applyNexusBlightScrubber(ServerLevel level, BlockPos pos, int radius, int intensity) {
      if (!ModList.get().isLoaded("echonexusprotocol")) {
         return;
      }
      try {
         NexusIndustrialCompat.stabilizeIndustrialBlight(level, pos, radius, intensity);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Nexus scrubber typed compat unavailable.", exception);
      }
   }

   private static void applyStationScrubber(ServerLevel level, BlockPos pos, int radius, int intensity) {
      if (!ModList.get().isLoaded("echostationfall")) {
         return;
      }
      try {
         StationfallIndustrialCompat.stabilizeStationScrubber(level, pos, radius, intensity);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Stationfall scrubber typed compat unavailable.", exception);
      }
   }

   private static void recordOrbitalComponent(ServerPlayer player, String itemId) {
      invokeStatic("echoorbitalremnants", "com.knoxhack.echoorbitalremnants.integration.OrbitalIndustrialCompat", "recordIndustrialComponent", player, itemId);
   }

   private static void supportOrbitalSuit(ServerPlayer player, int oxygen, int pressure, int radiation) {
      invokeStatic("echoorbitalremnants", "com.knoxhack.echoorbitalremnants.integration.OrbitalIndustrialCompat", "supportSuitFromIndustrial", player, oxygen, pressure, radiation);
   }

   private static void recordStationfallComponent(ServerPlayer player, String itemId) {
      if (!ModList.get().isLoaded("echostationfall")) {
         return;
      }
      try {
         StationfallIndustrialCompat.recordIndustrialComponent(player, itemId);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Stationfall Industrial component compat unavailable.", exception);
      }
   }

   private static void supportStationfallSuit(ServerPlayer player, int oxygen, int pressure, int panic) {
      if (!ModList.get().isLoaded("echostationfall")) {
         return;
      }
      try {
         StationfallIndustrialCompat.supportSuit(player, oxygen, pressure, panic);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Stationfall Industrial suit compat unavailable.", exception);
      }
   }

   private static void recordBlackboxComponent(ServerPlayer player, String itemId) {
      if (!ModList.get().isLoaded("echoblackboxprotocol")) {
         return;
      }
      try {
         BlackboxIndustrialCompat.recordIndustrialComponent(player, itemId);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Blackbox Industrial component compat unavailable.", exception);
      }
   }

   private static void stabilizeBlackboxMemory(ServerPlayer player, int stabilityGain, int falseSignalRelief) {
      if (!ModList.get().isLoaded("echoblackboxprotocol")) {
         return;
      }
      try {
         BlackboxIndustrialCompat.stabilizeMemory(player, stabilityGain, falseSignalRelief);
      } catch (LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Blackbox Industrial memory compat unavailable.", exception);
      }
   }

   private static void invokeStatic(String modId, String className, String methodName, Object... args) {
      if (!ModList.get().isLoaded(modId)) {
         return;
      }
      try {
         Class<?> type = Class.forName(className);
         for (java.lang.reflect.Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
               method.invoke(null, args);
               return;
            }
         }
      } catch (ReflectiveOperationException | LinkageError exception) {
         EchoIndustrialNexus.LOGGER.debug("Optional Industrial Nexus compat hook {}#{} unavailable.", className, methodName, exception);
      }
   }
}
