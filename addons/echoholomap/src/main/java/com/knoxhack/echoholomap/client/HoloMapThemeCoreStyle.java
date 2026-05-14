package com.knoxhack.echoholomap.client;

import net.minecraft.world.entity.player.Player;

final class HoloMapThemeCoreStyle {
    private HoloMapThemeCoreStyle() {
    }

    static int color(Player player, String method, int fallback) {
        if (player == null) {
            return fallback;
        }
        try {
            Class<?> api = Class.forName("com.knoxhack.echothemecore.api.EchoThemeApi");
            Object colors = api.getMethod("getColors", Player.class).invoke(null, player);
            return ((Integer) colors.getClass().getMethod(method).invoke(colors)).intValue();
        } catch (ReflectiveOperationException | LinkageError exception) {
            return fallback;
        }
    }

    static float intensity(Player player, String method, float fallback) {
        if (player == null) {
            return fallback;
        }
        try {
            Class<?> api = Class.forName("com.knoxhack.echothemecore.api.EchoThemeApi");
            Object render = api.getMethod("getRenderProfile", Player.class).invoke(null, player);
            return ((Float) render.getClass().getMethod(method).invoke(render)).floatValue();
        } catch (ReflectiveOperationException | LinkageError exception) {
            return fallback;
        }
    }
}
