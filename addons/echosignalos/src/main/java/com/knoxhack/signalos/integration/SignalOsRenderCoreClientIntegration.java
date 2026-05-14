package com.knoxhack.signalos.integration;

import com.knoxhack.echorendercore.api.RenderCoreBlockVisualHost;
import com.knoxhack.echorendercore.api.VisualState;
import com.knoxhack.echorendercore.client.RenderCoreBlockEntityRenderer;
import com.knoxhack.echorendercore.client.RenderCoreScreenVisuals;
import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.block.entity.SignalOsServerRackBlockEntity;
import com.knoxhack.signalos.block.entity.SignalOsTerminalBlockEntity;
import com.knoxhack.signalos.client.SignalOsServerRackScreen;
import com.knoxhack.signalos.client.SignalOsTerminalScreen;
import com.knoxhack.signalos.registry.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SignalOsRenderCoreClientIntegration {
   private static final Identifier TERMINAL_PROFILE = Identifier.fromNamespaceAndPath(SignalOS.MODID, "terminal");
   private static final Identifier RACK_PROFILE = Identifier.fromNamespaceAndPath(SignalOS.MODID, "server_rack");
   private static final Identifier TERMINAL_SCREEN_PROFILE = Identifier.fromNamespaceAndPath(SignalOS.MODID, "screen/terminal_hud");
   private static final Identifier RACK_SCREEN_PROFILE = Identifier.fromNamespaceAndPath(SignalOS.MODID, "screen/server_rack");
   private static boolean screenRegistered;

   private SignalOsRenderCoreClientIntegration() {
   }

   public static void registerBlockRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlockEntities.TERMINAL.get(),
         context -> new RenderCoreBlockEntityRenderer<>(context, SignalOsRenderCoreClientIntegration::terminalHost));
      event.registerBlockEntityRenderer(ModBlockEntities.SERVER_RACK.get(),
         context -> new RenderCoreBlockEntityRenderer<>(context, SignalOsRenderCoreClientIntegration::rackHost));
   }

   public static void registerScreenVisuals() {
      if (screenRegistered) {
         return;
      }
      screenRegistered = true;
      NeoForge.EVENT_BUS.addListener(SignalOsRenderCoreClientIntegration::renderScreenFrame);
   }

   private static RenderCoreBlockVisualHost terminalHost(SignalOsTerminalBlockEntity terminal, float partialTick) {
      return new RenderCoreBlockVisualHost() {
         @Override
         public Identifier visualProfileId() {
            return TERMINAL_PROFILE;
         }

         @Override
         public VisualState visualState() {
            return terminal.hasStoredRewards() ? VisualState.ACTIVE : VisualState.ONLINE;
         }

         @Override
         public float visualProgress() {
            return Math.min(1.0F, terminal.storedRewardCount() / 64.0F);
         }
      };
   }

   private static RenderCoreBlockVisualHost rackHost(SignalOsServerRackBlockEntity rack, float partialTick) {
      return new RenderCoreBlockVisualHost() {
         @Override
         public Identifier visualProfileId() {
            return RACK_PROFILE;
         }

         @Override
         public VisualState visualState() {
            return rack.driveCount() > 0 ? VisualState.ACTIVE : VisualState.IDLE;
         }

         @Override
         public float visualProgress() {
            return Math.min(1.0F, rack.driveCount() / (float)SignalOsServerRackBlockEntity.DRIVE_SLOTS);
         }
      };
   }

   private static void renderScreenFrame(ScreenEvent.Render.Post event) {
      Identifier profile = null;
      String label = "";
      if (event.getScreen() instanceof SignalOsTerminalScreen) {
         profile = TERMINAL_SCREEN_PROFILE;
         label = "SIGNALOS TERMINAL";
      } else if (event.getScreen() instanceof SignalOsServerRackScreen) {
         profile = RACK_SCREEN_PROFILE;
         label = "SIGNALOS RACK";
      }
      if (profile == null) {
         return;
      }
      Identifier finalProfile = profile;
      RenderCoreScreenVisuals.drawFrame(
         event.getGuiGraphics(),
         Minecraft.getInstance().font,
         () -> finalProfile,
         6,
         6,
         Math.max(1, event.getScreen().width - 12),
         Math.max(1, event.getScreen().height - 12),
         label
      );
   }
}
