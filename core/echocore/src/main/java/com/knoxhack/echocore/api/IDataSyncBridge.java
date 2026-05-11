package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Optional bridge for synchronizing shared data values to clients.
 */
public interface IDataSyncBridge {
    IDataSyncBridge NOOP = new IDataSyncBridge() {
    };

    default void requestFullSync(ServerPlayer player) {
    }

    default void markDirty(DataScope scope, String ownerId, Identifier keyId) {
    }

    default long revision() {
        return 0L;
    }
}
