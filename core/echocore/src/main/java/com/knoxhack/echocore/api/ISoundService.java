package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * Optional sound provider. The fallback is intentionally inert so mods can run without SoundCore.
 */
public interface ISoundService {

    default boolean available() {
        return false;
    }

    default boolean playEvent(Identifier eventId) {
        return false;
    }

    default boolean stopEvent(Identifier eventId) {
        return false;
    }

    default List<Identifier> activeEvents() {
        return List.of();
    }
}
