package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.chat.Component;
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
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * RadAway — Medical aid to flush radiation buildup from the system.
 */
public class RadAwayItem extends Item {

    public RadAwayItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            var survivalData = serverPlayer.getData(ModAttachments.SURVIVAL_DATA.get());
            
            // Decays 50% of outstanding radiation
            survivalData.decayRadiation(survivalData.getRadiationLevel() * 0.5f);
            if (survivalData.getRadiationLevel() < 0) {
                survivalData.decayRadiation(Float.MAX_VALUE); // Zero out just in case
            }
            serverPlayer.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);

            // Minor regeneration to patch radiation burns
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false));

            // Sound effect - flushing/cleansing
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 0.9f);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 0.6f);

            // ECHO-7 Voice Line
            serverPlayer.sendSystemMessage(Component.literal(com.knoxhack.echoashfallprotocol.echo.EchoMessages.getMessage(
                    com.knoxhack.echoashfallprotocol.echo.EchoMessages.Context.RADAWAY_USED)));
            
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
