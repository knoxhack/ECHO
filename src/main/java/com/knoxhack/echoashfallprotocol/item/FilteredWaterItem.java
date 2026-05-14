package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.chat.Component;
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
 * Filtered Water Bottle - produced by crude filter.
 * Better than dirty water but not as good as clean water.
 */
public class FilteredWaterItem extends Item {

    public FilteredWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Filtered water gives moderate hydration with a food penalty
            var survivalData = player.getData(ModAttachments.SURVIVAL_DATA.get());
            survivalData.addHydration(30);
            player.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);

            // Food penalty - less nutrition than clean water
            player.getFoodData().eat(1, 0.1f);

            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.water.filtered_consumed"));
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.accept(Component.translatable("tooltip.EchoAshfallProtocol.filtered_water.desc"));
        tooltip.accept(Component.translatable("tooltip.EchoAshfallProtocol.filtered_water.hydration"));
    }
}
