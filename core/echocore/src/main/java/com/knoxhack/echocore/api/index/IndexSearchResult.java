package com.knoxhack.echocore.api.index;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record IndexSearchResult(
        Identifier id,
        String type,
        String title,
        ItemStack icon,
        int score) {
    public IndexSearchResult {
        type = type == null || type.isBlank() ? "entry" : type.strip();
        title = title == null || title.isBlank() ? (id == null ? "" : id.toString()) : title.strip();
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
    }
}
