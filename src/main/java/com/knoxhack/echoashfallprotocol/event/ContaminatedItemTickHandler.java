package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.item.ContaminatedItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Applies poison to players carrying contaminated resource items.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class ContaminatedItemTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 40 != 0) return;

        boolean hasContaminated = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof ContaminatedItem) {
                hasContaminated = true;
                break;
            }
        }

        if (hasContaminated) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
        }
    }
}
