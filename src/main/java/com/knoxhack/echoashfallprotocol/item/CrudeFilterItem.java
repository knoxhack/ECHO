package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.registry.ModItems;
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
 * Crude Filter - handheld filter for converting dirty water to filtered water.
 * Has 4 uses before breaking.
 */
public class CrudeFilterItem extends Item {

    private static final int MAX_USES = 4;

    public CrudeFilterItem(Properties properties) {
        super(properties.durability(MAX_USES));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack filterStack = player.getItemInHand(hand);

        // Look for dirty water in the player's inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.DIRTY_WATER_BOTTLE.get()) {
                if (!level.isClientSide()) {
                    // Convert dirty water to filtered water
                    stack.shrink(1);
                    ItemStack filteredWater = new ItemStack(ModItems.FILTERED_WATER_BOTTLE.get(), 1);

                    if (stack.isEmpty()) {
                        player.getInventory().setItem(i, filteredWater);
                    } else {
                        if (!player.getInventory().add(filteredWater)) {
                            player.drop(filteredWater, false);
                        }
                    }

                    // Damage the filter
                    filterStack.hurtAndBreak(1, player, hand);

                    player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.filter.crude_used"));
                }
                return InteractionResult.SUCCESS;
            }
        }

        // No dirty water found
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.filter.no_dirty_water"));
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.accept(Component.translatable("tooltip.EchoAshfallProtocol.crude_filter.desc", remainingUses));
    }
}
