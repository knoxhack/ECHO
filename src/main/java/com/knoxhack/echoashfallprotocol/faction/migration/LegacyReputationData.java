package com.knoxhack.echoashfallprotocol.faction.migration;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Migration-only reader for the retired Remnants/Salvagers/Mutants reputation attachment.
 */
public class LegacyReputationData implements ValueIOSerializable {
    private int remnantRep;
    private int salvagerRep;
    private int mutantRep;

    public int remnantRep() {
        return remnantRep;
    }

    public int salvagerRep() {
        return salvagerRep;
    }

    public int mutantRep() {
        return mutantRep;
    }

    public boolean hasAnyProgress() {
        return remnantRep != 0 || salvagerRep != 0 || mutantRep != 0;
    }

    public void clear() {
        remnantRep = 0;
        salvagerRep = 0;
        mutantRep = 0;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("remnantRep", remnantRep);
        output.putInt("salvagerRep", salvagerRep);
        output.putInt("mutantRep", mutantRep);
        output.putInt("remnantPerks", 0);
        output.putInt("salvagerPerks", 0);
        output.putInt("mutantPerks", 0);
    }

    @Override
    public void deserialize(ValueInput input) {
        remnantRep = clamp(input.getIntOr("remnantRep", 0));
        salvagerRep = clamp(input.getIntOr("salvagerRep", 0));
        mutantRep = clamp(input.getIntOr("mutantRep", 0));
    }

    private static int clamp(int value) {
        return Math.max(-100, Math.min(100, value));
    }
}
