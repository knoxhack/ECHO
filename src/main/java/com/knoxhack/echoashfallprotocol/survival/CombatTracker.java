package com.knoxhack.echoashfallprotocol.survival;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = "echoashfallprotocol")
public class CombatTracker {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CombatData combatData = player.getData(ModAttachments.COMBAT_DATA);
        if (combatData != null) {
            combatData.onCombatTick(player.tickCount);
        }
    }
}
