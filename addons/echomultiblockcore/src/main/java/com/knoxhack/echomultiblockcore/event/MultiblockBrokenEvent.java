package com.knoxhack.echomultiblockcore.event;

import com.knoxhack.echomultiblockcore.api.MultiblockRuntimeSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

public class MultiblockBrokenEvent extends Event {
    private final ServerLevel level;
    private final Identifier definitionId;
    private final BlockPos controllerPos;
    private final MultiblockRuntimeSnapshot beforeSnapshot;

    public MultiblockBrokenEvent(ServerLevel level, Identifier definitionId, BlockPos controllerPos) {
        this(level, definitionId, controllerPos, null);
    }

    public MultiblockBrokenEvent(ServerLevel level, Identifier definitionId, BlockPos controllerPos,
            MultiblockRuntimeSnapshot beforeSnapshot) {
        this.level = level;
        this.definitionId = definitionId;
        this.controllerPos = controllerPos == null ? BlockPos.ZERO : controllerPos.immutable();
        this.beforeSnapshot = beforeSnapshot;
    }

    public ServerLevel level() {
        return level;
    }

    public Identifier definitionId() {
        return definitionId;
    }

    public BlockPos controllerPos() {
        return controllerPos;
    }

    public MultiblockRuntimeSnapshot beforeSnapshot() {
        return beforeSnapshot;
    }
}
