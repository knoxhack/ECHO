package com.knoxhack.echocore.api;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/**
 * Immutable default implementation of {@link IMapLayer}.
 */
public record EchoMapLayer(
        Identifier id,
        String title,
        int sortOrder,
        int color,
        boolean visibleByDefault) implements IMapLayer {
    public EchoMapLayer {
        Objects.requireNonNull(id, "id");
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        color = color == 0 ? 0xFF66E8FF : color;
    }
}
