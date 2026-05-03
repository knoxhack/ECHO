package com.knoxhack.echoorbitalremnants.item;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.LaunchReadiness;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.world.ModDimensions;
import com.knoxhack.echoorbitalremnants.world.OrbitalDebrisField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import java.util.Set;

public class EmergencyRocketItem extends Item {
    public EmergencyRocketItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            if (SuitEvents.isOrbitalExposure(player) && progress.hasEarthReturnPoint() && player instanceof ServerPlayer serverPlayer) {
                ServerLevel returnLevel = serverPlayer.level().getServer().getLevel(ModDimensions.keyFromString(progress.earthReturnDimension()));
                if (returnLevel == null) {
                    returnLevel = serverPlayer.level().getServer().overworld();
                }
                serverPlayer.teleportTo(returnLevel, progress.earthReturnX(), progress.earthReturnY(), progress.earthReturnZ(), Set.of(), player.getYRot(), player.getXRot(), false);
                player.sendSystemMessage(Component.literal("ECHO-7 // Emergency re-entry vector accepted."));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (SuitEvents.isOrbitalExposure(player) && !progress.hasEarthReturnPoint()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Re-entry denied. No Earth return vector is saved."));
                return InteractionResult.CONSUME;
            }

            LaunchReadiness readiness = LaunchReadiness.evaluateForLaunch(player);
            if (!readiness.ready()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // Launch abort. Missing launch checks:"));
                readiness.missing().stream().limit(8).forEach(player::sendSystemMessage);
                return InteractionResult.CONSUME;
            }

            progress.setEarthReturnPoint(player);
            progress.setReturnPoint(player);
            progress.markLaunchPrepared(player);
            progress.markLowOrbitReached(player);

            if (player instanceof ServerPlayer serverPlayer) {
                ServerLevel targetLevel = ModDimensions.resolve(serverPlayer.level().getServer(), ModDimensions.LOW_EARTH_ORBIT, serverPlayer.level());
                double orbitY = targetLevel.dimension() == ModDimensions.LOW_EARTH_ORBIT ? 96.0D : Math.max(player.getY() + 80.0D, Config.ORBITAL_ALTITUDE.get());
                BlockPos target = BlockPos.containing(player.getX(), orbitY, player.getZ());
                OrbitalDebrisField.seedArrivalField(targetLevel, target);
                Entity dockingAi = ModEntities.CORRUPTED_DOCKING_AI.get().create(targetLevel, EntitySpawnReason.EVENT);
                if (dockingAi != null) {
                    dockingAi.setPos(target.getX() + 6.0D, target.getY() + 1.0D, target.getZ() - 6.0D);
                    targetLevel.addFreshEntity(dockingAi);
                }
                serverPlayer.teleportTo(targetLevel, player.getX(), orbitY, player.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);
            }

            player.sendSystemMessage(Component.literal("ECHO-7 // Emergency Rocket ignition. Low Earth orbit acquired."));
            player.sendSystemMessage(Component.literal("\"I was not born in your pod. I fell with it.\""));
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
