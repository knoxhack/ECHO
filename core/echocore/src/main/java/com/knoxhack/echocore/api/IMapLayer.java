package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

/**
 * Public layer descriptor for ECHO-compatible map UIs.
 */
public interface IMapLayer {
    Identifier id();

    String title();

    int sortOrder();

    int color();

    boolean visibleByDefault();
}
