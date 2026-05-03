package com.knoxhack.echoashfallprotocol.client;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.item.AshfallTooltip;
import com.knoxhack.echoashfallprotocol.registry.ModDataComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Renders Ashfall item tooltip data components without deprecated Item tooltip overrides.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID, value = Dist.CLIENT)
public final class AshfallTooltipHandler {
    private AshfallTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        AshfallTooltip tooltip = event.getItemStack().get(ModDataComponents.ASHFALL_TOOLTIP.get());
        if (tooltip == null) {
            return;
        }
        tooltip.addToTooltip(event.getContext(), event.getToolTip()::add, event.getFlags(),
                event.getItemStack().getComponents());
    }
}
