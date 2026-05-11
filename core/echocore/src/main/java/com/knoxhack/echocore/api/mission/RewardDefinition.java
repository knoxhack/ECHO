package com.knoxhack.echocore.api.mission;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record RewardDefinition(
        Identifier id,
        MissionRewardClaimMode claimMode,
        ItemStack stack,
        String label,
        String detail,
        Map<String, String> metadata) {
    public RewardDefinition {
        if (id == null) {
            throw new IllegalArgumentException("Mission reward id cannot be null.");
        }
        claimMode = claimMode == null ? MissionRewardClaimMode.CLAIMABLE : claimMode;
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        label = label == null || label.isBlank()
                ? (stack.isEmpty() ? id.getPath() : stack.getHoverName().getString())
                : label;
        detail = detail == null ? "" : detail;
        metadata = Map.copyOf(metadata == null ? Map.of() : new LinkedHashMap<>(metadata));
    }

    public static RewardDefinition item(Identifier id, MissionRewardClaimMode claimMode, ItemStack stack) {
        return new RewardDefinition(id, claimMode, stack, "", "", Map.of());
    }

    public static RewardDefinition text(Identifier id, String label, String detail) {
        return new RewardDefinition(id, MissionRewardClaimMode.IMMEDIATE, ItemStack.EMPTY, label, detail, Map.of());
    }
}
