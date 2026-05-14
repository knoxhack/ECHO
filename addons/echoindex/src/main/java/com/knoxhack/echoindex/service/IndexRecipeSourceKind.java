package com.knoxhack.echoindex.service;

import com.knoxhack.echocore.api.index.IndexRecipeView;

public enum IndexRecipeSourceKind {
    VANILLA,
    ADDON,
    BLOCK_DROP,
    LOOT_TABLE,
    WORLDGEN,
    SOURCE_CARD,
    UNKNOWN;

    public static IndexRecipeSourceKind of(IndexRecipeView view) {
        if (view == null) {
            return UNKNOWN;
        }
        if (isSourceCard(view)) {
            String title = view.title().toLowerCase(java.util.Locale.ROOT);
            String notes = String.join(" ", view.notes()).toLowerCase(java.util.Locale.ROOT);
            String combined = title + " " + notes;
            if (combined.contains("block drop") || combined.contains("block loot")) {
                return BLOCK_DROP;
            }
            if (combined.contains("world generation") || combined.contains("worldgen")) {
                return WORLDGEN;
            }
            if (combined.contains("loot")) {
                return LOOT_TABLE;
            }
            return SOURCE_CARD;
        }
        if ("minecraft".equals(view.sourceModId())) {
            return VANILLA;
        }
        return ADDON;
    }

    public static boolean isSourceCard(IndexRecipeView view) {
        return view != null && IndexSourceRecipeProvider.CATEGORY.equals(view.categoryId());
    }

    public String label() {
        return switch (this) {
            case BLOCK_DROP -> "Block Drop";
            case LOOT_TABLE -> "Loot Source";
            case WORLDGEN -> "World Generation";
            case SOURCE_CARD -> "Source";
            case VANILLA -> "Vanilla";
            case ADDON -> "Addon";
            case UNKNOWN -> "Unknown";
        };
    }
}
