package com.knoxhack.echocore.api.index;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record IndexEntry(
        Identifier id,
        Identifier categoryId,
        String titleKey,
        String subtitleKey,
        String summaryKey,
        String bodyKey,
        ItemStack icon,
        String sourceModId,
        List<String> tags,
        IndexEntryState defaultState,
        List<Identifier> relatedEntries,
        List<Identifier> linkedItems,
        List<Identifier> linkedRecipes,
        int sortOrder) {
    public IndexEntry {
        Objects.requireNonNull(id, "Index entry id is required.");
        Objects.requireNonNull(categoryId, "Index entry category id is required.");
        titleKey = clean(titleKey, id.toString());
        subtitleKey = subtitleKey == null ? "" : subtitleKey.strip();
        summaryKey = summaryKey == null ? "" : summaryKey.strip();
        bodyKey = bodyKey == null ? "" : bodyKey.strip();
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        sourceModId = clean(sourceModId, id.getNamespace());
        tags = tags == null ? List.of() : tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::strip)
                .distinct()
                .toList();
        defaultState = defaultState == null ? IndexEntryState.VISIBLE : defaultState;
        relatedEntries = relatedEntries == null ? List.of() : List.copyOf(relatedEntries);
        linkedItems = linkedItems == null ? List.of() : List.copyOf(linkedItems);
        linkedRecipes = linkedRecipes == null ? List.of() : List.copyOf(linkedRecipes);
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.isBlank() ? fallback : cleaned;
    }
}
