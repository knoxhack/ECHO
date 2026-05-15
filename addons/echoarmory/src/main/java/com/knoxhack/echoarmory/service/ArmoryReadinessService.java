package com.knoxhack.echoarmory.service;

import com.knoxhack.echoarmory.content.ArmoryContent;
import com.knoxhack.echoarmory.content.ArmoryLoadoutDefinition;
import com.knoxhack.echoarmory.content.GearDefinition;
import com.knoxhack.echoarmory.content.ModuleDefinition;
import com.knoxhack.echoarmory.data.EnergyState;
import com.knoxhack.echoarmory.data.EquipmentTier;
import com.knoxhack.echoarmory.item.ArmoryData;
import com.knoxhack.echoarmory.registry.ModDataComponents;
import com.knoxhack.echoarmory.registry.ModItems;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class ArmoryReadinessService {
   private ArmoryReadinessService() {
   }

   public static List<Report> reports(Player player) {
      return ArmoryContent.loadouts().stream().map(loadout -> report(player, loadout)).toList();
   }

   public static Optional<Report> report(Player player, String loadoutId) {
      if (loadoutId == null || loadoutId.isBlank()) {
         return Optional.empty();
      }
      String selected = loadoutId.strip();
      return ArmoryContent.loadouts().stream()
         .filter(loadout -> loadout.id().toString().equals(selected) || loadout.id().getPath().equals(selected))
         .findFirst()
         .map(loadout -> report(player, loadout));
   }

   public static Optional<Report> bestReport(Player player) {
      Report best = null;
      for (Report report : reports(player)) {
         if (best == null || better(report, best)) {
            best = report;
         }
      }
      return Optional.ofNullable(best);
   }

   public static Report report(Player player, ArmoryLoadoutDefinition loadout) {
      if (loadout == null) {
         throw new IllegalArgumentException("Loadout is required.");
      }
      if (player == null) {
         return new Report(loadout, State.MISSING, 0, zeroProtections(), List.of("player telemetry"), List.of(), List.of(), List.of(), logisticsAvailable(loadout));
      }

      ArrayList<String> missing = new ArrayList<>();
      ArrayList<String> staged = new ArrayList<>();
      ArrayList<String> locked = new ArrayList<>();
      Set<String> installedModules = installedModules(player);
      int tier = equippedTier(player);
      Map<ArmoryData.ProtectionType, Integer> protections = equippedProtections(player);

      for (String gearId : requiredGear(loadout)) {
         Optional<GearDefinition> gear = ArmoryContent.gear(gearId);
         String label = gear.map(GearDefinition::title).orElse(gearId);
         if (gear.isPresent() && !ArmoryData.factionGateSatisfied(player, gear.get())) {
            locked.add(ArmoryData.factionGateLine(gear.get()));
            continue;
         }
         if (gear.isPresent() && equippedFor(player, gearId, gear.get())) {
            continue;
         }
         if (hasInventoryItem(player, gearId)) {
            staged.add(label + " staged");
         } else {
            missing.add(label);
         }
      }

      if (tier < loadout.minTier()) {
         if (hasInventoryTier(player, loadout.minTier())) {
            staged.add("tier " + loadout.minTier() + " gear staged");
         } else {
            missing.add("tier " + loadout.minTier() + " gear");
         }
      }

      for (String moduleId : loadout.modules()) {
         Optional<ModuleDefinition> module = ArmoryContent.module(moduleId);
         String label = module.map(ModuleDefinition::title).orElse(moduleId);
         String fullId = module.map(definition -> definition.id().toString()).orElse(moduleId);
         if (installedModules.contains(fullId)) {
            continue;
         }
         if (hasInventoryItem(player, moduleId)) {
            staged.add(label + " ready to install");
         } else {
            missing.add(label);
         }
      }

      for (Map.Entry<ArmoryData.ProtectionType, Integer> requirement : loadout.requiredProtections().entrySet()) {
         ArmoryData.ProtectionType type = requirement.getKey();
         int required = requirement.getValue();
         int actual = protections.getOrDefault(type, 0);
         if (actual >= required) {
            continue;
         }
         String label = type.name().toLowerCase(java.util.Locale.ROOT) + " protection " + actual + "/" + required;
         if (potentialProtection(player, type) >= required) {
            staged.add(label + " staged");
         } else {
            missing.add(label);
         }
      }

      for (String gearId : requiredGear(loadout)) {
         ArmoryContent.gear(gearId).ifPresent(gear -> {
            if (gear.energyCapacity() <= 0) {
               return;
            }
            ItemStack stack = equippedStack(player, gearId, gear);
            if (stack.isEmpty()) {
               return;
            }
            EnergyState energy = stack.getOrDefault(ModDataComponents.ENERGY_STATE.get(), EnergyState.EMPTY);
            if (energy.capacity() > 0 && energy.stored() <= 0) {
               if (hasRechargeMaterial(player)) {
                  staged.add(gear.title() + " energy empty");
               } else {
                  missing.add("recharge fuel for " + gear.title());
               }
            }
         });
      }

      State state = locked.isEmpty()
         ? (missing.isEmpty() ? (staged.isEmpty() ? State.READY : State.STAGED) : State.MISSING)
         : State.LOCKED;
      return new Report(loadout, state, tier, protections, missing, staged, locked, List.copyOf(installedModules), logisticsAvailable(loadout));
   }

   public static String protectionSummary(Map<ArmoryData.ProtectionType, Integer> protections) {
      return "T/R/C/H/F "
         + protections.getOrDefault(ArmoryData.ProtectionType.TOXIC, 0) + "/"
         + protections.getOrDefault(ArmoryData.ProtectionType.RADIATION, 0) + "/"
         + protections.getOrDefault(ArmoryData.ProtectionType.COLD, 0) + "/"
         + protections.getOrDefault(ArmoryData.ProtectionType.HEAT, 0) + "/"
         + protections.getOrDefault(ArmoryData.ProtectionType.FRACTURE, 0);
   }

   private static boolean better(Report candidate, Report current) {
      if (rank(candidate.state()) != rank(current.state())) {
         return rank(candidate.state()) > rank(current.state());
      }
      if (candidate.blockerCount() != current.blockerCount()) {
         return candidate.blockerCount() < current.blockerCount();
      }
      return candidate.loadout().order() < current.loadout().order();
   }

   private static int rank(State state) {
      return switch (state) {
         case READY -> 4;
         case STAGED -> 3;
         case MISSING -> 2;
         case LOCKED -> 1;
      };
   }

   private static List<String> requiredGear(ArmoryLoadoutDefinition loadout) {
      LinkedHashSet<String> gear = new LinkedHashSet<>();
      if (!loadout.weapon().isBlank()) {
         gear.add(loadout.weapon());
      }
      gear.addAll(loadout.armor());
      return List.copyOf(gear);
   }

   private static boolean equippedFor(Player player, String itemId, GearDefinition gear) {
      return !equippedStack(player, itemId, gear).isEmpty();
   }

   private static ItemStack equippedStack(Player player, String itemId, GearDefinition gear) {
      EquipmentSlot slot = slotFor(gear);
      if (slot != null) {
         ItemStack stack = player.getItemBySlot(slot);
         return matchesItem(stack, itemId) ? stack : ItemStack.EMPTY;
      }
      if ("shield".equals(gear.baseType()) && matchesItem(player.getOffhandItem(), itemId)) {
         return player.getOffhandItem();
      }
      return matchesItem(player.getMainHandItem(), itemId) ? player.getMainHandItem() : ItemStack.EMPTY;
   }

   private static EquipmentSlot slotFor(GearDefinition gear) {
      return switch (gear.baseType()) {
         case "armor_head" -> EquipmentSlot.HEAD;
         case "armor_chest" -> EquipmentSlot.CHEST;
         case "armor_legs" -> EquipmentSlot.LEGS;
         case "armor_feet" -> EquipmentSlot.FEET;
         default -> null;
      };
   }

   private static Set<String> installedModules(Player player) {
      LinkedHashSet<String> modules = new LinkedHashSet<>();
      for (ItemStack stack : equippedStacks(player)) {
         modules.addAll(ArmoryData.modules(stack).modules());
      }
      return modules;
   }

   private static int equippedTier(Player player) {
      int tier = 0;
      for (ItemStack stack : equippedStacks(player)) {
         tier = Math.max(tier, tier(stack));
      }
      return tier;
   }

   private static int tier(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      }
      EquipmentTier component = stack.get(ModDataComponents.EQUIPMENT_TIER.get());
      if (component != null) {
         return component.tier();
      }
      return ArmoryData.gear(stack).map(GearDefinition::tier).orElse(0);
   }

   private static boolean hasInventoryTier(Player player, int minTier) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (tier(player.getInventory().getItem(i)) >= minTier) {
            return true;
         }
      }
      return false;
   }

   private static List<ItemStack> equippedStacks(Player player) {
      ArrayList<ItemStack> stacks = new ArrayList<>(ArmoryData.armorStacks(player));
      stacks.add(player.getItemInHand(InteractionHand.MAIN_HAND));
      stacks.add(player.getItemInHand(InteractionHand.OFF_HAND));
      return List.copyOf(stacks);
   }

   private static Map<ArmoryData.ProtectionType, Integer> equippedProtections(Player player) {
      EnumMap<ArmoryData.ProtectionType, Integer> protections = zeroProtectionMap();
      for (ArmoryData.ProtectionType type : ArmoryData.ProtectionType.values()) {
         protections.put(type, ArmoryData.protection(player, type));
      }
      return Map.copyOf(protections);
   }

   private static int potentialProtection(Player player, ArmoryData.ProtectionType type) {
      int total = ArmoryData.protection(player, type);
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack stack = player.getInventory().getItem(i);
         Optional<ModuleDefinition> module = ArmoryData.module(stack);
         if (module.isPresent()) {
            total += protection(module.get(), type) * Math.max(1, stack.getCount());
         }
      }
      return Math.min(100, total);
   }

   private static int protection(ModuleDefinition module, ArmoryData.ProtectionType type) {
      return switch (type) {
         case TOXIC -> module.toxicProtection();
         case RADIATION -> module.radiationProtection();
         case COLD -> module.coldProtection();
         case HEAT -> module.heatProtection();
         case FRACTURE -> module.fractureProtection();
      };
   }

   private static boolean hasInventoryItem(Player player, String itemId) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         if (matchesItem(player.getInventory().getItem(i), itemId)) {
            return true;
         }
      }
      return false;
   }

   private static boolean matchesItem(ItemStack stack, String itemId) {
      if (stack.isEmpty() || itemId == null || itemId.isBlank()) {
         return false;
      }
      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id != null && (id.toString().equals(itemId) || id.getPath().equals(itemId));
   }

   private static boolean hasRechargeMaterial(Player player) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack stack = player.getInventory().getItem(i);
         if (stack.is(ModItems.VEIL_CRYSTAL.get()) || stack.is(ModItems.RESONANCE_SHARD.get())) {
            return true;
         }
      }
      return false;
   }

   private static Map<ArmoryData.ProtectionType, Integer> zeroProtections() {
      return Map.copyOf(zeroProtectionMap());
   }

   private static EnumMap<ArmoryData.ProtectionType, Integer> zeroProtectionMap() {
      EnumMap<ArmoryData.ProtectionType, Integer> protections = new EnumMap<>(ArmoryData.ProtectionType.class);
      for (ArmoryData.ProtectionType type : ArmoryData.ProtectionType.values()) {
         protections.put(type, 0);
      }
      return protections;
   }

   private static boolean logisticsAvailable(ArmoryLoadoutDefinition loadout) {
      return loadout != null && !loadout.logisticsPreset().isBlank() && ModList.get().isLoaded("echologisticsnetwork");
   }

   public enum State {
      READY,
      STAGED,
      MISSING,
      LOCKED
   }

   public record Report(
      ArmoryLoadoutDefinition loadout,
      State state,
      int tier,
      Map<ArmoryData.ProtectionType, Integer> protections,
      List<String> missing,
      List<String> staged,
      List<String> locked,
      List<String> installedModules,
      boolean logisticsAvailable
   ) {
      public Report {
         protections = Map.copyOf(protections == null ? Map.of() : protections);
         missing = List.copyOf(missing == null ? List.of() : missing);
         staged = List.copyOf(staged == null ? List.of() : staged);
         locked = List.copyOf(locked == null ? List.of() : locked);
         installedModules = List.copyOf(installedModules == null ? List.of() : installedModules);
      }

      public boolean ready() {
         return state == State.READY;
      }

      public int blockerCount() {
         return missing.size() + staged.size() + locked.size();
      }

      public String firstBlocker() {
         if (!locked.isEmpty()) {
            return locked.getFirst();
         }
         if (!missing.isEmpty()) {
            return "Missing " + missing.getFirst();
         }
         if (!staged.isEmpty()) {
            return staged.getFirst();
         }
         return "Ready for deployment";
      }

      public String summaryLine() {
         return state + " // tier " + tier + " | " + protectionSummary(protections) + " | " + firstBlocker();
      }
   }
}
