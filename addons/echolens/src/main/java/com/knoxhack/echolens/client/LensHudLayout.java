package com.knoxhack.echolens.client;

import java.util.ArrayList;
import java.util.List;

public final class LensHudLayout {
    public static final int SCREEN_PADDING = 8;

    private LensHudLayout() {
    }

    public static Bounds clampPanel(int preferredX, int preferredY, int width, int height, int screenWidth,
            int screenHeight) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int maxX = Math.max(SCREEN_PADDING, screenWidth - safeWidth - SCREEN_PADDING);
        int maxY = Math.max(SCREEN_PADDING, screenHeight - safeHeight - SCREEN_PADDING);
        return new Bounds(clamp(preferredX, SCREEN_PADDING, maxX), clamp(preferredY, SCREEN_PADDING, maxY),
                safeWidth, safeHeight);
    }

    public static ActionStrip actionStrip(int panelWidth, int[] preferredChipWidths, int chipHeight, int gap,
            int horizontalPadding) {
        if (preferredChipWidths == null || preferredChipWidths.length == 0) {
            return new ActionStrip(List.of(), 0);
        }
        int contentWidth = Math.max(1, panelWidth - horizontalPadding * 2);
        List<ActionChip> chips = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (int index = 0; index < preferredChipWidths.length; index++) {
            int chipWidth = clamp(preferredChipWidths[index], Math.min(36, contentWidth), contentWidth);
            if (x > 0 && x + chipWidth > contentWidth) {
                x = 0;
                y += chipHeight + gap;
            }
            chips.add(new ActionChip(index, horizontalPadding + x, y, chipWidth));
            x += chipWidth + gap;
        }
        return new ActionStrip(chips, y + chipHeight);
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    public record Bounds(int x, int y, int width, int height) {
    }

    public record ActionStrip(List<ActionChip> chips, int height) {
        public ActionStrip {
            chips = List.copyOf(chips == null ? List.of() : chips);
            height = Math.max(0, height);
        }
    }

    public record ActionChip(int index, int x, int y, int width) {
    }
}
