package com.knoxhack.echomultiblockcore.api;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public record MultiblockStatusSnapshot(
        Identifier definitionId,
        String name,
        MultiblockState state,
        float integrity,
        double completion,
        BlockPos controllerPos,
        List<String> installedModules,
        List<String> roboticArms,
        List<String> currentTasks,
        List<String> warnings,
        String progressionTitle,
        String progressionCompletionHint) {
    public MultiblockStatusSnapshot(
            Identifier definitionId,
            String name,
            MultiblockState state,
            float integrity,
            double completion,
            BlockPos controllerPos,
            List<String> installedModules,
            List<String> roboticArms,
            List<String> currentTasks,
            List<String> warnings) {
        this(definitionId, name, state, integrity, completion, controllerPos, installedModules, roboticArms,
                currentTasks, warnings, "", "");
    }

    public MultiblockStatusSnapshot {
        name = name == null || name.isBlank() ? (definitionId == null ? "Multiblock" : definitionId.getPath()) : name.strip();
        state = state == null ? MultiblockState.UNBUILT : state;
        integrity = Math.max(0.0F, Math.min(100.0F, integrity));
        completion = Math.max(0.0D, Math.min(1.0D, completion));
        controllerPos = controllerPos == null ? BlockPos.ZERO : controllerPos.immutable();
        installedModules = List.copyOf(installedModules == null ? List.of() : installedModules);
        roboticArms = List.copyOf(roboticArms == null ? List.of() : roboticArms);
        currentTasks = List.copyOf(currentTasks == null ? List.of() : currentTasks);
        warnings = List.copyOf(warnings == null ? List.of() : warnings);
        progressionTitle = progressionTitle == null ? "" : progressionTitle.strip();
        progressionCompletionHint = progressionCompletionHint == null ? "" : progressionCompletionHint.strip();
    }
}
