package com.knoxhack.signalos.api;

import java.util.Locale;
import net.minecraft.resources.Identifier;

/**
 * Metadata for a SignalOS desktop app. Client renderers can use the app type to
 * choose a built-in surface while addons can still register shell-visible app
 * entries through Java or JSON.
 */
public record SignalOsApp(
        Identifier id,
        String title,
        String type,
        String summary,
        int order,
        int accentColor,
        Identifier icon,
        String permission) {
    public SignalOsApp {
        id = TerminalIds.requireLowercase(id, "SignalOS app");
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        type = clean(type, "custom");
        summary = summary == null ? "" : summary.strip();
        accentColor = opaque(accentColor == 0 ? 0x66E8FF : accentColor);
        if (icon != null) {
            icon = TerminalIds.requireLowercase(icon, "SignalOS app icon");
        }
        permission = clean(permission, "user");
    }

    public static Builder builder(String id) {
        return new Builder(TerminalIds.parse(id, "SignalOS app"));
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private static int opaque(int color) {
        return (color >>> 24) == 0 ? 0xFF000000 | color : color;
    }

    public static final class Builder {
        private final Identifier id;
        private String title = "";
        private String type = "custom";
        private String summary = "";
        private int order;
        private int accentColor = 0xFF66E8FF;
        private Identifier icon;
        private String permission = "user";

        private Builder(Identifier id) {
            this.id = TerminalIds.requireLowercase(id, "SignalOS app");
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder accentColor(int accentColor) {
            this.accentColor = accentColor;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon == null || icon.isBlank() ? null : TerminalIds.parse(icon, "SignalOS app icon");
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public SignalOsApp build() {
            return new SignalOsApp(id, title, type, summary, order, accentColor, icon, permission);
        }
    }
}
