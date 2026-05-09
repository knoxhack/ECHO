package com.knoxhack.signalos.client;

import com.knoxhack.signalos.network.SignalOsTerminalStatePacket;
import net.minecraft.resources.Identifier;

public final class SignalOsClientState {
    private static volatile SignalOsTerminalStatePacket terminalState = SignalOsTerminalStatePacket.empty();

    private SignalOsClientState() {
    }

    public static void apply(SignalOsTerminalStatePacket packet) {
        terminalState = packet == null ? SignalOsTerminalStatePacket.empty() : packet;
    }

    public static boolean isMissionCompleted(Identifier missionId) {
        return missionId != null && terminalState.completedMissions().contains(missionId);
    }

    public static boolean isMissionClaimed(Identifier missionId) {
        return missionId != null && terminalState.claimedMissions().contains(missionId);
    }

    public static boolean isArchiveRead(Identifier archiveId) {
        return archiveId != null && terminalState.readArchives().contains(archiveId);
    }

    public static int pendingRewardCount() {
        return terminalState.pendingRewardCount();
    }
}
