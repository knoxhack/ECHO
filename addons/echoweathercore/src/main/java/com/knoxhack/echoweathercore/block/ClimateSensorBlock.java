package com.knoxhack.echoweathercore.block;

import com.knoxhack.echoweathercore.api.WeatherCoreApi;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ClimateSensorBlock extends Block {
    public ClimateSensorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            var modifiers = WeatherCoreApi.getWeatherModifiers(level, pos);
            boolean sheltered = WeatherCoreApi.isSheltered(level, pos);
            player.sendSystemMessage(Component.literal("Climate Sensor Reading:"));
            player.sendSystemMessage(Component.literal("Sheltered: " + sheltered));
            player.sendSystemMessage(Component.literal("Visibility: " + (int) (modifiers.visibilityMultiplier() * 100) + "%"));
            player.sendSystemMessage(Component.literal("Scanner Reliability: " + (int) (modifiers.scannerReliabilityMultiplier() * 100) + "%"));
        }
        return InteractionResult.SUCCESS;
    }
}
