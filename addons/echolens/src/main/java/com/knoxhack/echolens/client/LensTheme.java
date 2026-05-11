package com.knoxhack.echolens.client;

import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.config.LensConfig;

public record LensTheme(
        int panel,
        int header,
        int border,
        int glow,
        int text,
        int muted,
        int good,
        int warning,
        int danger,
        int echo) {
    public static LensTheme current() {
        return switch (LensConfig.value(LensConfig.THEME, LensConfig.LensThemeId.ECHO_DARK)) {
            case CLEAN_MINIMAL -> new LensTheme(0xE8F5F7F8, 0xEEFDFDFD, 0x88374550, 0x55374550,
                    0xFF1B2429, 0xFF59646A, 0xFF1E7F4F, 0xFF9D6A00, 0xFF9E3030, 0xFF256E8D);
            case VANILLA_COMPACT -> new LensTheme(0xD8202020, 0xE02A2A2A, 0xAA6F6F6F, 0x6655FFFF,
                    0xFFFFFFFF, 0xFFB0B0B0, 0xFF80FF80, 0xFFFFFF80, 0xFFFF8080, 0xFF80E8FF);
            case ASHFALL_HAZARD -> new LensTheme(0xEC130B0A, 0xEE24100D, 0xAAAD4238, 0x99FF6B35,
                    0xFFFFEFE6, 0xFFC9A79F, 0xFFA8E06D, 0xFFFFB04C, 0xFFFF5B48, 0xFFFF7442);
            case ECHO_DARK -> new LensTheme(0xE8071017, 0xF00B1720, 0x7738DFF4, 0x884CCBFF,
                    0xFFEAF8FF, 0xFF8FA7B0, 0xFFA6E22E, 0xFFFFD166, 0xFFFF5A6E, 0xFF66D9EF);
        };
    }

    public int tone(LensTone tone) {
        return switch (tone == null ? LensTone.NEUTRAL : tone) {
            case GOOD -> good;
            case WARNING -> warning;
            case DANGER -> danger;
            case MUTED -> muted;
            case ECHO, INFO -> echo;
            case NEUTRAL -> text;
        };
    }

    public int alpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((color >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
