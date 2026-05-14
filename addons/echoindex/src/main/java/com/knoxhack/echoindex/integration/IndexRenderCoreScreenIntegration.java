package com.knoxhack.echoindex.integration;

import com.knoxhack.echorendercore.client.RenderCoreScreenFrameOptions;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisuals;
import com.knoxhack.echoindex.EchoIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class IndexRenderCoreScreenIntegration {
   private static final Identifier OVERLAY_PROFILE = Identifier.fromNamespaceAndPath(EchoIndex.MODID, "screen/index_overlay");
   private static final RenderCoreScreenFrameOptions OVERLAY_FRAME_OPTIONS =
      new RenderCoreScreenFrameOptions("", false, false, true, true, true);

   private IndexRenderCoreScreenIntegration() {
   }

   public static void drawOverlayFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
      RenderCoreScreenVisuals.drawFrame(graphics, Minecraft.getInstance().font, () -> OVERLAY_PROFILE,
         x, y, width, height, OVERLAY_FRAME_OPTIONS);
   }
}
