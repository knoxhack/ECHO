package com.knoxhack.echothemecore.api;

import com.knoxhack.echothemecore.content.ThemeRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class EchoThemeApi {
    private EchoThemeApi() {
    }

    public static EchoTheme getTheme() {
        return ThemeRegistry.getCurrentTheme();
    }

    public static EchoTheme getTheme(Player player) {
        return ThemeRegistry.getThemeFor(player);
    }

    public static EchoTheme getTheme(Identifier id) {
        return ThemeRegistry.get(id);
    }

    public static Identifier getThemeId(Player player) {
        return getTheme(player).id();
    }

    public static void setGlobalTheme(Identifier id) {
        ThemeRegistry.setGlobalTheme(id);
    }

    public static void setPlayerTheme(UUID playerId, Identifier id) {
        ThemeRegistry.setPlayerTheme(playerId, id);
    }

    public static List<EchoTheme> getThemes() {
        return ThemeRegistry.listThemes();
    }

    public static EchoThemeColors getColors(Player player) {
        return getTheme(player).colors();
    }

    public static EchoThemeRenderProfile getRenderProfile(Player player) {
        return getTheme(player).renderProfile();
    }

    public static EchoThemeVanillaUiProfile getVanillaUiProfile(Player player) {
        return getTheme(player).vanillaUiProfile();
    }

    public static ThemeVisualSettings getEffectiveVisualSettings(Player player) {
        return ThemeVisualSettings.resolve(getTheme(player));
    }

    public static int color(Player player, EchoThemeColorKey key) {
        return getTheme(player).colors().color(key);
    }

    public static Optional<Identifier> getTexture(Player player, EchoThemeTextureKey key) {
        EchoTheme theme = getTheme(player);
        Optional<Identifier> vanilla = theme.vanillaUiProfile().texture(key);
        return vanilla.isPresent() ? vanilla : theme.uiAssets().texture(key);
    }

    public static Optional<Identifier> getSound(Player player, EchoThemeSoundKey key) {
        return getTheme(player).soundProfile().sound(key);
    }

    public static ThemeTransition getTransition(Identifier fromTheme, Identifier toTheme) {
        EchoTheme target = ThemeRegistry.get(toTheme);
        EchoThemeRenderProfile render = target.renderProfile();
        return new ThemeTransition(
            fromTheme,
            target.id(),
            render.transitionStyle(),
            ThemeRegistry.transitionTicks(),
            render.hologramColor(),
            render.hologramSecondary(),
            render.edgeGlowStrength(),
            render.particleIntensity()
        );
    }

    public static void playThemeTransition(Player player, Identifier newTheme) {
        ThemeRegistry.setPlayerTheme(player.getUUID(), newTheme);
    }
}
