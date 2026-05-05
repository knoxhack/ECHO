package com.knoxhack.echoashfallprotocol.faction.migration;

import com.knoxhack.echoashfallprotocol.faction.AshfallBiomeFactions;
import java.util.Locale;
import net.minecraft.resources.Identifier;

/**
 * One-release mapping for old Ashfall 3-faction save/entity identifiers.
 */
public final class LegacyFactionIds {
    private LegacyFactionIds() {
    }

    public static Identifier map(String key) {
        String value = key == null ? "" : key.toLowerCase(Locale.ROOT).trim();
        if (value.contains("remnant") || value.contains("soldier") || value.contains("military")
                || value.contains("reactor") || value.contains("radiation")) {
            return AshfallBiomeFactions.RADWARDEN_COMPACT;
        }
        if (value.contains("mutant") || value.contains("feral") || value.contains("ghoul")
                || value.contains("zombie") || value.contains("toxic") || value.contains("bio")) {
            return AshfallBiomeFactions.SPOREBOUND_SANCTUM;
        }
        if (value.contains("salvager") || value.contains("scavenger") || value.contains("bandit")
                || value.contains("trade") || value.contains("scrap")) {
            return AshfallBiomeFactions.CRASHBREAK_SALVAGE;
        }
        return AshfallBiomeFactions.SURVIVOR_NETWORK;
    }
}
