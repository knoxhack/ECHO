package com.knoxhack.echothemecore.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.api.DistortionStyle;
import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeBlockPalette;
import com.knoxhack.echothemecore.api.EchoThemeColors;
import com.knoxhack.echothemecore.api.EchoThemeRenderProfile;
import com.knoxhack.echothemecore.api.EchoThemeSoundProfile;
import com.knoxhack.echothemecore.api.EchoThemeUiAssets;
import com.knoxhack.echothemecore.api.EchoThemeVanillaUiProfile;
import com.knoxhack.echothemecore.api.HologramStyle;
import com.knoxhack.echothemecore.api.ParticleStyle;
import com.knoxhack.echothemecore.api.TransitionStyle;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class ThemeJsonReloadListener extends SimplePreparableReloadListener<Map<Identifier, EchoTheme>> {
    private static final List<String> THEME_DIRS = List.of("themes", EchoThemeCore.MODID + "/themes");

    @Override
    protected Map<Identifier, EchoTheme> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, EchoTheme> themes = new LinkedHashMap<>();
        for (String themeDir : THEME_DIRS) {
            for (Map.Entry<Identifier, Resource> entry : manager.listResources(themeDir, id -> id.getPath().endsWith(".json")).entrySet()) {
                Identifier resourceId = entry.getKey();
                Identifier fallbackId = contentId(resourceId, themeDir);
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement root = JsonParser.parseReader(reader);
                    if (!root.isJsonObject()) {
                        throw new JsonParseException("Root must be a JSON object.");
                    }
                    EchoTheme theme = parseTheme(fallbackId, root.getAsJsonObject());
                    if (themes.putIfAbsent(theme.id(), theme) != null) {
                        EchoThemeCore.LOGGER.warn("Duplicate ThemeCore theme id {} from {} ignored.", theme.id(), resourceId);
                    } else if (ThemeCoreConfig.debugThemeLogging()) {
                        EchoThemeCore.LOGGER.info("Loaded ThemeCore theme {} from {}.", theme.id(), resourceId);
                    }
                } catch (IOException | RuntimeException exception) {
                    EchoThemeCore.LOGGER.warn("Could not parse ThemeCore theme file {}.", resourceId, exception);
                }
            }
        }
        return themes;
    }

    @Override
    protected void apply(Map<Identifier, EchoTheme> themes, ResourceManager manager, ProfilerFiller profiler) {
        ThemeRegistry.replaceLoaded(themes);
    }

    public static EchoTheme parseThemeForTests(Identifier fallbackId, JsonObject json) {
        return parseTheme(fallbackId, json);
    }

    private static EchoTheme parseTheme(Identifier fallbackId, JsonObject json) {
        Identifier id = identifier(json, "id", fallbackId);
        JsonObject colorsJson = requiredObject(json, "colors", id);
        EchoThemeColors colors = parseColors(colorsJson);
        EchoThemeUiAssets ui = parseUi(object(json, "ui"), id);
        EchoThemeRenderProfile render = parseRender(object(json, "render"), colors);
        EchoThemeSoundProfile sound = parseSound(object(json, "sound"));
        EchoThemeBlockPalette blocks = parseBlocks(object(json, "block_palette"));
        EchoThemeVanillaUiProfile vanilla = parseVanilla(object(json, "vanilla_ui"), colors, ui, render);
        return new EchoTheme(
            id,
            string(json, "display_name", id.getPath()),
            string(json, "description", ""),
            colors,
            ui,
            render,
            sound,
            blocks,
            vanilla,
            stringMap(object(json, "metadata"))
        );
    }

    private static EchoThemeColors parseColors(JsonObject json) {
        return new EchoThemeColors(
            color(json, "primary", 0xFF00E5FF),
            color(json, "secondary", 0xFFB44CFF),
            color(json, "accent", 0xFFFF2BD6),
            color(json, "background", 0xFF030711),
            color(json, "panel", 0xCC08111F),
            color(json, "panel_alt", 0xCC0D1A2E),
            color(json, "glass", 0x8810243A),
            color(json, "border", 0xFF2BEAFF),
            color(json, "border_soft", 0xFF1A6F8A),
            color(json, "text", 0xFFEAFBFF),
            color(json, "muted_text", 0xFF8AAFC2),
            color(json, "success", 0xFF45FFB0),
            color(json, "warning", 0xFFFFD166),
            color(json, "error", 0xFFFF4D6D),
            color(json, "locked", 0xFF3B4652),
            color(json, "glow", 0xFF00E5FF),
            color(json, "selection", 0xFFB44CFF)
        );
    }

    private static EchoThemeUiAssets parseUi(JsonObject json, Identifier id) {
        String themeFolder = id.getPath();
        return new EchoThemeUiAssets(
            texture(json, "background_texture", themeFolder, "background"),
            texture(json, "panel_texture", themeFolder, "glass_panel"),
            texture(json, "panel_alt_texture", themeFolder, "glass_panel_alt"),
            texture(json, "button_texture", themeFolder, "glass_button"),
            texture(json, "button_hover_texture", themeFolder, "glass_button_hover"),
            texture(json, "tab_texture", themeFolder, "tab"),
            texture(json, "tab_active_texture", themeFolder, "tab_active"),
            texture(json, "mission_card_texture", themeFolder, "mission_card"),
            texture(json, "mission_card_selected_texture", themeFolder, "mission_card_selected"),
            texture(json, "status_chip_texture", themeFolder, "status_chip"),
            texture(json, "progress_bar_texture", themeFolder, "progress_bar"),
            texture(json, "scrollbar_texture", themeFolder, "scrollbar"),
            texture(json, "icon_pack", themeFolder, "icons/icon_theme"),
            texture(json, "hologram_overlay", themeFolder, "hologram_overlay"),
            texture(json, "energy_overlay", themeFolder, "energy_overlay"),
            texture(json, "edge_glow", themeFolder, "edge_glow"),
            texture(json, "particle_glints", themeFolder, "particle_glints"),
            texture(json, "locked_overlay", themeFolder, "locked_overlay")
        );
    }

    private static EchoThemeRenderProfile parseRender(JsonObject json, EchoThemeColors colors) {
        return new EchoThemeRenderProfile(
            color(json, "hologram_color", colors.primary()),
            color(json, "hologram_secondary", colors.secondary()),
            color(json, "particle_primary", colors.primary()),
            color(json, "particle_secondary", colors.accent()),
            color(json, "emissive_primary", colors.primary()),
            color(json, "emissive_secondary", colors.secondary()),
            color(json, "edge_glow_color", colors.glow()),
            color(json, "warning_glow_color", colors.warning()),
            color(json, "success_glow_color", colors.success()),
            color(json, "error_glow_color", colors.error()),
            color(json, "distortion_color", colors.secondary()),
            decimal(json, "glow_intensity", 0.85F),
            decimal(json, "emissive_intensity", 0.9F),
            decimal(json, "hologram_opacity", 0.68F),
            decimal(json, "glass_opacity", 0.62F),
            decimal(json, "noise_strength", 0.0F),
            decimal(json, "distortion_strength", 0.0F),
            decimal(json, "edge_glow_strength", 0.75F),
            decimal(json, "hologram_pulse_strength", 0.45F),
            decimal(json, "energy_pattern_strength", 0.35F),
            decimal(json, "particle_intensity", 0.65F),
            decimal(json, "animation_intensity", 0.75F),
            decimal(json, "pulse_speed", 0.85F),
            HologramStyle.byName(string(json, "hologram_style", "CYBER_GLASS"), HologramStyle.CYBER_GLASS),
            ParticleStyle.byName(string(json, "particle_style", "SOFT_GLINTS"), ParticleStyle.SOFT_GLINTS),
            DistortionStyle.byName(string(json, "distortion_style", "NONE"), DistortionStyle.NONE),
            string(json, "overlay_style", "GLASS_GEOMETRIC"),
            TransitionStyle.byName(string(json, "transition_style", "GLASS_FADE"), TransitionStyle.GLASS_FADE)
        );
    }

    private static EchoThemeSoundProfile parseSound(JsonObject json) {
        return new EchoThemeSoundProfile(
            optionalId(json, "ui_click"),
            optionalId(json, "ui_error"),
            optionalId(json, "ui_open"),
            optionalId(json, "ui_close"),
            optionalId(json, "theme_music"),
            optionalId(json, "stinger_confirm"),
            optionalId(json, "stinger_warning")
        );
    }

    private static EchoThemeBlockPalette parseBlocks(JsonObject json) {
        return new EchoThemeBlockPalette(
            idArray(json, "recommended_blocks"),
            idArray(json, "panel_blocks"),
            idArray(json, "glass_blocks"),
            idArray(json, "light_blocks"),
            idArray(json, "accent_blocks")
        );
    }

    private static EchoThemeVanillaUiProfile parseVanilla(JsonObject json, EchoThemeColors colors, EchoThemeUiAssets ui, EchoThemeRenderProfile render) {
        if (json == null) {
            return EchoThemeVanillaUiProfile.fromParts(colors, ui, render);
        }
        EchoThemeVanillaUiProfile fallback = EchoThemeVanillaUiProfile.fromParts(colors, ui, render);
        return new EchoThemeVanillaUiProfile(
            identifier(json, "background_texture", fallback.backgroundTexture()),
            identifier(json, "panel_texture", fallback.panelTexture()),
            identifier(json, "button_texture", fallback.buttonTexture()),
            identifier(json, "hotbar_texture", fallback.hotbarTexture()),
            identifier(json, "tooltip_texture", fallback.tooltipTexture()),
            identifier(json, "toast_texture", fallback.toastTexture()),
            identifier(json, "boss_bar_texture", fallback.bossBarTexture()),
            color(json, "background_tint", fallback.backgroundTint()),
            color(json, "panel_tint", fallback.panelTint()),
            color(json, "tooltip_tint", fallback.tooltipTint()),
            color(json, "widget_accent", fallback.widgetAccent()),
            color(json, "hotbar_accent", fallback.hotbarAccent()),
            color(json, "chat_accent", fallback.chatAccent()),
            decimal(json, "overlay_opacity", fallback.overlayOpacity()),
            decimal(json, "panel_opacity", fallback.panelOpacity()),
            decimal(json, "edge_glow_strength", fallback.edgeGlowStrength()),
            bool(json, "reduce_vanilla_brown", fallback.reduceVanillaBrown())
        );
    }

    private static Identifier contentId(Identifier resourceId, String folder) {
        String path = resourceId.getPath();
        String prefix = folder + "/";
        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
    }

    private static JsonObject object(JsonObject json, String key) {
        JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonObject requiredObject(JsonObject json, String key, Identifier id) {
        JsonObject object = object(json, key);
        if (object == null) {
            throw new JsonParseException("Theme " + id + " is missing required object '" + key + "'.");
        }
        return object;
    }

    private static String string(JsonObject json, String key, String fallback) {
        JsonElement element = json == null ? null : json.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsString();
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        JsonElement element = json == null ? null : json.get(key);
        return element == null || element.isJsonNull() ? fallback : element.getAsBoolean();
    }

    private static float decimal(JsonObject json, String key, float fallback) {
        JsonElement element = json == null ? null : json.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        return Math.max(0.0F, Math.min(2.0F, element.getAsFloat()));
    }

    private static int color(JsonObject json, String key, int fallback) {
        return EchoThemeColors.parseHex(string(json, key, null), fallback);
    }

    private static Identifier identifier(JsonObject json, String key, Identifier fallback) {
        Identifier parsed = Identifier.tryParse(string(json, key, ""));
        return parsed == null ? fallback : parsed;
    }

    private static Identifier optionalId(JsonObject json, String key) {
        return Identifier.tryParse(string(json, key, ""));
    }

    private static Identifier texture(JsonObject json, String key, String themeFolder, String fallbackName) {
        Identifier parsed = Identifier.tryParse(string(json, key, ""));
        return parsed == null
            ? Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/" + themeFolder + "/" + fallbackName + ".png")
            : parsed;
    }

    private static List<Identifier> idArray(JsonObject json, String key) {
        JsonElement element = json == null ? null : json.get(key);
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }
        List<Identifier> ids = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            Identifier id = Identifier.tryParse(entry.getAsString());
            if (id != null) {
                ids.add(id);
            }
        }
        return List.copyOf(ids);
    }

    private static Map<String, String> stringMap(JsonObject json) {
        if (json == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if (!entry.getValue().isJsonNull()) {
                result.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return result;
    }
}
