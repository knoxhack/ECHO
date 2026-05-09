package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Supplies spoiler-safe Discovery Grid entries and resolves live completion state.
 */
public interface EchoDiscoveryProvider {
    List<EchoDiscoveryEntry> entries(Player player);

    default EchoDiscoveryState state(Player player, EchoDiscoveryEntry entry) {
        return EchoDiscoveryState.LOCKED;
    }

    default boolean owns(Identifier id) {
        if (id == null) {
            return false;
        }
        for (EchoDiscoveryEntry entry : entries(null)) {
            if (entry != null && id.equals(entry.id())) {
                return true;
            }
        }
        return false;
    }
}
