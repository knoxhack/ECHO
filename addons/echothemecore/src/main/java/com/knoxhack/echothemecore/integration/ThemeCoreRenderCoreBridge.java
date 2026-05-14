package com.knoxhack.echothemecore.integration;

import com.knoxhack.echothemecore.api.EchoRenderThemeProvider;
import com.knoxhack.echothemecore.api.EchoThemeApi;
import com.knoxhack.echothemecore.api.EchoThemeRenderColorKey;
import com.knoxhack.echothemecore.api.EchoThemeRenderIntensityKey;
import com.knoxhack.echothemecore.api.EchoThemeRenderProfile;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class ThemeCoreRenderCoreBridge {
    private static final EchoRenderThemeProvider PROVIDER = new Provider();

    private ThemeCoreRenderCoreBridge() {
    }

    public static boolean isRenderCoreLoaded() {
        return ModList.get().isLoaded("echorendercore");
    }

    public static EchoRenderThemeProvider provider() {
        return PROVIDER;
    }

    public static boolean registerIfAvailable() {
        return isRenderCoreLoaded();
    }

    private static final class Provider implements EchoRenderThemeProvider {
        @Override
        public Identifier getThemeId(Player player) {
            return player == null ? ThemeRegistry.globalThemeId() : EchoThemeApi.getThemeId(player);
        }

        @Override
        public EchoThemeRenderProfile getRenderProfile(Player player) {
            return player == null ? ThemeRegistry.getCurrentTheme().renderProfile() : EchoThemeApi.getRenderProfile(player);
        }

        @Override
        public int resolveColor(Player player, EchoThemeRenderColorKey key) {
            return getRenderProfile(player).color(key);
        }

        @Override
        public float resolveIntensity(Player player, EchoThemeRenderIntensityKey key) {
            return getRenderProfile(player).intensity(key);
        }
    }
}
