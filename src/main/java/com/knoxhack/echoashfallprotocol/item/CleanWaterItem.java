package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Clean Water Bottle - produced by the Water Purifier.
 * Restores hydration and gives minor hunger restoration.
 */
public class CleanWaterItem extends Item {

    public CleanWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Clean water restores hydration
            var survivalData = player.getData(ModAttachments.SURVIVAL_DATA.get());
            survivalData.addHydration(40);
            player.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);

            player.getFoodData().eat(2, 0.3f);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 0.5f, 1.0f);
            if (player instanceof ServerPlayer serverPlayer) {
                QuestData quest = QuestData.get(serverPlayer);
                quest.visitLocation("special", "water:clean_consumed");
                QuestData.saveAndSync(serverPlayer, quest);
            }
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.water.clean_consumed"));
            consumeBottle(player, hand, stack);
        }

        return InteractionResult.SUCCESS;
    }

    private static void consumeBottle(Player player, InteractionHand hand, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return;
        }

        stack.shrink(1);
        ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, emptyBottle);
        } else if (!player.getInventory().add(emptyBottle)) {
            player.drop(emptyBottle, false);
        }
    }
}
