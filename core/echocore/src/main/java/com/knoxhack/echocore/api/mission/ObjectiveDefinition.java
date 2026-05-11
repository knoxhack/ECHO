package com.knoxhack.echocore.api.mission;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ObjectiveDefinition(
        Identifier id,
        MissionObjectiveType type,
        String label,
        String detail,
        ItemStack icon,
        int required,
        boolean hidden,
        Map<String, String> criteria) {
    public ObjectiveDefinition {
        if (id == null) {
            throw new IllegalArgumentException("Mission objective id cannot be null.");
        }
        type = type == null ? MissionObjectiveType.CUSTOM : type;
        label = label == null || label.isBlank() ? id.getPath() : label;
        detail = detail == null ? "" : detail;
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        required = Math.max(1, required);
        criteria = Map.copyOf(criteria == null ? Map.of() : new LinkedHashMap<>(criteria));
    }

    public static ObjectiveDefinition simple(
            Identifier id,
            MissionObjectiveType type,
            String label,
            String detail,
            ItemStack icon,
            int required) {
        return new ObjectiveDefinition(id, type, label, detail, icon, required, false, Map.of());
    }
}
