package com.knoxhack.echoorbitalremnants.suit;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.network.OrbitalEventVisualPayload;
import com.knoxhack.echoorbitalremnants.progression.OrbitalEventType;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoorbitalremnants.world.ModDimensions;
import com.knoxhack.echoorbitalremnants.world.OrbitalDebrisField;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class SuitEvents {
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean exposed = isOrbitalExposure(player);
        SuitState state = SuitState.get(player);
        boolean fullSuit = SuitState.hasFullPressureSuit(player);
        boolean magneticBoots = SuitState.hasMagneticBoots(player);
        boolean oxygenBooster = SuitState.hasOxygenBooster(player);
        state.updateEnvironment(fullSuit, magneticBoots, exposed);

        if (exposed && player.tickCount % Config.tunedSurvivalInterval(Config.OXYGEN_DRAIN_TICKS) == 0) {
            state.tickVacuum(player, fullSuit, magneticBoots, true, oxygenBooster);
            tryAutoRecovery(player, state);
        } else if (!exposed && player.tickCount % 80 == 0) {
            state.tickVacuum(player, fullSuit, magneticBoots, false, oxygenBooster);
        }

        if (exposed && player.tickCount % Config.tunedSurvivalInterval(Config.RADIATION_GAIN_TICKS) == 0) {
            state.addRadiation(SuitState.hasRadiationVisor(player));
        }

        if (exposed && !player.level().isClientSide() && player.tickCount % 100 == 0) {
            applyDimensionHazards(player, state);
        }

        if (exposed
                && player.level().dimension() == ModDimensions.EUROPA_CRYO_OCEAN
                && !SuitState.hasThermalLiner(player)
                && player.tickCount % 100 == 0) {
            state.compromisePressure(3);
        }

        if (exposed && !magneticBoots) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y < -0.08D) {
                player.setDeltaMovement(motion.x, -0.08D, motion.z);
                player.fallDistance = 0.0F;
            }
        }

        if (!player.level().isClientSide() && exposed) {
            handleServerOrbitalEffects(player, state);
            if (player.tickCount % 3600 == 0 && player.level() instanceof ServerLevel serverLevel) {
                OrbitalDebrisField.seedAmbientDebris(serverLevel, player.blockPosition());
            }
        }

        state.save(player);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().put("echoorbitalremnants_suit",
                event.getOriginal().getPersistentData().getCompoundOrEmpty("echoorbitalremnants_suit").copy());
        event.getEntity().getPersistentData().put("echoorbitalremnants_progress",
                event.getOriginal().getPersistentData().getCompoundOrEmpty("echoorbitalremnants_progress").copy());
    }

    public static boolean isOrbitalExposure(Player player) {
        return ModDimensions.isSpaceLevel(player.level()) || player.getY() >= Config.ORBITAL_ALTITUDE.get() - 32;
    }

    private static void tryAutoRecovery(Player player, SuitState state) {
        if (state.oxygen() <= 20 && state.backupAirAvailable() && consume(player, ModItems.EMERGENCY_OXYGEN_CELL.get())) {
            state.useEmergencyOxygen();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO-7 // Backup air cartridge engaged."));
        }

        if (state.pressure() <= 25 && state.autoSealAvailable() && consume(player, ModItems.SUIT_SEALANT_PATCH.get())) {
            state.applySealantPatch();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("ECHO-7 // Auto-seal patch applied. Suit pressure recovering."));
        }
    }

    private static void handleServerOrbitalEffects(Player player, SuitState state) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (state.critical()
                && player.tickCount % Config.tunedSurvivalInterval(Config.VACUUM_DAMAGE_TICKS) == 0
                && !player.hasInfiniteMaterials()) {
            player.hurtServer(serverLevel, player.damageSources().drown(), 2.0F);
        }

        int frequency = Config.tunedOrbitalEventFrequency();
        if (frequency > 0 && player.tickCount % frequency == 0) {
            OrbitalEventType[] events = OrbitalEventType.values();
            OrbitalEventType event = events[player.getRandom().nextInt(events.length)];
            player.sendSystemMessage(event.diagnosticMessage());
            sendOrbitalEventVisual(player, event);
            applyEventPressure(player, state, event);
            spawnOrbitalThreat(serverLevel, player, event);
        }

        if (Config.FEATURE_THREATS_ENABLED.get()
                && player.tickCount % 600 == 0
                && isFeatureThreatZone(player)
                && player.getRandom().nextInt(100) < Config.DEEP_SITE_THREAT_CHANCE.get()) {
            spawnFeatureThreat(serverLevel, player);
        }
    }

    private static void applyDimensionHazards(Player player, SuitState state) {
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        if (player.level().dimension() == ModDimensions.LOW_EARTH_ORBIT && !progress.orbitSurveyComplete()
                && nearbyBlock(player, 7, ModBlocks.BROKEN_SOLAR_PANEL.get(), ModBlocks.ORBITAL_PLATING.get(), ModBlocks.SATELLITE_PLATING.get(), ModBlocks.STATION_RELAY_NODE.get())) {
            state.drainOxygen(Config.tunedHazardDrain(nearbyBlock(player, 5, ModBlocks.DOCKING_BEACON.get()) ? 3 : 2));
            state.compromisePressure(Config.tunedHazardDrain(nearbyBlock(player, 7, ModBlocks.SATELLITE_PLATING.get()) ? 3 : 2));
        }
        if (player.level().dimension() == ModDimensions.LUNAR_SCAR_ZONE && !progress.moonSurveyComplete()
                && nearbyBlock(player, 7, ModBlocks.NEXUS_TOUCHED_STONE.get(), ModBlocks.NEXUS_GROWTH.get(), ModBlocks.NEXUS_DUST_BLOCK.get(), ModBlocks.SURVEY_MARKER.get(), ModBlocks.HELIUM_EXTRACTOR_NODE.get())) {
            for (int i = 0; i < Config.tunedHazardDrain(1); i++) {
                state.addRadiation(SuitState.hasRadiationVisor(player));
            }
        }
        if (player.level().dimension() == ModDimensions.MARS_ASH_BASIN && !progress.marsSurveyComplete()
                && !hasItem(player, ModItems.MARTIAN_PRESSURE_VALVE.get()) && !hasItem(player, ModItems.PRESSURE_REGULATOR.get())
                && !nearbyBlock(player, 6, ModBlocks.SIGNAL_RELAY.get(), ModBlocks.OXYGEN_PIPE.get(), ModBlocks.MARS_PRESSURE_CONSOLE.get())) {
            state.compromisePressure(Config.tunedHazardDrain(nearbyBlock(player, 7, ModBlocks.MARTIAN_DUST.get(), ModBlocks.MARTIAN_BASALT.get()) ? 4 : 2));
        }
        if (player.level().dimension() == ModDimensions.EUROPA_CRYO_OCEAN && !progress.europaSurveyComplete()
                && !nearbyBlock(player, 6, ModBlocks.THERMAL_VENT.get(), ModBlocks.EUROPA_THERMAL_ARRAY.get())) {
            state.drainOxygen(Config.tunedHazardDrain(1));
            if (!SuitState.hasThermalLiner(player)) {
                state.compromisePressure(Config.tunedHazardDrain(nearbyBlock(player, 7, ModBlocks.CRYO_ICE.get(), ModBlocks.PACKED_CRYO_ICE.get(), ModBlocks.FROZEN_CABLE.get()) ? 3 : 2));
            }
        }
        if (player.level().dimension() == ModDimensions.NEXUS_ANOMALY_BELT && !progress.nexusStabilized()
                && nearbyBlock(player, 8, ModBlocks.NEXUS_ANCHOR.get(), ModBlocks.NEXUS_GROWTH.get(), ModBlocks.NEXUS_TOUCHED_STONE.get())) {
            for (int i = 0; i < Config.tunedHazardDrain(1); i++) {
                state.addRadiation(false);
            }
            if (progress.echoZeroEncountered()) {
                state.compromisePressure(Config.tunedHazardDrain(1));
            }
        }
    }

    private static void applyEventPressure(Player player, SuitState state, OrbitalEventType event) {
        switch (event) {
            case DEBRIS_STORM -> state.compromisePressure(Config.tunedHazardDrain(4));
            case SOLAR_FLARE -> {
                for (int i = 0; i < Config.tunedHazardDrain(2); i++) {
                    state.addRadiation(SuitState.hasRadiationVisor(player));
                }
            }
            case STATION_BLACKOUT -> state.drainOxygen(Config.tunedHazardDrain(5));
            case NEXUS_PULSE -> {
                for (int i = 0; i < Config.tunedHazardDrain(1); i++) {
                    state.addRadiation(false);
                }
                state.compromisePressure(Config.tunedHazardDrain(3));
            }
        }
    }

    private static void sendOrbitalEventVisual(Player player, OrbitalEventType event) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        int overlay = switch (event) {
            case DEBRIS_STORM -> 0x665A5550;
            case SOLAR_FLARE -> 0x66FFD166;
            case STATION_BLACKOUT -> 0x99000000;
            case NEXUS_PULSE -> 0x664D28FF;
        };
        int particle = switch (event) {
            case DEBRIS_STORM -> 0xFFB7B0A0;
            case SOLAR_FLARE -> 0xFFFFF0A0;
            case STATION_BLACKOUT -> 0xFF66E8FF;
            case NEXUS_PULSE -> 0xFFE09CFF;
        };
        PacketDistributor.sendToPlayer(serverPlayer, new OrbitalEventVisualPayload(
                event.name(),
                overlay,
                particle,
                1.0F,
                player.getRandom().nextLong()));
    }

    private static void spawnOrbitalThreat(ServerLevel level, Player player, OrbitalEventType event) {
        Entity entity = switch (event) {
            case DEBRIS_STORM, SOLAR_FLARE -> ModEntities.ECHO_DEFENSE_DRONE.get().create(level, EntitySpawnReason.EVENT);
            case STATION_BLACKOUT -> ModEntities.VACUUM_WRAITH.get().create(level, EntitySpawnReason.EVENT);
            case NEXUS_PULSE -> ModEntities.BROKEN_ASTRONAUT.get().create(level, EntitySpawnReason.EVENT);
        };
        if (entity == null) {
            return;
        }
        double dx = player.getRandom().nextInt(11) - 5;
        double dz = player.getRandom().nextInt(11) - 5;
        entity.setPos(player.getX() + dx, player.getY() + 1.0D, player.getZ() + dz);
        entity.setYRot(player.getYRot());
        level.addFreshEntity(entity);
    }

    private static void spawnFeatureThreat(ServerLevel level, Player player) {
        Entity entity;
        if (player.level().dimension() == ModDimensions.LOW_EARTH_ORBIT || player.level().dimension() == ModDimensions.EUROPA_CRYO_OCEAN) {
            entity = player.level().dimension() == ModDimensions.EUROPA_CRYO_OCEAN && player.getRandom().nextInt(4) == 0
                    ? ModEntities.EUROPA_CRYO_WARDEN.get().create(level, EntitySpawnReason.EVENT)
                    : ModEntities.ECHO_DEFENSE_DRONE.get().create(level, EntitySpawnReason.EVENT);
        } else if (player.level().dimension() == ModDimensions.MARS_ASH_BASIN) {
            entity = ModEntities.BROKEN_ASTRONAUT.get().create(level, EntitySpawnReason.EVENT);
        } else if (player.level().dimension() == ModDimensions.LUNAR_SCAR_ZONE || player.level().dimension() == ModDimensions.NEXUS_ANOMALY_BELT) {
            entity = ModEntities.NEXUS_HUSK.get().create(level, EntitySpawnReason.EVENT);
        } else {
            entity = ModEntities.VACUUM_WRAITH.get().create(level, EntitySpawnReason.EVENT);
        }
        if (entity == null) {
            return;
        }
        double dx = player.getRandom().nextInt(9) - 4;
        double dz = player.getRandom().nextInt(9) - 4;
        entity.setPos(player.getX() + dx, player.getY() + 1.0D, player.getZ() + dz);
        level.addFreshEntity(entity);
    }

    public static boolean isFeatureThreatZone(Player player) {
        return nearbyBlock(player, 8,
                ModBlocks.BROKEN_SOLAR_PANEL.get(),
                ModBlocks.DOCKING_BEACON.get(),
                ModBlocks.STATION_RELAY_NODE.get(),
                ModBlocks.NEXUS_TOUCHED_STONE.get(),
                ModBlocks.HELIUM_EXTRACTOR_NODE.get(),
                ModBlocks.MARTIAN_DUST.get(),
                ModBlocks.MARTIAN_BASALT.get(),
                ModBlocks.MARS_PRESSURE_CONSOLE.get(),
                ModBlocks.FROZEN_CABLE.get(),
                ModBlocks.EUROPA_THERMAL_ARRAY.get(),
                ModBlocks.NEXUS_GROWTH.get(),
                ModBlocks.NEXUS_ANCHOR.get());
    }

    private static boolean consume(Player player, net.minecraft.world.item.Item item) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean hasItem(Player player, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearbyBlock(Player player, int radius, net.minecraft.world.level.block.Block... blocks) {
        net.minecraft.core.BlockPos center = player.blockPosition();
        for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
            net.minecraft.world.level.block.Block current = player.level().getBlockState(pos).getBlock();
            for (net.minecraft.world.level.block.Block block : blocks) {
                if (current == block) {
                    return true;
                }
            }
        }
        return false;
    }
}
