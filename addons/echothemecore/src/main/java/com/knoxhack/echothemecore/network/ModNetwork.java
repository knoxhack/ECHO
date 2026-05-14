package com.knoxhack.echothemecore.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(ThemeSyncPacket.TYPE, ThemeSyncPacket.CODEC,
            (packet, context) -> context.enqueueWork(() -> ThemeCoreClientPacketHooks.applyTheme(packet.themeId())));
        registrar.playToClient(PlayerThemeSyncPacket.TYPE, PlayerThemeSyncPacket.CODEC,
            (packet, context) -> context.enqueueWork(() -> ThemeCoreClientPacketHooks.applyTheme(packet.themeId())));
    }
}
