package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Optional lens / scan provider. The fallback is intentionally inert so mods can run without Lens.
 */
public interface ILensService {

    default boolean available() {
        return false;
    }

    default boolean registerScanType(Identifier scanId, String displayName) {
        return false;
    }

    default List<Identifier> scanTypes() {
        return List.of();
    }

    default boolean openLens(Player player) {
        return false;
    }
}
