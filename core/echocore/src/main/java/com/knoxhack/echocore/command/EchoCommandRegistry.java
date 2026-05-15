package com.knoxhack.echocore.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiagnosticBlocker;
import com.knoxhack.echocore.api.EchoDiagnosticService;
import com.knoxhack.echocore.api.EchoIntegrations;
import com.knoxhack.echocore.api.EchoModuleInfo;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.IDataService;
import com.knoxhack.echocore.api.ILensService;
import com.knoxhack.echocore.api.mission.IMissionService;
import com.knoxhack.echocore.api.network.INetworkService;
import com.knoxhack.echocore.api.IRuntimeBudgetService;
import com.knoxhack.echocore.api.ISoundService;
import com.knoxhack.echocore.api.ITerminalService;
import com.knoxhack.echocore.api.IThemeService;
import com.knoxhack.echocore.api.IWorldRegionService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Shared command registry for the {@code /echo} root command.
 * Core and addon modules can contribute subcommands through {@link #register}.
 */
public final class EchoCommandRegistry {
    private static final List<LiteralArgumentBuilder<CommandSourceStack>> SUBCOMMANDS = new ArrayList<>();

    private EchoCommandRegistry() {
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> subcommand) {
        if (subcommand != null) {
            SUBCOMMANDS.add(subcommand);
        }
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("echo");

        root.then(Commands.literal("doctor").executes(ctx -> doctor(ctx.getSource())));
        root.then(Commands.literal("modules").executes(ctx -> modules(ctx.getSource())));
        root.then(Commands.literal("integrations").executes(ctx -> integrations(ctx.getSource())));
        root.then(Commands.literal("services").executes(ctx -> services(ctx.getSource())));
        root.then(Commands.literal("themes").executes(ctx -> themes(ctx.getSource())));
        root.then(Commands.literal("data").executes(ctx -> data(ctx.getSource())));
        root.then(Commands.literal("export-diagnostics").executes(ctx -> exportDiagnostics(ctx.getSource())));

        for (LiteralArgumentBuilder<CommandSourceStack> sub : SUBCOMMANDS) {
            root.then(sub);
        }

        dispatcher.register(root);
    }

    private static int doctor(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== ECHO Doctor ==="), false);
        List<EchoModuleInfo> modules = EchoCoreServices.moduleReport();
        int severe = 0;

        for (EchoModuleInfo module : modules) {
            if (!module.loaded() && module.expectedInAll()) {
                source.sendSuccess(() -> Component.literal("  MISSING: " + module.displayName()), false);
                severe++;
            }
        }

        Player player = source.getPlayer();
        if (player != null) {
            List<EchoDiagnosticBlocker> blockers = EchoCoreServices.diagnostics(player);
            for (EchoDiagnosticBlocker blocker : blockers) {
                if (blocker.severity() == EchoDiagnosticBlocker.Severity.CRITICAL
                        || blocker.severity() == EchoDiagnosticBlocker.Severity.BLOCKED) {
                    source.sendSuccess(() -> Component.literal(
                            "  SEVERE [" + blocker.severity() + "]: " + blocker.title() + " - " + blocker.detail()), false);
                    severe++;
                }
            }
        }

        EchoIntegrations i = EchoIntegrations.current();
        if (i.hasAshfall()) {
            source.sendSuccess(() -> Component.literal("  Ashfall connected."), false);
        }
        if (!i.hasTerminal()) {
            source.sendSuccess(() -> Component.literal("  Terminal not present (optional)."), false);
        }
        if (!i.hasThemeCore()) {
            source.sendSuccess(() -> Component.literal("  ThemeCore not present (optional)."), false);
        }

        if (severe == 0) {
            source.sendSuccess(() -> Component.literal("  No severe issues detected."), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int modules(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== ECHO Modules ==="), false);
        for (EchoModuleInfo module : EchoCoreServices.moduleReport()) {
            String status = module.loaded() ? "[LOADED]" : "[MISSING]";
            source.sendSuccess(() -> Component.literal("  " + status + " " + module.statusLine()), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int integrations(CommandSourceStack source) {
        EchoIntegrations i = EchoIntegrations.current();
        source.sendSuccess(() -> Component.literal("=== ECHO Integrations ==="), false);
        source.sendSuccess(() -> Component.literal("  Mode: " + i.mode()), false);
        source.sendSuccess(() -> Component.literal("  Terminal: " + i.hasTerminal()), false);
        source.sendSuccess(() -> Component.literal("  ThemeCore: " + i.hasThemeCore()), false);
        source.sendSuccess(() -> Component.literal("  Index: " + i.hasIndex()), false);
        source.sendSuccess(() -> Component.literal("  MissionCore: " + i.hasMissionCore()), false);
        source.sendSuccess(() -> Component.literal("  HoloMap: " + i.hasHoloMap()), false);
        source.sendSuccess(() -> Component.literal("  SoundCore: " + i.hasSoundCore()), false);
        source.sendSuccess(() -> Component.literal("  WorldCore: " + i.hasWorldCore()), false);
        source.sendSuccess(() -> Component.literal("  DataCore: " + i.hasDataCore()), false);
        source.sendSuccess(() -> Component.literal("  RuntimeGuard: " + i.hasRuntimeGuard()), false);
        source.sendSuccess(() -> Component.literal("  RenderCore: " + i.hasRenderCore()), false);
        source.sendSuccess(() -> Component.literal("  Lens: " + i.hasLens()), false);
        source.sendSuccess(() -> Component.literal("  Ashfall: " + i.hasAshfall()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int services(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== ECHO Services ==="), false);
        source.sendSuccess(() -> Component.literal("  Mission: " + serviceStatus(IMissionService.class)), false);
        source.sendSuccess(() -> Component.literal("  Network: " + serviceStatus(INetworkService.class)), false);
        source.sendSuccess(() -> Component.literal("  Data: " + serviceStatus(IDataService.class)), false);
        source.sendSuccess(() -> Component.literal("  World: " + serviceStatus(IWorldRegionService.class)), false);
        source.sendSuccess(() -> Component.literal("  Terminal: " + serviceStatus(ITerminalService.class)), false);
        source.sendSuccess(() -> Component.literal("  Theme: " + serviceStatus(IThemeService.class)), false);
        source.sendSuccess(() -> Component.literal("  Sound: " + serviceStatus(ISoundService.class)), false);
        source.sendSuccess(() -> Component.literal("  RuntimeBudget: " + serviceStatus(IRuntimeBudgetService.class)), false);
        source.sendSuccess(() -> Component.literal("  Lens: " + serviceStatus(ILensService.class)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static <T> String serviceStatus(Class<T> type) {
        return EchoServiceRegistry.find(type).isPresent() ? "real" : "no-op";
    }

    private static int themes(CommandSourceStack source) {
        EchoIntegrations i = EchoIntegrations.current();
        IThemeService themeService = EchoCoreServices.themeService();
        source.sendSuccess(() -> Component.literal("=== ECHO Themes ==="), false);
        source.sendSuccess(() -> Component.literal("  ThemeCore loaded: " + i.hasThemeCore()), false);
        source.sendSuccess(() -> Component.literal("  Active theme: " + themeService.currentThemeName()), false);
        source.sendSuccess(() -> Component.literal("  Known tokens: " + themeService.knownTokens().size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int data(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("=== ECHO Data ==="), false);
        source.sendSuccess(() -> Component.literal("  DataCore available: " + EchoServiceRegistry.find(IDataService.class).isPresent()), false);
        source.sendSuccess(() -> Component.literal("  Registered keys: " + EchoCoreServices.dataService().registeredKeys().size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int exportDiagnostics(CommandSourceStack source) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();

        JsonArray modules = new JsonArray();
        for (EchoModuleInfo module : EchoCoreServices.moduleReport()) {
            JsonObject m = new JsonObject();
            m.addProperty("modId", module.modId());
            m.addProperty("displayName", module.displayName());
            m.addProperty("version", module.version());
            m.addProperty("loaded", module.loaded());
            modules.add(m);
        }
        root.add("modules", modules);

        EchoIntegrations i = EchoIntegrations.current();
        JsonObject integrations = new JsonObject();
        integrations.addProperty("mode", i.mode().name());
        integrations.addProperty("hasTerminal", i.hasTerminal());
        integrations.addProperty("hasThemeCore", i.hasThemeCore());
        integrations.addProperty("hasAshfall", i.hasAshfall());
        root.add("integrations", integrations);

        try {
            Path dir = Path.of("echo-diagnostics");
            Files.createDirectories(dir);
            Path file = dir.resolve("echo-diagnostics-" + System.currentTimeMillis() + ".json");
            Files.writeString(file, gson.toJson(root));
            String pathStr = file.toAbsolutePath().toString();
            source.sendSuccess(() -> Component.literal("Exported diagnostics to: " + pathStr), false);
        } catch (IOException exception) {
            source.sendSuccess(() -> Component.literal("Export failed: " + exception.getMessage()), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
