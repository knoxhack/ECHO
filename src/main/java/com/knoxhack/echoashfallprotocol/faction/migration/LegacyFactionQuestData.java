package com.knoxhack.echoashfallprotocol.faction.migration;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Migration-only placeholder for the retired three-faction quest attachment.
 */
public class LegacyFactionQuestData implements ValueIOSerializable {
    private boolean hadLegacyProgress;

    public boolean hadLegacyProgress() {
        return hadLegacyProgress;
    }

    public void clear() {
        hadLegacyProgress = false;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("activeCount", 0);
        output.putInt("completedCount", 0);
        output.putInt("progressCount", 0);
        output.putInt("acceptedCount", 0);
        output.putInt("completedTickCount", 0);
        output.putString("trackedQuestId", "");
    }

    @Override
    public void deserialize(ValueInput input) {
        hadLegacyProgress = input.getIntOr("activeCount", 0) > 0
                || input.getIntOr("completedCount", 0) > 0
                || input.getIntOr("progressCount", 0) > 0
                || !input.getStringOr("trackedQuestId", "").isBlank();
    }
}
