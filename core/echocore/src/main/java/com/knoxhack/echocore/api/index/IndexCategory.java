package com.knoxhack.echocore.api.index;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record IndexCategory(
        Identifier id,
        String titleKey,
        String descriptionKey,
        ItemStack icon,
        int sortOrder,
        String sourceModId) {
    public IndexCategory {
        Objects.requireNonNull(id, "Index category id is required.");
        titleKey = clean(titleKey, id.toString());
        descriptionKey = descriptionKey == null ? "" : descriptionKey.strip();
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        sourceModId = clean(sourceModId, id.getNamespace());
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.isBlank() ? fallback : cleaned;
    }
}
