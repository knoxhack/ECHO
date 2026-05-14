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
 * Boiled Water Bottle - produced by smelting dirty water.
 * Basic purification without advanced filtering.
 */
public class BoiledWaterItem extends Item {

    public BoiledWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Boiled water gives decent hydration but with a small penalty
            var survivalData = player.getData(ModAttachments.SURVIVAL_DATA.get());
            survivalData.addHydration(25);
            // Small thirst penalty - reduces hydration slightly after drinking
            survivalData.addHydration(-5);
            player.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);

            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.water.boiled_consumed"));
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
        tooltip.accept(Component.translatable("tooltip.EchoAshfallProtocol.boiled_water.desc"));
        tooltip.accept(Component.translatable("tooltip.EchoAshfallProtocol.boiled_water.hydration"));
    }
}
