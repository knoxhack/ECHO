package com.knoxhack.echothemecore.client.vanilla;

import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeColors;
import com.knoxhack.echothemecore.api.EchoThemeTextureKey;
import com.knoxhack.echothemecore.api.EchoThemeVanillaUiProfile;
import com.knoxhack.echothemecore.client.ClientThemeCache;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class VanillaUiSkinLayer {
    private static VanillaUiSurface lastSurface = VanillaUiSurface.UNKNOWN;
    private static String lastScreenClass = "";

    private VanillaUiSkinLayer() {
    }

    public static VanillaUiSurface currentSurface() {
        return lastSurface;
    }

    public static String currentScreenClass() {
        return lastScreenClass;
    }

    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        lastSurface = VanillaUiScreenClassifier.classify(screen);
        lastScreenClass = screen == null ? "" : screen.getClass().getName();
        if (!VanillaUiScreenClassifier.enabled(lastSurface)) {
            return;
        }
        EchoTheme theme = ClientThemeCache.currentTheme();
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        if (isContainerSurface(lastSurface) && screen instanceof AbstractContainerScreen<?> container) {
            renderContainerAccents(graphics, container, theme);
        } else {
            renderScreenAccents(graphics, screen, lastSurface, theme);
        }
        if (ThemeCoreConfig.showDebugScreenNames()) {
            drawDebugName(graphics, screen, lastSurface, theme);
        }
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!ThemeCoreConfig.vanillaUiEnabled() || Minecraft.getInstance().player == null) {
            return;
        }
        EchoTheme theme = ClientThemeCache.currentTheme();
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        if (ThemeCoreConfig.bool(ThemeCoreConfig.THEME_HOTBAR)) {
            renderHotbarAccent(graphics, width, height, theme);
        }
        if (ThemeCoreConfig.bool(ThemeCoreConfig.THEME_BOSS_BAR)) {
            renderBossBarAccent(graphics, width, theme);
        }
        if (ThemeCoreConfig.bool(ThemeCoreConfig.THEME_CHAT)) {
            renderChatAccent(graphics, height, theme);
        }
    }

    private static void renderScreenAccents(GuiGraphicsExtractor graphics, Screen screen, VanillaUiSurface surface, EchoTheme theme) {
        if (screen == null) {
            return;
        }
        EchoThemeColors colors = theme.colors();
        EchoThemeVanillaUiProfile vanilla = theme.vanillaUiProfile();
        int w = screen.width;
        int h = screen.height;
        int bg = VanillaUiColors.cappedAlpha(vanilla.backgroundTint(), surface == VanillaUiSurface.MAIN_MENU ? 72 : 54);
        int panel = VanillaUiColors.cappedAlpha(vanilla.panelTint(), ThemeCoreConfig.vanillaSafeMode() ? 86 : 122);
        int border = VanillaUiColors.cappedAlpha(colors.border(), ThemeCoreConfig.bool(ThemeCoreConfig.VANILLA_EDGE_GLOW) ? 185 : 105);

        if (ThemeCoreConfig.bool(ThemeCoreConfig.ENERGY_BACKGROUND) && surface != VanillaUiSurface.LOADING) {
            graphics.fill(0, 0, w, 18, bg);
            graphics.fill(0, h - 18, w, h, bg);
        }

        // Border glow (kept as color; thin lines don't benefit from texture blit)
        graphics.fill(0, 0, 2, h, border);
        graphics.fill(w - 2, 0, w, h, border);
        graphics.fill(0, 0, w, 1, border);
        graphics.fill(0, h - 1, w, h, border);

        if (ThemeCoreConfig.bool(ThemeCoreConfig.TRANSPARENT_PANELS) && surface != VanillaUiSurface.MAIN_MENU) {
            int inset = Math.max(12, Math.min(w, h) / 18);
            graphics.fill(inset, inset, w - inset, inset + 1, panel);
            graphics.fill(inset, h - inset - 1, w - inset, h - inset, panel);
        }

        // Texture-backed panel decoration for supported surfaces
        if (!ThemeCoreConfig.vanillaSafeMode()) {
            switch (surface) {
                case MAIN_MENU -> blitIfPresent(graphics, theme, EchoThemeTextureKey.VANILLA_TITLE_BACKPLATE, w / 2 - 200, h / 2 - 80, 400, 160);
                case PAUSE_MENU -> blitIfPresent(graphics, theme, EchoThemeTextureKey.VANILLA_PAUSE_PANEL, w / 2 - 150, h / 2 - 120, 300, 240);
                case OPTIONS_MENU, WORLD_SELECT, MULTIPLAYER -> blitIfPresent(graphics, theme, EchoThemeTextureKey.VANILLA_PANEL, w / 2 - 180, h / 2 - 140, 360, 280);
                case LOADING -> blitIfPresent(graphics, theme, EchoThemeTextureKey.VANILLA_BACKGROUND, 0, 0, w, h);
                default -> {
                }
            }
        }

        if (ClientThemeCache.transitioning()) {
            int alpha = Math.round(70.0F * (1.0F - ClientThemeCache.transitionProgress()));
            graphics.fill(0, 0, w, h, EchoThemeColors.withAlpha(colors.glow(), alpha));
        }
    }

    private static void renderContainerAccents(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> screen, EchoTheme theme) {
        EchoThemeColors colors = theme.colors();
        int x = screen.getLeftPos();
        int y = screen.getTopPos();
        int w = screen.getImageWidth();
        int h = screen.getImageHeight();
        int border = VanillaUiColors.cappedAlpha(colors.border(), 170);
        int soft = VanillaUiColors.cappedAlpha(colors.borderSoft(), 96);
        int glow = VanillaUiColors.cappedAlpha(colors.glow(), ThemeCoreConfig.vanillaSafeMode() ? 55 : 82);
        if (ThemeCoreConfig.bool(ThemeCoreConfig.GLASS_INVENTORY_PANELS)) {
            // Texture-backed container frame when available, safe around slots
            Optional<Identifier> frame = textureFor(theme, EchoThemeTextureKey.VANILLA_CONTAINER_FRAME);
            if (frame.isPresent() && !ThemeCoreConfig.vanillaSafeMode()) {
                blitStretched(graphics, frame.get(), x - 4, y - 4, w + 8, h + 8);
            }
            graphics.outline(x - 3, y - 3, w + 6, h + 6, soft);
            graphics.outline(x - 1, y - 1, w + 2, h + 2, border);
        }
        if (ThemeCoreConfig.bool(ThemeCoreConfig.VANILLA_EDGE_GLOW)) {
            graphics.fill(Math.max(0, x - 7), Math.max(0, y - 7), x - 3, y + h + 7, glow);
            graphics.fill(x + w + 3, Math.max(0, y - 7), Math.min(screen.width, x + w + 7), y + h + 7, glow);
        }
    }

    private static void renderHotbarAccent(GuiGraphicsExtractor graphics, int width, int height, EchoTheme theme) {
        int selected = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.getInventory().getSelectedSlot();
        int hotbarX = width / 2 - 91;
        int hotbarY = height - 22;
        int accent = VanillaUiColors.cappedAlpha(theme.vanillaUiProfile().hotbarAccent(), 165);
        int soft = VanillaUiColors.cappedAlpha(theme.colors().borderSoft(), 90);

        Optional<Identifier> hotbarTex = textureFor(theme, EchoThemeTextureKey.VANILLA_SELECTED_SLOT);
        if (hotbarTex.isPresent() && !ThemeCoreConfig.vanillaSafeMode()) {
            blitStretched(graphics, hotbarTex.get(), hotbarX, hotbarY + 18, 182, 4);
        } else {
            graphics.fill(hotbarX, hotbarY + 20, hotbarX + 182, hotbarY + 22, soft);
        }
        graphics.outline(hotbarX + selected * 20, hotbarY, 22, 22, accent);
    }

    private static void renderBossBarAccent(GuiGraphicsExtractor graphics, int width, EchoTheme theme) {
        int x = width / 2 - 92;
        int color = VanillaUiColors.cappedAlpha(theme.colors().glow(), 70);
        Optional<Identifier> bossBar = textureFor(theme, EchoThemeTextureKey.VANILLA_BOSS_BAR_ACCENT);
        if (bossBar.isPresent() && !ThemeCoreConfig.vanillaSafeMode()) {
            blitStretched(graphics, bossBar.get(), x, 10, 184, 8);
        } else {
            graphics.fill(x, 13, x + 184, 14, color);
        }
    }

    private static void renderChatAccent(GuiGraphicsExtractor graphics, int height, EchoTheme theme) {
        int color = VanillaUiColors.cappedAlpha(theme.vanillaUiProfile().chatAccent(), 60);
        graphics.fill(0, height - 58, 3, height - 18, color);
    }

    private static void drawDebugName(GuiGraphicsExtractor graphics, Screen screen, VanillaUiSurface surface, EchoTheme theme) {
        Font font = Minecraft.getInstance().font;
        int panel = VanillaUiColors.cappedAlpha(theme.colors().panel(), 180);
        int text = VanillaUiColors.readableText(theme.colors(), panel);
        String name = surface + "  " + (screen == null ? "<none>" : screen.getClass().getSimpleName());
        graphics.fill(4, 4, 8 + font.width(name) + 4, 18, panel);
        graphics.text(font, name, 8, 8, text, false);
    }

    private static boolean isContainerSurface(VanillaUiSurface surface) {
        return switch (surface) {
            case INVENTORY, CREATIVE_INVENTORY, CONTAINER, FURNACE, CRAFTING, ANVIL, ENCHANTING, GRINDSTONE, SMITHING -> true;
            default -> false;
        };
    }

    private static void blitIfPresent(GuiGraphicsExtractor graphics, EchoTheme theme, EchoThemeTextureKey key, int x, int y, int w, int h) {
        Optional<Identifier> tex = textureFor(theme, key);
        if (tex.isPresent()) {
            blitStretched(graphics, tex.get(), x, y, w, h);
        }
    }

    private static Optional<Identifier> textureFor(EchoTheme theme, EchoThemeTextureKey key) {
        Optional<Identifier> module = theme.moduleTexture(key);
        if (module.isPresent()) {
            return module;
        }
        Optional<Identifier> vanilla = theme.vanillaUiProfile().texture(key);
        if (vanilla.isPresent()) {
            return vanilla;
        }
        return theme.uiAssets().texture(key);
    }

    private static void blitStretched(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int w, int h) {
        graphics.blit(texture, x, y, x + w, y + h, 0.0F, 1.0F, 0.0F, 1.0F);
    }
}
