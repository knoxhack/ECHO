package com.knoxhack.echothemecore.client;

import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ClientThemeCache {
    private static Identifier currentThemeId = ThemeRegistry.CYBERGLASS_ID;
    private static Identifier previousThemeId = ThemeRegistry.CYBERGLASS_ID;
    private static int transitionTicks;
    private static int transitionAge;

    private ClientThemeCache() {
    }

    public static EchoTheme currentTheme() {
        return ThemeRegistry.get(currentThemeId);
    }

    public static Identifier currentThemeId() {
        return currentThemeId;
    }

    public static Identifier previousThemeId() {
        return previousThemeId;
    }

    public static void applyServerTheme(Identifier themeId) {
        Identifier resolved = ThemeRegistry.get(themeId).id();
        if (resolved.equals(currentThemeId)) {
            return;
        }
        previousThemeId = currentThemeId;
        currentThemeId = resolved;
        transitionTicks = ThemeCoreConfig.enableThemeTransitions() ? ThemeCoreConfig.themeTransitionTicks() : 0;
        transitionAge = 0;
    }

    public static float transitionProgress() {
        if (transitionTicks <= 0) {
            return 1.0F;
        }
        return Math.min(1.0F, transitionAge / (float) transitionTicks);
    }

    public static boolean transitioning() {
        return transitionTicks > 0 && transitionAge < transitionTicks;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && currentThemeId == null) {
            currentThemeId = ThemeRegistry.globalThemeId();
        }
        if (transitionAge < transitionTicks) {
            transitionAge++;
        }
    }
}
