package com.knoxhack.echorendercore.profile;

import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.api.VisualVariant;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;

public record VisualLayerProfile(
   String id,
   VisualLayerKind kind,
   Identifier texture,
   String material,
   Set<VisualState> states,
   Set<VisualVariant> variants,
   List<String> partFilter,
   int color,
   float alpha,
   boolean emissive
) {
   public VisualLayerProfile {
      id = id == null || id.isBlank() ? "layer" : id.trim();
      kind = kind == null ? VisualLayerKind.OVERLAY : kind;
      material = material == null || material.isBlank() ? "default" : material.trim();
      states = states == null ? Set.of() : Set.copyOf(states);
      variants = variants == null ? Set.of() : Set.copyOf(variants);
      partFilter = partFilter == null ? List.of() : partFilter.stream()
         .filter(value -> value != null && !value.isBlank())
         .map(String::trim)
         .toList();
      alpha = Math.max(0.0F, Math.min(1.0F, alpha));
   }

   public boolean matches(VisualState state, VisualVariant variant) {
      boolean stateMatches = states.isEmpty() || states.contains(state);
      boolean variantMatches = variants.isEmpty() || variants.contains(variant == null ? VisualVariant.DEFAULT : variant);
      return stateMatches && variantMatches;
   }

   public int colorWithAlpha() {
      int base = color == 0 ? 0xFFFFFFFF : color;
      int scaledAlpha = Math.round(((base >>> 24) & 0xFF) * alpha);
      return (scaledAlpha << 24) | (base & 0x00FFFFFF);
   }
}
