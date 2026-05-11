package com.knoxhack.signalos.client;

import com.knoxhack.signalos.network.SignalOsTerminalStatePacket;
import com.knoxhack.signalos.api.SignalOsDataRecord;
import java.util.List;
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

    public static boolean networkOnline() {
        return terminalState.networkOnline();
    }

    public static String networkId() {
        return terminalState.networkId();
    }

    public static int accessTier() {
        return terminalState.accessTier();
    }

    public static int networkRadius() {
        return terminalState.networkRadius();
    }

    public static String networkAnchor() {
        return terminalState.networkAnchor();
    }

    public static int terminalCount() {
        return terminalState.terminalCount();
    }

    public static int workstationCount() {
        return terminalState.workstationCount();
    }

    public static int serverRackCount() {
        return terminalState.serverRackCount();
    }

    public static int relayCount() {
        return terminalState.relayCount();
    }

    public static int networkDeviceCount() {
        return terminalCount() + workstationCount() + serverRackCount() + relayCount();
    }

    public static List<SignalOsDataRecord> dataRecords() {
        return terminalState.dataRecords();
    }
}
