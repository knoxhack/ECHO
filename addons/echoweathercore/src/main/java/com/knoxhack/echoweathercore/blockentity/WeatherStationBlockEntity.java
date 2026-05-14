package com.knoxhack.echoweathercore.blockentity;

import com.knoxhack.echoweathercore.api.WeatherCoreApi;
import com.knoxhack.echoweathercore.api.forecast.WeatherForecast;
import com.knoxhack.echoweathercore.registry.WeatherCoreBlockEntities;
import com.knoxhack.echoweathercore.server.WeatherForecastManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WeatherStationBlockEntity extends BlockEntity {
    public WeatherStationBlockEntity(BlockPos pos, BlockState state) {
        super(WeatherCoreBlockEntities.WEATHER_STATION.get(), pos, state);
    }

    public void onUse(Player player) {
        if (level == null || level.isClientSide()) return;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;

        List<WeatherForecast> forecasts = WeatherCoreApi.getForecast(sp);
        if (!forecasts.isEmpty()) {
            player.sendSystemMessage(Component.literal("=== Weather Station Forecast ==="));
            for (WeatherForecast f : forecasts) {
                player.sendSystemMessage(Component.literal(" - " + f.displayName() + " [" + f.phase() + "]"));
            }
        } else {
            player.sendSystemMessage(Component.literal("Weather Station: No active or forecasted weather."));
        }
    }
}
