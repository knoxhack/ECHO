package com.knoxhack.echoashfallprotocol.client;

import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Adds lightweight discoverability for all Substrate Grinder inputs.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID, value = Dist.CLIENT)
public final class SubstrateTooltipHandler {
    private SubstrateTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        OreGrinderBlockEntity.GrinderRecipe recipe = OreGrinderBlockEntity.getSubstrateRecipe(stack);
        if (recipe == null) {
            return;
        }

        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add(Component.literal(stack.getItem() instanceof BlockItem ? recipe.categoryLabel() : "Substrate Grinder input")
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(recipeLine(recipe));
        if (recipe.byproduct() != null) {
            tooltip.add(Component.literal(Math.round(recipe.byproductChance() * 100.0F) + "% side output: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(itemName(recipe.byproduct(), recipe.byproductCount()).withStyle(ChatFormatting.GRAY)));
        }
        if (Config.VERBOSE_TOOLTIPS.get()) {
            tooltip.add(Component.literal(recipe.handlingHint())
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal(recipe.powerPerOperation() + " FE / " + recipe.processTime() + " ticks")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static Component recipeLine(OreGrinderBlockEntity.GrinderRecipe recipe) {
        MutableComponent line = Component.literal("Grinder: ").withStyle(ChatFormatting.GRAY);
        line.append(Component.literal(recipe.inputCount() + "x").withStyle(ChatFormatting.YELLOW));
        line.append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_GRAY));
        line.append(itemName(recipe.output(), recipe.outputCount()).withStyle(ChatFormatting.WHITE));
        return line;
    }

    private static MutableComponent itemName(Item item, int count) {
        return Component.literal(count + "x " + new ItemStack(item).getHoverName().getString());
    }
}
