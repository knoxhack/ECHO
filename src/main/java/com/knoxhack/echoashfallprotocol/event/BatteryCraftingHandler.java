package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.item.BatteryItem;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class BatteryCraftingHandler {
    private BatteryCraftingHandler() {
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (crafted.is(ModItems.ADVANCED_BATTERY.get())) {
            preserveInputBatteryEnergy(crafted, event.getInventory(), ModItems.BASIC_BATTERY.get().getDefaultInstance());
        } else if (crafted.is(ModItems.ELITE_BATTERY.get())) {
            preserveInputBatteryEnergy(crafted, event.getInventory(), ModItems.ADVANCED_BATTERY.get().getDefaultInstance());
        }
    }

    private static void preserveInputBatteryEnergy(ItemStack crafted, Container inventory, ItemStack sourceBattery) {
        int stored = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(sourceBattery.getItem())) {
                stored += BatteryItem.getStoredEnergy(stack);
            }
        }
        if (stored > 0) {
            BatteryItem.setStoredEnergy(crafted, stored);
        }
    }
}
