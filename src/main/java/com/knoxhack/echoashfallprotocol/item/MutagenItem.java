package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.gameplay.RadiationHelper;
import com.knoxhack.echoashfallprotocol.survival.MutationManager;
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
 * Mutagen Vial — A dangerous consumable yielding a permanent genetic buff but inflicting sickness.
 */
public class MutagenItem extends Item {

    public MutagenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Mutagen adds massive radiation directly
            RadiationHelper.addRadiation(serverPlayer, 40.0f);

            // Nausea to represent genetic shock
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1, false, false));
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, false));

            // ECHO-7 Warning Voice Line
            serverPlayer.sendSystemMessage(Component.literal(com.knoxhack.echoashfallprotocol.echo.EchoMessages.getMessage(
                    com.knoxhack.echoashfallprotocol.echo.EchoMessages.Context.MUTAGEN_USED)));

            // Force a mutation automatically if possible
            MutationManager.tryMutate(serverPlayer, 100.0f);

            // Consume
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
