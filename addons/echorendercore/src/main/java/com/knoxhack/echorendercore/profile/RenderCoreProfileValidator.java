package com.knoxhack.echorendercore.profile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class RenderCoreProfileValidator {
   private RenderCoreProfileValidator() {
   }

   public static ProfileDiagnosticsReport diagnostics(
         Map<Identifier, VisualProfile> visuals,
         Map<Identifier, AnimationProfile> animations,
         Map<Identifier, ParticleProfile> particles,
         int discoveredJsonCount,
         int loadedJsonCount,
         int failedJsonCount) {
      ProfilePerformanceReport performanceReport = analyzePerformance(visuals, animations, particles);
      ProfileValidationReport validationReport = validate(visuals, animations, particles).merge(performanceReport.asValidationReport());
      ProfileCacheMetrics metrics = ProfileCacheMetrics.from(
         visuals,
         animations,
         particles,
         validationReport,
         performanceReport,
         discoveredJsonCount,
         loadedJsonCount,
         failedJsonCount
      );
      return new ProfileDiagnosticsReport(validationReport, performanceReport, metrics);
   }

   public static ProfileValidationReport validate(
         Map<Identifier, VisualProfile> visuals,
         Map<Identifier, AnimationProfile> animations,
         Map<Identifier, ParticleProfile> particles) {
      ArrayList<ProfileValidationIssue> issues = new ArrayList<>();
      for (VisualProfile visual : visuals.values()) {
         validateVisual(visual, animations, particles, issues);
      }
      return new ProfileValidationReport(issues);
   }

   public static ProfilePerformanceReport analyzePerformance(
         Map<Identifier, VisualProfile> visuals,
         Map<Identifier, AnimationProfile> animations,
         Map<Identifier, ParticleProfile> particles) {
      Map<String, ProfilePerformanceSummary> summaries = new LinkedHashMap<>();
      ArrayList<ProfilePerformanceIssue> issues = new ArrayList<>();
      for (VisualProfile visual : visuals.values()) {
         AnimationProfile animationProfile = visual.animationProfile() == null ? null : animations.get(visual.animationProfile());
         ParticleProfile particleProfile = visual.particleProfile() == null ? null : particles.get(visual.particleProfile());
         int animationClipCount = animationProfile == null ? 0 : animationProfile.animations().size();
         int trackCount = animationProfile == null ? 0 : animationProfile.animations().values().stream()
            .mapToInt(clip -> clip.tracks().size())
            .sum();
         int emitterCount = particleProfile == null ? 0 : particleProfile.emitters().size();
         int maxBurst = particleProfile == null ? 0 : particleProfile.emitters().values().stream()
            .mapToInt(ParticleEmitter::burstCount)
            .max()
            .orElse(0);
         float emitterRate = particleProfile == null ? 0.0F : (float)particleProfile.emitters().values().stream()
            .mapToDouble(emitter -> emitter.rate() * Math.max(1, emitter.burstCount()))
            .sum();
         int maskedLayerCount = (int)visual.layers().stream()
            .filter(layer -> !layer.partFilter().isEmpty())
            .count();
         summaries.put(visual.id().toString(), new ProfilePerformanceSummary(
            visual.id(),
            visual.layers().size(),
            maskedLayerCount,
            animationClipCount,
            trackCount,
            emitterCount,
            maxBurst,
            emitterRate
         ));
         addPerformanceWarnings(issues, visual.id(), visual.layers().size(), emitterRate, trackCount);
      }
      return new ProfilePerformanceReport(summaries, issues);
   }

   private static void validateVisual(VisualProfile visual, Map<Identifier, AnimationProfile> animations,
         Map<Identifier, ParticleProfile> particles, ArrayList<ProfileValidationIssue> issues) {
      if (visual.baseTexture() == null) {
         warn(issues, visual.id(), "missing_base_texture", "base_texture", "Visual profile has no base texture; callers must provide a fallback texture.",
            "Add base_texture or make sure every caller supplies a fallback texture.");
      }
      if (visual.animationProfile() != null && !animations.containsKey(visual.animationProfile())) {
         warn(issues, visual.id(), "missing_profile_reference", "animation_profile", "Referenced animation profile " + visual.animationProfile() + " was not loaded.",
            "Create the animation profile JSON or update animation_profile.");
      }
      if (visual.particleProfile() != null && !particles.containsKey(visual.particleProfile())) {
         warn(issues, visual.id(), "missing_profile_reference", "particle_profile", "Referenced particle profile " + visual.particleProfile() + " was not loaded.",
            "Create the particle profile JSON or update particle_profile.");
      }
      AnimationProfile animationProfile = visual.animationProfile() == null ? null : animations.get(visual.animationProfile());
      if (animationProfile != null) {
         for (Map.Entry<com.knoxhack.echorendercore.api.VisualState, String> entry : visual.stateAnimations().entrySet()) {
            if (animationProfile.clip(entry.getValue()) == null) {
               warn(issues, visual.id(), "missing_animation_clip", "state_animations." + entry.getKey().name(), "Missing animation clip '" + entry.getValue() + "'.",
                  "Add the clip to the referenced animation profile or rename this state animation.");
            }
         }
      }
      ParticleProfile particleProfile = visual.particleProfile() == null ? null : particles.get(visual.particleProfile());
      if (particleProfile != null) {
         for (ParticleEmitter emitter : particleProfile.emitters().values()) {
            if (!emitter.anchor().isBlank() && !visual.anchors().containsKey(emitter.anchor())) {
               warn(issues, visual.id(), "missing_anchor", "particles." + emitter.id(), "Emitter anchor '" + emitter.anchor() + "' is not declared by the visual profile.",
                  "Declare the anchor in the visual profile anchors object.");
            }
            if (emitter.options().lifetime() > 0) {
               warn(issues, visual.id(), "unsupported_particle_option", "particles." + emitter.id() + ".options.lifetime",
                  "Particle lifetime is parsed for validation but vanilla particle spawning does not apply it directly.",
                  "Use a particle type that encodes lifetime itself or register a custom option resolver.");
            }
            Set<String> supportedOptions = supportedParticleOptions(emitter.options());
            for (String option : emitter.options().custom().keySet()) {
               if (supportedOptions.contains(option)) {
                  continue;
               }
               warn(issues, visual.id(), "unsupported_particle_option", "particles." + emitter.id() + ".options." + option,
                  "Particle option '" + option + "' is parsed for forward compatibility but has no built-in V4 resolver.",
                  "Register a client particle option resolver for options.type or remove the unsupported option.");
            }
         }
      }
      validateBlockPartSelectors(visual, -1).issues().forEach(issues::add);
   }

   public static ProfileValidationReport validateLayerParts(VisualProfile visual, Set<String> knownParts) {
      ArrayList<ProfileValidationIssue> issues = new ArrayList<>();
      if (visual == null || knownParts == null) {
         return new ProfileValidationReport(issues);
      }
      for (VisualLayerProfile layer : visual.layers()) {
         for (String part : layer.partFilter()) {
            if (!knownParts.contains(part)) {
               warn(issues, visual.id(), "masked_part_missing", "layers." + layer.id() + ".parts",
                  "Layer '" + layer.id() + "' references missing named part '" + part + "'.",
                  "Add the model part alias or remove the part from the layer mask.");
            }
         }
      }
      return new ProfileValidationReport(issues);
   }

   public static ProfileValidationReport validateBlockPartSelectors(VisualProfile visual, int collectedPartCount) {
      return validateBlockPartSelectors(visual, collectedPartCount, null, null);
   }

   public static ProfileValidationReport validateBlockPartSelectors(
         VisualProfile visual,
         int collectedPartCount,
         BlockState blockState,
         Set<Integer> availableTintIndices) {
      ArrayList<ProfileValidationIssue> issues = new ArrayList<>();
      if (visual == null) {
         return new ProfileValidationReport(issues);
      }
      for (Map.Entry<String, BlockPartSelectorProfile> entry : visual.blockParts().entrySet()) {
         BlockPartSelectorProfile selector = entry.getValue();
         if (selector.isEmptySelector()) {
            warn(issues, visual.id(), "block_part_selector_empty", "block_parts." + entry.getKey(),
               "Block part alias '" + entry.getKey() + "' has no selector rules and will not match baked model parts.",
               "Add indices, directions, material_flags, ambient_occlusion, tint_indices, or block_state.");
         }
         if (collectedPartCount >= 0) {
            for (int index : selector.indices()) {
               if (index < 0 || index >= collectedPartCount) {
                  warn(issues, visual.id(), "block_part_index_out_of_range", "block_parts." + entry.getKey() + ".indices",
                     "Block part alias '" + entry.getKey() + "' references collected part index " + index + " but only " + collectedPartCount + " part(s) exist.",
                  "Update the selector index or switch to direction/material selectors.");
               }
            }
         }
         if (availableTintIndices != null) {
            for (int tintIndex : selector.tintIndices()) {
               if (!availableTintIndices.contains(tintIndex)) {
                  warn(issues, visual.id(), "block_part_tint_index_missing", "block_parts." + entry.getKey() + ".tint_indices",
                     "Block part alias '" + entry.getKey() + "' requires tint index " + tintIndex + " but collected parts expose "
                        + availableTintIndices + ".",
                     "Update tint_indices or remove the tint rule from this selector.");
               }
            }
         }
         if (blockState != null && !selector.blockState().isEmpty()) {
            validateBlockStateRules(visual, entry.getKey(), selector, blockState, issues);
         }
      }
      return new ProfileValidationReport(issues);
   }

   private static void validateBlockStateRules(VisualProfile visual, String alias, BlockPartSelectorProfile selector,
         BlockState blockState, ArrayList<ProfileValidationIssue> issues) {
      for (Map.Entry<String, Set<String>> rule : selector.blockState().entrySet()) {
         Property<?> property = findProperty(blockState, rule.getKey());
         if (property == null) {
            warn(issues, visual.id(), "block_state_property_missing", "block_parts." + alias + ".block_state." + rule.getKey(),
               "Block part alias '" + alias + "' requires block state property '" + rule.getKey() + "', but " + blockState + " does not expose it.",
               "Use a property that exists on the target block state or remove this block_state selector.");
            continue;
         }
         String value = serializedPropertyValue(blockState, property);
         if (!rule.getValue().isEmpty() && !rule.getValue().contains(value)) {
            warn(issues, visual.id(), "block_state_property_value_missing", "block_parts." + alias + ".block_state." + rule.getKey(),
               "Block part alias '" + alias + "' requires block state property '" + rule.getKey() + "' value " + rule.getValue()
                  + ", but the resolved value is '" + value + "'.",
               "Add the current serialized value to the selector list or use a less specific alias.");
         }
      }
   }

   private static Property<?> findProperty(BlockState blockState, String name) {
      for (Property<?> property : blockState.getProperties()) {
         if (property.getName().equals(name)) {
            return property;
         }
      }
      return null;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private static String serializedPropertyValue(BlockState blockState, Property property) {
      return property.getName((Comparable)blockState.getValue(property));
   }

   private static Set<String> supportedParticleOptions(ParticleOptionsSpec options) {
      String type = options.type().toLowerCase(java.util.Locale.ROOT);
      return switch (type) {
         case "dust", "minecraft:dust" -> Set.of();
         case "dust_transition", "minecraft:dust_color_transition" -> Set.of("from_color", "fromColor", "to_color", "toColor");
         case "color", "entity_effect", "minecraft:entity_effect" -> Set.of();
         case "item", "minecraft:item" -> Set.of("item");
         case "block", "minecraft:block" -> Set.of("block", "block_state", "blockState");
         case "trail", "minecraft:trail" -> Set.of("target", "duration");
         default -> Set.of();
      };
   }

   private static void addPerformanceWarnings(ArrayList<ProfilePerformanceIssue> issues, Identifier id, int layerCount, float emitterRate, int trackCount) {
      if (layerCount > 12) {
         issues.add(new ProfilePerformanceIssue(id, "profile_perf_high_layer_count", ProfileValidationSeverity.WARNING,
            "Visual profile has " + layerCount + " layer(s), which can become expensive when many instances render.", layerCount, 12));
      }
      if (emitterRate > 8.0F) {
         issues.add(new ProfilePerformanceIssue(id, "profile_perf_high_emitter_rate", ProfileValidationSeverity.WARNING,
            "Particle profile estimates " + emitterRate + " particle(s) per tick before runtime gates.", Math.round(emitterRate), 8));
      }
      if (trackCount > 96) {
         issues.add(new ProfilePerformanceIssue(id, "profile_perf_high_animation_track_count", ProfileValidationSeverity.WARNING,
            "Animation profile contains " + trackCount + " track(s).", trackCount, 96));
      }
   }

   private static void warn(ArrayList<ProfileValidationIssue> issues, Identifier id, String code, String path, String message, String suggestion) {
      issues.add(new ProfileValidationIssue(ProfileValidationSeverity.WARNING, id, code, path, message, suggestion));
   }
}
