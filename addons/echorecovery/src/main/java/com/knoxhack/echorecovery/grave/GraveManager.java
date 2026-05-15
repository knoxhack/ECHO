package com.knoxhack.echorecovery.grave;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.block.entity.GraveBlockEntity;
import com.knoxhack.echorecovery.config.RecoveryConfig;
import com.knoxhack.echorecovery.data.RecoveryWorldData;
import com.knoxhack.echorecovery.registry.ModBlocks;
import com.knoxhack.echorecovery.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class GraveManager {
    private GraveManager() {}

    public static GraveAccessResult accessGrave(GraveBlockEntity grave, UUID playerId, boolean admin) {
        if (grave.isRecovered() || grave.isExpired()) {
            return GraveAccessResult.GONE;
        }
        UUID owner = grave.ownerId();
        if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
            return GraveAccessResult.ALLOWED;
        }
        if (admin && RecoveryConfig.ADMIN_BYPASS.get()) {
            return GraveAccessResult.ALLOWED;
        }
        if (playerId.equals(owner)) {
            return GraveAccessResult.ALLOWED;
        }
        if (RecoveryConfig.TEAM_ACCESS.get()) {
            // TODO: team check integration
        }
        int publicAfter = RecoveryConfig.PUBLIC_ACCESS_AFTER_MINUTES.get();
        if (publicAfter > 0) {
            long ageMinutes = (System.currentTimeMillis() - grave.createdAt()) / 60000L;
            if (ageMinutes >= publicAfter) {
                return GraveAccessResult.ALLOWED;
            }
        }
        if (RecoveryConfig.GRAVE_THEFT.get()) {
            return GraveAccessResult.ALLOWED;
        }
        return GraveAccessResult.DENIED;
    }

    public static boolean canBreak(GraveBlockEntity grave, UUID playerId, boolean admin) {
        if (admin && RecoveryConfig.ADMIN_BYPASS.get()) {
            return true;
        }
        UUID owner = grave.ownerId();
        if (owner.getMostSignificantBits() == 0 && owner.getLeastSignificantBits() == 0) {
            return true;
        }
        return playerId.equals(owner);
    }

    public static void recoverGrave(GraveBlockEntity grave, Player player) {
        if (grave.isRecovered() || grave.isExpired()) {
            return;
        }
        Level level = grave.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        NonNullList<ItemStack> items = grave.items();
        Inventory inv = player.getInventory();
        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!inv.add(stack.copy())) {
                overflow.add(stack.copy());
            }
        }
        items.clear();
        int xp = grave.xpStored();
        if (xp > 0 && RecoveryConfig.STORE_XP.get()) {
            player.giveExperiencePoints(xp);
            grave.setXpStored(0);
        }
        level.playSound(null, grave.getBlockPos(), ModSounds.GRAVE_RECOVER.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        grave.setRecovered(true);
        grave.setChanged();
        level.removeBlock(grave.getBlockPos(), false);
        if (!overflow.isEmpty() && RecoveryConfig.DROP_OVERFLOW_ITEMS.get()) {
            for (ItemStack stack : overflow) {
                Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), stack);
            }
        }
        if (level instanceof ServerLevel serverLevel) {
            RecoveryWorldData data = RecoveryWorldData.getOrCreate(serverLevel);
            data.removeGrave(grave.ownerId(), grave.getBlockPos());
        }
    }

    public static void dropGraveContents(GraveBlockEntity grave, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        for (ItemStack stack : grave.items()) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy());
            }
        }
        grave.items().clear();
        int xp = grave.xpStored();
        if (xp > 0 && level instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, net.minecraft.world.phys.Vec3.atCenterOf(pos), xp);
            grave.setXpStored(0);
        }
    }

    public static BlockPos findSafePosition(ServerLevel level, BlockPos origin) {
        if (!RecoveryConfig.SAFE_PLACEMENT.get()) {
            return origin;
        }
        if (isSafe(level, origin)) {
            return origin;
        }
        int radius = RecoveryConfig.SAFE_PLACEMENT_RADIUS.get();
        for (int r = 1; r <= radius; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) != r && Math.abs(dy) != r && Math.abs(dz) != r) {
                            continue;
                        }
                        BlockPos test = origin.offset(dx, dy, dz);
                        if (isSafe(level, test)) {
                            return test;
                        }
                    }
                }
            }
        }
        return origin;
    }

    private static boolean isSafe(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        if (state.isAir() && below.isSolidRender()) {
            return !isDangerous(level, pos);
        }
        return false;
    }

    private static boolean isDangerous(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();
        if (b == Blocks.LAVA || b == Blocks.FIRE || b == Blocks.SOUL_FIRE || b == Blocks.CACTUS || b == Blocks.MAGMA_BLOCK) {
            return true;
        }
        BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() == Blocks.LAVA || below.getBlock() == Blocks.FIRE) {
            return true;
        }
        return false;
    }

    public static BlockPos resolveVoidDeathPosition(ServerPlayer player) {
        RecoveryConfig.VoidDeathMode mode = RecoveryConfig.VOID_DEATH_MODE.get();
        if (mode == RecoveryConfig.VoidDeathMode.DISABLED) {
            return player.blockPosition();
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return player.blockPosition();
        }
        if (RecoveryConfig.FALLBACK_TO_BED.get()) {
            if (player.getRespawnConfig() != null) {
                return serverLevel.getRespawnData().pos();
            }
        }
        if (RecoveryConfig.FALLBACK_TO_SPAWN.get()) {
            return serverLevel.getRespawnData().pos();
        }
        return player.blockPosition();
    }
}
