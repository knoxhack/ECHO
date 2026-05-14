package com.knoxhack.echoterminal.integration;

import com.knoxhack.echorendercore.api.RenderCoreBlockVisualHost;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.client.RenderCoreBlockEntityRenderer;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisualHost;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisuals;
import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreens;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.registry.ModBlockEntities;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class TerminalRenderCoreClientIntegration {
   private static final Identifier BLOCK_PROFILE = Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "echo_terminal");
   private static final Identifier SCREEN_PROFILE = Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "screen/terminal_hud");
   private static boolean screenRegistered;

   private TerminalRenderCoreClientIntegration() {
   }

   public static void registerBlockRenderer(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.ECHO_TERMINAL.get(),
         context -> new RenderCoreBlockEntityRenderer<>(context, TerminalRenderCoreClientIntegration::host));
   }

   public static void registerScreenVisuals() {
      if (screenRegistered) {
         return;
      }
      screenRegistered = true;
      NeoForge.EVENT_BUS.addListener(TerminalRenderCoreClientIntegration::renderScreenFrame);
   }

   private static RenderCoreBlockVisualHost host(EchoTerminalBlockEntity terminal, float partialTick) {
      return new RenderCoreBlockVisualHost() {
         @Override
         public Identifier visualProfileId() {
            return BLOCK_PROFILE;
         }

         @Override
         public VisualState visualState() {
            return terminal.getStoredRewardCount() > 0 ? VisualState.ACTIVE : VisualState.ONLINE;
         }

         @Override
         public float visualProgress() {
            return Math.min(1.0F, terminal.getStoredRewardCount() / 64.0F);
         }
      };
   }

   private static void renderScreenFrame(ScreenEvent.Render.Post event) {
      if (!EchoTerminalScreens.isManagedTerminalScreen(event.getScreen()) || !TerminalClientOptions.useVisualAssets()) {
         return;
      }
      RenderCoreScreenVisuals.ScreenVisualData data = RenderCoreScreenVisuals.resolve(() -> SCREEN_PROFILE);
      int alpha = TerminalClientOptions.reduceMotion() ? 0x44 : 0x66;
      int accent = withAlpha(data.accentColor(), alpha);
      int border = withAlpha(data.borderColor(), TerminalClientOptions.reduceMotion() ? 0x24 : 0x34);
      int margin = 8;
      int x = margin;
      int y = margin;
      int w = Math.max(1, event.getScreen().width - margin * 2);
      int h = Math.max(1, event.getScreen().height - margin * 2);
      drawCornerAccents(event.getGuiGraphics(), x, y, w, h, accent, border);
   }

   private static void drawCornerAccents(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
         int accent, int border) {
      int longTick = Math.max(28, Math.min(96, Math.min(w, h) / 6));
      int shortTick = Math.max(16, longTick / 2);
      graphics.fill(x, y, x + longTick, y + 1, accent);
      graphics.fill(x, y, x + 1, y + longTick, accent);
      graphics.fill(x + w - longTick, y, x + w, y + 1, accent);
      graphics.fill(x + w - 1, y, x + w, y + longTick, accent);
      graphics.fill(x, y + h - 1, x + longTick, y + h, accent);
      graphics.fill(x, y + h - longTick, x + 1, y + h, accent);
      graphics.fill(x + w - longTick, y + h - 1, x + w, y + h, accent);
      graphics.fill(x + w - 1, y + h - longTick, x + w, y + h, accent);
      graphics.fill(x + shortTick, y + 3, x + shortTick * 2, y + 4, border);
      graphics.fill(x + w - shortTick * 2, y + h - 4, x + w - shortTick, y + h - 3, border);
   }

   private static int withAlpha(int color, int alpha) {
      return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
   }

   public static Identifier screenProfileForTests() {
      return SCREEN_PROFILE;
   }

   public static boolean shouldRenderScreenAccentForTests() {
      return TerminalClientOptions.useVisualAssets();
   }
}
