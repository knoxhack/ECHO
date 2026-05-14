package com.knoxhack.echotutorialcore.network;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import com.knoxhack.echotutorialcore.api.hint.TutorialHint;
import com.knoxhack.echotutorialcore.data.TutorialPlayerData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TutorialNetworking {
    private TutorialNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(TutorialNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(ShowTutorialHintPacket.TYPE, ShowTutorialHintPacket.CODEC, TutorialNetworking::handleShowHint);
        registrar.playToClient(ShowTutorialCardPacket.TYPE, ShowTutorialCardPacket.CODEC, TutorialNetworking::handleShowCard);
        registrar.playToClient(SetGuideModePacket.TYPE, SetGuideModePacket.CODEC, TutorialNetworking::handleSetGuideMode);
        registrar.playToClient(UnlockTutorialCardPacket.TYPE, UnlockTutorialCardPacket.CODEC, TutorialNetworking::handleUnlockCard);
        registrar.playToClient(SyncTutorialProgressPacket.TYPE, SyncTutorialProgressPacket.CODEC, TutorialNetworking::handleSyncProgress);
    }

    public static void sendShowHint(ServerPlayer player, TutorialHint hint) {
        if (hint == null) return;
        PacketDistributor.sendToPlayer(player, new ShowTutorialHintPacket(
                hint.id(), hint.title(), hint.message(), hint.details()));
    }

    public static void sendShowCard(ServerPlayer player, Identifier cardId) {
        if (cardId == null) return;
        PacketDistributor.sendToPlayer(player, new ShowTutorialCardPacket(cardId));
    }

    public static void sendSetGuideMode(ServerPlayer player, TutorialGuideMode mode) {
        if (mode == null) return;
        PacketDistributor.sendToPlayer(player, new SetGuideModePacket(mode.name()));
    }

    public static void sendUnlockCard(ServerPlayer player, Identifier cardId) {
        if (cardId == null) return;
        PacketDistributor.sendToPlayer(player, new UnlockTutorialCardPacket(cardId));
    }

    public static void sendSyncProgress(ServerPlayer player) {
        TutorialPlayerData data = TutorialPlayerData.get(player);
        PacketDistributor.sendToPlayer(player, new SyncTutorialProgressPacket(
                data.guideMode().name(), new java.util.ArrayList<>(data.progressFlags())));
    }

    // Client-side handlers use reflection so this class stays safe on dedicated server.
    private static void handleShowHint(ShowTutorialHintPacket packet, IPayloadContext context) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        context.enqueueWork(() -> {
            try {
                Class<?> display = Class.forName("com.knoxhack.echotutorialcore.client.TutorialClientDisplay");
                display.getMethod("showHint", ShowTutorialHintPacket.class).invoke(null, packet);
            } catch (ReflectiveOperationException ignored) {
                EchoTutorialCore.LOGGER.debug("TutorialClientDisplay not available for hint.");
            }
        });
    }

    private static void handleShowCard(ShowTutorialCardPacket packet, IPayloadContext context) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        context.enqueueWork(() -> {
            try {
                Class<?> display = Class.forName("com.knoxhack.echotutorialcore.client.TutorialClientDisplay");
                display.getMethod("showCardToast", Identifier.class).invoke(null, packet.cardId());
            } catch (ReflectiveOperationException ignored) {
                EchoTutorialCore.LOGGER.debug("TutorialClientDisplay not available for card toast.");
            }
        });
    }

    private static void handleSetGuideMode(SetGuideModePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            EchoTutorialCore.LOGGER.debug("Client received guide mode: {}", packet.modeName());
        });
    }

    private static void handleUnlockCard(UnlockTutorialCardPacket packet, IPayloadContext context) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        context.enqueueWork(() -> {
            try {
                Class<?> display = Class.forName("com.knoxhack.echotutorialcore.client.TutorialClientDisplay");
                display.getMethod("showUnlockCard", Identifier.class).invoke(null, packet.cardId());
            } catch (ReflectiveOperationException ignored) {
                EchoTutorialCore.LOGGER.debug("TutorialClientDisplay not available for unlock card.");
            }
        });
    }

    private static void handleSyncProgress(SyncTutorialProgressPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            EchoTutorialCore.LOGGER.debug("Client received progress sync.");
        });
    }
}
