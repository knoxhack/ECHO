package com.knoxhack.echocore.api.index;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record IndexRecipeCategory(
        Identifier id,
        String title,
        ItemStack icon,
        int accentColor,
        int order) implements IIndexRecipeCategory {
    public IndexRecipeCategory {
        Objects.requireNonNull(id, "Index recipe category id is required.");
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        accentColor = accentColor == 0 ? 0xFF66E8FF : accentColor;
    }
}
