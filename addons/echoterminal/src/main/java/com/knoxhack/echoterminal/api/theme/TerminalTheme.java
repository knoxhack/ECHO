package com.knoxhack.echoterminal.api.theme;

import com.knoxhack.echoterminal.api.TerminalApiIds;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record TerminalTheme(
        Identifier id,
        String displayName,
        TerminalThemeTokens tokens,
        TerminalIconSet icons,
        Map<String, TerminalChapterStyle> chapterStyles,
        TerminalChapterStyle fallbackChapterStyle) {
    public TerminalTheme {
        TerminalApiIds.requireLowercase(id, "Terminal theme");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Terminal theme display name is required.");
        }
        tokens = Objects.requireNonNull(tokens, "Terminal theme tokens are required.");
        icons = icons == null ? TerminalIconSet.builder().build() : icons;
        chapterStyles = Map.copyOf(chapterStyles == null ? Map.of() : chapterStyles);
        fallbackChapterStyle = fallbackChapterStyle == null
                ? TerminalChapterStyle.builder("", displayName).colors(tokens.colors().accent(), tokens.colors().muted()).build()
                : fallbackChapterStyle;
    }

    public static Builder builder(Identifier id, String displayName) {
        return new Builder(id, displayName);
    }

    public TerminalChapterStyle chapterStyle(TerminalThemeContext context) {
        if (context != null) {
            TerminalChapterStyle byNamespace = chapterStyles.get(clean(context.namespace()));
            if (byNamespace != null) {
                return byNamespace;
            }
            TerminalChapterStyle byChapter = chapterStyles.get(clean(context.chapterId()));
            if (byChapter != null) {
                return byChapter;
            }
            if (context.activeTabId() != null) {
                TerminalChapterStyle byTabNamespace = chapterStyles.get(clean(context.activeTabId().getNamespace()));
                if (byTabNamespace != null) {
                    return byTabNamespace;
                }
            }
        }
        return fallbackChapterStyle;
    }

    public Identifier icon(TerminalIconKey key, TerminalThemeContext context, Identifier fallback) {
        TerminalChapterStyle style = chapterStyle(context);
        Identifier resolved = style.icons().resolve(key, null);
        return resolved == null ? icons.resolve(key, fallback) : resolved;
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }

    public static final class Builder {
        private final Identifier id;
        private final String displayName;
        private TerminalThemeTokens tokens;
        private TerminalIconSet icons = TerminalIconSet.builder().build();
        private final Map<String, TerminalChapterStyle> chapterStyles = new LinkedHashMap<>();
        private TerminalChapterStyle fallbackChapterStyle;

        private Builder(Identifier id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder tokens(TerminalThemeTokens tokens) {
            this.tokens = tokens;
            return this;
        }

        public Builder icons(TerminalIconSet icons) {
            this.icons = icons == null ? TerminalIconSet.builder().build() : icons;
            return this;
        }

        public Builder chapterStyle(TerminalChapterStyle style) {
            if (style != null && !style.key().isBlank()) {
                chapterStyles.put(style.key(), style);
            }
            return this;
        }

        public Builder fallbackChapterStyle(TerminalChapterStyle style) {
            fallbackChapterStyle = style;
            return this;
        }

        public TerminalTheme build() {
            return new TerminalTheme(id, displayName, tokens, icons, chapterStyles, fallbackChapterStyle);
        }
    }
}
