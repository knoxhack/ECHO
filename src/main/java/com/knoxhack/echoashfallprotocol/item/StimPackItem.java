package com.knoxhack.echoashfallprotocol.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class StimPackItem extends Item {
    public StimPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, false, true));

            // Enhanced sound - injection + power up
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 1.4f);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.3f, 1.8f);

            // Spawn adrenaline/speed particles
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Speed particles (white sparks)
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        8, 0.4, 0.4, 0.4, 0.1);
                // Green regeneration particles
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        player.getX(), player.getY() + 0.5, player.getZ(),
                        6, 0.3, 0.5, 0.3, 0.1);
            }

            player.sendSystemMessage(Component.literal(com.knoxhack.echoashfallprotocol.echo.EchoMessages.getMessage(
                    com.knoxhack.echoashfallprotocol.echo.EchoMessages.Context.STIMPAK_USED)));
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
