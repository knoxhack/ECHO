package com.knoxhack.echoashfallprotocol.entity;

import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * ECHO-7's portable AI companion drone with repair progression and multiple modes.
 */
public class EchoCompanionDrone extends Mob {

    public enum DroneMode {
        FOLLOW("Follow", "Following at close range"),
        SCOUT("Scout", "Flying ahead to detect threats"),
        COMBAT("Combat", "Engaging hostile entities"),
        SCAVENGE("Scavenge", "Collecting debris and items"),
        PATROL("Patrol", "Circling area for defense");

        private final String displayName;
        private final String description;

        DroneMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // Synched data for client-server sync
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LIGHT = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_OWNER = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_REPAIR_LEVEL = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.INT);
    // ECHO-7 voice linkage synced data
    private static final EntityDataAccessor<Integer> DATA_MOOD = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_SPEECH_TEXT = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_SPEECH_TICKS = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ALERT_FLASH = SynchedEntityData.defineId(EchoCompanionDrone.class, EntityDataSerializers.INT);

    // Mood enum mirroring EchoPersonality.Mood ordinals (avoids client->server dep cycle)
    public static final int MOOD_PROFESSIONAL = 1;
    public static final int MOOD_CHEERFUL = 0;
    public static final int MOOD_CONCERNED = 2;
    public static final int MOOD_URGENT = 3;
    public static final int MOOD_REFLECTIVE = 4;
    public static final int MOOD_SARCASTIC = 5;

    // Repair thresholds
    public static final int REPAIR_FOLLOW = 25;
    public static final int REPAIR_SCOUT = 50;
    public static final int REPAIR_INVENTORY = 75;
    public static final int REPAIR_FULL = 100;

    private static final double FOLLOW_STOP_DISTANCE_SQR = 4.0D;
    private static final double FOLLOW_SLOW_DISTANCE_SQR = 25.0D;
    private static final double FOLLOW_TELEPORT_DISTANCE_SQR = 1600.0D;
    private static final double SCAVENGE_SCAN_RADIUS = 4.0D;
    private static final double SCAVENGE_COLLECT_DISTANCE_SQR = 3.0D;
    private static final int SCAVENGE_ACTION_COOLDOWN = 40;
    private static final double PATROL_RADIUS = 6.0D;
    private static final double PATROL_RETURN_DISTANCE_SQR = 576.0D;
    private static final double PATROL_THREAT_SCAN_RADIUS = 12.0D;
    private static final int PATROL_TARGET_RESELECT_TICKS = 60;
    private static final int PATROL_ATTACK_COOLDOWN = 20;

    private DroneMode currentMode = DroneMode.FOLLOW;
    private final ItemStack[] inventory = new ItemStack[9];
    private int scavengeCooldown = 0;
    private int patrolRetargetTicks = 0;
    private int patrolAttackCooldown = 0;
    private Vec3 patrolTarget = Vec3.ZERO;
    
    // Repair materials needed
    private int denseAlloyChunks = 0;
    private int circuitBoards = 0;
    private int powerCells = 0;
    private static final int NEEDED_ALLOY = 3;
    private static final int NEEDED_CIRCUITS = 2;
    private static final int NEEDED_CELLS = 1;
    
    // Intel handler for faction reconnaissance
    private final com.knoxhack.echoashfallprotocol.entity.drone.DroneIntelHandler intelHandler = new com.knoxhack.echoashfallprotocol.entity.drone.DroneIntelHandler();
    
    // Combat AI for faction-aware targeting
    private final com.knoxhack.echoashfallprotocol.entity.drone.DroneCombatAI combatAI = new com.knoxhack.echoashfallprotocol.entity.drone.DroneCombatAI(this);

    public EchoCompanionDrone(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MODE, 0);
        builder.define(DATA_LIGHT, false);
        builder.define(DATA_OWNER, "");
        builder.define(DATA_REPAIR_LEVEL, 15); // Start at 15% damaged
        builder.define(DATA_MOOD, MOOD_PROFESSIONAL);
        builder.define(DATA_SPEECH_TEXT, "");
        builder.define(DATA_SPEECH_TICKS, 0);
        builder.define(DATA_ALERT_FLASH, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public void setOwner(Player player) {
        this.entityData.set(DATA_OWNER, player.getUUID().toString());
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(DATA_OWNER, uuid != null ? uuid.toString() : "");
    }

    public UUID getOwnerUUID() {
        String uuidStr = this.entityData.get(DATA_OWNER);
        return uuidStr != null && !uuidStr.isEmpty() ? UUID.fromString(uuidStr) : null;
    }

    public DroneMode getCurrentMode() {
        int ordinal = this.entityData.get(DATA_MODE);
        DroneMode[] modes = DroneMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : DroneMode.FOLLOW;
    }

    public void setCurrentMode(DroneMode mode) {
        if (mode == null) {
            mode = DroneMode.FOLLOW;
        }
        if (canSwitchToMode(mode)) {
            this.currentMode = mode;
            this.entityData.set(DATA_MODE, mode.ordinal());
            if (mode != DroneMode.COMBAT) {
                clearCombatState();
            }
        }
    }

    public void cycleMode() {
        DroneMode[] modes = DroneMode.values();
        int start = getCurrentMode().ordinal();
        for (int offset = 1; offset <= modes.length; offset++) {
            DroneMode next = modes[(start + offset) % modes.length];
            if (canSwitchToMode(next)) {
                setCurrentMode(next);
                return;
            }
        }
        forceFollowMode();
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public void toggleLight() {
        this.entityData.set(DATA_LIGHT, !this.entityData.get(DATA_LIGHT));
    }

    public boolean isLightEnabled() {
        return this.entityData.get(DATA_LIGHT);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            UUID owner = getOwnerUUID();

            // First-time bond: if unowned, claim it
            if (owner == null) {
                setOwnerUUID(player.getUUID());
                serverPlayer.sendSystemMessage(Component.literal("\u00a7b[ECHO-7 // DRONE]\u00a7r Bio-signature registered. Linking to terminal network..."));
                return InteractionResult.SUCCESS;
            }

            // Only owner can interact
            if (!owner.equals(player.getUUID())) {
                return InteractionResult.PASS;
            }

            // Right-click with repair items to repair the drone
            ItemStack held = player.getItemInHand(hand);
            int currentRepair = getRepairLevel();
            if (currentRepair < REPAIR_FULL) {
                if (held.is(com.knoxhack.echoashfallprotocol.registry.ModItems.DENSE_ALLOY_CHUNK.get())) {
                    held.shrink(1);
                    denseAlloyChunks++;
                    if (denseAlloyChunks >= NEEDED_ALLOY) {
                        denseAlloyChunks -= NEEDED_ALLOY;
                        setRepairLevel(currentRepair + 10);
                        serverPlayer.sendSystemMessage(Component.literal("\u00a7b[ECHO-7 // DRONE]\u00a7r Hull repair applied. Integrity: " + getRepairLevel() + "%"));
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("\u00a77[ECHO-7 // DRONE]\u00a7r Hull components accepted. (" + denseAlloyChunks + "/" + NEEDED_ALLOY + ")"));
                    }
                    return InteractionResult.SUCCESS;
                }
                if (held.is(com.knoxhack.echoashfallprotocol.registry.ModItems.CIRCUIT_BOARD.get())) {
                    held.shrink(1);
                    circuitBoards++;
                    if (circuitBoards >= NEEDED_CIRCUITS) {
                        circuitBoards -= NEEDED_CIRCUITS;
                        setRepairLevel(currentRepair + 15);
                        serverPlayer.sendSystemMessage(Component.literal("\u00a7b[ECHO-7 // DRONE]\u00a7r Systems online. Integrity: " + getRepairLevel() + "%"));
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("\u00a77[ECHO-7 // DRONE]\u00a7r Circuit components accepted. (" + circuitBoards + "/" + NEEDED_CIRCUITS + ")"));
                    }
                    return InteractionResult.SUCCESS;
                }
                if (held.is(com.knoxhack.echoashfallprotocol.registry.ModItems.ENERGY_CELL.get())) {
                    held.shrink(1);
                    powerCells++;
                    if (powerCells >= NEEDED_CELLS) {
                        powerCells -= NEEDED_CELLS;
                        setRepairLevel(currentRepair + 20);
                        serverPlayer.sendSystemMessage(Component.literal("\u00a7b[ECHO-7 // DRONE]\u00a7r Power systems restored. Integrity: " + getRepairLevel() + "%"));
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("\u00a77[ECHO-7 // DRONE]\u00a7r Power components accepted. (" + powerCells + "/" + NEEDED_CELLS + ")"));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // Shift + right-click: cycle drone mode (if unlocked)
            if (player.isShiftKeyDown()) {
                cycleMode();
                serverPlayer.sendSystemMessage(Component.literal("\u00a7b[ECHO-7 // DRONE]\u00a7r Mode: " + getCurrentMode().getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }

            // Plain right-click: point players to the modular terminal owner.
            serverPlayer.sendSystemMessage(Component.literal(
                    "\u00a7b[ECHO-7 // DRONE]\u00a7r Press M with ECHO: Terminal installed for full command access."));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    
    // Repair system methods
    public int getRepairLevel() {
        return this.entityData.get(DATA_REPAIR_LEVEL);
    }
    
    public void setRepairLevel(int level) {
        this.entityData.set(DATA_REPAIR_LEVEL, Math.min(level, REPAIR_FULL));
    }
    
    public boolean isModeUnlocked(DroneMode mode) {
        int repair = getRepairLevel();
        return switch (mode) {
            case FOLLOW -> true;
            case SCOUT, COMBAT -> repair >= REPAIR_SCOUT;
            case SCAVENGE -> repair >= REPAIR_INVENTORY;
            case PATROL -> repair >= REPAIR_FULL;
        };
    }
    
    public boolean canSwitchToMode(DroneMode mode) {
        return isModeUnlocked(mode);
    }

    public void forceFollowMode() {
        this.currentMode = DroneMode.FOLLOW;
        this.entityData.set(DATA_MODE, DroneMode.FOLLOW.ordinal());
        clearCombatState();
    }

    public boolean recallToOwner() {
        return recallTo(getOwner());
    }

    public boolean recallTo(Player owner) {
        if (owner == null || owner.isRemoved()) {
            return false;
        }

        forceFollowMode();
        Vec3 target = getOwnerHoverTarget(owner);
        this.setPos(target.x, target.y, target.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.speak("Recall acknowledged.", MOOD_PROFESSIONAL, 40, 8);
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        UUID owner = getOwnerUUID();
        if (owner != null) {
            output.putString("OwnerUUID", owner.toString());
        }
        output.putInt("Mode", getCurrentMode().ordinal());
        output.putBoolean("LightEnabled", isLightEnabled());
        output.putInt("RepairLevel", getRepairLevel());
        output.putInt("DenseAlloyChunks", denseAlloyChunks);
        output.putInt("CircuitBoards", circuitBoards);
        output.putInt("PowerCells", powerCells);
        output.putInt("Mood", getMoodId());
        output.putString("SpeechText", getSpeechText());
        output.putInt("SpeechTicks", getSpeechTicks());
        output.putInt("AlertFlash", getAlertFlash());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("OwnerUUID").ifPresent(uuid -> {
            try {
                setOwnerUUID(UUID.fromString(uuid));
            } catch (IllegalArgumentException ignored) {
                setOwnerUUID(null);
            }
        });
        int modeOrdinal = input.getIntOr("Mode", DroneMode.FOLLOW.ordinal());
        DroneMode[] modes = DroneMode.values();
        DroneMode loadedMode = modeOrdinal >= 0 && modeOrdinal < modes.length ? modes[modeOrdinal] : DroneMode.FOLLOW;
        this.entityData.set(DATA_MODE, loadedMode.ordinal());
        this.currentMode = loadedMode;
        this.entityData.set(DATA_LIGHT, input.getBooleanOr("LightEnabled", false));
        setRepairLevel(input.getIntOr("RepairLevel", 15));
        denseAlloyChunks = input.getIntOr("DenseAlloyChunks", 0);
        circuitBoards = input.getIntOr("CircuitBoards", 0);
        powerCells = input.getIntOr("PowerCells", 0);
        setMoodId(input.getIntOr("Mood", MOOD_PROFESSIONAL));
        this.entityData.set(DATA_SPEECH_TEXT, input.getStringOr("SpeechText", ""));
        this.entityData.set(DATA_SPEECH_TICKS, input.getIntOr("SpeechTicks", 0));
        this.entityData.set(DATA_ALERT_FLASH, input.getIntOr("AlertFlash", 0));
        if (!isModeUnlocked(getCurrentMode())) {
            forceFollowMode();
        }
    }
    
    @Nullable
    public Player getOwner() {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        return this.level().getPlayerByUUID(ownerId);
    }

    // --- ECHO-7 Voice Linkage ---

    public int getMoodId() { return this.entityData.get(DATA_MOOD); }
    public void setMoodId(int mood) { this.entityData.set(DATA_MOOD, mood); }

    public String getSpeechText() { return this.entityData.get(DATA_SPEECH_TEXT); }
    public int getSpeechTicks() { return this.entityData.get(DATA_SPEECH_TICKS); }
    public boolean isSpeaking() { return getSpeechTicks() > 0 && !getSpeechText().isEmpty(); }

    public int getAlertFlash() { return this.entityData.get(DATA_ALERT_FLASH); }
    public void triggerAlert(int ticks) { this.entityData.set(DATA_ALERT_FLASH, Math.max(getAlertFlash(), ticks)); }

    /**
     * Have the drone "speak" — sets hologram text for a duration.
     * Server-side only. Clients receive via synched data.
     */
    public void speak(String stripped, int moodId, int holdTicks, int alertFlashTicks) {
        if (this.level().isClientSide()) return;
        String clamped = stripped == null ? "" : stripped;
        if (clamped.length() > 140) clamped = clamped.substring(0, 140) + "...";
        this.entityData.set(DATA_SPEECH_TEXT, clamped);
        this.entityData.set(DATA_SPEECH_TICKS, Math.max(20, holdTicks));
        this.entityData.set(DATA_MOOD, moodId);
        if (alertFlashTicks > 0) {
            triggerAlert(alertFlashTicks);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Ensure we're in a valid mode based on repair level
        if (!isModeUnlocked(getCurrentMode())) {
            forceFollowMode();
        }

        // Server-side countdown for speech + alert
        if (!this.level().isClientSide()) {
            DroneMode mode = getCurrentMode();
            ServerLevel serverLevel = this.level() instanceof ServerLevel currentLevel ? currentLevel : null;
            if (mode == DroneMode.FOLLOW) {
                tickOwnerFollow();
            } else if (mode == DroneMode.SCAVENGE && serverLevel != null) {
                tickScavengeMode(serverLevel);
            } else if (mode == DroneMode.PATROL && serverLevel != null) {
                tickPatrolMode(serverLevel);
            } else if (mode != DroneMode.COMBAT) {
                clearCombatState();
            }

            int speech = this.entityData.get(DATA_SPEECH_TICKS);
            if (speech > 0) {
                this.entityData.set(DATA_SPEECH_TICKS, speech - 1);
                if (speech - 1 <= 0) {
                    this.entityData.set(DATA_SPEECH_TEXT, "");
                }
            }
            int alert = this.entityData.get(DATA_ALERT_FLASH);
            if (alert > 0) {
                this.entityData.set(DATA_ALERT_FLASH, alert - 1);
            }
            
            // Faction intel gathering based on drone mode
            if (serverLevel != null) {
                switch (mode) {
                    case SCOUT -> intelHandler.tickScoutMode(this, serverLevel);
                    case COMBAT -> {
                        intelHandler.tickCombatMode(this, serverLevel);
                        combatAI.tickCombat(serverLevel); // Faction-aware targeting + abilities
                    }
                    case PATROL -> intelHandler.tickCombatMode(this, serverLevel);
                    default -> {}
                }
                // Always try to intercept transmissions (all modes)
                intelHandler.tryInterceptTransmission(this, serverLevel);
            }
        }
    }

    private void tickScavengeMode(ServerLevel level) {
        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            clearCombatState();
            return;
        }

        clearCombatState();
        if (scavengeCooldown > 0) {
            scavengeCooldown--;
        }

        if (returnToOwnerIfFar(owner, PATROL_RETURN_DISTANCE_SQR)) {
            return;
        }

        ItemEntity droppedItem = findNearestDroppedItem(level);
        if (droppedItem != null) {
            moveToward(droppedItem.position().add(0.0D, 0.35D, 0.0D), 0.22D);
            if (scavengeCooldown <= 0 && distanceToSqr(droppedItem) <= SCAVENGE_COLLECT_DISTANCE_SQR) {
                collectDroppedItem(droppedItem, owner);
            }
            return;
        }

        BlockPos debris = findNearestDebrisBlock(level);
        if (debris != null) {
            Vec3 target = Vec3.atCenterOf(debris).add(0.0D, 0.45D, 0.0D);
            moveToward(target, 0.22D);
            if (scavengeCooldown <= 0 && target.distanceToSqr(position()) <= 5.0D) {
                level.destroyBlock(debris, true, this);
                scavengeCooldown = SCAVENGE_ACTION_COOLDOWN;
                speak("Debris salvaged.", MOOD_PROFESSIONAL, 30, 0);
            }
            return;
        }

        double angle = (level.getGameTime() + getId() * 17L) * 0.045D;
        moveToward(owner.position().add(Math.cos(angle) * 3.0D, 1.65D, Math.sin(angle) * 3.0D), 0.14D);
    }

    private void tickPatrolMode(ServerLevel level) {
        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            clearCombatState();
            return;
        }

        if (patrolRetargetTicks > 0) {
            patrolRetargetTicks--;
        }
        if (patrolAttackCooldown > 0) {
            patrolAttackCooldown--;
        }

        if (returnToOwnerIfFar(owner, PATROL_RETURN_DISTANCE_SQR)) {
            return;
        }

        Mob threat = findNearestPatrolThreat(level, owner);
        if (threat != null) {
            setTarget(threat);
            setAggressive(true);
            getLookControl().setLookAt(threat, 30.0F, getMaxHeadXRot());
            moveToward(threat.position().add(0.0D, threat.getBbHeight() * 0.5D, 0.0D), 0.34D);
            if (distanceToSqr(threat) <= 6.25D && patrolAttackCooldown <= 0) {
                doHurtTarget(level, threat);
                patrolAttackCooldown = PATROL_ATTACK_COOLDOWN;
            }
            return;
        }

        clearCombatState();
        if (patrolTarget == Vec3.ZERO || patrolRetargetTicks <= 0 || patrolTarget.distanceToSqr(position()) < 2.0D) {
            retargetPatrol(owner, level);
        }
        moveToward(patrolTarget, 0.24D);
        getLookControl().setLookAt(owner, 20.0F, getMaxHeadXRot());
    }

    private boolean returnToOwnerIfFar(Player owner, double softLimitSqr) {
        double distanceSqr = distanceToSqr(owner);
        if (distanceSqr > FOLLOW_TELEPORT_DISTANCE_SQR) {
            Vec3 target = getOwnerHoverTarget(owner);
            setPos(target.x, target.y, target.z);
            setDeltaMovement(Vec3.ZERO);
            speak("Signal reacquired.", MOOD_CONCERNED, 40, 8);
            return true;
        }
        if (distanceSqr > softLimitSqr) {
            moveToward(getOwnerHoverTarget(owner), 0.34D);
            getLookControl().setLookAt(owner, 20.0F, getMaxHeadXRot());
            return true;
        }
        return false;
    }

    private ItemEntity findNearestDroppedItem(ServerLevel level) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(SCAVENGE_SCAN_RADIUS),
                item -> item.isAlive() && !item.getItem().isEmpty());
        ItemEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ItemEntity item : items) {
            double distance = distanceToSqr(item);
            if (distance < nearestDistance) {
                nearest = item;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private BlockPos findNearestDebrisBlock(ServerLevel level) {
        BlockPos center = blockPosition();
        int radius = (int) SCAVENGE_SCAN_RADIUS;
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-radius, -1, -radius),
                center.offset(radius, 2, radius))) {
            if (!level.isLoaded(cursor) || !level.getBlockState(cursor).is(ModBlocks.DEBRIS_BLOCK.get())) {
                continue;
            }
            double dx = cursor.getX() + 0.5D - getX();
            double dy = cursor.getY() + 0.5D - getY();
            double dz = cursor.getZ() + 0.5D - getZ();
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance < nearestDistance) {
                nearest = cursor.immutable();
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private void collectDroppedItem(ItemEntity item, Player owner) {
        ItemStack original = item.getItem();
        ItemStack remaining = original.copy();
        owner.getInventory().add(remaining);

        if (remaining.getCount() < original.getCount()) {
            int collected = original.getCount() - remaining.getCount();
            if (remaining.isEmpty()) {
                item.discard();
            } else {
                item.setItem(remaining);
            }
            owner.sendSystemMessage(Component.literal("[ECHO-7 // DRONE] Retrieved "
                    + collected + "x " + original.getHoverName().getString() + "."));
            speak("Salvage retrieved.", MOOD_PROFESSIONAL, 30, 0);
        } else {
            speak("Inventory full.", MOOD_CONCERNED, 30, 4);
        }

        scavengeCooldown = SCAVENGE_ACTION_COOLDOWN;
    }

    private Mob findNearestPatrolThreat(ServerLevel level, Player owner) {
        Mob nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Mob mob : level.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(PATROL_THREAT_SCAN_RADIUS),
                mob -> isPatrolThreat(mob, owner))) {
            double distance = mob.distanceToSqr(owner);
            if (distance < nearestDistance) {
                nearest = mob;
                nearestDistance = distance;
            }
        }

        for (Mob mob : level.getEntitiesOfClass(Mob.class, getBoundingBox().inflate(PATROL_THREAT_SCAN_RADIUS),
                mob -> isPatrolThreat(mob, owner))) {
            double distance = mob.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearest = mob;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private boolean isPatrolThreat(Mob mob, Player owner) {
        if (mob == this || !mob.isAlive() || mob.isInvisible()) {
            return false;
        }
        if (mob.isAlliedTo(owner) || mob.isAlliedTo(this)) {
            return false;
        }
        return mob instanceof Monster || mob.getTarget() == owner || mob.getTarget() == this;
    }

    private void retargetPatrol(Player owner, ServerLevel level) {
        double angle = (level.getGameTime() * 0.07D) + getId() * 0.31D;
        patrolTarget = owner.position().add(
                Math.cos(angle) * PATROL_RADIUS,
                1.65D + Math.sin(angle * 0.5D) * 0.35D,
                Math.sin(angle) * PATROL_RADIUS);
        patrolRetargetTicks = PATROL_TARGET_RESELECT_TICKS;
    }

    private void moveToward(Vec3 target, double speed) {
        double dx = target.x - getX();
        double dy = target.y - getY();
        double dz = target.z - getZ();
        double distanceSqr = dx * dx + dy * dy + dz * dz;
        if (distanceSqr < 0.0001D) {
            setDeltaMovement(getDeltaMovement().scale(0.35D));
            return;
        }

        double distance = Math.sqrt(distanceSqr);
        Vec3 existing = getDeltaMovement().scale(0.35D);
        setDeltaMovement(existing.add(
                dx / distance * speed,
                dy / distance * Math.min(speed, 0.28D),
                dz / distance * speed));
    }

    private void tickOwnerFollow() {
        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            return;
        }

        this.setNoGravity(true);
        this.getNavigation().stop();
        clearCombatState();
        this.getLookControl().setLookAt(owner, 20.0F, this.getMaxHeadXRot());

        Vec3 target = getOwnerHoverTarget(owner);
        double targetX = target.x;
        double targetY = target.y;
        double targetZ = target.z;

        double dx = targetX - this.getX();
        double dy = targetY - this.getY();
        double dz = targetZ - this.getZ();
        double distanceSqr = dx * dx + dy * dy + dz * dz;

        if (distanceSqr > FOLLOW_TELEPORT_DISTANCE_SQR) {
            this.setPos(targetX, targetY, targetZ);
            this.setDeltaMovement(Vec3.ZERO);
            this.speak("Signal reacquired.", MOOD_CONCERNED, 40, 8);
            return;
        }

        if (distanceSqr <= FOLLOW_STOP_DISTANCE_SQR) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
            return;
        }

        double distance = Math.sqrt(distanceSqr);
        double speed = distanceSqr > 100.0D ? 0.38D : distanceSqr > FOLLOW_SLOW_DISTANCE_SQR ? 0.26D : 0.14D;
        Vec3 existing = this.getDeltaMovement().scale(0.28D);
        this.setDeltaMovement(existing.add(
            dx / distance * speed,
            dy / distance * Math.min(speed, 0.24D),
            dz / distance * speed
        ));
    }

    private Vec3 getOwnerHoverTarget(Player owner) {
        Vec3 look = owner.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        if (flatLook.lengthSqr() < 0.001D) {
            flatLook = Vec3.directionFromRotation(0.0F, owner.getYRot());
            flatLook = new Vec3(flatLook.x, 0.0D, flatLook.z);
        }
        flatLook = flatLook.normalize();
        Vec3 side = new Vec3(-flatLook.z, 0.0D, flatLook.x).normalize();
        return owner.position()
            .subtract(flatLook.scale(1.8D))
            .add(side.scale(0.9D))
            .add(0.0D, 1.65D, 0.0D);
    }

    private void clearCombatState() {
        if (this.getTarget() != null) {
            this.setTarget(null);
        }
        this.setAggressive(false);
        combatAI.clearMark();
    }

    public boolean hasMarkedTarget(Entity entity) {
        return combatAI.isMarked(entity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }
}
