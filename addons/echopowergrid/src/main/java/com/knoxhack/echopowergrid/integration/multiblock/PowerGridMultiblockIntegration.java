package com.knoxhack.echopowergrid.integration.multiblock;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.config.PowerGridConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Optional integration bridge for ECHO MultiblockCore.
 *
 * <p>Multiblock controllers can call {@link #requestPower(Level, BlockPos, long)} to draw EP
 * from an adjacent ECHO PowerGrid network. This is a soft bridge: if PowerGrid is not present
 * or the position is not on a network, the request safely returns zero.
 */
public final class PowerGridMultiblockIntegration {
    private PowerGridMultiblockIntegration() {}

    public static void register() {
        EchoPowerGrid.LOGGER.info("ECHO PowerGrid MultiblockCore bridge registered.");
    }

    /**
     * Requests EP from the PowerGrid network at the given position.
     *
     * @param level     the level
     * @param pos       block position of the multiblock controller
     * @param epPerTick amount of EP requested per tick
     * @return the amount of EP actually available (0 if no network or insufficient power)
     */
    public static long requestPower(Level level, BlockPos pos, long epPerTick) {
        if (!PowerGridConfig.ENABLED.get()) return 0;
        long available = EchoPowerGridApi.getAvailablePower(level, pos);
        return Math.min(available, epPerTick);
    }

    /**
     * Returns true if the position is connected to a powered network.
     */
    public static boolean isPowered(Level level, BlockPos pos) {
        if (!PowerGridConfig.ENABLED.get()) return false;
        return EchoPowerGridApi.isPowered(level, pos);
    }
}
