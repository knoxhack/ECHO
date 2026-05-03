package com.knoxhack.echoashfallprotocol.boss;

import com.knoxhack.echoashfallprotocol.network.BossNavigationPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BossHudSync {
    private static final double LIVE_SYNC_RADIUS = 160.0D;

    private BossHudSync() {
    }

    public static void syncBestTarget(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, BossHudTargetResolver.resolve(player));
    }

    public static void syncLiveBoss(LivingEntity boss, int phase) {
        if (!(boss.level() instanceof ServerLevel level)) {
            return;
        }
        BossHudProfiles.byEntityId(BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).toString())
                .map(profile -> BossHudTargetResolver.packetForLiveBoss(boss, profile, phase))
                .ifPresent(packet -> {
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                    boss.getBoundingBox().inflate(LIVE_SYNC_RADIUS), ServerPlayer::isAlive)) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        });
    }

    public static void clearBoss(ServerPlayer player, String bossId) {
        PacketDistributor.sendToPlayer(player, BossNavigationPacket.inactive(bossId));
    }
}
