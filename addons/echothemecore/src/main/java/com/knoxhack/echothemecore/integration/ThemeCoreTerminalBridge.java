package com.knoxhack.echothemecore.integration;

import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeColors;
import com.knoxhack.echothemecore.api.EchoThemeTextureKey;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

public final class ThemeCoreTerminalBridge {
    private static final String TERMINAL_MODID = "echoterminal";
    private static boolean registered = false;

    private ThemeCoreTerminalBridge() {
    }

    public static boolean isTerminalLoaded() {
        return ModList.get().isLoaded(TERMINAL_MODID);
    }

    public static void registerIfAvailable() {
        if (!isTerminalLoaded() || registered) {
            return;
        }
        try {
            registerCyberGlassTerminalTheme();
            registered = true;
            EchoThemeCore.LOGGER.info("ECHO ThemeCore registered CyberGlass TerminalTheme adapter.");
        } catch (Exception e) {
            EchoThemeCore.LOGGER.warn("Could not register CyberGlass TerminalTheme adapter: {}", e.getMessage());
        }
    }

    private static void registerCyberGlassTerminalTheme() {
        // Use reflection-free classloading through the known API surface
        com.knoxhack.echoterminal.api.theme.TerminalThemeRegistry.register(buildTheme());
    }

    private static com.knoxhack.echoterminal.api.theme.TerminalTheme buildTheme() {
        EchoTheme theme = ThemeRegistry.get(ThemeRegistry.CYBERGLASS_ID);
        EchoThemeColors c = theme.colors();
        com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Colors colors =
            new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Colors(
                c.background(), c.panel(), c.panelAlt(),
                c.panel(), c.panelAlt(),
                c.glass(), c.selection(),
                c.text(), c.mutedText(),
                c.primary(), c.secondary(),
                c.success(), c.warning(), c.error(),
                c.glow()
            );
        com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Panels panels =
            new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Panels(
                c.panel(), c.panelAlt(), c.glass(),
                c.selection(), c.glass(), c.locked(),
                c.panel(), 0.68F
            );
        com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Borders borders =
            new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Borders(
                c.borderSoft(), c.border(), c.glow(),
                c.primary(), c.locked(), c.glow()
            );
        com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Assets assets =
            new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Assets(
                theme.uiAssets().backgroundTexture(),
                module(theme, EchoThemeTextureKey.TERMINAL_PANEL, theme.uiAssets().panelTexture()),
                module(theme, EchoThemeTextureKey.TERMINAL_MISSION_CARD, theme.uiAssets().missionCardSelectedTexture()),
                theme.uiAssets().panelAltTexture(),
                theme.uiAssets().edgeGlow(),
                theme.uiAssets().hologramOverlay(),
                theme.uiAssets().energyOverlay(),
                Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/theme_banner.png"),
                theme.uiAssets().edgeGlow()
            );
        com.knoxhack.echoterminal.api.theme.TerminalThemeTokens tokens =
            new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens(
                colors,
                null,
                panels,
                borders,
                new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Prompt(c.primary(), c.text(), c.warning(), c.error()),
                new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Output(c.text(), c.mutedText(), c.success(), c.warning(), c.error()),
                new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.States(c.success(), c.primary(), c.secondary(), c.locked(), c.warning(), c.success(), c.error()),
                new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Dividers(c.borderSoft(), c.border(), c.glow(), EchoThemeColors.withAlpha(c.primary(), 42)),
                new com.knoxhack.echoterminal.api.theme.TerminalThemeTokens.Effects(false, true, true, EchoThemeColors.withAlpha(c.background(), 205), EchoThemeColors.withAlpha(c.glow(), 112)),
                assets
            );
        com.knoxhack.echoterminal.api.theme.TerminalIconSet icons = cyberGlassIcons();
        return com.knoxhack.echoterminal.api.theme.TerminalTheme.builder(
                Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "cyberglass"),
                "CyberGlass"
            )
            .tokens(tokens)
            .icons(icons)
            .fallbackChapterStyle(chapter("cyberglass", "CyberGlass", c.primary(), c.secondary(), icons))
            .chapterStyle(chapter("minecraft", "Baseline", c.primary(), c.success(), icons))
            .chapterStyle(chapter("echoashfallprotocol", "Ashfall", c.warning(), c.error(), icons))
            .chapterStyle(chapter("echoindustrialnexus", "Industrial Nexus", c.primary(), c.warning(), icons))
            .chapterStyle(chapter("echonexusprotocol", "Nexus Protocol", c.secondary(), c.accent(), icons))
            .chapterStyle(chapter("echoorbitalremnants", "Orbital Remnants", c.primary(), c.secondary(), icons))
            .chapterStyle(chapter("echostationfall", "Stationfall", c.error(), c.warning(), icons))
            .chapterStyle(chapter("echoblackboxprotocol", "Blackbox Protocol", c.accent(), c.mutedText(), icons))
            .build();
    }

    private static Identifier module(EchoTheme theme, EchoThemeTextureKey key, Identifier fallback) {
        return theme.moduleTexture(key).orElse(fallback);
    }

    private static com.knoxhack.echoterminal.api.theme.TerminalChapterStyle chapter(
        String key,
        String displayName,
        int accent,
        int secondary,
        com.knoxhack.echoterminal.api.theme.TerminalIconSet icons
    ) {
        return com.knoxhack.echoterminal.api.theme.TerminalChapterStyle.builder(key, displayName)
            .colors(accent, secondary)
            .banner(Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/theme_banner.png"))
            .panel(Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/mission_card.png"))
            .border(Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/edge_glow.png"))
            .icons(icons)
            .build();
    }

    private static com.knoxhack.echoterminal.api.theme.TerminalIconSet cyberGlassIcons() {
        com.knoxhack.echoterminal.api.theme.TerminalIconSet.Builder builder =
            com.knoxhack.echoterminal.api.theme.TerminalIconSet.builder()
                .fallback(icon("theme"));
        for (com.knoxhack.echoterminal.api.theme.TerminalIconKey key
            : com.knoxhack.echoterminal.api.theme.BuiltinTerminalThemes.defaultIcons().icons().keySet()) {
            builder.icon(key, iconFor(key));
        }
        builder.icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echoashfallprotocol"), icon("core"))
            .icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echoindustrialnexus"), icon("industrial"))
            .icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echonexusprotocol"), icon("nexus"))
            .icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echoorbitalremnants"), icon("orbital"))
            .icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echostationfall"), icon("blackbox"))
            .icon(com.knoxhack.echoterminal.api.theme.TerminalIconKey.chapter("echoblackboxprotocol"), icon("blackbox"));
        return builder.build();
    }

    private static Identifier iconFor(com.knoxhack.echoterminal.api.theme.TerminalIconKey key) {
        String category = key.category();
        String name = key.name();
        if ("group".equals(category)) {
            if (name.contains("nexus") || name.contains("endgame")) {
                return icon("nexus");
            }
            if (name.contains("orbital")) {
                return icon("orbital");
            }
            if (name.contains("system")) {
                return icon("runtime");
            }
            if (name.contains("chapter") || name.contains("addon")) {
                return icon("index");
            }
            return icon("terminal");
        }
        if ("page".equals(category)) {
            if (name.contains("route") || name.contains("map")) {
                return icon("holomap");
            }
            if (name.contains("vital") || name.contains("scan")) {
                return icon("lens");
            }
            if (name.contains("reward")) {
                return icon("missions");
            }
            return icon("index");
        }
        if ("action".equals(category)) {
            return name.contains("scan") ? icon("lens") : icon("missions");
        }
        if ("state".equals(category)) {
            if (name.contains("locked") || name.contains("blocker")) {
                return icon("blackbox");
            }
            if (name.contains("warning")) {
                return icon("runtime");
            }
            return icon("core");
        }
        if ("mission_category".equals(category)) {
            if (name.contains("combat")) {
                return icon("armory");
            }
            if (name.contains("exploration")) {
                return icon("holomap");
            }
            if (name.contains("tech") || name.contains("craft")) {
                return icon("industrial");
            }
            return icon("missions");
        }
        return switch (category) {
            case "reward" -> icon("missions");
            case "chapter" -> icon("index");
            case "theme" -> icon("theme");
            default -> icon("theme");
        };
    }

    private static Identifier icon(String name) {
        return Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/icons/icon_" + name + ".png");
    }
}
