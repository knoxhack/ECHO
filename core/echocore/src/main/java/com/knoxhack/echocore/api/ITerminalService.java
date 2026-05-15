package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Optional terminal provider. The fallback is intentionally inert so gameplay mods can run without Terminal.
 */
public interface ITerminalService {

    /**
     * Returns true if the Terminal module is loaded and functional.
     */
    default boolean available() {
        return false;
    }

    /**
     * Open the terminal screen for the given player, if possible.
     */
    default boolean openTerminal(Player player) {
        return false;
    }

    /**
     * Register a dashboard card type by id. The implementation may ignore unknown ids.
     */
    default boolean registerDashboardCard(Identifier cardId) {
        return false;
    }

    /**
     * List currently registered dashboard card ids.
     */
    default List<Identifier> dashboardCards() {
        return List.of();
    }
}
