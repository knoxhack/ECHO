package com.knoxhack.echoweathercore.block;

import com.knoxhack.echoweathercore.api.WeatherCoreApi;
import com.knoxhack.echoweathercore.api.weather.WeatherRouteRisk;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RouteWarningPostBlock extends Block {
    public RouteWarningPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            WeatherRouteRisk risk = WeatherCoreApi.getRouteWeatherRisk(level, pos, null);
            player.sendSystemMessage(Component.literal("Route Warning Post: Risk is " + risk));
        }
        return InteractionResult.SUCCESS;
    }
}
