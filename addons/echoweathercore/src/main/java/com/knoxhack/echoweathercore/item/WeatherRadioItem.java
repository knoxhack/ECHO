package com.knoxhack.echoweathercore.item;

import com.knoxhack.echoweathercore.api.WeatherCoreApi;
import com.knoxhack.echoweathercore.api.forecast.WeatherForecast;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class WeatherRadioItem extends Item {
    public WeatherRadioItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            List<WeatherForecast> forecasts = WeatherCoreApi.getForecast(sp);
            if (!forecasts.isEmpty()) {
                player.sendSystemMessage(Component.literal("Weather Radio - Regional Forecast:"));
                for (WeatherForecast f : forecasts) {
                    player.sendSystemMessage(Component.literal(" - " + f.displayName() + " [" + f.phase() + ", " + f.severity() + "]"));
                }
            } else {
                player.sendSystemMessage(Component.literal("Weather Radio: No regional weather events."));
            }
            player.getCooldowns().addCooldown(stack, 40);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.literal("Shows regional weather forecast."));
    }
}
