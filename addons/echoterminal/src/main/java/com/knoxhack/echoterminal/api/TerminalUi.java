package com.knoxhack.echoterminal.api;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.knoxhack.echoterminal.api.theme.TerminalChapterStyle;
import com.knoxhack.echoterminal.api.theme.TerminalIconKey;
import com.knoxhack.echoterminal.api.theme.TerminalTheme;
import com.knoxhack.echoterminal.api.theme.TerminalThemeRegistry;
import com.knoxhack.echoterminal.api.theme.TerminalThemeTokens;
import java.util.Locale;

public final class TerminalUi {
    public static int CYAN = 0xFF66E8FF;
    public static int CYAN_DIM = 0xFF2E8E9D;
    public static int TEXT = 0xFFE9FBFF;
    public static int MUTED = 0xFF8CA7B5;
    public static int GREEN = 0xFF92F7A6;
    public static int AMBER = 0xFFFFD166;
    public static int RED = 0xFFFF8FA3;
    public static int PANEL = 0x6610242F;
    public static int PANEL_DARK = 0xB6050D14;
    public static int ROW = 0xFF0D171F;
    public static int ROW_SELECTED = 0xFF123241;
    private static final float TERMINAL_PANEL_ASPECT = 2.0F;
    private static final float TERMINAL_BACKDROP_ASPECT = 16.0F / 9.0F;

    public enum ImageFit {
        STRETCH,
        COVER,
        CONTAIN
    }

    private TerminalUi() {
    }

    public static void applyThemeGlobals(TerminalTheme theme) {
        TerminalThemeTokens tokens = (theme == null ? TerminalThemeRegistry.defaultTheme() : theme).tokens();
        CYAN = tokens.colors().accent();
        CYAN_DIM = tokens.colors().accentDim();
        TEXT = tokens.colors().text();
        MUTED = tokens.colors().muted();
        GREEN = tokens.colors().success();
        AMBER = tokens.colors().warning();
        RED = tokens.colors().danger();
        PANEL = tokens.panels().baseFill();
        PANEL_DARK = tokens.panels().darkFill();
        ROW = tokens.colors().row();
        ROW_SELECTED = tokens.colors().rowSelected();
    }

    public static TerminalTheme theme(TerminalRenderContext context) {
        return context == null ? TerminalThemeRegistry.defaultTheme() : context.theme();
    }

    public static TerminalThemeTokens tokens(TerminalRenderContext context) {
        return theme(context).tokens();
    }

    public static TerminalChapterStyle chapterStyle(TerminalRenderContext context) {
        return theme(context).chapterStyle(context == null ? null : context.themeContext());
    }

    public static int chapterAccent(TerminalRenderContext context, int fallback) {
        TerminalChapterStyle style = chapterStyle(context);
        return style == null || style.accentColor() == 0 ? fallback : style.accentColor();
    }

    public static int chapterSecondary(TerminalRenderContext context, int fallback) {
        TerminalChapterStyle style = chapterStyle(context);
        return style == null || style.secondaryColor() == 0 ? fallback : style.secondaryColor();
    }

    public static Identifier chapterPanel(TerminalRenderContext context) {
        TerminalChapterStyle style = chapterStyle(context);
        Identifier panel = style == null ? null : style.panel();
        return panel == null ? tokens(context).assets().panelPlate() : panel;
    }

    public static Identifier chapterBanner(TerminalRenderContext context) {
        TerminalChapterStyle style = chapterStyle(context);
        Identifier banner = style == null ? null : style.banner();
        return banner == null ? tokens(context).assets().defaultBanner() : banner;
    }

    public static int text(TerminalRenderContext context) {
        return tokens(context).colors().text();
    }

    public static int muted(TerminalRenderContext context) {
        return tokens(context).colors().muted();
    }

    public static int success(TerminalRenderContext context) {
        return tokens(context).colors().success();
    }

    public static int warning(TerminalRenderContext context) {
        return tokens(context).colors().warning();
    }

    public static int danger(TerminalRenderContext context) {
        return tokens(context).colors().danger();
    }

    public static int accent(TerminalRenderContext context) {
        return tokens(context).colors().accent();
    }

    public static int accentDim(TerminalRenderContext context) {
        return tokens(context).colors().accentDim();
    }

    public static Identifier themedIcon(TerminalRenderContext context, TerminalIconKey key, Identifier fallback) {
        return theme(context).icon(key, context == null ? null : context.themeContext(), fallback);
    }

    public static Identifier themedGroupIcon(TerminalRenderContext context, String group) {
        return themedIcon(context, TerminalIconKey.group(group), TerminalVisualAssets.terminalGroupIcon(group));
    }

    public static Identifier themedPageIcon(TerminalRenderContext context, String title) {
        return themedIcon(context, TerminalIconKey.page(semanticName(title)), TerminalVisualAssets.terminalPageIcon(title));
    }

    public static Identifier themedActionIcon(TerminalRenderContext context, String action, Identifier fallback) {
        return themedIcon(context, TerminalIconKey.action(semanticName(action)), fallback);
    }

    public static Identifier themedStateIcon(TerminalRenderContext context, String state, Identifier fallback) {
        return themedIcon(context, TerminalIconKey.state(semanticName(state)), fallback);
    }

    public static Identifier themedMissionIcon(
            TerminalRenderContext context, Identifier missionId, String category) {
        Identifier fallback = TerminalVisualAssets.missionIconArt(missionId, category);
        if (fallback != null && fallback.getPath().startsWith("textures/gui/mission_icons/")) {
            return fallback;
        }
        Identifier chapter = missionId == null ? null : themedIcon(context,
                TerminalIconKey.chapter(missionId.getNamespace()), null);
        if (chapter != null && category == null) {
            return chapter;
        }
        return themedIcon(context, TerminalIconKey.missionCategory(missionCategoryKey(category)), fallback);
    }

    public static Identifier themedSemanticIcon(TerminalRenderContext context, String name, Identifier fallback) {
        String key = semanticName(name);
        Identifier icon = themedIcon(context, TerminalIconKey.state(key), null);
        if (icon != null) {
            return icon;
        }
        icon = themedIcon(context, TerminalIconKey.action(key), null);
        if (icon != null) {
            return icon;
        }
        icon = themedIcon(context, TerminalIconKey.page(key), null);
        if (icon != null) {
            return icon;
        }
        return themedIcon(context, TerminalIconKey.fallback("unknown"), fallback);
    }

    private static String missionCategoryKey(String category) {
        String key = semanticName(category);
        if (key.contains("hazard") || key.contains("weather") || key.contains("storm") || key.contains("biome")) {
            return "hazard";
        }
        if (key.contains("survival") || key.contains("water") || key.contains("radiation")) {
            return "survival";
        }
        if (key.contains("craft") || key.contains("machine") || key.contains("recipe")) {
            return "crafting";
        }
        if (key.contains("tech") || key.contains("research") || key.contains("power") || key.contains("grid")) {
            return "tech";
        }
        if (key.contains("explor") || key.contains("world") || key.contains("route") || key.contains("poi")) {
            return "exploration";
        }
        if (key.contains("combat") || key.contains("guardian") || key.contains("warden") || key.contains("boss")) {
            return "combat";
        }
        if (key.contains("story") || key.contains("nexus") || key.contains("archive")) {
            return "story";
        }
        return "side_ops";
    }

    public static void section(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String text, int x, int y, int color) {
        graphics.text(font(context), trim(context, text, Math.max(40, context.contentX() + context.contentWidth() - x)),
                x, y, opaque(color), tokens(context).typography().shadowText());
    }

    public static int sectionHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String title, String detail, int x, int y, int width, int color) {
        if (detail != null && !detail.isBlank()) {
            int detailWidth = Math.max(48, Math.min(160, width / 3));
            int detailX = x + width - detailWidth;
            line(context, graphics, title, x, y, Math.max(24, detailX - x - 8), color);
            line(context, graphics, detail, detailX, y, detailWidth, muted(context));
        } else {
            line(context, graphics, title, x, y, width, color);
        }
        divider(graphics, x, y + 14, width, color);
        return y + 20;
    }

    public static void panel(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, PANEL_DARK);
        graphics.outline(x, y, w, h, 0x55244352);
    }

    public static void panel(TerminalRenderContext context, GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        TerminalThemeTokens tokens = tokens(context);
        drawPanelTexture(context, graphics, tokens.assets().panelPlate(), x, y, w, h, tokens.panels().imageDarken());
        graphics.fill(x, y, x + w, y + h, tokens.panels().darkFill());
        graphics.outline(x, y, w, h, tokens.borders().subtle());
    }

    public static void densePanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xBB071017);
        graphics.outline(x, y, w, h, 0x44244352);
        graphics.fill(x, y, x + Math.max(18, Math.min(w, w / 5)), y + 1, opaque(color));
    }

    public static void densePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        TerminalThemeTokens tokens = tokens(context);
        drawPanelTexture(context, graphics, tokens.assets().panelPlate(), x, y, w, h,
                Math.min(0.82F, tokens.panels().imageDarken() + 0.12F));
        graphics.fill(x, y, x + w, y + h, tokens.panels().elevatedFill());
        graphics.outline(x, y, w, h, tokens.borders().subtle());
        graphics.fill(x, y, x + Math.max(18, Math.min(w, w / 5)), y + 1, opaque(color));
    }

    public static void flatHudPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xE8071017);
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.min(h - 1, 22), 0x1466E8FF);
        graphics.outline(x, y, w, h, 0x5E2E8E9D);
        graphics.fill(x, y, x + Math.max(28, Math.min(w, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(24, Math.min(w, w / 7)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 26), 0x5555DDEF);
        if (w > 56 && h > 26) {
            graphics.fill(x + 8, y + 8, x + w - 8, y + 9, 0x1C38DFF4);
        }
    }

    public static void flatHudPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        TerminalThemeTokens tokens = tokens(context);
        drawPanelTexture(context, graphics, chapterPanel(context), x, y, w, h, tokens.panels().imageDarken());
        graphics.fill(x, y, x + w, y + h, tokens.panels().elevatedFill());
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.min(h - 1, 22), tokens.panels().headerFill());
        graphics.outline(x, y, w, h, tokens.borders().normal());
        graphics.fill(x, y, x + Math.max(28, Math.min(w, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(24, Math.min(w, w / 7)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 26), tokens.borders().glow());
        if (w > 56 && h > 26) {
            graphics.fill(x + 8, y + 8, x + w - 8, y + 9, tokens.dividers().line());
        }
    }

    public static void cinematicPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xD8050C13);
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.min(h - 1, 26), 0x18163843);
        graphics.outline(x, y, w, h, 0x5E2E8E9D);
        graphics.fill(x, y, x + Math.min(w, Math.max(42, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.min(w, Math.max(36, w / 6)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 32), 0x6638DFF4);
        graphics.fill(x + w - Math.min(w, 38), y + h - 2, x + w, y + h, 0x4438DFF4);
        if (w > 48 && h > 44) {
            graphics.fill(x + 10, y + 10, x + w - 10, y + 11, 0x1766E8FF);
            graphics.fill(x + 10, y + h - 11, x + w - 10, y + h - 10, 0x182E8E9D);
        }
    }

    public static void cinematicHeroPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.76F, false, ImageFit.COVER);
        cinematicPanel(context, graphics, x, y, w, h, color);
        line(context, graphics, title, x + 10, y + 10, Math.max(40, w - 20), color);
        if (detail != null && !detail.isBlank()) {
            wrap(context, graphics, detail, x + 10, y + 25, Math.max(40, w - 20), text(context));
        }
    }

    public static void imagePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame);
    }

    public static void imagePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame, ImageFit fit) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame, fit);
    }

    public static void imagePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame,
            ImageFit fit, float sourceAspect) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame, fit, sourceAspect);
    }

    public static void imagePanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame, ImageFit.STRETCH);
    }

    public static void imagePanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame, ImageFit fit) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame, fit, TERMINAL_PANEL_ASPECT);
    }

    public static void imagePanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame,
            ImageFit fit, float sourceAspect) {
        if (!textureAvailable(texture)) {
            fallbackVisualPanel(graphics, x, y, w, h, color);
        } else {
            blitFitted(graphics, texture, x, y, w, h, fit, sourceAspect);
        }
        int alpha = Math.max(0, Math.min(230, Math.round(darken * 255.0F)));
        graphics.fill(x, y, x + w, y + h, (alpha << 24) | 0x071017);
        if (frame) {
            graphics.outline(x, y, w, h, CYAN_DIM);
            graphics.fill(x, y, x + Math.max(22, Math.min(w, w / 4)), y + 2, opaque(color));
            graphics.fill(x, y + h - 2, x + Math.max(22, Math.min(w, w / 5)), y + h, opaque(color));
        }
    }

    public static void cardPlate(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken) {
        cardPlate(graphics, texture, x, y, w, h, color, darken, ImageFit.STRETCH);
    }

    public static void cardPlate(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, ImageFit fit) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, false, fit);
        graphics.outline(x, y, w, h, 0x7738DFF4);
        graphics.fill(x, y, x + Math.max(34, Math.min(w, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(28, Math.min(w, w / 7)), y + h, opaque(color));
    }

    public static void hdBackplatePanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, ImageFit fit) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, false, fit);
        graphics.outline(x, y, w, h, 0x6638DFF4);
        graphics.fill(x, y, x + Math.max(42, Math.min(w, w / 6)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(34, Math.min(w, w / 8)), y + h, opaque(color));
        if (w > 72 && h > 34) {
            graphics.fill(x + 10, y + 10, x + w - 10, y + 11, 0x1838DFF4);
        }
    }

    public static void texturedPanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken) {
        cardPlate(graphics, texture, x, y, w, h, color, darken);
    }

    public static void texturedPanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, ImageFit fit) {
        cardPlate(graphics, texture, x, y, w, h, color, darken, fit);
    }

    public static int dataPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail, int color, float darken) {
        cardPlate(graphics, texture, x, y, w, h, color, darken);
        return sectionHeader(context, graphics, title, detail, x + 14, y + 14, Math.max(24, w - 28), color);
    }

    public static int dataPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail,
            int color, float darken, ImageFit fit) {
        cardPlate(graphics, texture, x, y, w, h, color, darken, fit);
        return sectionHeader(context, graphics, title, detail, x + 14, y + 14, Math.max(24, w - 28), color);
    }

    public static int flatDataPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String title, String detail, int color) {
        flatHudPanel(context, graphics, x, y, w, h, color);
        return sectionHeader(context, graphics, title, detail, x + 14, y + 14, Math.max(24, w - 28), color);
    }

    public static int iconTitleHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, TerminalIcon fallback, int x, int y, int w, int h,
            String title, String detail, String status, int color, int statusColor) {
        cardPlate(graphics, chapterPanel(context), x, y, w, h, color, tokens(context).panels().imageDarken());
        int iconSize = Math.min(56, Math.max(34, h - 36));
        hybridIconBadge(graphics, texture, fallback, x + 14, y + Math.max(12, (h - iconSize) / 2), iconSize,
                color, true);
        int textX = x + iconSize + 28;
        int pillW = status == null || status.isBlank() ? 0 : Math.max(70, Math.min(112, w / 5));
        line(context, graphics, title, textX, y + 18, Math.max(40, w - (textX - x) - pillW - 22), text(context));
        if (detail != null && !detail.isBlank()) {
            wrap(context, graphics, detail, textX, y + 36, Math.max(40, w - (textX - x) - 18), muted(context));
        }
        if (pillW > 0) {
            miniStatusPill(context, graphics, status, x + w - pillW - 14, y + 18, pillW,
                    statusColor, true);
        }
        return y + h;
    }

    public static int v2HeroHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier artTexture, Identifier iconTexture, TerminalIcon fallback, int x, int y, int w, int h,
            String title, String detail, String body, String primaryStatus, String secondaryStatus,
            float progressValue, int color, int statusColor, boolean useArt) {
        int safeW = Math.max(80, w);
        int safeH = Math.max(92, h);
        if (useArt && artTexture != null) {
            imagePanel(context, graphics, artTexture, x, y, safeW, safeH, color, 0.42F, true, ImageFit.COVER);
            graphics.fill(x, y, x + safeW, y + safeH, 0x58071117);
        } else {
            flatHudPanel(context, graphics, x, y, safeW, safeH, color);
        }
        int iconSize = Math.min(52, Math.max(34, safeH - 48));
        int iconX = x + 12;
        int iconY = y + Math.max(12, (safeH - iconSize) / 2);
        hybridIconBadge(graphics, iconTexture, fallback, iconX, iconY, iconSize, color, true);

        boolean showStatus = primaryStatus != null && !primaryStatus.isBlank() && safeW >= 270;
        int pillW = showStatus ? Math.max(82, Math.min(126, safeW / 5)) : 0;
        int pillX = x + safeW - pillW - 14;
        int textX = iconX + iconSize + 16;
        int textRightInset = showStatus ? pillW + 28 : 16;
        int textW = Math.max(48, safeW - (textX - x) - textRightInset);
        line(context, graphics, title == null ? "" : title, textX, y + 18, textW, text(context));
        if (detail != null && !detail.isBlank()) {
            line(context, graphics, detail, textX, y + 34, textW, color);
        }
        if (showStatus) {
            missionStatusPill(context, graphics, primaryStatus, pillX, y + 16, pillW);
            if (secondaryStatus != null && !secondaryStatus.isBlank()) {
                miniStatusPill(context, graphics, secondaryStatus, pillX, y + 34, pillW, statusColor, false);
            }
        }

        String safeBody = body == null ? "" : body;
        int bodyY = y + 52;
        int bodyW = Math.max(48, safeW - (textX - x) - 16);
        int bodyColor = statusColor == muted(context) ? muted(context) : text(context);
        if (safeH - (bodyY - y) < 34) {
            line(context, graphics, safeBody, textX, bodyY, bodyW, bodyColor);
        } else {
            wrap(context, graphics, safeBody, textX, bodyY, bodyW, bodyColor);
        }
        progress(graphics, x + 12, y + safeH - 14, safeW - 24, 6, progressValue, color);
        return y + safeH;
    }

    public static void dataListRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String title, String detail, String status,
            boolean selected, boolean hovered, int color, int statusColor) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill()
                : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : tokens.borders().subtle());
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : tokens.dividers().line());
        if (selected) {
            graphics.fill(x + Math.max(28, w / 3), y + h - 3, x + w - 8, y + h - 1, tokens.borders().strong());
        }
        int pillW = status == null || status.isBlank() ? 0 : Math.max(68, Math.min(112, w / 3));
        line(context, graphics, title, x + 10, y + Math.max(5, h >= 30 ? 7 : (h - 8) / 2),
                Math.max(40, w - pillW - 24), selected ? text(context) : muted(context));
        if (detail != null && !detail.isBlank() && h >= 30) {
            line(context, graphics, detail, x + 10, y + 20, Math.max(40, w - 20), muted(context));
        }
        if (pillW > 0) {
            miniStatusPill(context, graphics, status, x + w - pillW - 10, y + Math.max(3, (h - 14) / 2),
                    pillW, statusColor, selected);
        }
    }

    public static void iconDataListRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, TerminalIcon fallback, int x, int y, int w, int h,
            String title, String detail, String status, boolean selected, boolean hovered,
            int color, int statusColor, boolean iconActive) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill()
                : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : tokens.borders().subtle());
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : tokens.dividers().line());
        if (selected) {
            graphics.fill(x + Math.max(34, w / 3), y + h - 3, x + w - 8, y + h - 1, tokens.borders().strong());
        }
        int iconSize = Math.min(18, Math.max(12, h - 8));
        hybridIcon(graphics, texture, fallback, x + 8, y + Math.max(4, (h - iconSize) / 2),
                iconSize, statusColor, iconActive);
        int pillW = status == null || status.isBlank() ? 0 : Math.max(68, Math.min(112, w / 3));
        int textX = x + iconSize + 22;
        line(context, graphics, title, textX, y + Math.max(5, h >= 30 ? 7 : (h - 8) / 2),
                Math.max(40, w - (textX - x) - pillW - 16), selected ? text(context) : muted(context));
        if (detail != null && !detail.isBlank() && h >= 30) {
            line(context, graphics, detail, textX, y + 20, Math.max(40, w - (textX - x) - 10), muted(context));
        }
        if (pillW > 0) {
            miniStatusPill(context, graphics, status, x + w - pillW - 10, y + Math.max(3, (h - 14) / 2),
                    pillW, statusColor, selected);
        }
    }

    public static int imageHero(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.62F, true);
        return y + h + 8;
    }

    private static void drawPanelTexture(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, float darken) {
        if (context == null || !context.themeContext().visualAssets() || !textureAvailable(texture)) {
            return;
        }
        blitFitted(graphics, texture, x, y, w, h, ImageFit.COVER, TERMINAL_PANEL_ASPECT);
        int alpha = Math.max(0, Math.min(230, Math.round(Math.max(0.0F, Math.min(1.0F, darken)) * 255.0F)));
        graphics.fill(x, y, x + w, y + h, (alpha << 24) | (tokens(context).colors().background() & 0x00FFFFFF));
    }

    public static void questArtCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, boolean selected, boolean hovered) {
        imagePanel(context, graphics, texture, x, y, w, h, color, selected ? 0.68F : 0.78F, false);
        if (hovered) {
            graphics.fill(x, y, x + w, y + h, 0x22163843);
        }
        graphics.outline(x, y, w, h, selected ? opaque(color) : 0x4438DFF4);
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : 0x7738DFF4);
        graphics.fill(x, y + h - 2, x + w, y + h, selected ? opaque(color) : 0x5538DFF4);
    }

    private static void fallbackVisualPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xEE071017);
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.max(2, h / 3), 0x33163843);
        for (int i = 0; i < 4; i++) {
            int lineY = y + 8 + i * Math.max(8, h / 5);
            graphics.fill(x + 8, lineY, x + Math.max(18, w - 8 - i * 14), lineY + 1, 0x33244352);
        }
        graphics.fill(x, y, x + Math.max(22, w / 5), y + 2, opaque(color));
    }

    private static void blitFitted(GuiGraphicsExtractor graphics, Identifier texture,
            int x, int y, int w, int h, ImageFit fit, float sourceAspect) {
        if (w <= 0 || h <= 0) {
            return;
        }
        ImageFit mode = fit == null ? ImageFit.STRETCH : fit;
        float safeSourceAspect = sourceAspect <= 0.0F ? TERMINAL_PANEL_ASPECT : sourceAspect;
        if (mode == ImageFit.CONTAIN) {
            float destAspect = w / (float) h;
            int drawW = w;
            int drawH = h;
            int drawX = x;
            int drawY = y;
            if (destAspect > safeSourceAspect) {
                drawW = Math.max(1, Math.round(h * safeSourceAspect));
                drawX = x + (w - drawW) / 2;
            } else if (destAspect < safeSourceAspect) {
                drawH = Math.max(1, Math.round(w / safeSourceAspect));
                drawY = y + (h - drawH) / 2;
            }
            graphics.fill(x, y, x + w, y + h, 0xFF071017);
            graphics.blit(texture, drawX, drawY, drawX + drawW, drawY + drawH,
                    0.0F, 1.0F, 0.0F, 1.0F);
            return;
        }

        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;
        if (mode == ImageFit.COVER) {
            float destAspect = w / (float) h;
            if (destAspect > safeSourceAspect) {
                float visibleV = Math.max(0.0F, Math.min(1.0F, safeSourceAspect / destAspect));
                v0 = (1.0F - visibleV) * 0.5F;
                v1 = 1.0F - v0;
            } else if (destAspect < safeSourceAspect) {
                float visibleU = Math.max(0.0F, Math.min(1.0F, destAspect / safeSourceAspect));
                u0 = (1.0F - visibleU) * 0.5F;
                u1 = 1.0F - u0;
            }
        }
        graphics.blit(texture, x, y, x + w, y + h, u0, u1, v0, v1);
    }

    public static void divider(GuiGraphicsExtractor graphics, int x, int y, int w, int color) {
        graphics.fill(x, y, x + w, y + 1, 0x55244352);
        graphics.fill(x, y, x + Math.max(12, w / 5), y + 1, opaque(color));
    }

    public static void selectableRow(GuiGraphicsExtractor graphics, int x, int y, int w, int h,
            boolean selected, boolean hovered, int accentColor) {
        int bg = selected ? ROW_SELECTED : (hovered ? 0xFF102630 : ROW);
        graphics.fill(x, y, x + w, y + h, bg);
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(accentColor));
        }
    }

    public static void selectableRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, boolean selected, boolean hovered, int accentColor) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill()
                : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(accentColor) : tokens.borders().subtle());
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(accentColor));
        }
    }

    public static void chip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 13, 0xFF10232C);
        graphics.fill(x, y + 11, x + width, y + 13, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 3, opaque(color));
    }

    public static void statusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color, boolean selected) {
        if (isSemanticStatus(label)) {
            drawSemanticStatusPill(context, graphics, label, x, y, width, 14);
            return;
        }
        int bg = selected ? ROW_SELECTED : 0xFF10232C;
        graphics.fill(x, y, x + width, y + 14, bg);
        graphics.outline(x, y, width, 14, selected ? opaque(color) : 0x5538DFF4);
        graphics.fill(x, y + 12, x + width, y + 14, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 4,
                selected ? text(context) : opaque(color));
    }

    public static void miniStatusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color, boolean filled) {
        if (isSemanticStatus(label)) {
            drawSemanticStatusPill(context, graphics, label, x, y, width, 14);
            return;
        }
        TerminalThemeTokens tokens = tokens(context);
        int bg = filled ? opaque(color) : tokens.colors().row();
        int text = filled ? tokens.colors().background() : opaque(color);
        graphics.fill(x, y, x + width, y + 14, bg);
        graphics.outline(x, y, width, 14, filled ? opaque(color) : tokens.borders().normal());
        graphics.fill(x, y + 12, x + width, y + 14, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 8), x + width / 2, y + 4, text);
    }

    public static void missionStatusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width) {
        drawSemanticStatusPill(context, graphics, label, x, y, width, 14);
    }

    private static boolean isSemanticStatus(String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        return switch (label.toUpperCase()) {
            case "READY", "DONE", "OPEN", "AVAILABLE", "ACTIVE", "NEEDED", "NEED", "LOCKED", "VIEW" -> true;
            default -> false;
        };
    }

    private static void drawSemanticStatusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int height) {
        String value = label == null ? "" : label.toUpperCase();
        TerminalThemeTokens tokens = tokens(context);
        int bg;
        int border;
        int rail;
        int text;
        switch (value) {
            case "READY", "DONE", "OPEN", "AVAILABLE" -> {
                bg = withAlpha(tokens.colors().success(), 0x34);
                border = withAlpha(tokens.colors().success(), 0xAA);
                rail = tokens.colors().success();
                text = tokens.colors().success();
            }
            case "ACTIVE", "NEEDED", "NEED" -> {
                bg = withAlpha(tokens.colors().warning(), 0x34);
                border = withAlpha(tokens.colors().warning(), 0xAA);
                rail = tokens.colors().warning();
                text = tokens.colors().warning();
            }
            case "LOCKED", "VIEW" -> {
                bg = tokens.panels().disabledFill();
                border = tokens.borders().disabled();
                rail = tokens.dividers().line();
                text = tokens.colors().muted();
            }
            default -> {
                bg = tokens.colors().rowSelected();
                border = tokens.borders().selected();
                rail = tokens.colors().accentDim();
                text = tokens.colors().text();
            }
        }
        int safeHeight = Math.max(14, height);
        graphics.fill(x, y, x + width, y + safeHeight, bg);
        graphics.outline(x, y, width, safeHeight, border);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, tokens.borders().glow());
        graphics.fill(x, y + safeHeight - 2, x + width, y + safeHeight, rail);
        graphics.centeredText(font(context), trim(context, value, width - 14), x + width / 2,
                y + Math.max(2, (safeHeight - 8) / 2), text);
    }

    public static void tabChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
            String label, boolean selected, boolean hovered, int color) {
        int bg = selected ? ROW_SELECTED : hovered ? 0xFF102630 : ROW;
        int text = selected ? TEXT : MUTED;
        int accent = selected ? opaque(color) : CYAN_DIM;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, selected ? opaque(color) : 0x4438DFF4);
        graphics.fill(x, y + height - 2, x + width, y + height, accent);
        graphics.centeredText(font, trim(font, label, width - 8), x + width / 2, y + Math.max(4, (height - 8) / 2), text);
    }

    public static void categoryChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
            String label, boolean selected, boolean hovered, int color) {
        int bg = selected ? 0x8A07131B : hovered ? 0x66102630 : 0x33050D14;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, selected ? 0x8866E8FF : 0x332E8E9D);
        graphics.fill(x, y, x + 3, y + height, selected ? opaque(color) : 0x552E8E9D);
        graphics.centeredText(font, trim(font, label, width - 12), x + width / 2,
                y + Math.max(4, (height - 8) / 2), selected ? TEXT : MUTED);
    }

    public static void pageTab(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
            String label, boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xE00B3341 : hovered ? 0xAA102630 : 0x44050D14;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, selected ? opaque(color) : 0x2F2E8E9D);
        if (selected) {
            graphics.fill(x + 1, y + 1, x + width - 1, y + Math.max(2, height / 2), 0x3020024A);
            graphics.fill(x, y + height - 2, x + width, y + height, opaque(color));
            graphics.fill(x + width / 4, y + height - 4, x + width - width / 4, y + height - 2, 0xAAE9FBFF);
            int glowHeight = Math.min(14, Math.max(0, 48 - height));
            if (glowHeight > 0) {
                graphics.fill(x, y + height, x + width, y + height + glowHeight, 0x3300E5FF);
            }
        }
        graphics.centeredText(font, trim(font, label, width - 10), x + width / 2,
                y + Math.max(4, (height - 8) / 2), selected ? TEXT : MUTED);
    }

    public static void sidebarGroupChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
            String label, boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xFF123241 : hovered ? 0xFF102630 : 0xAA0A151C;
        int text = selected ? TEXT : MUTED;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, selected ? opaque(color) : 0x33244352);
        graphics.fill(x, y, x + 3, y + height, selected ? opaque(color) : CYAN_DIM);
        graphics.text(font, trim(font, label, width - 18), x + 10, y + Math.max(4, (height - 8) / 2), text, false);
    }

    public static void sidebarTabChip(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
            String label, String summary, boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xFF123241 : hovered ? 0xFF102630 : ROW;
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.outline(x, y, width, height, selected ? opaque(color) : 0x33244352);
        graphics.fill(x, y + height - 2, x + width, y + height, selected ? opaque(color) : CYAN_DIM);
        graphics.text(font, trim(font, label, width - 12), x + 7, y + 5, selected ? TEXT : MUTED, false);
        if (height >= 28 && summary != null && !summary.isBlank()) {
            graphics.text(font, trim(font, summary, width - 12), x + 7, y + 17, MUTED, false);
        }
    }

    public static int pageHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String title, String detail, int x, int y, int width, int color) {
        densePanel(context, graphics, x, y, width, 30, color);
        line(context, graphics, title, x + 8, y + 6, Math.max(40, width / 2), color);
        if (detail != null && !detail.isBlank()) {
            String trimmed = trim(context, detail, Math.max(40, width / 2 - 10));
            graphics.text(font(context), trimmed, x + width - 8 - font(context).width(trimmed),
                    y + 6, muted(context), false);
        }
        return y + 38;
    }

    public static int shortcutCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String value, String detail, int color, boolean hovered) {
        int height = Math.max(42, 30 + wrappedHeight(context, detail, width - 16));
        graphics.fill(x, y, x + width, y + height, hovered ? 0xFF102630 : PANEL_DARK);
        graphics.outline(x, y, width, height, hovered ? opaque(color) : 0x44244352);
        graphics.fill(x, y, x + 3, y + height, opaque(color));
        line(context, graphics, title, x + 8, y + 6, width - 16, MUTED);
        line(context, graphics, value, x + 8, y + 18, width - 16, color);
        wrap(context, graphics, detail, x + 8, y + 31, width - 16, TEXT);
        return height;
    }

    public static int missionLaneHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String count, int color) {
        graphics.fill(x, y, x + width, y + 17, 0x990A151C);
        graphics.fill(x, y, x + 3, y + 17, opaque(color));
        line(context, graphics, title, x + 8, y + 4, width - 80, color);
        if (count != null && !count.isBlank()) {
            line(context, graphics, count, x + width - 70, y + 4, 64, MUTED);
        }
        return y + 20;
    }

    public static int stickyActionBar(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, int height, String title, String detail, int color) {
        graphics.fill(x, y, x + width, y + height, 0xEE071017);
        graphics.outline(x, y, width, height, 0x5538DFF4);
        graphics.fill(x, y, x + Math.max(24, Math.min(width, width / 5)), y + 2, opaque(color));
        line(context, graphics, title, x + 8, y + 7, width - 16, color);
        wrap(context, graphics, detail, x + 8, y + 20, width - 16, TEXT);
        return y + height + 6;
    }

    public static void filterChip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, boolean selected, boolean enabled, int color, boolean hovered) {
        int bg = selected ? 0xFF123241 : hovered && enabled ? 0xFF102630 : 0xFF0D171F;
        int text = selected ? TEXT : enabled ? MUTED : CYAN_DIM;
        int accent = selected ? color : CYAN_DIM;
        graphics.fill(x, y, x + width, y + 15, bg);
        graphics.outline(x, y, width, 15, selected ? opaque(color) : 0x4438DFF4);
        graphics.fill(x, y + 13, x + width, y + 15, opaque(accent));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 4, opaque(text));
    }

    public static int statusCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, String value, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 14);
        int height = Math.max(54, 42 + detailHeight);
        panel(context, graphics, x, y, width, height);
        line(context, graphics, label, x + 7, y + 7, width - 14, muted(context));
        line(context, graphics, value, x + 7, y + 22, width - 14, color);
        wrap(context, graphics, detail, x + 7, y + 37, width - 14, text(context));
        return height;
    }

    public static int dataCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String value, String detail, int color) {
        return statusCard(context, graphics, x, y, width, title, value, detail, color);
    }

    public static int denseDataCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String value, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 12);
        int height = Math.max(44, 32 + detailHeight);
        densePanel(context, graphics, x, y, width, height, color);
        line(context, graphics, title, x + 6, y + 6, width - 12, muted(context));
        line(context, graphics, value, x + 6, y + 18, width - 12, color);
        wrap(context, graphics, detail, x + 6, y + 31, width - 12, text(context));
        return height;
    }

    public static int commandStrip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 14);
        int height = Math.max(34, 24 + detailHeight);
        panel(context, graphics, x, y, width, height);
        line(context, graphics, title, x + 7, y + 7, width - 14, color);
        wrap(context, graphics, detail, x + 7, y + 20, width - 14, text(context));
        return y + height + 5;
    }

    public static int compactCommandStrip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 12);
        int height = Math.max(28, 18 + detailHeight);
        densePanel(context, graphics, x, y, width, height, color);
        line(context, graphics, title, x + 6, y + 5, width - 12, color);
        wrap(context, graphics, detail, x + 6, y + 17, width - 12, text(context));
        return y + height + 4;
    }

    public static int emptyState(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int height = Math.max(42, 28 + wrappedHeight(context, detail, width - 16));
        panel(context, graphics, x, y, width, height);
        line(context, graphics, title, x + 8, y + 8, width - 16, color);
        wrap(context, graphics, detail, x + 8, y + 22, width - 16, muted(context));
        return y + height + 5;
    }

    public static int callout(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int bodyHeight = wrappedHeight(context, detail, width - 20);
        int height = Math.max(38, 25 + bodyHeight);
        graphics.fill(x, y, x + width, y + height, tokens(context).colors().row());
        graphics.fill(x, y, x + 3, y + height, opaque(color));
        graphics.outline(x, y, width, height, tokens(context).borders().normal());
        line(context, graphics, title, x + 9, y + 7, width - 18, color);
        wrap(context, graphics, detail, x + 9, y + 20, width - 18, text(context));
        return y + height + 5;
    }

    public static int metricRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, String value, int color) {
        int valueWidth = Math.min(112, Math.max(54, width / 3));
        line(context, graphics, label, x, y, Math.max(24, width - valueWidth - 8), MUTED);
        line(context, graphics, value, x + width - valueWidth, y, valueWidth, color);
        return y + 13;
    }

    public static int denseMetricRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, String value, int color) {
        int valueWidth = Math.min(104, Math.max(50, width / 3));
        line(context, graphics, label, x, y, Math.max(24, width - valueWidth - 8), MUTED);
        line(context, graphics, value, x + width - valueWidth, y, valueWidth, color);
        return y + 11;
    }

    public static int keyValue(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, String value, int color) {
        int split = Math.min(150, Math.max(86, width / 3));
        line(context, graphics, label, x, y, split - 6, MUTED);
        line(context, graphics, value, x + split, y, Math.max(24, width - split), color);
        return y + 14;
    }

    public static int checklistRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String label, boolean ok, String detail) {
        int color = ok ? GREEN : AMBER;
        int split = Math.min(160, Math.max(96, width / 3));
        line(context, graphics, (ok ? "[x] " : "[ ] ") + label, x, y, split - 6, color);
        return wrap(context, graphics, detail, x + split, y, Math.max(24, width - split), color) + 3;
    }

    public static int disabledActionRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String reason, int color) {
        int bodyHeight = wrappedHeight(context, reason, width - 14);
        int height = Math.max(23, bodyHeight + 14);
        panel(graphics, x, y, width, height);
        wrap(context, graphics, reason, x + 7, y + 7, width - 14, color);
        return y + height + 4;
    }

    public static int disabledReasonRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String reason, int color) {
        return disabledActionRow(context, graphics, x, y, width, reason, color);
    }

    public static void line(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String text, int x, int y, int maxWidth, int color) {
        graphics.text(font(context), trim(context, text, Math.max(20, maxWidth)), x, y, opaque(color),
                tokens(context).typography().shadowText());
    }

    public static int wrap(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String text, int x, int y, int maxWidth, int color) {
        int cy = y;
        String value = text == null ? "" : text;
        for (String paragraph : value.split("\\R", -1)) {
            if (paragraph.isEmpty()) {
                cy += 11;
                continue;
            }
            for (var line : font(context).split(Component.literal(paragraph), Math.max(24, maxWidth))) {
                graphics.text(font(context), line, x, cy, opaque(color), tokens(context).typography().shadowText());
                cy += 11;
            }
        }
        return cy;
    }

    public static void button(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, boolean enabled, boolean hovered) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = enabled ? (hovered ? tokens.panels().hoverFill() : tokens.panels().selectedFill())
                : tokens.panels().disabledFill();
        int accent = enabled ? color : tokens.colors().accentDim();
        graphics.fill(x, y, x + w, y + 18, bg);
        graphics.outline(x, y, w, 18, enabled ? tokens.borders().normal() : tokens.borders().disabled());
        graphics.fill(x, y + 16, x + w, y + 18, accent);
        graphics.centeredText(font(context), trim(context, label, w - 8), x + w / 2, y + 5,
                enabled ? tokens.colors().text() : tokens.colors().muted());
    }

    public static void compactButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, boolean enabled, boolean hovered) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = enabled ? (hovered ? tokens.panels().hoverFill() : tokens.colors().row())
                : tokens.panels().disabledFill();
        int accent = enabled ? color : tokens.colors().accentDim();
        graphics.fill(x, y, x + w, y + 16, bg);
        graphics.outline(x, y, w, 16, enabled ? tokens.borders().normal() : tokens.borders().disabled());
        graphics.fill(x, y + 14, x + w, y + 16, accent);
        graphics.centeredText(font(context), trim(context, label, w - 8), x + w / 2, y + 4,
                enabled ? tokens.colors().text() : tokens.colors().muted());
    }

    public static void primaryCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label, int color, boolean hovered) {
        primaryCommandButton(context, graphics, x, y, w, h, label, null, color, hovered);
    }

    public static void primaryCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label, Identifier texture, int color, boolean hovered) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = hovered ? tokens.panels().hoverFill() : tokens.panels().selectedFill();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, tokens.borders().selected());
        graphics.fill(x, y + h - 3, x + w, y + h, opaque(color));
        graphics.fill(x + 4, y + 4, x + 7, y + h - 4, opaque(color));
        if (hovered && h > 18) {
            graphics.fill(x + 8, y + 3, x + w - 8, y + 4, tokens.borders().glow());
        }
        drawCommandLabel(context, graphics, x, y, w, h, label, texture, color, tokens.colors().text(), true);
    }

    public static void disabledCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label) {
        disabledCommandButton(context, graphics, x, y, w, h, label, null);
    }

    public static void disabledCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label, Identifier texture) {
        TerminalThemeTokens tokens = tokens(context);
        graphics.fill(x, y, x + w, y + h, tokens.panels().disabledFill());
        graphics.outline(x, y, w, h, tokens.borders().disabled());
        graphics.fill(x + 4, y + 4, x + 7, y + h - 4, tokens.dividers().line());
        graphics.fill(x, y + h - 2, x + w, y + h, tokens.borders().disabled());
        drawCommandLabel(context, graphics, x, y, w, h, label, texture,
                tokens.colors().accentDim(), tokens.colors().muted(), false);
    }

    public static void iconBadge(GuiGraphicsExtractor graphics, TerminalIcon icon,
            int x, int y, int size, int color, boolean active) {
        graphics.fill(x, y, x + size, y + size, 0xAA071017);
        graphics.outline(x, y, size, size, active ? opaque(color) : 0x5538DFF4);
        graphics.fill(x, y + size - 2, x + size, y + size, active ? opaque(color) : CYAN_DIM);
        icon.draw(graphics, x + 8, y + 8, Math.max(18, size - 16), color, active);
    }

    public static void iconBadge(TerminalRenderContext context, GuiGraphicsExtractor graphics, TerminalIcon icon,
            int x, int y, int size, int color, boolean active) {
        TerminalThemeTokens tokens = tokens(context);
        graphics.fill(x, y, x + size, y + size, active ? tokens.colors().rowSelected() : tokens.colors().row());
        graphics.outline(x, y, size, size, active ? opaque(color) : tokens.borders().normal());
        graphics.fill(x, y + size - 2, x + size, y + size, active ? opaque(color) : tokens.colors().accentDim());
        icon.draw(graphics, x + 8, y + 8, Math.max(18, size - 16), color, active);
    }

    public static void iconTextureBadge(GuiGraphicsExtractor graphics, Identifier texture,
            int x, int y, int size, int color, boolean active) {
        boolean hasTexture = textureAvailable(texture) && size > 12;
        graphics.fill(x, y, x + size, y + size, active ? 0xCC071017 : 0x99071117);
        if (hasTexture) {
            graphics.blit(texture, x + 1, y + 1, x + size - 1, y + size - 1,
                    0.0F, 1.0F, 0.0F, 1.0F);
            graphics.outline(x, y, size, size, active ? opaque(color) : 0x5538DFF4);
        } else {
            graphics.outline(x, y, size, size, active ? opaque(color) : 0x5538DFF4);
            graphics.fill(x + 2, y + 2, x + size - 2, y + 4, active ? 0x5538DFF4 : 0x332E8E9D);
            graphics.fill(x + 2, y + size - 4, x + size - 2, y + size - 2,
                    active ? opaque(color) : 0x552E8E9D);
        }
    }

    public static void iconTextureBadge(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int size, int color, boolean active) {
        TerminalThemeTokens tokens = tokens(context);
        boolean hasTexture = textureAvailable(texture) && size > 12;
        graphics.fill(x, y, x + size, y + size, active ? tokens.colors().rowSelected() : tokens.colors().row());
        if (hasTexture) {
            graphics.blit(texture, x + 1, y + 1, x + size - 1, y + size - 1,
                    0.0F, 1.0F, 0.0F, 1.0F);
            graphics.outline(x, y, size, size, active ? opaque(color) : tokens.borders().normal());
        } else {
            graphics.outline(x, y, size, size, active ? opaque(color) : tokens.borders().normal());
            graphics.fill(x + 2, y + 2, x + size - 2, y + 4,
                    active ? tokens.borders().normal() : tokens.dividers().line());
            graphics.fill(x + 2, y + size - 4, x + size - 2, y + size - 2,
                    active ? opaque(color) : tokens.dividers().line());
        }
    }

    public static void cinematicPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        TerminalThemeTokens tokens = tokens(context);
        drawPanelTexture(context, graphics, chapterPanel(context), x, y, w, h, tokens.panels().imageDarken());
        graphics.fill(x, y, x + w, y + h, tokens.colors().content());
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.min(h - 1, 26), tokens.panels().headerFill());
        graphics.outline(x, y, w, h, tokens.borders().normal());
        graphics.fill(x, y, x + Math.min(w, Math.max(42, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.min(w, Math.max(36, w / 6)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 32), tokens.borders().glow());
        graphics.fill(x + w - Math.min(w, 38), y + h - 2, x + w, y + h, tokens.borders().normal());
        if (w > 48 && h > 44) {
            graphics.fill(x + 10, y + 10, x + w - 10, y + 11, tokens.dividers().line());
            graphics.fill(x + 10, y + h - 11, x + w - 10, y + h - 10, tokens.dividers().gridLine());
        }
    }

    public static void hybridIconBadge(GuiGraphicsExtractor graphics, Identifier texture, TerminalIcon fallback,
            int x, int y, int size, int color, boolean active) {
        if (textureAvailable(texture)) {
            iconTextureBadge(graphics, texture, x, y, size, color, active);
        } else {
            iconBadge(graphics, fallback == null ? TerminalIcon.DEFAULT : fallback, x, y, size, color, active);
        }
    }

    public static void hybridIconBadge(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, TerminalIcon fallback, int x, int y, int size, int color, boolean active) {
        if (textureAvailable(texture)) {
            iconTextureBadge(context, graphics, texture, x, y, size, color, active);
        } else {
            iconBadge(context, graphics, fallback == null ? TerminalIcon.DEFAULT : fallback, x, y, size, color, active);
        }
    }

    public static void hybridIcon(GuiGraphicsExtractor graphics, Identifier texture, TerminalIcon fallback,
            int x, int y, int size, int color, boolean active) {
        drawHybridIcon(graphics, texture, fallback, x, y, size, color, active);
    }

    public static int statusLineRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, TerminalIcon icon, String label, String value, int color) {
        return statusLineRow(context, graphics, x, y, width, icon, null, label, value, color);
    }

    public static int statusLineRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, TerminalIcon icon, Identifier texture, String label, String value, int color) {
        drawHybridIcon(graphics, texture, icon, x, y - 2, 14, color, false);
        line(context, graphics, label, x + 20, y, Math.max(24, width - 92), muted(context));
        line(context, graphics, value, x + Math.max(80, width - 84), y, Math.min(84, width / 3), color);
        return y + 18;
    }

    public static void progress(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float progress, int color) {
        int fill = Math.max(0, Math.min(w - 2, Math.round((w - 2) * Math.max(0.0F, Math.min(1.0F, progress)))));
        graphics.fill(x, y, x + w, y + h, 0xFF263842);
        graphics.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, opaque(color));
    }

    public static void progress(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, float progress, int color) {
        int fill = Math.max(0, Math.min(w - 2, Math.round((w - 2) * Math.max(0.0F, Math.min(1.0F, progress)))));
        graphics.fill(x, y, x + w, y + h, tokens(context).colors().rowSelected());
        graphics.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, opaque(color));
    }

    public static void meter(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int value, int color) {
        int clamped = Math.max(0, Math.min(100, value));
        line(context, graphics, label + " " + clamped + "%", x, y, Math.max(20, w), text(context));
        progress(context, graphics, x + 118, y + 2, Math.max(50, w - 118), 8, clamped / 100.0F, color);
    }

    public static void compactMeter(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int value, int color) {
        int clamped = Math.max(0, Math.min(100, value));
        line(context, graphics, label + " " + clamped + "%", x, y, Math.max(20, w), text(context));
        progress(context, graphics, x, y + 12, Math.max(36, w), 6, clamped / 100.0F, color);
    }

    public static void itemSlot(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            ItemStack stack, int x, int y, int color, boolean hovered) {
        graphics.fill(x, y, x + 20, y + 20, tokens(context).colors().row());
        graphics.outline(x, y, 20, 20, tokens(context).borders().normal());
        if (stack != null && !stack.isEmpty()) {
            graphics.item(stack, x + 2, y + 2);
            graphics.itemDecorations(font(context), stack, x + 2, y + 2);
            if (hovered) {
                graphics.outline(x, y, 20, 20, opaque(color));
                graphics.setTooltipForNextFrame(font(context), stack, x + 10, y + 10);
            }
        } else {
            graphics.fill(x + 5, y + 9, x + 15, y + 11, accentDim(context));
        }
    }

    public static int itemRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            ItemStack stack, int x, int y, int width, String label, String detail,
            int color, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, 20, 20);
        itemSlot(context, graphics, stack, x, y, color, hovered);
        line(context, graphics, label, x + 26, y + 1, width - 26, color);
        int detailHeight = wrappedHeight(context, detail, width - 26);
        wrap(context, graphics, detail, x + 26, y + 12, width - 26, MUTED);
        return y + Math.max(24, 13 + detailHeight) + 3;
    }

    public static int itemGrid(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            java.util.List<ItemStack> stacks, int x, int y, int width, int color, int mouseX, int mouseY) {
        if (stacks == null || stacks.isEmpty()) {
            line(context, graphics, "No item rewards recorded.", x, y, width, MUTED);
            return y + 14;
        }
        int slotStep = 24;
        int columns = Math.max(1, Math.min(8, Math.max(1, width) / slotStep));
        int cy = y;
        int index = 0;
        for (ItemStack stack : stacks) {
            int sx = x + (index % columns) * slotStep;
            int sy = cy + (index / columns) * slotStep;
            itemSlot(context, graphics, stack, sx, sy, color, inside(mouseX, mouseY, sx, sy, 20, 20));
            index++;
        }
        int rows = (int) Math.ceil(stacks.size() / (double) columns);
        return y + rows * slotStep + 2;
    }

    public static int itemGridHeight(int stackCount, int width) {
        if (stackCount <= 0) {
            return 14;
        }
        int columns = Math.max(1, Math.min(8, Math.max(1, width) / 24));
        return ((stackCount + columns - 1) / columns) * 24 + 2;
    }

    public static void appShellBackdrop(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, boolean visuals, boolean reducedMotion) {
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        graphics.fill(0, 0, screenW, screenH, 0xFF02070C);
        if (visuals) {
            if (textureAvailable(texture)) {
                blitFitted(graphics, texture, 0, 0, screenW, screenH, ImageFit.COVER, TERMINAL_BACKDROP_ASPECT);
            }
            graphics.fill(0, 0, screenW, screenH, reducedMotion ? 0xDD02070C : 0xC902070C);
            graphics.fill(0, 0, screenW, Math.max(80, screenH / 5), 0x55100528);
            graphics.fill(0, screenH - Math.max(70, screenH / 6), screenW, screenH, 0x6602070C);
        }
        drawTerminalGrid(graphics, screenW, screenH, reducedMotion ? 34 : 28);
        graphics.fill(x, y, x + w, y + h, 0x9002070C);
        graphics.outline(x, y, w, h, 0x8A38DFF4);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 18, 0x2A163843);
        graphics.fill(x + 12, y + 10, x + Math.min(x + 278, x + w / 3), y + 11, opaque(color));
        graphics.fill(x + 12, y + 10, x + 14, y + 68, opaque(color));
        graphics.fill(x + w - Math.min(278, w / 3), y + 10, x + w - 12, y + 11, 0x5538DFF4);
        graphics.fill(x + w - 2, y + 46, x + w, y + h - 34, 0x442E8E9D);
        graphics.fill(x + 12, y + h - 18, x + Math.min(x + 250, x + w / 3), y + h - 17, 0x552E8E9D);
        graphics.fill(x + w - 18, y + 18, x + w, y + 34, 0x6602070C);
        graphics.fill(x + w - 34, y + 18, x + w - 18, y + 20, 0x6638DFF4);
    }

    public static void appShellBackdrop(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        TerminalThemeTokens tokens = tokens(context);
        Identifier texture = tokens.assets().shellBackdrop();
        boolean visuals = context == null || context.themeContext().visualAssets();
        boolean reducedMotion = context != null && context.themeContext().reducedMotion();
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        graphics.fill(0, 0, screenW, screenH, tokens.colors().background());
        if (visuals) {
            if (textureAvailable(texture)) {
                blitFitted(graphics, texture, 0, 0, screenW, screenH, ImageFit.COVER, TERMINAL_BACKDROP_ASPECT);
            }
            graphics.fill(0, 0, screenW, screenH,
                    reducedMotion ? (tokens.effects().overlayColor() | 0x0E000000) : tokens.effects().overlayColor());
            graphics.fill(0, 0, screenW, Math.max(80, screenH / 5), tokens.panels().headerFill());
            graphics.fill(0, screenH - Math.max(70, screenH / 6), screenW, screenH, tokens.colors().shell());
        }
        if (tokens.effects().grid()) {
            drawTerminalGrid(graphics, screenW, screenH, reducedMotion ? 34 : 28, tokens.dividers().gridLine());
        }
        graphics.fill(x, y, x + w, y + h, tokens.colors().shell());
        graphics.outline(x, y, w, h, tokens.borders().glow());
        graphics.fill(x + 1, y + 1, x + w - 1, y + 18, tokens.panels().headerFill());
        graphics.fill(x + 12, y + 10, x + Math.min(x + 278, x + w / 3), y + 11, opaque(color));
        graphics.fill(x + 12, y + 10, x + 14, y + 68, opaque(color));
        graphics.fill(x + w - Math.min(278, w / 3), y + 10, x + w - 12, y + 11, tokens.borders().normal());
        graphics.fill(x + w - 2, y + 46, x + w, y + h - 34, tokens.dividers().line());
        graphics.fill(x + 12, y + h - 18, x + Math.min(x + 250, x + w / 3), y + h - 17,
                tokens.dividers().line());
        graphics.fill(x + w - 18, y + 18, x + w, y + 34, tokens.colors().background());
        graphics.fill(x + w - 34, y + 18, x + w - 18, y + 20, tokens.borders().glow());
    }

    private static void drawTerminalGrid(GuiGraphicsExtractor graphics, int width, int height, int step) {
        drawTerminalGrid(graphics, width, height, step, 0x1D2E8E9D);
    }

    private static void drawTerminalGrid(GuiGraphicsExtractor graphics, int width, int height, int step, int grid) {
        for (int gx = 0; gx < width; gx += Math.max(12, step)) {
            graphics.fill(gx, 0, gx + 1, height, grid);
        }
        for (int gy = 0; gy < height; gy += Math.max(12, step)) {
            graphics.fill(0, gy, width, gy + 1, grid);
        }
        for (int gy = 2; gy < height; gy += 4) {
            graphics.fill(0, gy, width, gy + 1, 0x09000000);
        }
    }

    public static void topMetaBar(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String title, String subtitle, String meta, int color) {
        graphics.fill(x, y, x + w, y + 52, 0xDE020A10);
        graphics.fill(x + 12, y + 44, x + w - 12, y + 45, 0x3E38DFF4);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 14, 0x1C163843);
        hybridIconBadge(graphics, TerminalVisualAssets.ICON_BRAND_ECHO, TerminalIcon.CORE, x + 18, y + 8, 34, color, true);
        String online = meta == null || meta.isBlank() ? "LINK: STANDBY  |  USER: OPERATOR  |  ONLINE" : meta;
        boolean offline = online.toUpperCase().contains("OFFLINE");
        String rightSource = w < 640 ? (offline ? "OFFLINE" : "ONLINE") : online;
        int rightMax = Math.max(64, Math.min(w - 110, w < 640 ? 86 : w / 3));
        String right = trim(font, rightSource, rightMax);
        int rightColor = right.toUpperCase().contains("OFFLINE") ? RED : MUTED;
        int rightX = x + w - 26 - font.width(right);
        int textX = x + 58;
        int leftMax = Math.max(72, rightX - textX - 12);
        graphics.text(font, trim(font, title, leftMax), textX, y + 10, opaque(color), false);
        graphics.text(font, trim(font, subtitle, leftMax), textX, y + 27, MUTED, false);
        graphics.text(font, right, rightX, y + 22, rightColor, false);
        int dotColor = right.toUpperCase().contains("OFFLINE") ? RED : GREEN;
        graphics.fill(x + w - 18, y + 23, x + w - 12, y + 29, opaque(dotColor));
    }

    public static void topMetaBar(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String title, String subtitle, String meta, int color) {
        TerminalThemeTokens tokens = tokens(context);
        graphics.fill(x, y, x + w, y + 52, tokens.colors().shell());
        graphics.fill(x + 12, y + 44, x + w - 12, y + 45, tokens.dividers().majorLine());
        graphics.fill(x + 1, y + 1, x + w - 1, y + 14, tokens.panels().headerFill());
        hybridIconBadge(graphics,
                themedIcon(context, TerminalIconKey.theme("brand"), TerminalVisualAssets.ICON_BRAND_ECHO),
                TerminalIcon.CORE, x + 18, y + 8, 34, color, true);
        String online = meta == null || meta.isBlank() ? "LINK: STANDBY  |  USER: OPERATOR  |  ONLINE" : meta;
        boolean offline = online.toUpperCase().contains("OFFLINE");
        String rightSource = w < 640 ? (offline ? "OFFLINE" : "ONLINE") : online;
        int rightMax = Math.max(64, Math.min(w - 110, w < 640 ? 86 : w / 3));
        String right = trim(font, rightSource, rightMax);
        int rightColor = right.toUpperCase().contains("OFFLINE") ? tokens.colors().danger() : tokens.colors().muted();
        int rightX = x + w - 26 - font.width(right);
        int textX = x + 58;
        int leftMax = Math.max(72, rightX - textX - 12);
        graphics.text(font, trim(font, title, leftMax), textX, y + 10, opaque(color), false);
        graphics.text(font, trim(font, subtitle, leftMax), textX, y + 27, tokens.colors().muted(), false);
        graphics.text(font, right, rightX, y + 22, rightColor, false);
        int dotColor = right.toUpperCase().contains("OFFLINE") ? tokens.colors().danger() : tokens.colors().success();
        graphics.fill(x + w - 18, y + 23, x + w - 12, y + 29, opaque(dotColor));
    }

    public static void bottomShortcutBar(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String left, String right, int color) {
        graphics.fill(x, y, x + w, y + 30, 0xD8020A10);
        graphics.outline(x, y, w, 30, 0x2838DFF4);
        String r = trimBreadcrumb(font, right == null ? "" : right, Math.max(110, Math.min(420, w * 38 / 100)));
        int rightX = x + w - 16 - font.width(r);
        int leftLimit = Math.max(x + 80, rightX - 18);
        int cx = x + 14;
        String[] tokens = left == null ? new String[0] : left.split("\\s{2,}");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int space = token.indexOf(' ');
            String key = space <= 0 ? token : token.substring(0, space);
            String label = space <= 0 ? "" : token.substring(space + 1);
            int keyW = Math.max(22, font.width(key) + 8);
            if (cx + keyW + font.width(label) + 18 > leftLimit) {
                break;
            }
            graphics.fill(cx, y + 7, cx + keyW, y + 23, 0x88071117);
            graphics.outline(cx, y + 7, keyW, 16, 0x3438DFF4);
            graphics.centeredText(font, trim(font, key, keyW - 4), cx + keyW / 2, y + 12, opaque(color));
            cx += keyW + 7;
            if (!label.isBlank()) {
                String shortLabel = trim(font, label, 82);
                graphics.text(font, shortLabel, cx, y + 12, MUTED, false);
                cx += font.width(shortLabel) + 18;
            }
        }
        graphics.text(font, r, rightX, y + 12, opaque(color), false);
    }

    public static void bottomShortcutBar(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String left, String right, int color) {
        TerminalThemeTokens tokens = tokens(context);
        graphics.fill(x, y, x + w, y + 30, tokens.colors().shell());
        graphics.outline(x, y, w, 30, tokens.borders().subtle());
        String r = trimBreadcrumb(font, right == null ? "" : right, Math.max(110, Math.min(420, w * 38 / 100)));
        int rightX = x + w - 16 - font.width(r);
        int leftLimit = Math.max(x + 80, rightX - 18);
        int cx = x + 14;
        String[] parts = left == null ? new String[0] : left.split("\\s{2,}");
        for (String token : parts) {
            if (token.isBlank()) {
                continue;
            }
            int space = token.indexOf(' ');
            String key = space <= 0 ? token : token.substring(0, space);
            String label = space <= 0 ? "" : token.substring(space + 1);
            int keyW = Math.max(22, font.width(key) + 8);
            if (cx + keyW + font.width(label) + 18 > leftLimit) {
                break;
            }
            graphics.fill(cx, y + 7, cx + keyW, y + 23, tokens.colors().row());
            graphics.outline(cx, y + 7, keyW, 16, tokens.borders().normal());
            graphics.centeredText(font, trim(font, key, keyW - 4), cx + keyW / 2, y + 12, opaque(color));
            cx += keyW + 7;
            if (!label.isBlank()) {
                String shortLabel = trim(font, label, 82);
                graphics.text(font, shortLabel, cx, y + 12, tokens.colors().muted(), false);
                cx += font.width(shortLabel) + 18;
            }
        }
        graphics.text(font, r, rightX, y + 12, opaque(color), false);
    }

    public static void iconRailButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, boolean selected, boolean hovered, int color) {
        iconRailButton(graphics, font, x, y, w, h, icon, null, label, selected, hovered, color);
    }

    public static void iconRailButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label,
            boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xD80B3440 : hovered ? 0x880D2530 : 0x52071117;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : 0x2C4DBAF4);
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
        }
        drawHybridIcon(graphics, texture, icon, x + 9, y + 8, 24, color, selected);
        graphics.text(font, trim(font, label, w - 46), x + 42, y + Math.max(8, (h - 8) / 2),
                selected ? TEXT : MUTED, false);
    }

    public static void iconRailButton(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label,
            boolean selected, boolean hovered, int color) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill() : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : tokens.borders().subtle());
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
        }
        drawHybridIcon(graphics, texture, icon, x + 9, y + 8, 24, selected ? color : tokens.colors().accentDim(), selected);
        graphics.text(font, trim(font, label, w - 46), x + 42, y + Math.max(8, (h - 8) / 2),
                selected ? tokens.colors().text() : tokens.colors().muted(), false);
    }

    public static void pageRailButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, String summary,
            boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xEE0B3440 : hovered ? 0xAA0D2530 : 0x77071117;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : 0x334DBAF4);
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
        }
        icon.draw(graphics, x + 8, y + 7, 20, color, selected);
        graphics.text(font, trim(font, label, w - 42), x + 34, y + 7, selected ? TEXT : MUTED, false);
        if (summary != null && !summary.isBlank() && h >= 38) {
            graphics.text(font, trim(font, summary, w - 42), x + 34, y + 20, MUTED, false);
        }
    }

    public static void commandStackPanel(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xCF050B10);
        graphics.fill(x, y, x + w, y + 34, 0x5E071923);
        graphics.outline(x, y, w, h, 0x6238DFF4);
        graphics.fill(x, y, x + 3, y + h, opaque(color));
        graphics.fill(x, y, x + Math.max(48, w * 2 / 3), y + 2, opaque(color));
        graphics.fill(x + w - 2, y + 34, x + w, y + h - 16, 0x552E8E9D);
        graphics.fill(x + 8, y + 30, x + w - 8, y + 31, 0x332E8E9D);
        String title = w < 190 ? "ECHO" : "ECHO BUS";
        String subtitle = w < 190 ? "CHANNELS" : "SELECT SIGNAL CHANNEL";
        graphics.text(font, trim(font, title, w - 28), x + 12, y + 8, CYAN, false);
        graphics.text(font, trim(font, subtitle, w - 28), x + 12, y + 21, MUTED, false);
    }

    public static void commandStackPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, int color) {
        TerminalThemeTokens tokens = tokens(context);
        drawPanelTexture(context, graphics, tokens.assets().panelPlate(), x, y, w, h, tokens.panels().imageDarken());
        graphics.fill(x, y, x + w, y + h, tokens.colors().content());
        graphics.fill(x, y, x + w, y + 34, tokens.panels().headerFill());
        graphics.outline(x, y, w, h, tokens.borders().normal());
        graphics.fill(x, y, x + 3, y + h, opaque(color));
        graphics.fill(x, y, x + Math.max(48, w * 2 / 3), y + 2, opaque(color));
        graphics.fill(x + w - 2, y + 34, x + w, y + h - 16, tokens.dividers().line());
        graphics.fill(x + 8, y + 30, x + w - 8, y + 31, tokens.dividers().gridLine());
        String title = w < 190 ? "ECHO" : "ECHO BUS";
        String subtitle = w < 190 ? "CHANNELS" : "SELECT SIGNAL CHANNEL";
        graphics.text(font, trim(font, title, w - 28), x + 12, y + 8, tokens.colors().accent(), false);
        graphics.text(font, trim(font, subtitle, w - 28), x + 12, y + 21, tokens.colors().muted(), false);
    }

    public static void commandStackGroupLabel(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String label, boolean selected, int color) {
        int accent = selected ? opaque(color) : CYAN_DIM;
        graphics.text(font, trim(font, label.toUpperCase(), w - 18), x + 8, y + 3,
                selected ? accent : MUTED, false);
        graphics.fill(x + 8, y + 13, x + Math.max(x + 38, x + Math.min(w - 10, 116)), y + 14,
                selected ? accent : 0x442E8E9D);
    }

    public static void commandStackGroupButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, boolean selected, boolean hovered, int color) {
        commandStackGroupButton(graphics, font, x, y, w, h, icon, null, label, selected, hovered, color);
    }

    public static void commandStackGroupButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label,
            boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xC40C3340 : hovered ? 0x76102630 : 0x46071117;
        int border = selected ? 0xCC66E8FF : hovered ? 0x4A38DFF4 : 0x1E38DFF4;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : CYAN_DIM);
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(24, w / 3), y + h - 4, x + w - 8, y + h - 2, 0x5CE9FBFF);
            graphics.fill(x, y + 1, x + w, y + Math.min(h - 1, Math.max(3, h / 2)), 0x1066E8FF);
        }
        int iconSize = Math.min(22, Math.max(16, h - 8));
        drawHybridIcon(graphics, texture, icon, x + 7, y + Math.max(3, (h - iconSize) / 2), iconSize,
                selected ? color : CYAN_DIM, selected);
        graphics.text(font, trim(font, label.toUpperCase(), w - 42), x + 34,
                y + Math.max(5, (h - 8) / 2), selected ? TEXT : MUTED, false);
    }

    public static void commandStackGroupButton(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label,
            boolean selected, boolean hovered, int color) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill() : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        int border = selected ? tokens.borders().selected() : hovered ? tokens.borders().normal() : tokens.borders().subtle();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : tokens.colors().accentDim());
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(24, w / 3), y + h - 4, x + w - 8, y + h - 2, tokens.borders().strong());
            graphics.fill(x, y + 1, x + w, y + Math.min(h - 1, Math.max(3, h / 2)), tokens.effects().glowColor());
        }
        int iconSize = Math.min(22, Math.max(16, h - 8));
        drawHybridIcon(graphics, texture, icon, x + 7, y + Math.max(3, (h - iconSize) / 2), iconSize,
                selected ? color : tokens.colors().accentDim(), selected);
        graphics.text(font, trim(font, label.toUpperCase(), w - 42), x + 34,
                y + Math.max(5, (h - 8) / 2), selected ? tokens.colors().text() : tokens.colors().muted(), false);
    }

    public static void commandPageButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, String summary,
            boolean selected, boolean hovered, int color) {
        commandPageButton(graphics, font, x, y, w, h, icon, null, label, summary, selected, hovered, color);
    }

    public static void commandPageButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label, String summary,
            boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xBE0B3440 : hovered ? 0x760D2530 : 0x3E071117;
        int border = selected ? 0xCC66E8FF : hovered ? 0x4A38DFF4 : 0x1E38DFF4;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(26, w / 3), y + h - 4, x + w - 10, y + h - 2, 0x5CE9FBFF);
            graphics.fill(x, y + 1, x + w, y + Math.min(h - 1, Math.max(3, h / 2)), 0x1066E8FF);
        } else if (hovered) {
            graphics.fill(x, y + h - 1, x + w, y + h, 0x5538DFF4);
        }
        drawHybridIcon(graphics, texture, icon, x + 7, y + Math.max(4, (h - 20) / 2),
                Math.min(24, Math.max(16, h - 10)), selected ? color : CYAN_DIM, selected);
        graphics.text(font, trim(font, label, w - 42), x + 34, y + Math.max(5, h >= 30 ? 6 : (h - 8) / 2),
                selected ? TEXT : MUTED, false);
        if (h >= 32 && summary != null && !summary.isBlank()) {
            graphics.text(font, trim(font, summary, w - 42), x + 34, y + 19,
                    selected ? MUTED : 0xFF6F8793, false);
        }
    }

    public static void commandPageButton(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, Identifier texture, String label, String summary,
            boolean selected, boolean hovered, int color) {
        TerminalThemeTokens tokens = tokens(context);
        int bg = selected ? tokens.panels().selectedFill() : hovered ? tokens.panels().hoverFill() : tokens.colors().row();
        int border = selected ? tokens.borders().selected() : hovered ? tokens.borders().normal() : tokens.borders().subtle();
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(26, w / 3), y + h - 4, x + w - 10, y + h - 2, tokens.borders().strong());
            graphics.fill(x, y + 1, x + w, y + Math.min(h - 1, Math.max(3, h / 2)), tokens.effects().glowColor());
        } else if (hovered) {
            graphics.fill(x, y + h - 1, x + w, y + h, tokens.borders().normal());
        }
        drawHybridIcon(graphics, texture, icon, x + 7, y + Math.max(4, (h - 20) / 2),
                Math.min(24, Math.max(16, h - 10)), selected ? color : tokens.colors().accentDim(), selected);
        graphics.text(font, trim(font, label, w - 42), x + 34, y + Math.max(5, h >= 30 ? 6 : (h - 8) / 2),
                selected ? tokens.colors().text() : tokens.colors().muted(), false);
        if (h >= 32 && summary != null && !summary.isBlank()) {
            graphics.text(font, trim(font, summary, w - 42), x + 34, y + 19,
                    selected ? tokens.colors().muted() : tokens.output().mutedColor(), false);
        }
    }

    public static void diagnosticRail(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, boolean online, int color) {
        graphics.fill(x, y, x + w, y + h, 0x66071117);
        graphics.outline(x, y, w, h, 0x2238DFF4);
        graphics.text(font, trim(font, "ECHO BUS", w - 20), x + 10, y + 7, CYAN_DIM, false);
        int stateColor = online ? GREEN : RED;
        graphics.text(font, online ? "ONLINE" : "OFFLINE", x + 10, y + 20, stateColor, false);
        progress(graphics, x + 10, y + h - 12, Math.max(28, w - 20), 5, online ? 0.82F : 0.12F, color);
    }

    public static void diagnosticRail(TerminalRenderContext context, GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, boolean online, int color) {
        TerminalThemeTokens tokens = tokens(context);
        graphics.fill(x, y, x + w, y + h, tokens.colors().row());
        graphics.outline(x, y, w, h, tokens.borders().subtle());
        graphics.text(font, trim(font, "ECHO BUS", w - 20), x + 10, y + 7, tokens.colors().accentDim(), false);
        int stateColor = online ? tokens.colors().success() : tokens.colors().danger();
        graphics.text(font, online ? "ONLINE" : "OFFLINE", x + 10, y + 20, stateColor, false);
        progress(context, graphics, x + 10, y + h - 12, Math.max(28, w - 20), 5,
                online ? 0.82F : 0.12F, color);
    }

    public static void cinematicContentFrame(GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0x5E050B10);
        graphics.outline(x, y, w, h, 0x3438DFF4);
        graphics.fill(x, y, x + Math.max(44, Math.min(w, w / 7)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(36, Math.min(w, w / 8)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 46), 0x4A38DFF4);
        graphics.fill(x + w - Math.min(w, 60), y + h - 2, x + w, y + h, 0x3438DFF4);
    }

    public static int dashboardCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String title, int color) {
        drawPanelTexture(context, graphics, chapterPanel(context), x, y, w, h, tokens(context).panels().imageDarken());
        graphics.fill(x, y, x + w, y + h, tokens(context).panels().elevatedFill());
        graphics.outline(x, y, w, h, tokens(context).borders().normal());
        graphics.fill(x, y, x + Math.max(28, Math.min(w, w / 5)), y + 2, opaque(color));
        line(context, graphics, title, x + 8, y + 7, w - 16, color);
        return y + 22;
    }

    public static int heroCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.56F, true);
        line(context, graphics, title, x + 10, y + 10, w - 20, text(context));
        wrap(context, graphics, detail, x + 10, y + h - 34, w - 20, text(context));
        return y + h + 10;
    }

    public static int metricTile(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, String value, int color) {
        graphics.fill(x, y, x + w, y + 34, tokens(context).colors().row());
        graphics.outline(x, y, w, 34, tokens(context).borders().subtle());
        line(context, graphics, label, x + 7, y + 6, w - 14, muted(context));
        line(context, graphics, value, x + 7, y + 19, w - 14, color);
        return y + 38;
    }

    public static int objectiveRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, String detail, boolean complete, int color) {
        graphics.fill(x, y, x + w, y + 28, tokens(context).colors().row());
        graphics.outline(x, y, w, 28, complete ? withAlpha(success(context), 0x88) : tokens(context).borders().subtle());
        graphics.fill(x + 8, y + 8, x + 14, y + 14, complete ? success(context) : tokens(context).colors().rowSelected());
        line(context, graphics, label, x + 22, y + 5, w - 30, complete ? success(context) : text(context));
        line(context, graphics, detail, x + 22, y + 16, w - 30, muted(context));
        return y + 32;
    }

    public static void rewardTile(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            ItemStack stack, int x, int y, int w, String label, int color, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + 42, tokens(context).colors().row());
        graphics.outline(x, y, w, 42, tokens(context).borders().subtle());
        itemSlot(context, graphics, stack, x + 6, y + 6, color, inside(mouseX, mouseY, x + 6, y + 6, 20, 20));
        line(context, graphics, label, x + 32, y + 11, w - 38, text(context));
    }

    public static void missionCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail,
            String status, int color, boolean selected, boolean hovered) {
        if (texture != null) {
            imagePanel(context, graphics, texture, x, y, w, h, color, selected ? 0.64F : 0.78F, false);
        } else {
            graphics.fill(x, y, x + w, y + h, selected ? tokens(context).panels().selectedFill()
                    : hovered ? tokens(context).panels().hoverFill() : tokens(context).colors().row());
        }
        graphics.outline(x, y, w, h, selected ? opaque(color) : tokens(context).borders().subtle());
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : accentDim(context));
        line(context, graphics, title, x + 9, y + 7, w - 92, selected ? text(context) : muted(context));
        line(context, graphics, detail, x + 9, y + 20, w - 92, muted(context));
        miniStatusPill(context, graphics, status, x + w - 78, y + 8, 68, color, selected);
    }

    public static void actionButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, boolean enabled, boolean hovered) {
        button(context, graphics, x, y, w, label, color, enabled, hovered);
    }

    public static void dangerButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, boolean enabled, boolean hovered) {
        button(context, graphics, x, y, w, label, RED, enabled, hovered);
    }

    public static void sortDropdownLikeChip(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String label, int color) {
        graphics.fill(x, y, x + w, y + 16, 0xAA071017);
        graphics.outline(x, y, w, 16, 0x334DBAF4);
        graphics.text(font, trim(font, label, w - 16), x + 7, y + 5, MUTED, false);
        graphics.fill(x + w - 10, y + 7, x + w - 4, y + 8, opaque(color));
    }

    public static void scrollbar(GuiGraphicsExtractor graphics, int x, int y, int h, int scroll, int maxScroll, int color) {
        if (maxScroll <= 0 || h <= 16) {
            return;
        }
        graphics.fill(x, y, x + 3, y + h, 0x55244352);
        int thumbH = Math.max(18, h * h / (h + maxScroll));
        int thumbY = y + Math.round((h - thumbH) * (scroll / (float) maxScroll));
        graphics.fill(x, thumbY, x + 3, thumbY + thumbH, opaque(color));
    }

    public static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    public static String trim(TerminalRenderContext context, String text, int maxWidth) {
        return trim(font(context), text, maxWidth);
    }

    public static String trim(Font font, String text, int maxWidth) {
        return TerminalRenderCache.current().trim(font, text, maxWidth);
    }

    private static String trimBreadcrumb(Font font, String text, int maxWidth) {
        String value = text == null ? "" : text.strip();
        if (font.width(value) <= maxWidth) {
            return value;
        }
        int divider = value.indexOf('|');
        String prefix = divider >= 0 ? value.substring(0, divider + 1).strip() + " " : "";
        String path = divider >= 0 ? value.substring(divider + 1).strip() : value;
        String[] parts = path.split("\\s*/\\s*");
        if (parts.length > 1) {
            for (int keep = Math.min(3, parts.length); keep >= 1; keep--) {
                StringBuilder candidate = new StringBuilder(prefix).append("... / ");
                for (int i = parts.length - keep; i < parts.length; i++) {
                    if (i > parts.length - keep) {
                        candidate.append(" / ");
                    }
                    candidate.append(parts[i]);
                }
                String compact = candidate.toString();
                if (font.width(compact) <= maxWidth) {
                    return compact;
                }
            }
        }
        return trim(font, value, maxWidth);
    }

    private static String semanticName(String value) {
        String cleaned = value == null ? "" : value.strip().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return cleaned.isBlank() ? "unknown" : cleaned;
    }

    public static int wrappedHeight(TerminalRenderContext context, String text, int maxWidth) {
        return TerminalRenderCache.current().wrappedHeight(font(context), text, maxWidth);
    }

    public static int cardHeight(TerminalRenderContext context, String detail, int width) {
        return Math.max(54, 42 + wrappedHeight(context, detail, width - 14));
    }

    public static int listHeight(int rows, int rowHeight, int headingHeight) {
        return headingHeight + Math.max(0, rows) * rowHeight;
    }

    public static int clampScroll(int value, int contentHeight, int viewportHeight) {
        return Math.max(0, Math.min(value, Math.max(0, contentHeight - viewportHeight)));
    }

    public static int opaque(int color) {
        return (color >>> 24) == 0 ? 0xFF000000 | color : color;
    }

    private static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    private static Font font(TerminalRenderContext context) {
        return context.minecraft().font;
    }

    private static void drawHybridIcon(GuiGraphicsExtractor graphics, Identifier texture, TerminalIcon fallback,
            int x, int y, int size, int color, boolean active) {
        if (textureAvailable(texture) && size > 8) {
            graphics.blit(texture, x, y, x + size, y + size, 0.0F, 1.0F, 0.0F, 1.0F);
        } else {
            (fallback == null ? TerminalIcon.DEFAULT : fallback).draw(graphics, x, y, size, color, active);
        }
    }

    private static void drawCommandLabel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label, Identifier texture, int iconColor, int textColor, boolean active) {
        boolean drawIcon = textureAvailable(texture) && w >= 70 && h >= 16;
        int iconSize = Math.min(16, Math.max(12, h - 8));
        int textMax = drawIcon ? Math.max(28, w - 42) : w - 18;
        String trimmed = trim(context, label, textMax);
        int textY = y + Math.max(5, (h - 8) / 2);
        if (drawIcon) {
            drawHybridIcon(graphics, texture, TerminalIcon.DEFAULT, x + 10, y + Math.max(3, (h - iconSize) / 2),
                    iconSize, iconColor, active);
            int centered = x + w / 2 - font(context).width(trimmed) / 2 + 8;
            int textX = Math.max(x + 32, Math.min(centered, x + w - 8 - font(context).width(trimmed)));
            graphics.text(font(context), trimmed, textX, textY, textColor, false);
        } else {
            graphics.centeredText(font(context), trimmed, x + w / 2, textY, textColor);
        }
    }

    private static boolean textureAvailable(Identifier texture) {
        if (texture == null) {
            return false;
        }
        try {
            return Minecraft.getInstance().getResourceManager().getResource(texture).isPresent();
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }
}
