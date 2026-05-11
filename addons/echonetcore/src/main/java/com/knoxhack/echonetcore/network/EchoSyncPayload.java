package com.knoxhack.echonetcore.network;

import com.knoxhack.echonetcore.EchoNetCore;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EchoSyncPayload(
        EchoSyncType syncType,
        Identifier channelId,
        BlockPos pos,
        CompoundTag payload) implements CustomPacketPayload {
    private static final int MAX_ID = 160;

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "clientbound_sync");
    public static final Type<EchoSyncPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, EchoSyncPayload> CODEC =
            StreamCodec.of(EchoSyncPayload::write, EchoSyncPayload::read);

    public EchoSyncPayload {
        syncType = syncType == null ? EchoSyncType.PLAYER_DATA : syncType;
        channelId = channelId == null ? Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "unknown") : channelId;
        payload = payload == null ? new CompoundTag() : payload.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(FriendlyByteBuf buffer, EchoSyncPayload packet) {
        buffer.writeEnum(packet.syncType);
        buffer.writeUtf(packet.channelId.toString(), MAX_ID);
        buffer.writeBoolean(packet.pos != null);
        if (packet.pos != null) {
            buffer.writeBlockPos(packet.pos);
        }
        buffer.writeNbt(packet.payload);
    }

    private static EchoSyncPayload read(FriendlyByteBuf buffer) {
        EchoSyncType type = buffer.readEnum(EchoSyncType.class);
        Identifier channelId = Identifier.tryParse(buffer.readUtf(MAX_ID));
        BlockPos pos = buffer.readBoolean() ? buffer.readBlockPos() : null;
        return new EchoSyncPayload(type, channelId, pos, buffer.readNbt());
    }
}
