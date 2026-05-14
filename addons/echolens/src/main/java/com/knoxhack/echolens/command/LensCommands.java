package com.knoxhack.echolens.command;

import com.mojang.brigadier.Command;
import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.LensProviderDiagnostic;
import com.knoxhack.echolens.config.LensConfig;
import com.knoxhack.echolens.network.ModNetwork;
import com.knoxhack.echolens.registry.LensProviderRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = EchoLens.MODID)
public final class LensCommands {
    private LensCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("echolens")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("providers").executes(ctx -> providers(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("validate").executes(ctx -> validate(ctx.getSource().getPlayerOrException()))));
    }

    private static int status(ServerPlayer player) {
        tell(player, "ECHO Lens // Providers " + LensProviderRegistry.count()
                + " (" + LensProviderRegistry.serverProviders().size() + " server)"
                + ", NetCore " + online("echonetcore")
                + ", packets " + (ModNetwork.registered() ? "registered" : "offline")
                + ", Terminal " + online("echoterminal")
                + ", Index " + online("echoindex")
                + ", Ashfall " + online("echoashfallprotocol") + ".", ChatFormatting.AQUA);
        tell(player, "ECHO Lens // Server Deep Scan "
                + (LensConfig.bool(LensConfig.SERVER_DEEP_SCAN_ENABLED, true) ? "enabled" : "disabled")
                + ", distance " + LensConfig.decimal(LensConfig.SERVER_SCAN_DISTANCE, 24.0D)
                + ", privacy " + LensConfig.value(LensConfig.INVENTORY_ACCESS_POLICY,
                        com.knoxhack.echolens.api.LensAccessPolicy.PUBLIC_ONLY)
                + ".", ChatFormatting.GRAY);
        return Command.SINGLE_SUCCESS;
    }

    private static int providers(ServerPlayer player) {
        tell(player, "ECHO Lens // Providers (" + LensProviderRegistry.count() + "):", ChatFormatting.AQUA);
        for (LensProviderDiagnostic diagnostic : LensProviderRegistry.diagnostics()) {
            boolean serverProvider = LensProviderRegistry.serverProviders().stream()
                    .anyMatch(provider -> provider.id().equals(diagnostic.id()));
            tell(player, " - " + diagnostic.id()
                    + " | priority " + diagnostic.priority()
                    + " | category " + diagnostic.category()
                    + (serverProvider ? " | server" : " | client")
                    + " | " + (diagnostic.enabled() ? "enabled" : "category hidden")
                    + " | " + simpleClassName(diagnostic.providerClass()), ChatFormatting.GRAY);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int validate(ServerPlayer player) {
        if (!LensConfig.bool(LensConfig.DEBUG_COMMANDS, true)) {
            tell(player, "ECHO Lens // Validation commands are disabled in config.", ChatFormatting.RED);
            return 0;
        }
        if (LensProviderRegistry.count() == 0) {
            tell(player, "ECHO Lens // Validation failed: no providers registered.", ChatFormatting.RED);
            return 0;
        }
        tell(player, "ECHO Lens // Validation passed. Provider registry is populated and privacy policy is "
                + LensConfig.value(LensConfig.INVENTORY_ACCESS_POLICY,
                        com.knoxhack.echolens.api.LensAccessPolicy.PUBLIC_ONLY)
                + "; server providers " + LensProviderRegistry.serverProviders().size()
                + "; packet registration " + (ModNetwork.registered() ? "online" : "offline") + ".",
                ChatFormatting.GREEN);
        return Command.SINGLE_SUCCESS;
    }

    private static String online(String modId) {
        return ModList.get().isLoaded(modId) ? "online" : "offline";
    }

    private static String simpleClassName(String className) {
        if (className == null || className.isBlank()) {
            return "unknown";
        }
        int split = className.lastIndexOf('.');
        return split < 0 ? className : className.substring(split + 1);
    }

    private static void tell(ServerPlayer player, String message, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(message).withStyle(color));
    }
}
