package com.knoxhack.echoashfallprotocol.survival;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.gameplay.RadiationHelper;
import com.knoxhack.echoashfallprotocol.item.GasMaskItem;
import com.knoxhack.echoashfallprotocol.item.HazmatArmorItem;
import com.knoxhack.echoashfallprotocol.network.GraceCountdownPacket;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.world.StartingDropPodData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Server-side tick handler for all survival systems:
 * - Toxic-zone filter drain
 * - Radiation accumulation/decay
 * - Hydration loss
 * - Environmental effects
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class SurvivalTickHandler {

    private static final int TICK_INTERVAL = 20; // Once per second
    private static final String GRACE_PERIOD_NOTICE_SHOWN_KEY = "ashes_of_tomorrow.grace_period_notice_shown";
    private static final String SURVIVAL_WARNING_TICK_KEY = "ashes_of_tomorrow.survival_warning_tick";
    private static final String HYDRATION_DECAY_TICK_KEY = "ashes_of_tomorrow.hydration_decay_tick";
    private static final String TOXIC_AIR_WARNING_TICK_KEY = "ashes_of_tomorrow.toxic_air_warning_tick";
    private static final String LAST_MUTATION_ROLL_TICK_KEY = "ashes_of_tomorrow.last_mutation_roll_tick";
    private static final String SEVERE_RADIATION_TICKS_KEY = "ashes_of_tomorrow.severe_radiation_ticks";
    private static final String OPENING_PROMPT_PREFIX = "opening:";
    private static final String DAMAGE_HINT_PREFIX = "death_hint:";
    private static final int GRACE_ENDING_WARNING_TICKS = 20 * 60; // 1 minute
    private static final int SURVIVAL_WARNING_COOLDOWN_TICKS = 20 * 12;
    private static final int OPENING_WATER_PROMPT_TICKS = 20 * 90;
    private static final int POD_RETURN_RADIUS_BLOCKS = 48;
    private static final int SEVERE_RADIATION_SUSTAINED_TICKS = 20 * 30;
    private static final float SCRUBBER_RADIATION_DECAY_MULTIPLIER = 4.0f;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long currentTick = player.level().getGameTime();
        if (currentTick % TICK_INTERVAL != 0) return;

        SurvivalData data = player.getData(ModAttachments.SURVIVAL_DATA.get());
        boolean changed = false;
        int graceTicks = Math.max(0, Config.NEW_PLAYER_GRACE_TICKS.get());

        if (!data.isGraceInitialized()) {
            data.initializeGrace(currentTick);
            changed = true;
        }

        boolean graceActive = data.isGraceActive(currentTick, graceTicks);
        changed |= handleGraceMessages(player, data, currentTick, graceTicks, graceActive);

        syncGraceCountdown(player, data, currentTick, graceTicks, graceActive);
        handleOpeningGuidance(player, data, currentTick, graceTicks, graceActive);
        handleGraceExpiryNotice(player, data, currentTick, graceTicks, graceActive);

        HazardZoneManager.HazardSnapshot hazardState = HazardZoneManager.scan(player);
        changed |= data.setHazardSnapshot(hazardState);

        // === AIR FILTER SYSTEM ===
        changed |= handleAirFilter(player, data, graceActive, hazardState, currentTick);

        // === RADIATION SYSTEM ===
        changed |= handleRadiation(player, data, graceActive, hazardState, currentTick);

        // === CRYO / TEMPERATURE SYSTEM ===
        handleCold(player, hazardState, graceActive);

        // === HYDRATION SYSTEM ===
        if (shouldRunHydrationDecay(player, currentTick)) {
            changed |= handleHydration(player, data, graceActive);
        }

        if (!graceActive) {
            sendSurvivalFeedback(player, data, currentTick, hazardState);
        }

        if (changed) {
            player.setData(ModAttachments.SURVIVAL_DATA.get(), data);
            player.syncData(ModAttachments.SURVIVAL_DATA.get());
        }
    }

    private static boolean handleGraceMessages(ServerPlayer player, SurvivalData data, long currentTick, int graceTicks, boolean graceActive) {
        if (graceTicks <= 0) return false;

        boolean changed = false;
        if (graceActive && !data.isGraceStartMessageSent()) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.grace.start"));
            data.setGraceStartMessageSent(true);
            changed = true;
        }

        long remaining = data.getGraceTicksRemaining(currentTick, graceTicks);
        if (graceActive && remaining <= GRACE_ENDING_WARNING_TICKS && !data.isGraceEndingMessageSent()) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.grace.expiring"));
            data.setGraceEndingMessageSent(true);
            changed = true;
        }

        return changed;
    }

    private static void handleOpeningGuidance(ServerPlayer player, SurvivalData data, long currentTick, int graceTicks, boolean graceActive) {
        if (!graceActive || graceTicks <= 0 || !data.isGraceInitialized()) {
            return;
        }

        long elapsed = currentTick - data.getGraceStartTick();
        OpeningBasics basics = OpeningBasics.of(player, data);

        if (elapsed >= OPENING_WATER_PROMPT_TICKS && !basics.hasCleanWaterHandled() && markSpecialOnce(player, OPENING_PROMPT_PREFIX + "drink_water")) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.opening.drink_water"), true);
        }

        if (!basics.hasCleanWaterHandled() && isTooFarFromStartingPod(player) && markSpecialOnce(player, OPENING_PROMPT_PREFIX + "return_pod")) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.opening.return_pod"), true);
        }

        if (isNightApproaching(player) && !basics.hasShelter() && markSpecialOnce(player, OPENING_PROMPT_PREFIX + "shelter")) {
            player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.opening.shelter"), true);
        }

        sendCheckpointPrompt(player, data, currentTick, graceTicks, 20 * 60 * 5, "5m", basics);
        sendCheckpointPrompt(player, data, currentTick, graceTicks, 20 * 60 * 2, "2m", basics);
        sendCheckpointPrompt(player, data, currentTick, graceTicks, 20 * 60, "1m", basics);
    }

    private static void sendCheckpointPrompt(ServerPlayer player, SurvivalData data, long currentTick, int graceTicks,
                                             int thresholdTicks, String label, OpeningBasics basics) {
        long remaining = data.getGraceTicksRemaining(currentTick, graceTicks);
        if (remaining > thresholdTicks || basics.isStable()) {
            return;
        }

        String marker = OPENING_PROMPT_PREFIX + "buffer_" + label;
        if (markSpecialOnce(player, marker)) {
            player.sendSystemMessage(Component.translatable(
                    "message.EchoAshfallProtocol.opening.buffer_checkpoint",
                    label,
                    basics.missingList()), true);
        }
    }

    private static boolean handleAirFilter(ServerPlayer player, SurvivalData data, boolean graceActive,
                                           HazardZoneManager.HazardSnapshot hazardState, long currentTick) {
        boolean hasGasMask = player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof GasMaskItem;
        boolean hasFullHazmatSuit = HazardZoneManager.hasFullHazmat(player);
        boolean hasMask = hasGasMask || hasFullHazmatSuit;
        boolean changed = data.hasMask() != hasMask;
        data.setHasMask(hasMask);

        boolean inSafeZone = hazardState.safeZone();

        if (hasFullHazmatSuit || graceActive || !hazardState.toxicAir()) {
            if (data.getToxicAirWarningTicks() > 0) {
                data.setToxicAirWarningTicks(0);
                changed = true;
            }
            return changed;
        }

        if (hasGasMask && data.getAirFilterLife() > 0) {
            // Filters drain only while actually filtering toxic hazard air.
            int degradeAmount = Math.max(0, Math.round(Config.AIR_FILTER_DECAY_RATE.get()
                    * Math.max(1.0f, hazardState.toxicIntensity())));
            if (degradeAmount > 0) {
                data.decrementFilter(degradeAmount);
                changed = true;
            }
            if (data.getToxicAirWarningTicks() > 0) {
                data.setToxicAirWarningTicks(0);
                changed = true;
            }
            return changed;
        } else if ((!hasGasMask || data.isFilterDepleted()) && !inSafeZone) {
            int warningTicks = data.getToxicAirWarningTicks();
            int warningLimit = Math.max(0, Config.TOXIC_AIR_WARNING_TICKS.get());
            if (warningTicks < warningLimit) {
                data.setToxicAirWarningTicks(warningTicks + TICK_INTERVAL);
                sendToxicAirWarning(player, currentTick);
                return true;
            }
            // No protection and not in safe zone - take damage.
            if (currentTick % 60 == 0) { // Every 3 seconds
                sendDamageHintOnce(player, "toxic_air", Component.translatable("message.EchoAshfallProtocol.death_hint.toxic_air"));
                player.hurtServer((ServerLevel) player.level(), player.damageSources().magic(), 1.0f);
            }
            // Apply breathing debuffs
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false));
        } else if (inSafeZone && !hasGasMask) {
            if (player.level().getGameTime() % 200 == 0) {
                player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.hazard.scrubber_safe"), true);
            }
        }

        return changed;
    }

    private static boolean handleRadiation(ServerPlayer player, SurvivalData data, boolean graceActive,
                                           HazardZoneManager.HazardSnapshot hazardState, long currentTick) {
        boolean nearRadiation = hazardState.radiationZone();
        if (nearRadiation) {
            recordRadiationZoneScout(player);
        }

        if (nearRadiation && !hazardState.safeZone() && !graceActive) {
            // Check if player has rad resistance mutation
            MutationData mutations = player.getData(ModAttachments.MUTATION_DATA.get());
            double baseRadRate = Config.RADIATION_ACCUMULATION_RATE.get();
            if (baseRadRate < 0) baseRadRate = 1.0;
            float radAmount = (float) (baseRadRate * Math.max(0.25f, hazardState.radiationIntensity()));
            if (mutations.hasMutation(MutationData.MutationType.RAD_RESISTANCE)) {
                radAmount *= 0.5f;
            }
            radAmount = RadiationHelper.scaleIncomingRadiation(player, radAmount);
            radAmount *= Math.max(0.0f, 1.0f - HazardZoneManager.radiationResistance(player));
            if (radAmount > 0.0f) {
                sendDamageHintOnce(player, "radiation", Component.translatable("message.EchoAshfallProtocol.death_hint.radiation"));
                data.addRadiation(radAmount);
            }
        } else {
            // Natural radiation decay (configurable via Config.RADIATION_DECAY_RATE)
            double decayRate = Config.RADIATION_DECAY_RATE.get();
            float decayAmount = decayRate > 0 ? (float) decayRate : 0.2f;
            if (hazardState.safeZone()) {
                decayAmount *= SCRUBBER_RADIATION_DECAY_MULTIPLIER;
            }
            data.decayRadiation(decayAmount);
        }

        if (!graceActive) {
            // Apply radiation effects at high levels
            if (data.getRadiationLevel() > 75.0f) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0, false, false));
            }
            if (data.getRadiationLevel() > 90.0f) {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, false));
            }

            handleMutationRoll(player, data, currentTick);

            // Apply ongoing passive mutation benefits every second
            MutationManager.applyMutationEffects(player);
            // Apply ongoing side effects from mutations
            MutationData mutations = player.getData(ModAttachments.MUTATION_DATA.get());
            MutationManager.applySideEffects(player, mutations);
        }

        // Modernization - Sample radiation for the Terminal Graph
        data.updateRadiationHistory(data.getRadiationLevel());

        return true;
    }

    private static void handleCold(ServerPlayer player, HazardZoneManager.HazardSnapshot hazardState, boolean graceActive) {
        ColdData coldData = player.getData(ModAttachments.COLD_DATA.get());
        if (coldData.update(player, hazardState, graceActive)) {
            player.setData(ModAttachments.COLD_DATA.get(), coldData);
            player.syncData(ModAttachments.COLD_DATA.get());
        }
    }

    private static boolean handleHydration(ServerPlayer player, SurvivalData data, boolean graceActive) {
        if (graceActive) {
            return false;
        }

        // Hydration decreases over time (configurable)
        int decayRate = Config.HYDRATION_DECAY_RATE.get();
        if (decayRate > 0) {
            data.decrementHydration(decayRate);
        }

        int penaltyLevel = Math.max(0, Math.min(100, Config.HYDRATION_PENALTY_LEVEL.get()));
        if (data.getHydration() <= penaltyLevel) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, false, false));
        }
        if (data.getHydration() <= 0) {
            // Critical dehydration - take damage.
            if (player.level().getGameTime() % 80 == 0) {
                sendDamageHintOnce(player, "dehydration", Component.translatable("message.EchoAshfallProtocol.death_hint.dehydration"));
                player.hurtServer((net.minecraft.server.level.ServerLevel) player.level(), player.damageSources().starve(), 1.0f);
            }
        }

        return true;
    }

    private static void sendToxicAirWarningLegacy(ServerPlayer player, long currentTick) {
        CompoundTag playerData = player.getPersistentData();
        long nextWarningTick = playerData.getLong(TOXIC_AIR_WARNING_TICK_KEY).orElse(0L);
        if (currentTick < nextWarningTick) {
            return;
        }
        player.sendSystemMessage(Component.literal("\u00A7c[ECHO-7]\u00A7r Toxic air exposure. Seal mask or reach scrubbed air."), true);
        playerData.putLong(TOXIC_AIR_WARNING_TICK_KEY, currentTick + SURVIVAL_WARNING_COOLDOWN_TICKS);
    }

    private static void handleMutationRollLegacy(ServerPlayer player, SurvivalData data, long currentTick) {
        double mutationThreshold = Config.MUTATION_THRESHOLD.get();
        if (mutationThreshold < 0) mutationThreshold = 50.0;
        if (data.getRadiationLevel() <= mutationThreshold) {
            player.getPersistentData().putLong(SEVERE_RADIATION_TICKS_KEY, 0L);
            return;
        }

        CompoundTag playerData = player.getPersistentData();
        long sustainedTicks = playerData.getLong(SEVERE_RADIATION_TICKS_KEY).orElse(0L) + TICK_INTERVAL;
        playerData.putLong(SEVERE_RADIATION_TICKS_KEY, sustainedTicks);
        long lastRoll = playerData.getLong(LAST_MUTATION_ROLL_TICK_KEY).orElse(0L);
        if (sustainedTicks >= SEVERE_RADIATION_SUSTAINED_TICKS && currentTick - lastRoll >= 200L) {
            playerData.putLong(LAST_MUTATION_ROLL_TICK_KEY, currentTick);
            MutationManager.tryMutate(player, data.getRadiationLevel());
        }
    }

    private static boolean shouldRunHydrationDecay(ServerPlayer player, long currentTick) {
        int intervalTicks = Math.max(TICK_INTERVAL, Config.HYDRATION_DECAY_INTERVAL_TICKS.get());
        CompoundTag playerData = player.getPersistentData();
        long nextDecayTick = playerData.getLong(HYDRATION_DECAY_TICK_KEY).orElse(0L);
        if (currentTick < nextDecayTick) {
            return false;
        }
        playerData.putLong(HYDRATION_DECAY_TICK_KEY, currentTick + intervalTicks);
        return true;
    }

    private static void syncGraceCountdown(ServerPlayer player, SurvivalData data, long currentTick, int graceTicks, boolean graceActive) {
        CompoundTag playerData = player.getPersistentData();
        if (graceTicks <= 0 || !data.isGraceInitialized()) {
            return;
        }

        long remainingTicks = data.getGraceTicksRemaining(currentTick, graceTicks);
        if (graceActive && remainingTicks > 0L) {
            PacketDistributor.sendToPlayer(player, new GraceCountdownPacket(remainingTicks, true));
            return;
        }

        boolean graceExpired = currentTick - data.getGraceStartTick() >= graceTicks;
        if (graceExpired && !playerData.getBoolean(GRACE_PERIOD_NOTICE_SHOWN_KEY).orElse(false)) {
            PacketDistributor.sendToPlayer(player, new GraceCountdownPacket(0L, false));
        }
    }

    private static void handleGraceExpiryNotice(ServerPlayer player, SurvivalData data, long currentTick, int graceTicks, boolean graceActive) {
        CompoundTag playerData = player.getPersistentData();
        if (graceTicks <= 0 || !data.isGraceInitialized()) {
            return;
        }

        if (graceActive) {
            playerData.putBoolean(GRACE_PERIOD_NOTICE_SHOWN_KEY, false);
            return;
        }

        boolean graceExpired = currentTick - data.getGraceStartTick() >= graceTicks;
        if (graceExpired && !playerData.getBoolean(GRACE_PERIOD_NOTICE_SHOWN_KEY).orElse(false)) {
            OpeningBasics basics = OpeningBasics.of(player, data);
            Component message = basics.isStable()
                    ? Component.translatable("message.EchoAshfallProtocol.grace.expired.ready")
                    : Component.translatable("message.EchoAshfallProtocol.grace.expired.missing", basics.missingList());
            player.sendSystemMessage(message, true);
            playerData.putBoolean(GRACE_PERIOD_NOTICE_SHOWN_KEY, true);
        }
    }

    private static void sendSurvivalFeedback(ServerPlayer player, SurvivalData data, long currentTick,
                                             HazardZoneManager.HazardSnapshot hazardState) {
        CompoundTag playerData = player.getPersistentData();
        long nextWarningTick = playerData.getLong(SURVIVAL_WARNING_TICK_KEY).orElse(0L);
        if (currentTick < nextWarningTick) {
            return;
        }

        String message = null;
        if (hazardState.radiationStorm() && !hazardState.stormSheltered()) {
            message = "\u00A7c[ECHO-7]\u00A7r Radiation storm overhead. Get under cover or deploy a scrubber.";
        } else if (hazardState.nexusAnomaly()) {
            message = "\u00A7d[ECHO-7 // NEXUS]\u00A7r Anomaly pressure rising. Hazmat shielding and RadAway advised.";
        } else if (hazardState.acidContact()) {
            message = "\u00A7c[ECHO-7]\u00A7r Acid contact detected. Leave sludge and seal armor before continuing.";
        } else if (hazardState.cryoCold() && !HazardZoneManager.hasThermalProtection(player)) {
            message = "\u00A7b[ECHO-7]\u00A7r Cryo cold draining heat. Use thermal lining, fire, shelter, or scrubbed air.";
        } else if (hazardState.toxicAir() && (!data.hasMask() || data.isFilterDepleted())) {
            message = "\u00A7c[ECHO-7]\u00A7r Toxic air detected. Equip a gas mask with filter charge or leave the hazard zone.";
        } else if (data.hasMask() && data.getAirFilterLife() <= SurvivalData.MAX_AIR_FILTER * 0.15f) {
            message = "\u00A7e[ECHO-7]\u00A7r Air filter low. Replace cartridge before the next toxic route.";
        } else if (data.getRadiationLevel() >= 70.0f) {
            message = "\u00A7c[ECHO-7]\u00A7r Radiation spike. Retreat, use RadAway, then decontaminate salvage.";
        } else if (data.getHydration() <= Config.HYDRATION_WARNING_LEVEL.get()) {
            message = "\u00A7b[ECHO-7]\u00A7r Water reserve low. Drink clean water before the next expedition.";
        } else {
            MutationData mutations = player.getData(ModAttachments.MUTATION_DATA.get());
            if (mutations.getMutationCount() >= 3 || !mutations.getActiveSideEffects().isEmpty()) {
                message = "\u00A7d[ECHO-7]\u00A7r Mutation instability rising. Use Field Med Bay support before combat.";
            }
        }

        if (message != null) {
            player.sendSystemMessage(Component.literal(message), true);
            playerData.putLong(SURVIVAL_WARNING_TICK_KEY,
                    currentTick + Math.max(SURVIVAL_WARNING_COOLDOWN_TICKS, Config.HAZARD_WARNING_COOLDOWN_TICKS.get()));
        }
    }

    private static void sendToxicAirWarning(ServerPlayer player, long currentTick) {
        CompoundTag playerData = player.getPersistentData();
        long nextWarningTick = playerData.getLong(TOXIC_AIR_WARNING_TICK_KEY).orElse(0L);
        if (currentTick < nextWarningTick) {
            return;
        }
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.warning.toxic_air"), true);
        playerData.putLong(TOXIC_AIR_WARNING_TICK_KEY, currentTick + 40L);
    }

    private static void handleMutationRoll(ServerPlayer player, SurvivalData data, long currentTick) {
        CompoundTag playerData = player.getPersistentData();
        double severeThreshold = Math.max(Config.MUTATION_SEVERE_THRESHOLD.get(), Config.MUTATION_THRESHOLD.get());
        int severeTicks = playerData.getInt(SEVERE_RADIATION_TICKS_KEY).orElse(0);
        if (data.getRadiationLevel() < severeThreshold) {
            if (severeTicks > 0) {
                playerData.putInt(SEVERE_RADIATION_TICKS_KEY, 0);
            }
            return;
        }

        severeTicks += TICK_INTERVAL;
        playerData.putInt(SEVERE_RADIATION_TICKS_KEY, severeTicks);
        long lastRollTick = playerData.getLong(LAST_MUTATION_ROLL_TICK_KEY).orElse((long) -Config.MUTATION_ROLL_COOLDOWN_TICKS.get());
        int cooldownTicks = Math.max(200, Config.MUTATION_ROLL_COOLDOWN_TICKS.get());
        if (severeTicks >= SEVERE_RADIATION_SUSTAINED_TICKS && currentTick - lastRollTick >= cooldownTicks) {
            MutationManager.tryMutate(player, data.getRadiationLevel());
            playerData.putLong(LAST_MUTATION_ROLL_TICK_KEY, currentTick);
            playerData.putInt(SEVERE_RADIATION_TICKS_KEY, 0);
        }
    }

    private static boolean markSpecialOnce(ServerPlayer player, String marker) {
        QuestData quest = QuestData.get(player);
        if (quest.hasVisitedLocation("special", marker)) {
            return false;
        }
        quest.visitLocation("special", marker);
        QuestData.saveAndSync(player, quest);
        return true;
    }

    private static void recordRadiationZoneScout(ServerPlayer player) {
        QuestData quest = QuestData.get(player);
        if (quest.hasVisitedLocation("biome", "radiation_zone")) {
            return;
        }
        quest.visitLocation("biome", "radiation_zone");
        quest.visitLocation("special", "hazard:radiation_zone");
        QuestData.saveAndSync(player, quest);
        player.sendSystemMessage(Component.literal("\u00A76[ECHO-7]\u00A7r Radiation-zone scout recorded. Retreat before exposure sustains."), true);
    }

    private static void sendDamageHintOnce(ServerPlayer player, String marker, Component message) {
        if (markSpecialOnce(player, DAMAGE_HINT_PREFIX + marker)) {
            player.sendSystemMessage(message, true);
        }
    }

    private static boolean isTooFarFromStartingPod(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return false;
        }
        return StartingDropPodData.get(level)
                .findForPlayer(player.getUUID())
                .map(entry -> entry.interior().distSqr(player.blockPosition()) > POD_RETURN_RADIUS_BLOCKS * POD_RETURN_RADIUS_BLOCKS)
                .orElse(false);
    }

    private static boolean isNightApproaching(ServerPlayer player) {
        long dayTime = player.level().getGameTime() % 24000L;
        return dayTime >= 11000L && dayTime <= 13500L;
    }

    private static boolean hasBlockNearPlayer(ServerPlayer player, Block block, int radius) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
            if (player.level().getBlockState(pos).is(block)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBedNearPlayer(ServerPlayer player, int radius) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
            if (player.level().getBlockState(pos).getBlock() instanceof BedBlock) {
                return true;
            }
        }
        return false;
    }

    private record OpeningBasics(boolean hasMask, boolean hasCleanWaterHandled, boolean hasShelter, boolean hasTool) {
        static OpeningBasics of(ServerPlayer player, SurvivalData data) {
            QuestData quest = QuestData.get(player);
            boolean mask = player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof GasMaskItem
                    || HazmatArmorItem.hasFullSet(getArmorItems(player));
            boolean hasCleanWater = player.getInventory().contains(new ItemStack(ModItems.CLEAN_WATER_BOTTLE.get()));
            boolean cleanWaterHandled = quest.hasVisitedLocation("special", "water:clean_consumed")
                    || (data.getHydration() >= 80 && !hasCleanWater);
            boolean shelter = hasBlockNearPlayer(player, ModBlocks.ASH_CAMPFIRE.get(), 16)
                    || hasBlockNearPlayer(player, Blocks.CHEST, 12)
                    || hasBedNearPlayer(player, 16);
            boolean tool = player.getInventory().contains(new ItemStack(Items.WOODEN_SWORD))
                    || player.getInventory().contains(new ItemStack(ModItems.SCRAP_KNIFE.get()))
                    || player.getInventory().contains(new ItemStack(ModItems.BONE_KNIFE.get()))
                    || player.getInventory().contains(new ItemStack(ModItems.CRUDE_SPEAR.get()));
            return new OpeningBasics(mask, cleanWaterHandled, shelter, tool);
        }

        boolean hasMaskAndWater() {
            return hasMask && hasCleanWaterHandled;
        }

        boolean isStable() {
            return hasCleanWaterHandled && hasShelter && hasTool;
        }

        String missingList() {
            List<String> missing = new java.util.ArrayList<>();
            if (!hasCleanWaterHandled) missing.add("water");
            if (!hasShelter) missing.add("shelter");
            if (!hasTool) missing.add("tool");
            return missing.isEmpty() ? "none" : String.join(", ", missing);
        }
    }

    private static Iterable<ItemStack> getArmorItems(ServerPlayer player) {
        return List.of(
            player.getItemBySlot(EquipmentSlot.HEAD),
            player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS),
            player.getItemBySlot(EquipmentSlot.FEET)
        );
    }

}
