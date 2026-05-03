package com.knoxhack.echoashfallprotocol.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

public class BandageItem extends Item {
    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            player.removeEffect(MobEffects.POISON);
            player.heal(2.0f);

            // Enhanced sound - bandage wrap + heal chime
            level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6f, 0.8f);
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4f, 1.5f);

            // Spawn healing particles
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        4, 0.3, 0.3, 0.3, 0.1);
            }

            player.sendSystemMessage(Component.literal(com.knoxhack.echoashfallprotocol.echo.EchoMessages.getMessage(
                    com.knoxhack.echoashfallprotocol.echo.EchoMessages.Context.BANDAGE_USED)));
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
