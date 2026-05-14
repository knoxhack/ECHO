package com.knoxhack.echoweathercore.block;

import com.knoxhack.echoweathercore.api.WeatherCoreApi;
import com.knoxhack.echoweathercore.api.weather.WeatherPhase;
import com.knoxhack.echoweathercore.api.weather.WeatherType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EmergencySirenBlock extends Block {
    public EmergencySirenBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            boolean active = false;
            for (var type : WeatherType.values()) {
                if (WeatherCoreApi.isWeatherActive(level, type)) {
                    active = true;
                    break;
                }
            }
            player.sendSystemMessage(Component.literal("Emergency Siren: " + (active ? "ACTIVE WEATHER DETECTED" : "All clear.")));
        }
        return InteractionResult.SUCCESS;
    }
}
