package com.knoxhack.echoarmory.content;

import com.knoxhack.echoarmory.EchoArmory;
import com.knoxhack.echoarmory.item.ArmoryGearItem;
import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ArmoryContent {
   private static final Map<Identifier, GearDefinition> DEFAULT_GEAR = new LinkedHashMap<>();
   private static final Map<Identifier, ModuleDefinition> DEFAULT_MODULES = new LinkedHashMap<>();
   private static final Map<Identifier, SynergyDefinition> DEFAULT_SYNERGIES = new LinkedHashMap<>();
   private static final Map<Identifier, ArmoryLoadoutDefinition> DEFAULT_LOADOUTS = new LinkedHashMap<>();
   private static final Map<Identifier, FactionUnlockDefinition> DEFAULT_FACTION_UNLOCKS = new LinkedHashMap<>();
   private static final Map<Identifier, BossRecommendationDefinition> DEFAULT_BOSS_RECOMMENDATIONS = new LinkedHashMap<>();
   private static volatile LoadedContent jsonContent = LoadedContent.empty();

   static {
      defaults();
   }

   private ArmoryContent() {
   }

   public static void replaceJsonContent(LoadedContent loaded) {
      jsonContent = loaded == null ? LoadedContent.empty() : loaded;
      EchoArmory.LOGGER.info("ECHO Armory loaded {} gear, {} modules, {} synergies, {} loadouts, {} faction unlocks, and {} boss recommendations from JSON.",
         jsonContent.gear().size(), jsonContent.modules().size(), jsonContent.synergies().size(),
         jsonContent.loadouts().size(), jsonContent.factionUnlocks().size(), jsonContent.bossRecommendations().size());
   }

   public static List<GearDefinition> gear() {
      Map<Identifier, GearDefinition> merged = new LinkedHashMap<>(DEFAULT_GEAR);
      merged.putAll(jsonContent.gear());
      return merged.values().stream().sorted(Comparator.comparingInt(GearDefinition::tier).thenComparing(gear -> gear.id().toString())).toList();
   }

   public static List<ModuleDefinition> modules() {
      Map<Identifier, ModuleDefinition> merged = new LinkedHashMap<>(DEFAULT_MODULES);
      merged.putAll(jsonContent.modules());
      return merged.values().stream().sorted(Comparator.comparing(module -> module.id().toString())).toList();
   }

   public static List<SynergyDefinition> synergies() {
      Map<Identifier, SynergyDefinition> merged = new LinkedHashMap<>(DEFAULT_SYNERGIES);
      merged.putAll(jsonContent.synergies());
      return merged.values().stream().sorted(Comparator.comparing(synergy -> synergy.id().toString())).toList();
   }

   public static List<ArmoryLoadoutDefinition> loadouts() {
      Map<Identifier, ArmoryLoadoutDefinition> merged = new LinkedHashMap<>(DEFAULT_LOADOUTS);
      merged.putAll(jsonContent.loadouts());
      return merged.values().stream().sorted(Comparator.comparingInt(ArmoryLoadoutDefinition::order).thenComparing(loadout -> loadout.id().toString())).toList();
   }

   public static List<FactionUnlockDefinition> factionUnlocks() {
      Map<Identifier, FactionUnlockDefinition> merged = new LinkedHashMap<>(DEFAULT_FACTION_UNLOCKS);
      merged.putAll(jsonContent.factionUnlocks());
      return merged.values().stream().sorted(Comparator.comparing(unlock -> unlock.id().toString())).toList();
   }

   public static List<BossRecommendationDefinition> bossRecommendations() {
      Map<Identifier, BossRecommendationDefinition> merged = new LinkedHashMap<>(DEFAULT_BOSS_RECOMMENDATIONS);
      merged.putAll(jsonContent.bossRecommendations());
      return merged.values().stream().sorted(Comparator.comparing(recommendation -> recommendation.id().toString())).toList();
   }

   public static Optional<GearDefinition> gear(String id) {
      Identifier identifier = safeId(id);
      return identifier == null ? Optional.empty() : gear().stream().filter(definition -> definition.id().equals(identifier)).findFirst();
   }

   public static Optional<ModuleDefinition> module(String id) {
      Identifier identifier = safeId(id);
      return identifier == null ? Optional.empty() : modules().stream().filter(definition -> definition.id().equals(identifier)).findFirst();
   }

   public static void clearJsonForTests() {
      jsonContent = LoadedContent.empty();
   }

   public static List<String> validationErrors() {
      ArrayList<String> errors = new ArrayList<>();
      Set<String> knownTags = new HashSet<>();
      for (GearDefinition definition : gear()) {
         if (!registeredKind(definition.id(), ArmoryGearItem.ArmoryGearKind.WEAPON)
            && !registeredKind(definition.id(), ArmoryGearItem.ArmoryGearKind.ARMOR)) {
            errors.add("Gear id is not a registered Armory gear item: " + definition.id());
         }
         knownTags.addAll(definition.tags());
      }
      for (ModuleDefinition definition : modules()) {
         if (!registeredKind(definition.id(), ArmoryGearItem.ArmoryGearKind.MODULE)) {
            errors.add("Module id is not a registered Armory module item: " + definition.id());
         }
         knownTags.add(definition.slotType());
         knownTags.add(definition.effectType());
         knownTags.addAll(definition.synergyTags());
      }
      for (SynergyDefinition definition : synergies()) {
         knownTags.addAll(definition.requiredTags());
      }
      for (ArmoryLoadoutDefinition definition : loadouts()) {
         validateLoadoutItem(definition.weapon(), "weapon", definition, errors);
         for (String armorId : definition.armor()) {
            validateLoadoutItem(armorId, "armor", definition, errors);
         }
         for (String moduleId : definition.modules()) {
            if (module(moduleId).isEmpty()) {
               errors.add("Loadout " + definition.id() + " references missing module " + moduleId);
            }
         }
      }
      for (FactionUnlockDefinition definition : factionUnlocks()) {
         boolean found = gear().stream().anyMatch(gear -> gear.id().getPath().equals(definition.unlockId())
            || gear.id().toString().equals(definition.unlockId()));
         if (!found) {
            errors.add("Faction unlock " + definition.id() + " references missing gear unlock " + definition.unlockId());
         }
      }
      for (BossRecommendationDefinition definition : bossRecommendations()) {
         for (String tag : definition.recommendedTags()) {
            if (!knownTags.contains(tag)) {
               errors.add("Boss recommendation " + definition.id() + " references unknown tag " + tag);
            }
         }
      }
      return List.copyOf(errors);
   }

   static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoArmory.MODID, path);
   }

   private static Identifier safeId(String id) {
      if (id == null || id.isBlank()) {
         return null;
      }
      try {
         Identifier parsed = Identifier.parse(id);
         return parsed.getNamespace().equals("minecraft") && !id.contains(":") ? Identifier.fromNamespaceAndPath(EchoArmory.MODID, id) : parsed;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static void validateLoadoutItem(String itemId, String label, ArmoryLoadoutDefinition loadout, List<String> errors) {
      if (itemId == null || itemId.isBlank()) {
         return;
      }
      if (gear(itemId).isEmpty()) {
         errors.add("Loadout " + loadout.id() + " references missing " + label + " gear " + itemId);
      }
   }

   private static boolean registeredKind(Identifier id, ArmoryGearItem.ArmoryGearKind kind) {
      return BuiltInRegistries.ITEM.getOptional(id)
         .filter(item -> item instanceof ArmoryGearItem gearItem && gearItem.gearKind() == kind)
         .isPresent();
   }

   private static void defaults() {
      gear("alloy_sword", "Alloy Sword", "melee", 1, 3, 7.0F, 0, 120, "Early survival", "", List.of("damage", "elemental", "stability", "utility"), List.of("alloy", "blade"));
      gear("frost_blade", "Frost Blade", "melee", 2, 3, 8.5F, 0, 180, "Mid-tech", "", List.of("damage", "elemental", "stability", "utility", "power"), List.of("frost", "blade"));
      gear("veil_sabre", "Veil Sabre", "melee", 3, 4, 10.0F, 0, 260, "Industrial", "echoashfallprotocol:radwarden_compact", List.of("damage", "elemental", "stability", "utility", "power"), List.of("veil", "blade"));
      gear("harmonic_staff", "Harmonic Staff", "staff", 2, 4, 6.0F, 0, 240, "Mid-tech", "", List.of("elemental", "utility", "magic", "power"), List.of("resonance", "staff"));
      gear("arcane_dagger", "Arcane Dagger", "melee", 2, 2, 5.5F, 0, 120, "Mid-tech", "", List.of("damage", "utility", "stability"), List.of("arcane", "quick"));
      gear("energy_rifle", "Energy Rifle", "ranged", 3, 3, 9.5F, 0, 320, "Industrial", "echoashfallprotocol:crashbreak_salvage", List.of("damage", "elemental", "stability", "power"), List.of("energy", "ranged"));
      gear("veil_bow", "Veil Bow", "ranged", 2, 3, 8.0F, 0, 220, "Mid-tech", "", List.of("damage", "elemental", "utility", "power"), List.of("veil", "ranged"));
      gear("convergence_gun", "Convergence Gun", "ranged", 4, 4, 13.0F, 0, 520, "Endgame", "echoashfallprotocol:radwarden_compact", List.of("damage", "elemental", "stability", "utility", "power"), List.of("convergence", "energy", "ranged"));
      gear("resonance_hammer", "Resonance Hammer", "heavy", 3, 3, 12.0F, 0, 260, "Industrial", "", List.of("damage", "elemental", "stability", "power"), List.of("resonance", "heavy"));
      gear("sigil_chakram", "Sigil Chakram", "ranged", 4, 2, 8.5F, 0, 260, "Endgame", "", List.of("elemental", "utility", "magic"), List.of("sigil", "ranged"));
      gear("construct_gauntlet", "Construct Gauntlet", "support", 3, 3, 7.0F, 2, 300, "Industrial", "echoashfallprotocol:crashbreak_salvage", List.of("utility", "support", "power"), List.of("construct", "drone"));
      gear("arcane_shield", "Arcane Shield", "shield", 2, 2, 1.0F, 5, 180, "Mid-tech", "", List.of("defense", "utility", "power"), List.of("shield", "arcane"));
      gear("veil_resistant_helm", "Veil-resistant Helm", "armor_head", 2, 3, 0.0F, 3, 140, "Mid-tech", "echoashfallprotocol:radwarden_compact", List.of("defense", "survival", "utility", "power"), List.of("veil", "armor", "head"));
      gear("thermal_chestplate", "Thermal Chestplate", "armor_chest", 2, 3, 0.0F, 8, 180, "Mid-tech", "", List.of("defense", "survival", "utility", "power"), List.of("thermal", "armor", "chest"));
      gear("drone_leggings", "Drone-enhanced Leggings", "armor_legs", 3, 2, 0.0F, 6, 220, "Industrial", "echoashfallprotocol:crashbreak_salvage", List.of("defense", "mobility", "support", "utility"), List.of("drone", "armor", "legs"));
      gear("orbital_boots", "Orbital Boots", "armor_feet", 4, 2, 0.0F, 4, 220, "Endgame", "", List.of("defense", "mobility", "survival"), List.of("orbital", "armor", "feet"));
      gear("construct_harness", "Construct Harness", "armor_chest", 4, 4, 0.0F, 9, 360, "Endgame", "echoashfallprotocol:crashbreak_salvage", List.of("defense", "support", "utility", "power"), List.of("construct", "drone", "armor", "chest"));
      gear("sigil_augmented_suit", "Sigil Augmented Suit", "armor_chest", 4, 5, 0.0F, 10, 420, "Endgame", "", List.of("defense", "elemental", "survival", "mobility", "utility", "power"), List.of("sigil", "armor", "suit"));

      module("fire_core", "Fire Core", "elemental", "fire", 2.0F, 0, 20, 8, 0, 0, 0, 20, 0, List.of("melee", "ranged", "staff"), List.of("fire"));
      module("frost_core", "Frost Core", "elemental", "frost", 1.5F, 0, 18, 6, 0, 0, 35, 0, 0, List.of("melee", "ranged", "staff", "armor_chest"), List.of("frost"));
      module("lightning_core", "Lightning Core", "elemental", "lightning", 2.5F, 0, 26, 12, 0, 0, 0, 0, 0, List.of("melee", "ranged", "staff"), List.of("lightning"));
      module("void_core", "Void Core", "elemental", "void", 3.0F, 0, 32, 18, 0, 0, 0, 0, 30, List.of("melee", "ranged", "staff"), List.of("void", "veil"));
      module("stability_rune", "Stability Rune", "stability", "handling", 0.5F, 0, 0, 0, 0, 0, 0, 0, 5, List.of("melee", "ranged", "heavy", "staff"), List.of("stable"));
      module("life_leech_sigil", "Life Leech Sigil", "utility", "life_leech", 1.0F, 0, 18, 14, 0, 0, 0, 0, 0, List.of("melee", "heavy"), List.of("leech"));
      module("veil_shield", "Veil Shield", "defense", "fracture", 0.0F, 2, 12, 4, 0, 0, 0, 0, 45, List.of("armor_head", "armor_chest", "armor_legs", "armor_feet", "shield"), List.of("veil", "shield"));
      module("thermal_regulator", "Thermal Regulator", "survival", "thermal", 0.0F, 1, 8, 0, 0, 0, 40, 40, 0, List.of("armor_chest", "armor_legs", "armor_feet"), List.of("thermal"));
      module("gas_mask_filter", "Gas Mask Module", "survival", "toxic", 0.0F, 1, 5, 0, 55, 0, 0, 0, 0, List.of("armor_head", "armor_chest"), List.of("toxic"));
      module("radiation_shield", "Radiation Shield", "survival", "radiation", 0.0F, 2, 10, 2, 0, 55, 0, 0, 0, List.of("armor_head", "armor_chest", "armor_legs"), List.of("radiation"));
      module("mobility_servo", "Mobility Servo", "mobility", "movement", 0.0F, 0, 6, 0, 0, 0, 0, 0, 0, List.of("armor_legs", "armor_feet"), List.of("mobility"));
      module("drone_dock", "Drone Dock", "support", "repair_drone", 0.0F, 1, 20, 8, 0, 0, 0, 0, 10, List.of("armor_chest", "armor_legs", "support"), List.of("drone", "construct"));

      synergy("frost_aegis", "Frost Aegis", List.of("frost", "armor"), "ice_aura", 2, "Full frost protection and a frost weapon create a slowing aura.");
      synergy("veilbreaker", "Veilbreaker", List.of("veil", "blade", "shield"), "fracture_immunity", 3, "Veil armor and a Veil blade temporarily suppress fracture exposure.");
      synergy("construct_command", "Construct Command", List.of("construct", "drone"), "drone_scaling", 2, "Construct gauntlets and drone armor increase repair and shield output.");

      loadout("toxic_breach_kit", "Toxic Breach Kit", 0, "echoarmory:veil_resistant_helm", "echoarmory:alloy_sword", List.of("echoarmory:veil_resistant_helm", "echoarmory:thermal_chestplate"), List.of("echoarmory:gas_mask_filter"), 1, 40, "echoarmory:toxic_breach_kit");
      loadout("fracture_guardian_kit", "Fracture Guardian Kit", 20, "echoarmory:veil_sabre", "echoarmory:veil_sabre", List.of("echoarmory:veil_resistant_helm", "echoarmory:construct_harness"), List.of("echoarmory:veil_shield", "echoarmory:void_core"), 3, 60, "echoarmory:fracture_guardian_kit");
      loadout("orbital_assault_kit", "Orbital Assault Kit", 40, "echoarmory:energy_rifle", "echoarmory:energy_rifle", List.of("echoarmory:thermal_chestplate", "echoarmory:orbital_boots"), List.of("echoarmory:stability_rune", "echoarmory:mobility_servo"), 3, 45, "echoarmory:orbital_assault_kit");

      factionUnlock("salvager_energy_weapons", "echoashfallprotocol:crashbreak_salvage", 35, "energy_rifle", "Energy Rifle fabrication");
      factionUnlock("remnant_veil_armor", "echoashfallprotocol:radwarden_compact", 35, "veil_resistant_helm", "Veil-resistant armor");
      factionUnlock("construct_harness", "echoashfallprotocol:crashbreak_salvage", 55, "construct_harness", "Construct drone harness");

      bossRecommendation("fracture_heart", "Fracture Heart", 3, 60, List.of("veil", "shield", "void"), "Bring a Veil blade, Veil Shield modules, and stabilized energy reserves.");
      bossRecommendation("veilbound_guardian", "Veilbound Guardian", 2, 45, List.of("frost", "veil", "stable"), "Frost control and stability runes reduce Guardian counter-bursts.");
   }

   private static void gear(String path, String title, String baseType, int tier, int slots, float damage, int defense, int energy, String stage, String factionGate, List<String> allowedSlots, List<String> tags) {
      Identifier id = id(path);
      DEFAULT_GEAR.put(id, new GearDefinition(id, title, baseType, tier, slots, damage, defense, energy, stage, factionGate, allowedSlots, tags));
   }

   private static void module(String path, String title, String slotType, String effectType, float damageBonus, int defenseBonus, int energyCost, int instability, int toxic, int radiation, int cold, int heat, int fracture, List<String> compatibleTypes, List<String> synergyTags) {
      Identifier id = id(path);
      DEFAULT_MODULES.put(id, new ModuleDefinition(id, title, slotType, effectType, damageBonus, defenseBonus, energyCost, instability, toxic, radiation, cold, heat, fracture, compatibleTypes, synergyTags));
   }

   private static void synergy(String path, String title, List<String> tags, String effect, int potency, String hint) {
      Identifier id = id(path);
      DEFAULT_SYNERGIES.put(id, new SynergyDefinition(id, title, tags, effect, potency, hint));
   }

   private static void loadout(String path, String title, int order, String icon, String weapon, List<String> armor, List<String> modules, int minTier, int minProtection, String logisticsPreset) {
      Identifier id = id(path);
      DEFAULT_LOADOUTS.put(id, new ArmoryLoadoutDefinition(id, title, order, Identifier.parse(icon), weapon, armor, modules, minTier, minProtection, logisticsPreset));
   }

   private static void factionUnlock(String path, String factionId, int minReputation, String unlockId, String title) {
      Identifier id = id(path);
      DEFAULT_FACTION_UNLOCKS.put(id, new FactionUnlockDefinition(id, Identifier.parse(factionId), minReputation, unlockId, title));
   }

   private static void bossRecommendation(String path, String boss, int tier, int fracture, List<String> tags, String hint) {
      Identifier id = id(path);
      DEFAULT_BOSS_RECOMMENDATIONS.put(id, new BossRecommendationDefinition(id, boss, tier, fracture, tags, hint));
   }

   public record LoadedContent(
      Map<Identifier, GearDefinition> gear,
      Map<Identifier, ModuleDefinition> modules,
      Map<Identifier, SynergyDefinition> synergies,
      Map<Identifier, ArmoryLoadoutDefinition> loadouts,
      Map<Identifier, FactionUnlockDefinition> factionUnlocks,
      Map<Identifier, BossRecommendationDefinition> bossRecommendations
   ) {
      public LoadedContent {
         gear = Map.copyOf(gear == null ? Map.of() : gear);
         modules = Map.copyOf(modules == null ? Map.of() : modules);
         synergies = Map.copyOf(synergies == null ? Map.of() : synergies);
         loadouts = Map.copyOf(loadouts == null ? Map.of() : loadouts);
         factionUnlocks = Map.copyOf(factionUnlocks == null ? Map.of() : factionUnlocks);
         bossRecommendations = Map.copyOf(bossRecommendations == null ? Map.of() : bossRecommendations);
      }

      public static LoadedContent empty() {
         return new LoadedContent(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
      }
   }
}
