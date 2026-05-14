package com.knoxhack.echomultiblockcore.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, RobotAnimationPacket.TYPE, RobotAnimationPacket.CODEC,
                (packet, player, context) -> handleClient("handleRobotAnimation", packet));
        EchoNetPayloads.clientboundSync(registrar, MultiblockDefinitionMetadataPacket.TYPE, MultiblockDefinitionMetadataPacket.CODEC,
                (packet, player, context) -> handleClient("handleDefinitionMetadata", packet));
        EchoNetPayloads.clientboundSync(registrar, AutomationRecipeMetadataPacket.TYPE, AutomationRecipeMetadataPacket.CODEC,
                (packet, player, context) -> handleClient("handleAutomationRecipeMetadata", packet));
        EchoNetPayloads.clientboundSync(registrar, MultiblockBuildAssistPacket.TYPE, MultiblockBuildAssistPacket.CODEC,
                (packet, player, context) -> handleClient("handleBuildAssistMetadata", packet));
    }

    private static void handleClient(String method, Object packet) {
        try {
            Class.forName("com.knoxhack.echomultiblockcore.client.MultiblockClientPackets")
                    .getMethod(method, packet.getClass())
                    .invoke(null, packet);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
