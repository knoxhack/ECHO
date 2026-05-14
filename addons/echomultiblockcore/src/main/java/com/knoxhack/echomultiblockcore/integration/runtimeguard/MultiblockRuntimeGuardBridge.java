package com.knoxhack.echomultiblockcore.integration.runtimeguard;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.block.entity.MultiblockControllerBlockEntity;
import com.knoxhack.echoruntimeguard.RuntimeGuardConfig;
import com.knoxhack.echoruntimeguard.api.DirtyReason;
import com.knoxhack.echoruntimeguard.api.RuntimeGuardServices;
import com.knoxhack.echoruntimeguard.api.ValidationPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class MultiblockRuntimeGuardBridge {
    private MultiblockRuntimeGuardBridge() {
    }

    public static boolean requestValidation(MultiblockControllerBlockEntity controller, ServerLevel level,
            String priorityName, Runnable task) {
        if (controller == null || level == null || task == null) {
            return false;
        }
        if (!RuntimeGuardConfig.safeBool(RuntimeGuardConfig.MULTIBLOCK_SCHEDULER_ENABLED, true)) {
            return false;
        }
        RuntimeGuardServices.multiblocks().requestValidation(
                EchoMultiblockCore.id("controller"),
                level,
                controller.getBlockPos(),
                priority(priorityName),
                task);
        return true;
    }

    public static void markDirty(Level level, BlockPos controllerPos, String reasonName) {
        RuntimeGuardServices.multiblocks().markDirty(level, controllerPos, dirtyReason(reasonName));
    }

    private static ValidationPriority priority(String name) {
        if (name == null || name.isBlank()) {
            return ValidationPriority.SCHEDULED_IDLE;
        }
        try {
            return ValidationPriority.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return ValidationPriority.SCHEDULED_IDLE;
        }
    }

    private static DirtyReason dirtyReason(String name) {
        if (name == null || name.isBlank()) {
            return DirtyReason.DEBUG;
        }
        try {
            return DirtyReason.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return DirtyReason.DEBUG;
        }
    }
}
