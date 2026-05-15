package com.knoxhack.echorecovery.net;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.block.entity.GraveBlockEntity;
import com.knoxhack.echorecovery.grave.GraveAccessResult;
import com.knoxhack.echorecovery.grave.GraveManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RecoverAllPacket(BlockPos gravePos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RecoverAllPacket> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(EchoRecovery.MODID, "recover_all"));

    public static final StreamCodec<ByteBuf, RecoverAllPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RecoverAllPacket::gravePos,
        RecoverAllPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RecoverAllPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.level().getBlockEntity(packet.gravePos()) instanceof GraveBlockEntity grave) {
                    boolean admin = player.createCommandSourceStack().permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
                    GraveAccessResult result = GraveManager.accessGrave(grave, player.getUUID(), admin);
                    if (result == GraveAccessResult.ALLOWED) {
                        GraveManager.recoverGrave(grave, player);
                    }
                }
            }
        });
    }
}
