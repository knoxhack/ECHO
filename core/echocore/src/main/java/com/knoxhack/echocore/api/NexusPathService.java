package com.knoxhack.echocore.api;

import net.minecraft.world.entity.player.Player;

/**
 * Optional service exposed by mods that know whether a player has resolved an ECHO Nexus path.
 */
public interface NexusPathService {
    boolean hasPostNexusChoice(Player player);

    default String statusLine(Player player) {
        return hasPostNexusChoice(player)
                ? "Nexus path confirmed."
                : "Nexus path unresolved.";
    }
}
