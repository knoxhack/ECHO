package com.knoxhack.echocore.api.index;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public interface IIndexRegistry {
    boolean registerCategory(IndexCategory category);

    boolean registerEntry(IndexEntry entry);

    List<IndexCategory> categories(Player player);

    List<IndexEntry> entries(Player player);

    Optional<IndexEntry> entry(Player player, Identifier id);
}
