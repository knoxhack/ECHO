package com.knoxhack.echoweathercore.server;

import com.knoxhack.echoweathercore.api.weather.WeatherEffectModifiers;
import com.knoxhack.echoweathercore.api.weather.WeatherType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class WeatherCountermeasureManager {
    private static final Map<WeatherType, WeatherEffectModifiers> COUNTERMEASURES = new EnumMap<>(WeatherType.class);

    static {
        COUNTERMEASURES.put(WeatherType.ASH_STORM, new WeatherEffectModifiers(
            1.0, 1.15, 1.15, 1.1, 0.85, 1.0, 1.0, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.15, 1.0, 1.0, 1.0, 1.0, 0.85
        ));
        COUNTERMEASURES.put(WeatherType.TOXIC_RAIN, new WeatherEffectModifiers(
            1.0, 1.0, 1.0, 1.0, 0.7, 1.0, 0.6, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.75
        ));
        COUNTERMEASURES.put(WeatherType.RADIATION_STORM, new WeatherEffectModifiers(
            1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.6
        ));
        COUNTERMEASURES.put(WeatherType.CRYO_FRONT, new WeatherEffectModifiers(
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 1.0, 1.0,
            1.0, 1.0, 1.2, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.8
        ));
        COUNTERMEASURES.put(WeatherType.NEXUS_SIGNAL_STORM, new WeatherEffectModifiers(
            1.0, 1.2, 1.2, 1.2, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.2, 1.0, 1.0, 1.0, 1.0, 0.9
        ));
        COUNTERMEASURES.put(WeatherType.ELECTROMAGNETIC_BLACKOUT, new WeatherEffectModifiers(
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
            1.0, 0.6, 1.15, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.85
        ));
    }

    private WeatherCountermeasureManager() {}

    public static WeatherEffectModifiers getCountermeasureModifiers(WeatherType type) {
        return COUNTERMEASURES.getOrDefault(type, WeatherEffectModifiers.DEFAULT);
    }

    public static boolean isCountermeasureItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("filter") || path.contains("anchor") || path.contains("coil") || path.contains("cell");
    }
}
