package com.knoxhack.echoashfallprotocol.entity.drone;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class DroneCombatEventHandler {
    private static final double MARK_SCAN_RADIUS = 64.0D;
    private static final float MARK_DAMAGE_MULTIPLIER = 1.25F;

    private DroneCombatEventHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target == player || target.level().isClientSide()) {
            return;
        }

        boolean markedByOwnedDrone = !target.level().getEntitiesOfClass(
                EchoCompanionDrone.class,
                target.getBoundingBox().inflate(MARK_SCAN_RADIUS),
                drone -> player.getUUID().equals(drone.getOwnerUUID()) && drone.hasMarkedTarget(target)
        ).isEmpty();

        if (markedByOwnedDrone) {
            event.setNewDamage(event.getNewDamage() * MARK_DAMAGE_MULTIPLIER);
        }
    }
}
