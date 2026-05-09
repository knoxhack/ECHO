package com.knoxhack.signalos.network;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.TerminalArchiveRecord;
import com.knoxhack.signalos.api.TerminalMission;
import com.knoxhack.signalos.content.SignalOsContentRegistry;
import com.knoxhack.signalos.service.SignalOsBuiltinActions;
import com.knoxhack.signalos.service.SignalOsPlayerData;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Mirrors the server-owned SignalOS player progression state needed by terminal pages.
 */
public record SignalOsTerminalStatePacket(
        Set<Identifier> completedMissions,
        Set<Identifier> claimedMissions,
        Set<Identifier> readArchives,
        int pendingRewardCount) implements CustomPacketPayload {
    private static final int MAX_ID = 160;
    private static final int MAX_IDS = 4096;

    public static final Identifier ID = Identifier.fromNamespaceAndPath(SignalOS.MODID, "terminal_state");
    public static final Type<SignalOsTerminalStatePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SignalOsTerminalStatePacket> CODEC =
            StreamCodec.of(SignalOsTerminalStatePacket::write, SignalOsTerminalStatePacket::read);

    public SignalOsTerminalStatePacket {
        completedMissions = copyIds(completedMissions);
        claimedMissions = copyIds(claimedMissions);
        readArchives = copyIds(readArchives);
        pendingRewardCount = Math.max(0, pendingRewardCount);
    }

    public static SignalOsTerminalStatePacket empty() {
        return new SignalOsTerminalStatePacket(Set.of(), Set.of(), Set.of(), 0);
    }

    public static SignalOsTerminalStatePacket from(ServerPlayer player) {
        if (player == null) {
            return empty();
        }
        return create(player,
                mission -> SignalOsBuiltinActions.completed(player, mission),
                SignalOsTerminalServices.pendingRewardCount(player));
    }

    public static SignalOsTerminalStatePacket createForTests(Player player,
            Predicate<TerminalMission> completionResolver, int pendingRewardCount) {
        return create(player, completionResolver, pendingRewardCount);
    }

    private static SignalOsTerminalStatePacket create(Player player,
            Predicate<TerminalMission> completionResolver, int pendingRewardCount) {
        LinkedHashSet<Identifier> completed = new LinkedHashSet<>();
        LinkedHashSet<Identifier> claimed = new LinkedHashSet<>();
        LinkedHashSet<Identifier> read = new LinkedHashSet<>();

        if (player != null) {
            for (TerminalMission mission : SignalOsContentRegistry.missions()) {
                if (completionResolver != null && completionResolver.test(mission)) {
                    completed.add(mission.id());
                }
                if (SignalOsPlayerData.isMissionClaimed(player, mission.id())) {
                    claimed.add(mission.id());
                }
            }
            for (TerminalArchiveRecord archive : SignalOsContentRegistry.archives()) {
                if (SignalOsPlayerData.isArchiveRead(player, archive.id())) {
                    read.add(archive.id());
                }
            }
        }

        return new SignalOsTerminalStatePacket(completed, claimed, read, pendingRewardCount);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(RegistryFriendlyByteBuf buffer, SignalOsTerminalStatePacket packet) {
        writeIds(buffer, packet.completedMissions());
        writeIds(buffer, packet.claimedMissions());
        writeIds(buffer, packet.readArchives());
        buffer.writeVarInt(packet.pendingRewardCount());
    }

    private static SignalOsTerminalStatePacket read(RegistryFriendlyByteBuf buffer) {
        return new SignalOsTerminalStatePacket(
                readIds(buffer),
                readIds(buffer),
                readIds(buffer),
                buffer.readVarInt());
    }

    private static void writeIds(RegistryFriendlyByteBuf buffer, Set<Identifier> ids) {
        Set<Identifier> safeIds = copyIds(ids);
        if (safeIds.size() > MAX_IDS) {
            throw new IllegalArgumentException("SignalOS terminal state exceeded " + MAX_IDS + " ids.");
        }
        buffer.writeVarInt(safeIds.size());
        for (Identifier id : safeIds) {
            buffer.writeUtf(id.toString(), MAX_ID);
        }
    }

    private static Set<Identifier> readIds(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_IDS) {
            throw new IllegalArgumentException("Invalid SignalOS terminal state id count: " + count);
        }
        LinkedHashSet<Identifier> ids = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(Identifier.parse(buffer.readUtf(MAX_ID)));
        }
        return ids;
    }

    private static Set<Identifier> copyIds(Set<Identifier> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<Identifier> copy = ids.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(Identifier::toString))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(copy);
    }
}
