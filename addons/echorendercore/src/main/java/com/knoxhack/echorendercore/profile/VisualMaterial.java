package com.knoxhack.echorendercore.profile;

import com.knoxhack.echorendercore.animation.AnimationBlendMode;

public record VisualMaterial(
   String id,
   int color,
   float alpha,
   boolean emissive,
   AnimationBlendMode blendMode
) {
   public static final VisualMaterial DEFAULT = new VisualMaterial("default", 0xFFFFFFFF, 1.0F, false, AnimationBlendMode.REPLACE);

   public VisualMaterial {
      id = id == null || id.isBlank() ? "default" : id.trim();
      alpha = Math.max(0.0F, Math.min(1.0F, alpha));
      blendMode = blendMode == null ? AnimationBlendMode.REPLACE : blendMode;
   }
}
