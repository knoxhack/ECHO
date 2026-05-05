package com.knoxhack.echoorbitalremnants.item;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.world.EuropaCryoOcean;
import com.knoxhack.echoorbitalremnants.world.MarsAshBasin;
import com.knoxhack.echoorbitalremnants.world.ModDimensions;
import java.util.Set;
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

public class PlanetaryRouteItem extends Item {
    private final Target target;

    public PlanetaryRouteItem(Target target, Properties properties) {
        super(properties);
        this.target = target;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            if (SuitEvents.isOrbitalExposure(player) && progress.hasReturnPoint() && player instanceof ServerPlayer serverPlayer
                    && player.isShiftKeyDown()) {
                ServerLevel returnLevel = serverPlayer.level().getServer().getLevel(ModDimensions.keyFromString(progress.returnDimension()));
                if (returnLevel == null) {
                    returnLevel = serverPlayer.level().getServer().overworld();
                }
                serverPlayer.teleportTo(returnLevel, progress.returnX(), progress.returnY(), progress.returnZ(), Set.of(), player.getYRot(), player.getXRot(), false);
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " return vector committed."));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (SuitEvents.isOrbitalExposure(player) && player.isShiftKeyDown() && !progress.hasReturnPoint()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " return denied. No docking vector is saved; mark one before drifting farther."));
                return InteractionResult.CONSUME;
            }

            if (Config.DIMENSION_UNLOCKS_ENABLED.get() && !target.unlocked(progress) && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " route locked. Terminal telemetry lacks the handoff proof."));
                return InteractionResult.CONSUME;
            }
            if (Config.MID_GAME_OBJECTIVES_ENABLED.get() && !target.midGameReady(progress) && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " route locked. " + target.midGameLockMessage()));
                return InteractionResult.CONSUME;
            }
            if (!SuitEvents.isOrbitalExposure(player) && !player.hasInfiniteMaterials()) {
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " requires orbital staging. Launch to Low Earth Orbit before burning this route."));
                return InteractionResult.CONSUME;
            }

            if (player instanceof ServerPlayer serverPlayer && serverPlayer.level() instanceof ServerLevel serverLevel) {
                progress.setReturnPoint(player);
                ServerLevel targetLevel = ModDimensions.resolve(serverPlayer.level().getServer(), target.dimension, serverLevel);
                double targetX = targetLevel.dimension() == target.dimension ? 0.0D : player.getX() + target.fallbackX;
                double targetY = targetLevel.dimension() == target.dimension ? 96.0D : Config.ORBITAL_ALTITUDE.get();
                double targetZ = targetLevel.dimension() == target.dimension ? 0.0D : player.getZ() + target.fallbackZ;
                BlockPos arrival = BlockPos.containing(targetX, targetY, targetZ);
                target.seed(targetLevel, arrival);
                target.spawnThreats(targetLevel, arrival);
                target.mark(progress, player);
                serverPlayer.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), false);
                player.sendSystemMessage(Component.literal("ECHO-7 // " + target.displayName + " route acquired. " + target.arrivalMessage));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public enum Target {
        MARS("Mars Ash Basin", ModDimensions.MARS_ASH_BASIN, 2048.0D, -256.0D,
                "Terraforming towers are dead, but the dust still answers.") {
            @Override
            boolean unlocked(EchoTerminalProgress progress) {
                return progress.marsRouteUnlocked();
            }

            @Override
            boolean midGameReady(EchoTerminalProgress progress) {
                return progress.lunarExtractorGateOpen();
            }

            @Override
            String midGameLockMessage() {
                return "Restore three Helium Extractor Nodes before Mars transfer stops guessing.";
            }

            @Override
            void seed(ServerLevel level, BlockPos arrival) {
                MarsAshBasin.seedLandingSite(level, arrival);
            }

            @Override
            void spawnThreats(ServerLevel level, BlockPos arrival) {
                spawn(level, arrival.offset(6, 1, -7), ModEntities.ABANDONED_CAPTAIN.get());
                spawn(level, arrival.offset(-5, 1, 6), ModEntities.BROKEN_ASTRONAUT.get());
                spawn(level, arrival.offset(7, 1, 5), ModEntities.NEXUS_HUSK.get());
            }

            @Override
            void mark(EchoTerminalProgress progress, Player player) {
                progress.markMarsAshBasinVisited(player);
            }
        },
        EUROPA("Europa Cryo Ocean", ModDimensions.EUROPA_CRYO_OCEAN, -1536.0D, 640.0D,
                "Sub-ice labs detected below the fracture line.") {
            @Override
            boolean unlocked(EchoTerminalProgress progress) {
                return progress.europaRouteUnlocked();
            }

            @Override
            boolean midGameReady(EchoTerminalProgress progress) {
                return progress.marsHabitatGateOpen();
            }

            @Override
            String midGameLockMessage() {
                return "Repair three Mars Pressure Consoles before Europa prep stops bleeding pressure.";
            }

            @Override
            void seed(ServerLevel level, BlockPos arrival) {
                EuropaCryoOcean.seedLandingSite(level, arrival);
            }

            @Override
            void spawnThreats(ServerLevel level, BlockPos arrival) {
                spawn(level, arrival.offset(-6, 1, -5), ModEntities.VACUUM_WRAITH.get());
                spawn(level, arrival.offset(6, 1, 5), ModEntities.NEXUS_HUSK.get());
                spawn(level, arrival.offset(0, 1, 9), ModEntities.ECHO_DEFENSE_DRONE.get());
                spawn(level, arrival.offset(2, 2, -9), ModEntities.EUROPA_CRYO_WARDEN.get());
            }

            @Override
            void mark(EchoTerminalProgress progress, Player player) {
                progress.markEuropaCryoOceanVisited(player);
            }
        };

        private final String displayName;
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final double fallbackX;
        private final double fallbackZ;
        private final String arrivalMessage;

        Target(String displayName, net.minecraft.resources.ResourceKey<Level> dimension, double fallbackX, double fallbackZ, String arrivalMessage) {
            this.displayName = displayName;
            this.dimension = dimension;
            this.fallbackX = fallbackX;
            this.fallbackZ = fallbackZ;
            this.arrivalMessage = arrivalMessage;
        }

        abstract boolean unlocked(EchoTerminalProgress progress);

        abstract boolean midGameReady(EchoTerminalProgress progress);

        abstract String midGameLockMessage();

        abstract void seed(ServerLevel level, BlockPos arrival);

        abstract void spawnThreats(ServerLevel level, BlockPos arrival);

        abstract void mark(EchoTerminalProgress progress, Player player);

        private static void spawn(ServerLevel level, BlockPos pos, net.minecraft.world.entity.EntityType<?> type) {
            Entity entity = type.create(level, EntitySpawnReason.EVENT);
            if (entity != null) {
                entity.setPos(pos.getX(), pos.getY(), pos.getZ());
                level.addFreshEntity(entity);
            }
        }
    }
}
