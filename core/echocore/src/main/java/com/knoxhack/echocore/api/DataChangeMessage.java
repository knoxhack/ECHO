package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

/**
 * Runtime notification emitted when a shared data value changes locally or after a client sync.
 */
public record DataChangeMessage(
        DataScope scope,
        String ownerId,
        Identifier keyId,
        DataValueKind kind,
        long revision,
        boolean fullSnapshot) {
    public DataChangeMessage {
        ownerId = ownerId == null ? "" : ownerId;
    }
}
