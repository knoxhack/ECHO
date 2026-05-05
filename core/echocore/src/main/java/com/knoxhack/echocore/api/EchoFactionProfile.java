package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;

import net.minecraft.resources.Identifier;

/**
 * Player-specific faction state paired with a registered definition.
 */
public record EchoFactionProfile(
        EchoFactionDefinition definition,
        int reputation,
        EchoFactionStanding standing,
        boolean contacted,
        int completedContracts,
        List<Identifier> completedContractIds,
        Optional<Identifier> activeContractId,
        long cooldownUntil,
        String npcMemory,
        int contactCount,
        long lastInteractionTick,
        String lastRoleId) {

    public EchoFactionProfile {
        if (definition == null) {
            throw new IllegalArgumentException("definition cannot be null");
        }
        standing = standing == null
                ? EchoFactionStanding.fromReputation(reputation, contacted)
                : standing;
        activeContractId = activeContractId == null ? Optional.empty() : activeContractId;
        completedContractIds = completedContractIds == null ? List.of() : List.copyOf(completedContractIds);
        npcMemory = npcMemory == null ? "" : npcMemory.trim();
        contactCount = Math.max(0, contactCount);
        lastInteractionTick = Math.max(0L, lastInteractionTick);
        lastRoleId = lastRoleId == null ? "" : lastRoleId.trim();
    }

    public EchoFactionProfile(EchoFactionDefinition definition, int reputation, EchoFactionStanding standing,
            boolean contacted, int completedContracts, Optional<Identifier> activeContractId, long cooldownUntil,
            String npcMemory) {
        this(definition, reputation, standing, contacted, completedContracts, List.of(), activeContractId,
                cooldownUntil, npcMemory, contacted ? 1 : 0, 0L, "");
    }

    public String standingLine() {
        return definition.displayName() + ": " + standing.displayName() + " (" + reputation + ")";
    }
}
