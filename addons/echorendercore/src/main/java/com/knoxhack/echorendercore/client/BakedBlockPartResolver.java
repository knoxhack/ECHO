package com.knoxhack.echorendercore.client;

import com.knoxhack.echorendercore.profile.BlockPartSelectorProfile;
import com.knoxhack.echorendercore.profile.RenderCoreProfileValidator;
import com.knoxhack.echorendercore.profile.VisualProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BakedBlockPartResolver {
   private BakedBlockPartResolver() {
   }

   public static List<BlockStateModelPart> collect(BlockState blockState) {
      if (blockState == null) {
         return List.of();
      }
      BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
      ArrayList<BlockStateModelPart> parts = new ArrayList<>();
      model.collectParts(RandomSource.create(42L), parts);
      return parts;
   }

   public static Map<String, List<BlockStateModelPart>> resolve(BlockState blockState, VisualProfile profile) {
      List<BlockStateModelPart> collected = collect(blockState);
      if (profile != null && !profile.blockParts().isEmpty()) {
         var report = RenderCoreProfileValidator.validateBlockPartSelectors(profile, collected.size(), blockState, availableTintIndices(collected));
         report.issues().forEach(issue -> {
            if (DebugVisualOverrides.missingPartWarnings()) {
               RenderCoreWarnings.warn(issue.message());
            }
         });
      }
      return resolve(collected, blockState, profile);
   }

   public static Map<String, List<BlockStateModelPart>> resolve(List<BlockStateModelPart> collected, VisualProfile profile) {
      return resolve(collected, null, profile);
   }

   public static Map<String, List<BlockStateModelPart>> resolve(List<BlockStateModelPart> collected, BlockState blockState, VisualProfile profile) {
      if (profile == null || profile.blockParts().isEmpty()) {
         return Map.of();
      }
      Map<String, List<BlockStateModelPart>> aliases = new LinkedHashMap<>();
      for (Map.Entry<String, BlockPartSelectorProfile> entry : profile.blockParts().entrySet()) {
         aliases.put(entry.getKey(), select(collected, blockState, entry.getValue()));
      }
      return aliases;
   }

   public static List<BlockStateModelPart> select(List<BlockStateModelPart> collected, BlockPartSelectorProfile selector) {
      return select(collected, null, selector);
   }

   public static List<BlockStateModelPart> select(List<BlockStateModelPart> collected, BlockState blockState, BlockPartSelectorProfile selector) {
      if (collected == null || collected.isEmpty() || selector == null || selector.isEmptySelector()) {
         return List.of();
      }
      ArrayList<BlockStateModelPart> selected = new ArrayList<>();
      if (!selector.indices().isEmpty()) {
         for (int index : selector.indices()) {
            if (index >= 0 && index < collected.size()) {
               BlockStateModelPart part = collected.get(index);
               if (matchesRules(part, blockState, selector)) {
                  selected.add(part);
               }
            }
         }
         return selected;
      }
      for (BlockStateModelPart part : collected) {
         if (matchesRules(part, blockState, selector)) {
            selected.add(part);
         }
      }
      return selected;
   }

   public static List<Integer> matchedIndices(List<BlockStateModelPart> collected, List<BlockStateModelPart> selected) {
      if (collected == null || collected.isEmpty() || selected == null || selected.isEmpty()) {
         return List.of();
      }
      ArrayList<Integer> indices = new ArrayList<>();
      for (BlockStateModelPart part : selected) {
         int index = collected.indexOf(part);
         if (index >= 0) {
            indices.add(index);
         }
      }
      return indices;
   }

   public static Set<Integer> availableTintIndices(List<BlockStateModelPart> collected) {
      if (collected == null || collected.isEmpty()) {
         return Set.of();
      }
      HashSet<Integer> tintIndices = new HashSet<>();
      for (BlockStateModelPart part : collected) {
         addTintIndices(part, tintIndices);
      }
      return Set.copyOf(tintIndices);
   }

   private static boolean matchesRules(BlockStateModelPart part, BlockState blockState, BlockPartSelectorProfile selector) {
      if (!matchesBlockState(blockState, selector)) {
         return false;
      }
      if (selector.ambientOcclusion() != null && part.useAmbientOcclusion() != selector.ambientOcclusion()) {
         return false;
      }
      if (selector.materialFlags() != 0 && (part.materialFlags() & selector.materialFlags()) != selector.materialFlags()) {
         return false;
      }
      if (!selector.directions().isEmpty()) {
         boolean anyDirection = false;
         for (Direction direction : selector.directions()) {
            if (!part.getQuads(direction).isEmpty()) {
               anyDirection = true;
               break;
            }
         }
         if (!anyDirection) {
            return false;
         }
      }
      if (!selector.tintIndices().isEmpty() && !matchesTintIndex(part, selector.tintIndices())) {
         return false;
      }
      return true;
   }

   private static boolean matchesBlockState(BlockState blockState, BlockPartSelectorProfile selector) {
      if (selector.blockState().isEmpty()) {
         return true;
      }
      if (blockState == null) {
         return false;
      }
      for (Map.Entry<String, Set<String>> rule : selector.blockState().entrySet()) {
         Property<?> property = findProperty(blockState, rule.getKey());
         if (property == null) {
            return false;
         }
         String value = serializedPropertyValue(blockState, property);
         if (!rule.getValue().isEmpty() && !rule.getValue().contains(value)) {
            return false;
         }
      }
      return true;
   }

   private static boolean matchesTintIndex(BlockStateModelPart part, List<Integer> tintIndices) {
      for (BakedQuad quad : allQuads(part)) {
         BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
         if (materialInfo.isTinted() && tintIndices.contains(materialInfo.tintIndex())) {
            return true;
         }
      }
      return false;
   }

   private static void addTintIndices(BlockStateModelPart part, Set<Integer> tintIndices) {
      for (BakedQuad quad : allQuads(part)) {
         BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
         if (materialInfo.isTinted()) {
            tintIndices.add(materialInfo.tintIndex());
         }
      }
   }

   private static List<BakedQuad> allQuads(BlockStateModelPart part) {
      ArrayList<BakedQuad> quads = new ArrayList<>();
      for (Direction direction : Direction.values()) {
         quads.addAll(part.getQuads(direction));
      }
      quads.addAll(part.getQuads(null));
      return quads;
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
}
