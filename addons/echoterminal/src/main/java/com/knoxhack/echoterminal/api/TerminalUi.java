package com.knoxhack.echoterminal.api;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class TerminalUi {
    public static final int CYAN = 0xFF66E8FF;
    public static final int CYAN_DIM = 0xFF2E8E9D;
    public static final int TEXT = 0xFFE9FBFF;
    public static final int MUTED = 0xFF8CA7B5;
    public static final int GREEN = 0xFF92F7A6;
    public static final int AMBER = 0xFFFFD166;
    public static final int RED = 0xFFFF8FA3;
    public static final int PANEL = 0x6610242F;
    public static final int PANEL_DARK = 0xB6050D14;
    public static final int ROW = 0xFF0D171F;
    public static final int ROW_SELECTED = 0xFF123241;

    private TerminalUi() {
    }

    public static void section(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String text, int x, int y, int color) {
        graphics.text(font(context), trim(context, text, Math.max(40, context.contentX() + context.contentWidth() - x)),
                x, y, opaque(color), true);
    }

    public static int sectionHeader(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String title, String detail, int x, int y, int width, int color) {
        line(context, graphics, title, x, y, width, color);
        if (detail != null && !detail.isBlank()) {
            line(context, graphics, detail, x + Math.min(width - 24, Math.max(72, width / 3)), y,
                    Math.max(24, width - Math.max(72, width / 3)), MUTED);
        }
        divider(graphics, x, y + 14, width, color);
        return y + 20;
    }

    public static void panel(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, PANEL_DARK);
        graphics.outline(x, y, w, h, 0x55244352);
    }

    public static void densePanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xBB071017);
        graphics.outline(x, y, w, h, 0x44244352);
        graphics.fill(x, y, x + Math.max(18, Math.min(w, w / 5)), y + 1, opaque(color));
    }

    public static void cinematicPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0xE0050C13);
        graphics.fill(x + 1, y + 1, x + w - 1, y + Math.min(y + h - 1, y + 26), 0x22163843);
        graphics.outline(x, y, w, h, 0x7A2E8E9D);
        graphics.fill(x, y, x + Math.min(w, Math.max(42, w / 5)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.min(w, Math.max(36, w / 6)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(h, 32), 0xAA66E8FF);
        graphics.fill(x + w - Math.min(w, 38), y + h - 2, x + w, y + h, 0x7766E8FF);
        if (w > 48 && h > 44) {
            graphics.fill(x + 10, y + 10, x + w - 10, y + 11, 0x1E66E8FF);
            graphics.fill(x + 10, y + h - 11, x + w - 10, y + h - 10, 0x182E8E9D);
        }
    }

    public static void cinematicHeroPanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.76F, false);
        cinematicPanel(graphics, x, y, w, h, color);
        line(context, graphics, title, x + 10, y + 10, Math.max(40, w - 20), color);
        if (detail != null && !detail.isBlank()) {
            wrap(context, graphics, detail, x + 10, y + 25, Math.max(40, w - 20), TEXT);
        }
    }

    public static void imagePanel(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame) {
        imagePanel(graphics, texture, x, y, w, h, color, darken, frame);
    }

    public static void imagePanel(GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color, float darken, boolean frame) {
        if (texture == null) {
            fallbackVisualPanel(graphics, x, y, w, h, color);
        } else {
            graphics.blit(texture, x, y, x + w, y + h, 0.0F, 1.0F, 0.0F, 1.0F);
        }
        int alpha = Math.max(0, Math.min(230, Math.round(darken * 255.0F)));
        graphics.fill(x, y, x + w, y + h, (alpha << 24) | 0x071017);
        if (frame) {
            graphics.outline(x, y, w, h, 0x5538DFF4);
            graphics.fill(x, y, x + Math.max(22, Math.min(w, w / 4)), y + 2, opaque(color));
            graphics.fill(x, y + h - 2, x + Math.max(22, Math.min(w, w / 5)), y + h, opaque(color));
        }
    }

    public static int imageHero(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.62F, true);
        return y + h + 8;
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

    public static void chip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 13, 0xFF10232C);
        graphics.fill(x, y + 11, x + width, y + 13, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 3, opaque(color));
    }

    public static void statusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color, boolean selected) {
        int bg = selected ? ROW_SELECTED : 0xFF10232C;
        graphics.fill(x, y, x + width, y + 14, bg);
        graphics.outline(x, y, width, 14, selected ? opaque(color) : 0x5538DFF4);
        graphics.fill(x, y + 12, x + width, y + 14, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 4,
                selected ? TEXT : opaque(color));
    }

    public static void miniStatusPill(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            String label, int x, int y, int width, int color, boolean filled) {
        int bg = filled ? opaque(color) : 0xFF0D171F;
        int text = filled ? 0xFF061016 : opaque(color);
        graphics.fill(x, y, x + width, y + 12, bg);
        graphics.outline(x, y, width, 12, filled ? opaque(color) : 0x5538DFF4);
        graphics.fill(x, y + 10, x + width, y + 12, opaque(color));
        graphics.centeredText(font(context), trim(context, label, width - 6), x + width / 2, y + 3, text);
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
            graphics.fill(x + 1, y + 1, x + width - 1, y + Math.max(y + 2, y + height / 2), 0x3020024A);
            graphics.fill(x, y + height - 2, x + width, y + height, opaque(color));
            graphics.fill(x + width / 4, y + height - 4, x + width - width / 4, y + height - 2, 0xAAE9FBFF);
            graphics.fill(x, y + height, x + width, y + Math.min(y + height + 14, y + 48), 0x3300E5FF);
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
        densePanel(graphics, x, y, width, 30, color);
        line(context, graphics, title, x + 8, y + 6, Math.max(40, width / 2), color);
        if (detail != null && !detail.isBlank()) {
            String trimmed = trim(context, detail, Math.max(40, width / 2 - 10));
            graphics.text(font(context), trimmed, x + width - 8 - font(context).width(trimmed),
                    y + 6, MUTED, false);
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
        panel(graphics, x, y, width, height);
        line(context, graphics, label, x + 7, y + 7, width - 14, MUTED);
        line(context, graphics, value, x + 7, y + 22, width - 14, color);
        wrap(context, graphics, detail, x + 7, y + 37, width - 14, TEXT);
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
        densePanel(graphics, x, y, width, height, color);
        line(context, graphics, title, x + 6, y + 6, width - 12, MUTED);
        line(context, graphics, value, x + 6, y + 18, width - 12, color);
        wrap(context, graphics, detail, x + 6, y + 31, width - 12, TEXT);
        return height;
    }

    public static int commandStrip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 14);
        int height = Math.max(34, 24 + detailHeight);
        panel(graphics, x, y, width, height);
        line(context, graphics, title, x + 7, y + 7, width - 14, color);
        wrap(context, graphics, detail, x + 7, y + 20, width - 14, TEXT);
        return y + height + 5;
    }

    public static int compactCommandStrip(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int detailHeight = wrappedHeight(context, detail, width - 12);
        int height = Math.max(28, 18 + detailHeight);
        densePanel(graphics, x, y, width, height, color);
        line(context, graphics, title, x + 6, y + 5, width - 12, color);
        wrap(context, graphics, detail, x + 6, y + 17, width - 12, TEXT);
        return y + height + 4;
    }

    public static int emptyState(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int height = Math.max(42, 28 + wrappedHeight(context, detail, width - 16));
        panel(graphics, x, y, width, height);
        line(context, graphics, title, x + 8, y + 8, width - 16, color);
        wrap(context, graphics, detail, x + 8, y + 22, width - 16, MUTED);
        return y + height + 5;
    }

    public static int callout(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, String title, String detail, int color) {
        int bodyHeight = wrappedHeight(context, detail, width - 20);
        int height = Math.max(38, 25 + bodyHeight);
        graphics.fill(x, y, x + width, y + height, 0xBB0D171F);
        graphics.fill(x, y, x + 3, y + height, opaque(color));
        graphics.outline(x, y, width, height, 0x5538DFF4);
        line(context, graphics, title, x + 9, y + 7, width - 18, color);
        wrap(context, graphics, detail, x + 9, y + 20, width - 18, TEXT);
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
        graphics.text(font(context), trim(context, text, Math.max(20, maxWidth)), x, y, opaque(color), false);
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
                graphics.text(font(context), line, x, cy, opaque(color), false);
                cy += 11;
            }
        }
        return cy;
    }

    public static void button(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, boolean enabled, boolean hovered) {
        int bg = enabled ? (hovered ? 0xFF163343 : 0xFF10232C) : 0xFF11161A;
        int accent = enabled ? color : CYAN_DIM;
        graphics.fill(x, y, x + w, y + 18, bg);
        graphics.outline(x, y, w, 18, 0x5538DFF4);
        graphics.fill(x, y + 16, x + w, y + 18, accent);
        graphics.centeredText(font(context), trim(context, label, w - 8), x + w / 2, y + 5, enabled ? TEXT : MUTED);
    }

    public static void compactButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int color, boolean enabled, boolean hovered) {
        int bg = enabled ? (hovered ? 0xFF163343 : 0xFF0D1D25) : 0xFF0B1116;
        int accent = enabled ? color : CYAN_DIM;
        graphics.fill(x, y, x + w, y + 16, bg);
        graphics.outline(x, y, w, 16, enabled ? 0x5538DFF4 : 0x3338DFF4);
        graphics.fill(x, y + 14, x + w, y + 16, accent);
        graphics.centeredText(font(context), trim(context, label, w - 8), x + w / 2, y + 4,
                enabled ? TEXT : MUTED);
    }

    public static void primaryCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label, int color, boolean hovered) {
        int bg = hovered ? 0xEE15394A : 0xDD0B2530;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, opaque(color));
        graphics.fill(x, y + h - 3, x + w, y + h, opaque(color));
        graphics.fill(x + 4, y + 4, x + 7, y + h - 4, opaque(color));
        if (hovered && h > 18) {
            graphics.fill(x + 8, y + 3, x + w - 8, y + 4, 0x8866E8FF);
        }
        graphics.centeredText(font(context), trim(context, label, w - 18), x + w / 2,
                y + Math.max(5, (h - 8) / 2), TEXT);
    }

    public static void disabledCommandButton(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String label) {
        graphics.fill(x, y, x + w, y + h, 0x9A070D12);
        graphics.outline(x, y, w, h, 0x332E8E9D);
        graphics.fill(x + 4, y + 4, x + 7, y + h - 4, 0x55244352);
        graphics.fill(x, y + h - 2, x + w, y + h, 0x55244352);
        graphics.centeredText(font(context), trim(context, label, w - 18), x + w / 2,
                y + Math.max(5, (h - 8) / 2), MUTED);
    }

    public static void iconBadge(GuiGraphicsExtractor graphics, TerminalIcon icon,
            int x, int y, int size, int color, boolean active) {
        graphics.fill(x, y, x + size, y + size, 0xAA071017);
        graphics.outline(x, y, size, size, active ? opaque(color) : 0x5538DFF4);
        graphics.fill(x, y + size - 2, x + size, y + size, active ? opaque(color) : CYAN_DIM);
        icon.draw(graphics, x + 8, y + 8, Math.max(18, size - 16), color, active);
    }

    public static void iconTextureBadge(GuiGraphicsExtractor graphics, Identifier texture,
            int x, int y, int size, int color, boolean active) {
        graphics.fill(x, y, x + size, y + size, active ? 0xCC071017 : 0x99071117);
        graphics.outline(x, y, size, size, active ? opaque(color) : 0x5538DFF4);
        graphics.fill(x + 2, y + 2, x + size - 2, y + 4, active ? 0x5538DFF4 : 0x332E8E9D);
        graphics.fill(x + 2, y + size - 4, x + size - 2, y + size - 2,
                active ? opaque(color) : 0x552E8E9D);
        if (texture != null && size > 12) {
            int pad = Math.max(3, size / 10);
            graphics.blit(texture, x + pad, y + pad, x + size - pad, y + size - pad,
                    0.0F, 1.0F, 0.0F, 1.0F);
        }
    }

    public static int statusLineRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int width, TerminalIcon icon, String label, String value, int color) {
        icon.draw(graphics, x, y - 2, 14, color, false);
        line(context, graphics, label, x + 20, y, Math.max(24, width - 92), MUTED);
        line(context, graphics, value, x + Math.max(80, width - 84), y, Math.min(84, width / 3), color);
        return y + 18;
    }

    public static void progress(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float progress, int color) {
        int fill = Math.max(0, Math.min(w - 2, Math.round((w - 2) * Math.max(0.0F, Math.min(1.0F, progress)))));
        graphics.fill(x, y, x + w, y + h, 0xFF263842);
        graphics.fill(x + 1, y + 1, x + 1 + fill, y + h - 1, opaque(color));
    }

    public static void meter(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int value, int color) {
        int clamped = Math.max(0, Math.min(100, value));
        line(context, graphics, label + " " + clamped + "%", x, y, Math.max(20, w), TEXT);
        progress(graphics, x + 118, y + 2, Math.max(50, w - 118), 8, clamped / 100.0F, color);
    }

    public static void compactMeter(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, int value, int color) {
        int clamped = Math.max(0, Math.min(100, value));
        line(context, graphics, label + " " + clamped + "%", x, y, Math.max(20, w), TEXT);
        progress(graphics, x, y + 12, Math.max(36, w), 6, clamped / 100.0F, color);
    }

    public static void itemSlot(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            ItemStack stack, int x, int y, int color, boolean hovered) {
        graphics.fill(x, y, x + 20, y + 20, 0xFF0D171F);
        graphics.outline(x, y, 20, 20, 0x5538DFF4);
        if (stack != null && !stack.isEmpty()) {
            graphics.item(stack, x + 2, y + 2);
            graphics.itemDecorations(font(context), stack, x + 2, y + 2);
            if (hovered) {
                graphics.outline(x, y, 20, 20, opaque(color));
                graphics.setTooltipForNextFrame(font(context), stack, x + 10, y + 10);
            }
        } else {
            graphics.fill(x + 5, y + 9, x + 15, y + 11, CYAN_DIM);
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
            graphics.blit(texture, 0, 0, screenW, screenH, 0.0F, 1.0F, 0.0F, 1.0F);
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

    private static void drawTerminalGrid(GuiGraphicsExtractor graphics, int width, int height, int step) {
        int grid = 0x1D2E8E9D;
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
        graphics.fill(x, y, x + w, y + 52, 0xE2020A10);
        graphics.fill(x + 12, y + 44, x + w - 12, y + 45, 0x5538DFF4);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 14, 0x26163843);
        TerminalIcon.CORE.draw(graphics, x + 20, y + 10, 30, color, true);
        graphics.text(font, trim(font, title, Math.max(120, w / 2)), x + 58, y + 10, opaque(color), false);
        graphics.text(font, trim(font, subtitle, Math.max(120, w / 2)), x + 58, y + 27, MUTED, false);
        String online = meta == null || meta.isBlank() ? "LINK: STANDBY  |  USER: OPERATOR  |  ONLINE" : meta;
        String right = trim(font, online, Math.max(120, w / 3));
        int rightColor = right.toUpperCase().contains("OFFLINE") ? RED : MUTED;
        graphics.text(font, right, x + w - 18 - font.width(right), y + 22, rightColor, false);
    }

    public static void bottomShortcutBar(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String left, String right, int color) {
        graphics.fill(x, y, x + w, y + 30, 0xDD020A10);
        graphics.outline(x, y, w, 30, 0x4438DFF4);
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
            if (cx + keyW + font.width(label) + 18 > x + w - 250) {
                break;
            }
            graphics.fill(cx, y + 7, cx + keyW, y + 23, 0xAA071017);
            graphics.outline(cx, y + 7, keyW, 16, 0x5538DFF4);
            graphics.centeredText(font, trim(font, key, keyW - 4), cx + keyW / 2, y + 12, opaque(color));
            cx += keyW + 7;
            if (!label.isBlank()) {
                graphics.text(font, trim(font, label, 92), cx, y + 12, MUTED, false);
                cx += Math.min(92, font.width(label)) + 18;
            }
        }
        String r = trim(font, right == null ? "" : right, Math.max(80, w / 4));
        graphics.text(font, r, x + w - 16 - font.width(r), y + 12, opaque(color), false);
    }

    public static void iconRailButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xEE0B3440 : hovered ? 0xAA0D2530 : 0x66071117;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, selected ? opaque(color) : 0x334DBAF4);
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
        }
        icon.draw(graphics, x + 9, y + 8, 24, color, selected);
        graphics.text(font, trim(font, label, w - 46), x + 42, y + Math.max(8, (h - 8) / 2),
                selected ? TEXT : MUTED, false);
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
        graphics.fill(x, y, x + w, y + h, 0xD7050B10);
        graphics.fill(x, y, x + w, y + 34, 0x7A071923);
        graphics.outline(x, y, w, h, 0x7838DFF4);
        graphics.fill(x, y, x + 3, y + h, opaque(color));
        graphics.fill(x, y, x + Math.max(48, w * 2 / 3), y + 2, opaque(color));
        graphics.fill(x + w - 2, y + 34, x + w, y + h - 16, 0x552E8E9D);
        graphics.fill(x + 8, y + 30, x + w - 8, y + 31, 0x332E8E9D);
        String title = w < 190 ? "ECHO" : "ECHO BUS";
        String subtitle = w < 190 ? "CHANNELS" : "SELECT SIGNAL CHANNEL";
        graphics.text(font, trim(font, title, w - 28), x + 12, y + 8, CYAN, false);
        graphics.text(font, trim(font, subtitle, w - 28), x + 12, y + 21, MUTED, false);
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
        int bg = selected ? 0xDE0C3340 : hovered ? 0x99102630 : 0x66071117;
        int border = selected ? opaque(color) : hovered ? 0x6638DFF4 : 0x2638DFF4;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : CYAN_DIM);
        if (selected) {
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(24, w / 3), y + h - 4, x + w - 8, y + h - 2, 0x99E9FBFF);
            graphics.fill(x, y + 1, x + w, y + Math.min(y + h - 1, y + Math.max(3, h / 2)), 0x1E66E8FF);
        }
        int iconSize = Math.min(18, Math.max(14, h - 10));
        icon.draw(graphics, x + 8, y + Math.max(4, (h - iconSize) / 2), iconSize,
                selected ? color : CYAN_DIM, selected);
        graphics.text(font, trim(font, label.toUpperCase(), w - 40), x + 32,
                y + Math.max(5, (h - 8) / 2), selected ? TEXT : MUTED, false);
    }

    public static void commandPageButton(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, int h, TerminalIcon icon, String label, String summary,
            boolean selected, boolean hovered, int color) {
        int bg = selected ? 0xE50B3440 : hovered ? 0xA00D2530 : 0x54071117;
        int border = selected ? opaque(color) : hovered ? 0x6638DFF4 : 0x2638DFF4;
        graphics.fill(x, y, x + w, y + h, bg);
        graphics.outline(x, y, w, h, border);
        if (selected) {
            graphics.fill(x, y, x + 3, y + h, opaque(color));
            graphics.fill(x, y + h - 2, x + w, y + h, opaque(color));
            graphics.fill(x + Math.max(26, w / 3), y + h - 4, x + w - 10, y + h - 2, 0x99E9FBFF);
            graphics.fill(x, y + 1, x + w, y + Math.min(y + h - 1, y + Math.max(3, h / 2)), 0x1E66E8FF);
        } else if (hovered) {
            graphics.fill(x, y + h - 1, x + w, y + h, 0x7738DFF4);
        }
        icon.draw(graphics, x + 8, y + Math.max(5, (h - 18) / 2), Math.min(20, Math.max(14, h - 12)),
                selected ? color : CYAN_DIM, selected);
        graphics.text(font, trim(font, label, w - 42), x + 34, y + Math.max(5, h >= 30 ? 6 : (h - 8) / 2),
                selected ? TEXT : MUTED, false);
        if (h >= 32 && summary != null && !summary.isBlank()) {
            graphics.text(font, trim(font, summary, w - 42), x + 34, y + 19,
                    selected ? MUTED : 0xFF6F8793, false);
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

    public static void cinematicContentFrame(GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, 0x74050B10);
        graphics.outline(x, y, w, h, 0x5538DFF4);
        graphics.fill(x, y, x + Math.max(44, Math.min(w, w / 7)), y + 2, opaque(color));
        graphics.fill(x, y + h - 2, x + Math.max(x + 36, x + Math.min(w, w / 8)), y + h, opaque(color));
        graphics.fill(x + w - 2, y, x + w, y + Math.min(y + h, y + 46), 0x8838DFF4);
        graphics.fill(x + w - Math.min(w, 60), y + h - 2, x + w, y + h, 0x5538DFF4);
    }

    public static int dashboardCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, int h, String title, int color) {
        graphics.fill(x, y, x + w, y + h, 0xCC071017);
        graphics.outline(x, y, w, h, 0x5538DFF4);
        graphics.fill(x, y, x + Math.max(28, Math.min(w, w / 5)), y + 2, opaque(color));
        line(context, graphics, title, x + 8, y + 7, w - 16, color);
        return y + 22;
    }

    public static int heroCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail, int color) {
        imagePanel(context, graphics, texture, x, y, w, h, color, 0.56F, true);
        line(context, graphics, title, x + 10, y + 10, w - 20, TEXT);
        wrap(context, graphics, detail, x + 10, y + h - 34, w - 20, TEXT);
        return y + h + 10;
    }

    public static int metricTile(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, String value, int color) {
        graphics.fill(x, y, x + w, y + 34, 0xAA071017);
        graphics.outline(x, y, w, 34, 0x3338DFF4);
        line(context, graphics, label, x + 7, y + 6, w - 14, MUTED);
        line(context, graphics, value, x + 7, y + 19, w - 14, color);
        return y + 38;
    }

    public static int objectiveRow(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            int x, int y, int w, String label, String detail, boolean complete, int color) {
        graphics.fill(x, y, x + w, y + 28, 0x99071117);
        graphics.outline(x, y, w, 28, complete ? 0x5592F7A6 : 0x3338DFF4);
        graphics.fill(x + 8, y + 8, x + 14, y + 14, complete ? GREEN : 0xFF263842);
        line(context, graphics, label, x + 22, y + 5, w - 30, complete ? GREEN : TEXT);
        line(context, graphics, detail, x + 22, y + 16, w - 30, MUTED);
        return y + 32;
    }

    public static void rewardTile(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            ItemStack stack, int x, int y, int w, String label, int color, int mouseX, int mouseY) {
        graphics.fill(x, y, x + w, y + 42, 0x99071117);
        graphics.outline(x, y, w, 42, 0x3338DFF4);
        itemSlot(context, graphics, stack, x + 6, y + 6, color, inside(mouseX, mouseY, x + 6, y + 6, 20, 20));
        line(context, graphics, label, x + 32, y + 11, w - 38, TEXT);
    }

    public static void missionCard(TerminalRenderContext context, GuiGraphicsExtractor graphics,
            Identifier texture, int x, int y, int w, int h, String title, String detail,
            String status, int color, boolean selected, boolean hovered) {
        if (texture != null) {
            imagePanel(context, graphics, texture, x, y, w, h, color, selected ? 0.64F : 0.78F, false);
        } else {
            graphics.fill(x, y, x + w, y + h, hovered ? 0xFF102630 : ROW);
        }
        graphics.outline(x, y, w, h, selected ? opaque(color) : 0x3338DFF4);
        graphics.fill(x, y, x + 3, y + h, selected ? opaque(color) : CYAN_DIM);
        line(context, graphics, title, x + 9, y + 7, w - 92, selected ? TEXT : MUTED);
        line(context, graphics, detail, x + 9, y + 20, w - 92, MUTED);
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

    public static void searchBox(GuiGraphicsExtractor graphics, Font font,
            int x, int y, int w, String placeholder, int color) {
        graphics.fill(x, y, x + w, y + 16, 0xAA071017);
        graphics.outline(x, y, w, 16, 0x334DBAF4);
        graphics.text(font, trim(font, placeholder, w - 28), x + 8, y + 5, MUTED, false);
        TerminalIcon.SEARCH.draw(graphics, x + w - 20, y + 2, 12, color, false);
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

    private static Font font(TerminalRenderContext context) {
        return context.minecraft().font;
    }
}
