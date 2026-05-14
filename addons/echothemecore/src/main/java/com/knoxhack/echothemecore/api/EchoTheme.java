package com.knoxhack.echothemecore.api;

import java.util.Map;
import net.minecraft.resources.Identifier;

public record EchoTheme(
    Identifier id,
    String displayName,
    String description,
    EchoThemeColors colors,
    EchoThemeUiAssets uiAssets,
    EchoThemeRenderProfile renderProfile,
    EchoThemeSoundProfile soundProfile,
    EchoThemeBlockPalette blockPalette,
    EchoThemeVanillaUiProfile vanillaUiProfile,
    Map<String, String> metadata
) {
    public EchoTheme {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        if (vanillaUiProfile == null) {
            vanillaUiProfile = EchoThemeVanillaUiProfile.fromParts(colors, uiAssets, renderProfile);
        }
    }
}
