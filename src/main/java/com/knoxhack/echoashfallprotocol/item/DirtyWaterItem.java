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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Dirty Water Bottle — collected from water sources.
 * Drinking it gives brief nausea and emergency hydration.
 * Should be purified via Water Purifier machine.
 */
public class DirtyWaterItem extends Item {

    public DirtyWaterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Dirty water is now an emergency fallback, not a guaranteed punishment spiral.
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
            player.getFoodData().eat(1, 0.1f);
            var survivalData = player.getData(ModAttachments.SURVIVAL_DATA.get());
            survivalData.addHydration(20);
            player.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);
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
