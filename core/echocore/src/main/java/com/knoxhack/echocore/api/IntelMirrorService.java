package com.knoxhack.echocore.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * Optional service for mirroring addon milestones into a host mod's archive/intel storage.
 */
public interface IntelMirrorService {
    void mirrorIntel(ServerPlayer player, String sourceModId, String id, String title, String content);
}
