package com.knoxhack.echoterminal.integration;

import com.knoxhack.echorendercore.api.RenderCoreBlockVisualHost;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.client.RenderCoreBlockEntityRenderer;
import com.knoxhack.echorendercore.client.RenderCoreScreenFrameOptions;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisuals;
import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.block.entity.EchoTerminalBlockEntity;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreens;
import com.knoxhack.echoterminal.client.screen.TerminalClientOptions;
import com.knoxhack.echoterminal.registry.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class TerminalRenderCoreClientIntegration {
   private static final Identifier BLOCK_PROFILE = Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "echo_terminal");
   private static final Identifier SCREEN_PROFILE = Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "screen/terminal_hud");
   private static final RenderCoreScreenFrameOptions SCREEN_FRAME_OPTIONS =
      RenderCoreScreenFrameOptions.terminal("ECHO TERMINAL").build();
   private static final RenderCoreScreenFrameOptions REDUCED_SCREEN_FRAME_OPTIONS =
      RenderCoreScreenFrameOptions.terminal("ECHO TERMINAL")
         .scanlines(false)
         .edgeGlow(false)
         .glassGlints(false)
         .chromaticEdge(false)
         .build();
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
      int margin = 8;
      int x = margin;
      int y = margin;
      int w = Math.max(1, event.getScreen().width - margin * 2);
      int h = Math.max(1, event.getScreen().height - margin * 2);
      RenderCoreScreenVisuals.drawFrame(
         event.getGuiGraphics(),
         Minecraft.getInstance().font,
         () -> SCREEN_PROFILE,
         x,
         y,
         w,
         h,
         screenFrameOptions()
      );
   }

   private static RenderCoreScreenFrameOptions screenFrameOptions() {
      return TerminalClientOptions.reduceMotion() ? REDUCED_SCREEN_FRAME_OPTIONS : SCREEN_FRAME_OPTIONS;
   }

   public static Identifier screenProfileForTests() {
      return SCREEN_PROFILE;
   }

   public static boolean shouldRenderScreenAccentForTests() {
      return TerminalClientOptions.useVisualAssets();
   }

   public static RenderCoreScreenFrameOptions screenFrameOptionsForTests(boolean reducedMotion) {
      return reducedMotion ? REDUCED_SCREEN_FRAME_OPTIONS : SCREEN_FRAME_OPTIONS;
   }
}
