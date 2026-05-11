package com.knoxhack.signalos.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side action context for apps that need to interpret payloads with
 * operator and network metadata.
 */
public record SignalOsAppContext(
        ServerPlayer player,
        Identifier appId,
        String networkId,
        int accessTier) {
    public SignalOsAppContext {
        networkId = networkId == null || networkId.isBlank() ? "offline" : networkId.strip();
        accessTier = Math.max(0, accessTier);
    }
}
