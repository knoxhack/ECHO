package com.knoxhack.echolens.integration;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echorendercore.client.RenderCoreScreenFrameOptions;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class LensRenderCoreScreenIntegration {
   private static final Identifier LENS_PROFILE = Identifier.fromNamespaceAndPath(EchoLens.MODID, "screen/lens_overlay");
   private static final RenderCoreScreenFrameOptions LENS_FRAME_OPTIONS =
      new RenderCoreScreenFrameOptions("LENS", true, false, false, true, false);

   private LensRenderCoreScreenIntegration() {
   }

   public static void drawLensFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      RenderCoreScreenVisuals.drawFrame(graphics, Minecraft.getInstance().font, () -> LENS_PROFILE,
         x, y, width, height, LENS_FRAME_OPTIONS);
   }
}
