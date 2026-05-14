package com.knoxhack.echosoundcore.command;

import com.knoxhack.echosoundcore.EchoSoundCore;
import com.knoxhack.echosoundcore.SoundCoreCombatIntensity;
import com.knoxhack.echosoundcore.api.SoundCoreApi;
import com.knoxhack.echosoundcore.api.context.SoundCoreContext;
import com.knoxhack.echosoundcore.data.SoundCoreDataReloadListener;
import com.knoxhack.echosoundcore.registry.SoundCoreSounds;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class SoundCoreCommands {
    private SoundCoreCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("echosoundcore")
            .then(Commands.literal("play")
                .then(Commands.argument("soundId", StringArgumentType.string())
                    .executes(SoundCoreCommands::playSound)))
            .then(Commands.literal("stinger")
                .then(Commands.argument("id", StringArgumentType.string())
                    .executes(SoundCoreCommands::playStinger)))
            .then(Commands.literal("music")
                .then(Commands.argument("profileOrSoundId", StringArgumentType.string())
                    .executes(SoundCoreCommands::playMusic)))
            .then(Commands.literal("stop").executes(SoundCoreCommands::stop))
            .then(Commands.literal("context").executes(SoundCoreCommands::context))
            .then(Commands.literal("combat")
                .then(Commands.argument("level", StringArgumentType.word())
                    .executes(SoundCoreCommands::combat)))
            .then(Commands.literal("nexus")
                .then(Commands.argument("level", FloatArgumentType.floatArg(0.0f, 1.0f))
                    .executes(SoundCoreCommands::nexus)))
            .then(Commands.literal("debug").executes(SoundCoreCommands::debug))
            .then(Commands.literal("reload").executes(SoundCoreCommands::reload))
        );
    }

    private static int playSound(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Identifier id = Identifier.parse(StringArgumentType.getString(ctx, "soundId"));
        SoundEvent sound = findSoundById(id);
        if (sound == null) {
            source.sendFailure(Component.literal("Unknown SoundCore sound: " + id));
            return 0;
        }
        if (source.getPlayer() != null) {
            ServerPlayer player = source.getPlayer();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        source.sendSuccess(() -> Component.literal("Playing SoundCore sound: " + id), false);
        return 1;
    }

    private static int playStinger(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "id");
        SoundEvent sound = findSoundByPath(id);
        if (sound == null) {
            source.sendFailure(Component.literal("Unknown stinger: " + id));
            return 0;
        }
        if (source.getPlayer() != null) {
            ServerPlayer player = source.getPlayer();
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        source.sendSuccess(() -> Component.literal("Playing stinger: " + id), false);
        return 1;
    }

    private static int playMusic(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String arg = StringArgumentType.getString(ctx, "profileOrSoundId");
        source.sendSuccess(() -> Component.literal("Music selection: " + arg + " (handled client-side)"), false);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            tryInvokeClient("com.knoxhack.echosoundcore.client.music.SoundCoreMusicManager", "stopControlled");
            tryInvokeClient("com.knoxhack.echosoundcore.client.ambience.SoundCoreAmbienceManager", "stopAll");
        }
        source.sendSuccess(() -> Component.literal("Stopped SoundCore controlled audio."), false);
        return 1;
    }

    private static int context(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        SoundCoreContext current = com.knoxhack.echosoundcore.api.context.SoundCoreContextStack.current();
        source.sendSuccess(() -> Component.literal("ECHO SoundCore // Current Audio Context"), false);
        source.sendSuccess(() -> Component.literal("  Chapter: " + current.chapter()), false);
        source.sendSuccess(() -> Component.literal("  Combat: " + current.combatIntensity()), false);
        source.sendSuccess(() -> Component.literal("  Boss: " + current.bossId()), false);
        source.sendSuccess(() -> Component.literal("  Nexus Corruption: " + current.nexusCorruptionLevel()), false);
        source.sendSuccess(() -> Component.literal("  Terminal Open: " + current.terminalOpen()), false);
        return 1;
    }

    private static int combat(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String level = StringArgumentType.getString(ctx, "level");
        try {
            SoundCoreCombatIntensity intensity = SoundCoreCombatIntensity.valueOf(level.toUpperCase());
            SoundCoreApi.setCombatIntensity(intensity);
            source.sendSuccess(() -> Component.literal("Combat intensity set to: " + intensity), false);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid intensity. Use none, light, heavy, elite, boss, or siege."));
            return 0;
        }
    }

    private static int nexus(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        float level = FloatArgumentType.getFloat(ctx, "level");
        SoundCoreApi.setNexusCorruptionLevel(level);
        source.sendSuccess(() -> Component.literal("Nexus corruption level set to: " + level), false);
        return 1;
    }

    private static int debug(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("ECHO SoundCore // Debug Info"), false);
        source.sendSuccess(() -> Component.literal("  Music Profiles: " + SoundCoreDataReloadListener.getMusicProfiles().size()), false);
        source.sendSuccess(() -> Component.literal("  Ambience Profiles: " + SoundCoreDataReloadListener.getAmbienceProfiles().size()), false);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            Object track = tryInvokeClientResult("com.knoxhack.echosoundcore.client.music.SoundCoreMusicManager", "currentTrackId");
            Object loops = tryInvokeClientResult("com.knoxhack.echosoundcore.client.ambience.SoundCoreAmbienceManager", "activeLoops");
            source.sendSuccess(() -> Component.literal("  Current Track: " + (track != null ? track : "null")), false);
            source.sendSuccess(() -> Component.literal("  Active Ambience Loops: " + (loops != null ? ((java.util.Map<?,?>)loops).size() : 0)), false);
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("ECHO SoundCore // Use /reload to reload data-driven audio profiles."), false);
        return 1;
    }

    private static SoundEvent findSoundById(Identifier id) {
        for (var entry : SoundCoreSounds.getEntries()) {
            if (entry.getId().equals(id)) {
                return entry.get();
            }
        }
        return null;
    }

    private static SoundEvent findSoundByPath(String path) {
        Identifier id = EchoSoundCore.id(path);
        return findSoundById(id);
    }

    private static void tryInvokeClient(String className, String methodName) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method m = clazz.getMethod(methodName);
            m.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            EchoSoundCore.LOGGER.debug("Client-only class {} not available.", className);
        } catch (ReflectiveOperationException e) {
            EchoSoundCore.LOGGER.warn("Could not invoke {}.{}", className, methodName, e);
        }
    }

    private static Object tryInvokeClientResult(String className, String methodName) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Method m = clazz.getMethod(methodName);
            return m.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            EchoSoundCore.LOGGER.debug("Client-only class {} not available.", className);
            return null;
        } catch (ReflectiveOperationException e) {
            EchoSoundCore.LOGGER.warn("Could not invoke {}.{}", className, methodName, e);
            return null;
        }
    }
}
