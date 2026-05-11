package com.knoxhack.echocore.api.index;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface IIndexEntryProvider {
    Identifier id();

    default void register(IIndexRegistry registry) {
    }
}
