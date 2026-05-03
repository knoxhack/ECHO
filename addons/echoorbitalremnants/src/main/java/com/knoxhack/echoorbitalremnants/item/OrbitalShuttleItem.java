package com.knoxhack.echoorbitalremnants.item;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.world.LunarScarZone;
import com.knoxhack.echoorbitalremnants.world.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import java.util.Set;

public class OrbitalShuttleItem extends Item {
    public OrbitalShuttleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            if (SuitEvents.isOrbitalExposure(player) && progress.hasReturnPoint() && player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
                ServerLevel returnLevel = serverPlayer.level().getServer().getLevel(ModDimensions.keyFromString(progress.returnDimension()));
                if (returnLevel == null) {
                    returnLevel = serverPlayer.level().getServer().overworld();
                }
                serverPlayer.teleportTo(returnLevel, progress.returnX(), progress.returnY(), progress.returnZ(), Set.of(), player.getYRot(), player.getXRot(), false);
                player.sendSystemMessage(Component.literal("ECHO-7 // Shuttle return burn complete."));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (SuitEvents.isOrbitalExposure(player) && player.isShiftKeyDown() && !progress.hasReturnPoint()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Shuttle return denied. No docking vector is saved."));
                return InteractionResult.CONSUME;
            }
            if (Config.DIMENSION_UNLOCKS_ENABLED.get() && !progress.lunarSignalUnlocked() && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Shuttle lockout. Lunar Signal not resolved."));
                return InteractionResult.CONSUME;
            }
            if (Config.MID_GAME_OBJECTIVES_ENABLED.get() && !progress.stationNetworkGateOpen() && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Shuttle lockout. Repair three Station Relay Nodes to restore the Station Network."));
                return InteractionResult.CONSUME;
            }
            if (!SuitEvents.isOrbitalExposure(player) && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Shuttle requires orbital staging. Launch to Low Earth Orbit first."));
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel) {
                progress.setReturnPoint(player);
                ServerLevel targetLevel = ModDimensions.resolve(serverPlayer.level().getServer(), ModDimensions.LUNAR_SCAR_ZONE, serverLevel);
                double targetX = targetLevel.dimension() == ModDimensions.LUNAR_SCAR_ZONE ? 0.0D : player.getX() + 768.0D;
                double targetY = targetLevel.dimension() == ModDimensions.LUNAR_SCAR_ZONE ? 96.0D : Config.ORBITAL_ALTITUDE.get();
                double targetZ = targetLevel.dimension() == ModDimensions.LUNAR_SCAR_ZONE ? 0.0D : player.getZ() + 128.0D;
                BlockPos target = BlockPos.containing(targetX, targetY, targetZ);
                LunarScarZone.seedLandingSite(targetLevel, target);
                spawnNexusHusks(targetLevel, target);
                progress.markLunarSignalInvestigated(player);
                serverPlayer.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), false);
                player.sendSystemMessage(Component.literal("ECHO-7 // Lunar Scar Zone acquired. Gravity irregularities confirmed."));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void spawnNexusHusks(ServerLevel level, BlockPos target) {
        for (int i = 0; i < 3; i++) {
            Entity entity = (i == 0 ? ModEntities.LUNAR_NEXUS_HUSK.get() : ModEntities.NEXUS_HUSK.get()).create(level, EntitySpawnReason.EVENT);
            if (entity != null) {
                entity.setPos(target.getX() + 5 - i * 4, target.getY(), target.getZ() - 6 + i * 3);
                level.addFreshEntity(entity);
            }
        }
    }
}
