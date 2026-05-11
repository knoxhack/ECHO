package com.knoxhack.echorendercore.api;

import net.minecraft.resources.Identifier;

public interface IAdvancedVisualBlockEntity extends IVisualStateProvider {
   Identifier visualProfileId();

   default VisualVariant visualVariant() {
      return VisualVariant.DEFAULT;
   }
}
