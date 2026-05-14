package com.knoxhack.echorendercore.client;

import com.knoxhack.echorendercore.api.VisualContext;
import com.knoxhack.echorendercore.profile.RenderCoreProfiles;
import com.knoxhack.echorendercore.profile.VisualEffectProfile;
import com.knoxhack.echorendercore.profile.VisualProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class RenderCoreScreenVisuals {
   private RenderCoreScreenVisuals() {
   }

   public static ScreenVisualData resolve(RenderCoreScreenVisualHost host) {
      if (host == null || host.screenVisualProfileId() == null) {
         return ScreenVisualData.EMPTY;
      }
      Identifier profileId = host.screenVisualProfileId();
      VisualProfile profile = RenderCoreProfiles.visual(profileId);
      float partialTick = 0.0F;
      Minecraft minecraft = Minecraft.getInstance();
      float age = minecraft.level == null ? 0.0F : minecraft.level.getGameTime() + partialTick;
      VisualContext context = new VisualContext(
         profileId,
         host.screenVisualState(),
         host.screenVisualVariant(),
         host.screenVisualProgress(),
         age,
         partialTick,
         false,
         false,
         0
      );
      VisualEffectProfile effect = profile == null ? VisualEffectProfile.NONE : profile.effect();
      int accent = RenderCoreEffectPipeline.color(0xFF66E8FF, effect);
      int panel = RenderCoreEffectPipeline.color(0xEE061018, effect);
      int border = RenderCoreEffectPipeline.color(0xAA38DFF4, effect);
      ThemeCoreColors themeCore = ThemeCoreColors.resolve();
      if (themeCore != null) {
         accent = RenderCoreEffectPipeline.color(themeCore.accent(), effect);
         panel = RenderCoreEffectPipeline.color(themeCore.panel(), effect);
         border = RenderCoreEffectPipeline.color(themeCore.border(), effect);
      }
      return new ScreenVisualData(profileId, profile, context, effect, accent, panel, border, host.screenSurfaceType());
   }

   public static void drawFrame(GuiGraphicsExtractor graphics, Font font, RenderCoreScreenVisualHost host,
         int x, int y, int width, int height, String label) {
      drawFrame(graphics, font, host, x, y, width, height, RenderCoreScreenFrameOptions.legacy(label));
   }

   public static void drawFrame(GuiGraphicsExtractor graphics, Font font, RenderCoreScreenVisualHost host,
         int x, int y, int width, int height, RenderCoreScreenFrameOptions options) {
      if (graphics == null || width <= 0 || height <= 0) {
         return;
      }
      RenderCoreScreenFrameOptions resolvedOptions = options == null ? RenderCoreScreenFrameOptions.legacy("") : options;
      ScreenVisualData data = resolve(host);
      if (data == ScreenVisualData.EMPTY && !resolvedOptions.quietFallback()) {
         return;
      }
      int accent = data == ScreenVisualData.EMPTY ? 0xFF38E8FF : data.accentColor();
      int border = data == ScreenVisualData.EMPTY ? 0x9938E8FF : data.borderColor();
      graphics.outline(x, y, width, height, border);
      if (resolvedOptions.accentBars()) {
         graphics.fill(x, y, x + Math.max(32, width / 5), y + 2, accent);
         graphics.fill(x, y + height - 2, x + Math.max(24, width / 7), y + height, accent);
      }
      if (resolvedOptions.drawScanlines() && data != ScreenVisualData.EMPTY
            && (data.effect().scanlineStrength() > 0.0F || data.effect().kind().name().equals("TERMINAL_HUD"))) {
         int step = 7;
         float minStrength = resolvedOptions.scanlinesBehindContent() ? 0.035F : 0.08F;
         float maxStrength = resolvedOptions.scanlinesBehindContent() ? 0.16F : 0.28F;
         int scanColor = (Math.round(Math.min(maxStrength, Math.max(minStrength, data.effect().scanlineStrength())) * 255.0F) << 24)
            | (accent & 0x00FFFFFF);
         for (int line = y + 5; line < y + height - 3; line += step) {
            graphics.fill(x + 2, line, x + width - 2, line + 1, scanColor);
         }
      }
      String label = resolvedOptions.label();
      if (resolvedOptions.drawLabel() && font != null && !label.isBlank() && width >= 80) {
         graphics.text(font, font.plainSubstrByWidth(label, width - 12), x + 6, y + 5, accent, false);
      }
   }

   public record ScreenVisualData(
      Identifier profileId,
      VisualProfile profile,
      VisualContext context,
      VisualEffectProfile effect,
      int accentColor,
      int panelColor,
      int borderColor,
      String surfaceType
   ) {
      private static final ScreenVisualData EMPTY = new ScreenVisualData(
         null,
         null,
         null,
         VisualEffectProfile.NONE,
         0xFF66E8FF,
         0xEE061018,
         0xAA38DFF4,
         "screen"
      );
   }

   private record ThemeCoreColors(int accent, int panel, int border) {
      private static ThemeCoreColors resolve() {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player == null) {
            return null;
         }
         try {
            Class<?> api = Class.forName("com.knoxhack.echothemecore.api.EchoThemeApi");
            Object colors = api.getMethod("getColors", net.minecraft.world.entity.player.Player.class).invoke(null, minecraft.player);
            int accent = ((Integer) colors.getClass().getMethod("primary").invoke(colors)).intValue();
            int panel = ((Integer) colors.getClass().getMethod("panel").invoke(colors)).intValue();
            int border = ((Integer) colors.getClass().getMethod("border").invoke(colors)).intValue();
            return new ThemeCoreColors(accent, panel, border);
         } catch (ReflectiveOperationException | LinkageError exception) {
            return null;
         }
      }
   }
}
