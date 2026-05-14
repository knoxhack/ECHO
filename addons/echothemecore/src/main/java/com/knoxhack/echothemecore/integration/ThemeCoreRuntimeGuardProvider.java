package com.knoxhack.echothemecore.integration;

import com.knoxhack.echothemecore.api.EchoRuntimeGuardThemeProvider;
import com.knoxhack.echothemecore.api.EchoThemeApi;
import com.knoxhack.echothemecore.api.EchoThemeRenderProfile;
import net.minecraft.world.entity.player.Player;

public final class ThemeCoreRuntimeGuardProvider implements EchoRuntimeGuardThemeProvider {
    public static final ThemeCoreRuntimeGuardProvider INSTANCE = new ThemeCoreRuntimeGuardProvider();

    private ThemeCoreRuntimeGuardProvider() {
    }

    @Override
    public float glowCostLevel(Player player) {
        return EchoThemeApi.getRenderProfile(player).glowIntensity();
    }

    @Override
    public boolean distortionEnabled(Player player) {
        return EchoThemeApi.getRenderProfile(player).distortionStrength() > 0.0F;
    }

    @Override
    public float particleIntensity(Player player) {
        return EchoThemeApi.getRenderProfile(player).particleIntensity();
    }

    @Override
    public float animationIntensity(Player player) {
        return EchoThemeApi.getRenderProfile(player).animationIntensity();
    }
}
