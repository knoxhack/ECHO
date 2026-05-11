package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Shared persistent data service contract. Implemented by ECHO: DataCore when present.
 */
public interface IDataService {
    <T> IDataKey<T> registerKey(IDataKey<T> key);

    Optional<IDataKey<?>> key(Identifier id);

    List<IDataKey<?>> registeredKeys();

    IPlayerDataView player(Player player);

    IWorldDataView world(Level level);

    ITeamDataView team(Level level, Identifier teamId);

    IDataSyncBridge syncBridge();
}
