package com.knoxhack.echothemecore.test;

import com.google.gson.JsonParser;
import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeSoundKey;
import com.knoxhack.echothemecore.api.EchoThemeTextureKey;
import com.knoxhack.echothemecore.api.ThemeVisualSettings;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import com.knoxhack.echothemecore.content.ThemeJsonReloadListener;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import com.knoxhack.echothemecore.integration.ThemeCoreTerminalBridge;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, EchoThemeCore.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> THEME_PARSE =
        TEST_FUNCTIONS.register("theme_json_parse", () -> ModGameTests::themeJsonParse);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REGISTRY_FALLBACK =
        TEST_FUNCTIONS.register("registry_fallback", () -> ModGameTests::registryFallback);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VISUAL_SETTINGS =
        TEST_FUNCTIONS.register("visual_settings", () -> ModGameTests::visualSettings);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> NO_LEGACY_LINE_OVERLAYS =
        TEST_FUNCTIONS.register("no_legacy_line_overlay_terms", () -> ModGameTests::noLegacyLineOverlayTerms);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CYBERGLASS_FULL_THEME =
        TEST_FUNCTIONS.register("cyberglass_full_theme_contract", () -> ModGameTests::cyberglassFullThemeContract);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        register(event, "theme_json_parse", THEME_PARSE.getId());
        register(event, "registry_fallback", REGISTRY_FALLBACK.getId());
        register(event, "visual_settings", VISUAL_SETTINGS.getId());
        register(event, "no_legacy_line_overlay_terms", NO_LEGACY_LINE_OVERLAYS.getId());
        register(event, "cyberglass_full_theme_contract", CYBERGLASS_FULL_THEME.getId());
    }

    private static void themeJsonParse(GameTestHelper helper) {
        EchoTheme parsed = ThemeJsonReloadListener.parseThemeForTests(id("parse_test"),
            JsonParser.parseString("""
                {
                  "id": "echothemecore:parse_test",
                  "display_name": "Parse Test",
                  "colors": {
                    "primary": "#00E5FF",
                    "secondary": "#B44CFF",
                    "accent": "#FF2BD6",
                    "background": "#030711",
                    "panel": "#08111FCC",
                    "panel_alt": "#0D1A2ECC",
                    "glass": "#10243A88",
                    "border": "#2BEAFF",
                    "border_soft": "#1A6F8A",
                    "text": "#EAFBFF",
                    "muted_text": "#8AAFC2",
                    "success": "#45FFB0",
                    "warning": "#FFD166",
                    "error": "#FF4D6D",
                    "locked": "#3B4652",
                    "glow": "#00E5FF",
                    "selection": "#B44CFF"
                  }
                }
                """).getAsJsonObject());
        helper.assertTrue(parsed.id().equals(id("parse_test")), "Theme id should parse.");
        helper.assertTrue(parsed.colors().primary() == 0xFF00E5FF, "Primary color should parse to ARGB.");
        helper.assertTrue(parsed.uiAssets().panelTexture() != null, "UI assets should receive safe defaults.");
        helper.succeed();
    }

    private static void registryFallback(GameTestHelper helper) {
        ThemeRegistry.replaceLoaded(Map.of());
        helper.assertTrue(ThemeRegistry.get(id("missing")).id().equals(ThemeRegistry.CYBERGLASS_ID),
            "Missing themes should fall back to CyberGlass.");
        ThemeRegistry.setGlobalTheme(id("missing"));
        helper.assertTrue(ThemeRegistry.getCurrentTheme().id().equals(ThemeRegistry.CYBERGLASS_ID),
            "Invalid global theme should resolve to CyberGlass.");
        helper.succeed();
    }

    private static void visualSettings(GameTestHelper helper) {
        ThemeRegistry.setDebugVisualIntensity(0.5F);
        ThemeVisualSettings settings = ThemeVisualSettings.resolve(ThemeRegistry.getCurrentTheme());
        helper.assertTrue(settings.glowIntensity() <= 1.0F, "Debug scale should cap glow intensity.");
        ThemeRegistry.setDebugVisualIntensity(1.0F);
        helper.succeed();
    }

    private static void noLegacyLineOverlayTerms(GameTestHelper helper) {
        String forbidden = "scan" + "line";
        for (EchoTheme theme : ThemeRegistry.listThemes()) {
            String combined = theme.id() + " " + theme.displayName() + " " + theme.description() + " " + theme.metadata();
            helper.assertFalse(combined.toLowerCase(java.util.Locale.ROOT).contains(forbidden),
                "Theme metadata should not contain forbidden legacy line-overlay terms.");
        }
        helper.succeed();
    }

    private static void cyberglassFullThemeContract(GameTestHelper helper) {
        EchoTheme parsed = parsePackagedCyberGlass();
        ThemeRegistry.replaceLoaded(Map.of());
        EchoTheme builtin = ThemeRegistry.get(ThemeRegistry.CYBERGLASS_ID);
        helper.assertTrue(parsed.id().equals(builtin.id()), "Packaged and builtin CyberGlass ids should match.");
        helper.assertTrue(parsed.colors().primary() == builtin.colors().primary(), "Builtin CyberGlass primary color should match JSON.");
        helper.assertTrue(parsed.soundProfile().sound(EchoThemeSoundKey.UI_CLICK).isPresent(), "CyberGlass should expose themed UI click sound.");
        helper.assertFalse(builtin.blockPalette().recommendedBlocks().isEmpty(), "Builtin CyberGlass should expose block palette data.");
        helper.assertTrue("echothemecore:cyberglass".equals(ThemeCoreConfig.string(ThemeCoreConfig.DEFAULT_THEME)),
            "ThemeCore default theme config should default to CyberGlass.");
        helper.assertTrue("echothemecore:cyberglass".equals(ThemeCoreConfig.string(ThemeCoreConfig.FALLBACK_THEME)),
            "ThemeCore fallback theme config should default to CyberGlass.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_MAIN_MENU),
            "ThemeCore main menu theming should default on.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_TERMINAL),
            "ThemeCore Terminal theming should default on.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_HOLOMAP),
            "ThemeCore HoloMap theming should default on.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_LENS),
            "ThemeCore Lens theming should default on.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_RENDERCORE),
            "ThemeCore RenderCore theming should default on.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_SOUNDCORE),
            "ThemeCore SoundCore theming should default on.");
        helper.assertTrue(ThemeCoreConfig.vanillaUiEnabled(), "ThemeCore vanilla UI theming should default on.");
        helper.assertFalse(ThemeCoreConfig.vanillaSafeMode(), "ThemeCore vanilla UI safe mode should default off.");
        helper.assertFalse(ThemeCoreConfig.disableNoise(), "ThemeCore CyberGlass noise should default on.");
        helper.assertTrue(ThemeCoreConfig.disableUnknownScreens(), "ThemeCore unknown-screen protection should stay enabled.");
        helper.assertTrue(ThemeCoreConfig.bool(ThemeCoreConfig.DO_NOT_MODIFY_SLOT_POSITIONS),
            "ThemeCore slot-position protection should stay enabled.");
        helper.assertTrue(ThemeCoreConfig.preserveTextContrast(),
            "ThemeCore text contrast preservation should stay enabled.");
        for (EchoThemeTextureKey key : new EchoThemeTextureKey[] {
            EchoThemeTextureKey.HOLOMAP_MARKER_NEXUS,
            EchoThemeTextureKey.HOLOMAP_MARKER_RECLAIMED,
            EchoThemeTextureKey.LENS_PROGRESS_ARC,
            EchoThemeTextureKey.LENS_NOISE_OVERLAY,
            EchoThemeTextureKey.VANILLA_TOOLTIP_PANEL,
            EchoThemeTextureKey.VANILLA_TOAST_ACCENT,
            EchoThemeTextureKey.VANILLA_BOSS_BAR_ACCENT,
            EchoThemeTextureKey.VANILLA_WIDGET_OUTLINE,
            EchoThemeTextureKey.RENDERCORE_DISTORTION_OVERLAY
        }) {
            Identifier texture = parsed.moduleTexture(key)
                .orElseThrow(() -> new AssertionError("CyberGlass should expose module texture " + key));
            assertPackagedTexture(helper, texture);
            helper.assertTrue(builtin.moduleTexture(key).isPresent(), "Builtin CyberGlass should expose " + key);
        }
        assertTerminalBridgeIfLoaded(helper);
        helper.succeed();
    }

    private static void assertTerminalBridgeIfLoaded(GameTestHelper helper) {
        if (!ThemeCoreTerminalBridge.isTerminalLoaded()) {
            return;
        }
        ThemeCoreTerminalBridge.registerIfAvailable();
        try {
            Class<?> registry = Class.forName("com.knoxhack.echoterminal.api.theme.TerminalThemeRegistry");
            boolean contains = ((Boolean) registry.getMethod("contains", Identifier.class)
                .invoke(null, ThemeRegistry.CYBERGLASS_ID)).booleanValue();
            Object defaultId = registry.getMethod("defaultThemeId").invoke(null);
            Object theme = registry.getMethod("byId", Identifier.class).invoke(null, ThemeRegistry.CYBERGLASS_ID);
            Object tokens = theme.getClass().getMethod("tokens").invoke(theme);
            Object assets = tokens.getClass().getMethod("assets").invoke(tokens);
            Object icons = theme.getClass().getMethod("icons").invoke(theme);
            helper.assertTrue(contains, "Terminal should register the CyberGlass ThemeCore theme when loaded.");
            helper.assertTrue(ThemeRegistry.CYBERGLASS_ID.equals(defaultId),
                "Terminal should use CyberGlass as the active default when ThemeCore bridge is loaded.");
            helper.assertTrue(assets != null, "CyberGlass Terminal theme should expose asset tokens.");
            helper.assertTrue(icons != null, "CyberGlass Terminal theme should expose icons.");
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect CyberGlass Terminal bridge.", exception);
        }
    }

    private static EchoTheme parsePackagedCyberGlass() {
        String resource = "data/echothemecore/themes/cyberglass.json";
        try (InputStreamReader reader = new InputStreamReader(
            ModGameTests.class.getClassLoader().getResourceAsStream(resource), StandardCharsets.UTF_8)) {
            return ThemeJsonReloadListener.parseThemeForTests(ThemeRegistry.CYBERGLASS_ID, JsonParser.parseReader(reader).getAsJsonObject());
        } catch (IOException | NullPointerException exception) {
            throw new AssertionError("Could not read packaged CyberGlass JSON.", exception);
        }
    }

    private static void assertPackagedTexture(GameTestHelper helper, Identifier texture) {
        String path = "assets/" + texture.getNamespace() + "/" + texture.getPath();
        try (var stream = ModGameTests.class.getClassLoader().getResourceAsStream(path)) {
            helper.assertTrue(stream != null, "Expected packaged texture " + path);
        } catch (IOException exception) {
            helper.fail("Could not read packaged texture " + path + ": " + exception.getMessage());
        }
    }

    private static void register(RegisterGameTestsEvent event, String testName, Identifier functionId) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("themecore_" + testName));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
            environment, Identifier.withDefaultNamespace("empty"), 400, 0, true, Rotation.NONE, false, 1, 1,
            false, 16);
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, path);
    }

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoThemeCore.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }
}
