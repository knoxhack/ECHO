package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

/**
 * Player-specific availability and progress for one faction contract.
 */
public record EchoFactionContractState(
        Identifier contractId,
        boolean canAccept,
        boolean canComplete,
        String progressLine,
        String lockedReason) {

    public EchoFactionContractState {
        progressLine = progressLine == null ? "" : progressLine.trim();
        lockedReason = lockedReason == null ? "" : lockedReason.trim();
    }

    public static EchoFactionContractState unavailable(Identifier contractId, String reason) {
        return new EchoFactionContractState(contractId, false, false, "", reason);
    }

    public static EchoFactionContractState fromProfile(EchoFactionProfile profile, EchoFactionContract contract) {
        if (profile == null || contract == null) {
            return unavailable(contract == null ? null : contract.id(), "Contract signal unavailable.");
        }
        boolean active = profile.activeContractId().filter(contract.id()::equals).isPresent();
        boolean completed = profile.completedContractIds().contains(contract.id());
        boolean anotherActive = profile.activeContractId().isPresent() && !active;
        if (completed) {
            return new EchoFactionContractState(contract.id(), false, false, "Archived.", "This field contract is already archived.");
        }
        if (active) {
            return new EchoFactionContractState(contract.id(), false, true, "Ready for field confirmation.", "");
        }
        if (anotherActive) {
            return unavailable(contract.id(), "Another faction contract is already active.");
        }
        if (profile.reputation() < contract.requiredReputation()) {
            return unavailable(contract.id(), "Requires faction standing " + contract.requiredReputation() + ".");
        }
        return new EchoFactionContractState(contract.id(), true, false, "Available for field work.", "");
    }
}
