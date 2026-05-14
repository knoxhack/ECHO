package com.knoxhack.echoagriculturereclamation.content;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echocore.api.EchoCoreServices;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReclamationContent {
   private static final Map<String, ReclamationCropRule> DEFAULT_CROPS = defaultCrops();
   private static final EnumMap<SoilState, ReclamationSoilRule> DEFAULT_SOILS = defaultSoils();
   private static volatile Map<String, ReclamationCropRule> cropRules = DEFAULT_CROPS;
   private static volatile EnumMap<SoilState, ReclamationSoilRule> soilRules = DEFAULT_SOILS;
   private static volatile ReclamationMachineRules machineRules = ReclamationMachineRules.defaults();
   private static volatile ReclamationProgressionRules progressionRules = ReclamationProgressionRules.defaults();

   private ReclamationContent() {
   }

   public static ReclamationCropRule crop(CropSpec spec) {
      return cropRules.getOrDefault(spec.path(), defaultCropRule(spec));
   }

   public static ReclamationCropRule defaultCropRule(CropSpec spec) {
      return DEFAULT_CROPS.getOrDefault(spec.path(), ReclamationCropRule.defaultFor(spec));
   }

   public static ReclamationSoilRule soil(SoilState state) {
      return soilRules.getOrDefault(state, defaultSoilRule(state));
   }

   public static ReclamationSoilRule defaultSoilRule(SoilState state) {
      return DEFAULT_SOILS.getOrDefault(state, ReclamationSoilRule.defaultFor(state));
   }

   public static ReclamationMachineRules machines() {
      return machineRules;
   }

   public static ReclamationMachineRules defaultMachineRules() {
      return ReclamationMachineRules.defaults();
   }

   public static ReclamationProgressionRules progression() {
      return progressionRules;
   }

   public static ReclamationProgressionRules defaultProgressionRules() {
      return ReclamationProgressionRules.defaults();
   }

   public static void replaceJsonContent(LoadedContent loaded) {
      Map<String, ReclamationCropRule> nextCrops = new LinkedHashMap<>(DEFAULT_CROPS);
      loaded.cropRules().forEach((id, rule) -> {
         CropSpec spec = CropSpec.byPath(id);
         if (!spec.path().equals(id)) {
            EchoAgricultureReclamation.LOGGER.warn("Ignoring Agriculture crop rule for unknown crop id '{}'.", id);
         } else {
            nextCrops.put(id, rule.normalized(spec));
         }
      });

      EnumMap<SoilState, ReclamationSoilRule> nextSoils = new EnumMap<>(DEFAULT_SOILS);
      loaded.soilRules().forEach((state, rule) -> nextSoils.put(state, rule.normalized(state)));

      cropRules = Map.copyOf(nextCrops);
      soilRules = nextSoils;
      machineRules = loaded.machineRules().normalized();
      progressionRules = loaded.progressionRules().normalized();
      EchoAgricultureReclamation.LOGGER.info("Loaded Agriculture Reclamation data rules: {} crops, {} soils.", cropRules.size(), soilRules.size());
      EchoCoreServices.invalidateIndexRecipes("agriculture reclamation content changed");
   }

   private static Map<String, ReclamationCropRule> defaultCrops() {
      Map<String, ReclamationCropRule> defaults = new LinkedHashMap<>();
      CropSpec.ALL.forEach(spec -> defaults.put(spec.path(), ReclamationCropRule.defaultFor(spec)));
      return Map.copyOf(defaults);
   }

   private static EnumMap<SoilState, ReclamationSoilRule> defaultSoils() {
      EnumMap<SoilState, ReclamationSoilRule> defaults = new EnumMap<>(SoilState.class);
      for (SoilState state : SoilState.values()) {
         defaults.put(state, ReclamationSoilRule.defaultFor(state));
      }
      return defaults;
   }

   public record LoadedContent(
      Map<String, ReclamationCropRule> cropRules,
      EnumMap<SoilState, ReclamationSoilRule> soilRules,
      ReclamationMachineRules machineRules,
      ReclamationProgressionRules progressionRules
   ) {
   }
}
