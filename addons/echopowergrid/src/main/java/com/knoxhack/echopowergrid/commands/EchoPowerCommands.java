package com.knoxhack.echopowergrid.commands;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.api.PowerGridSnapshot;
import com.knoxhack.echopowergrid.block.entity.BatteryBlockEntity;
import com.knoxhack.echopowergrid.grid.PowerNetworkManager;
import com.knoxhack.echopowergrid.registry.ModBlocks;
import com.knoxhack.echopowergrid.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public final class EchoPowerCommands {
    private EchoPowerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("echo_power")
            .then(Commands.literal("status").executes(EchoPowerCommands::status))
            .then(Commands.literal("inspect").executes(EchoPowerCommands::inspect))
            .then(Commands.literal("networks").executes(EchoPowerCommands::networks))
            .then(Commands.literal("debug_chunk").executes(EchoPowerCommands::debugChunk))
            .then(Commands.literal("give_test_kit")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(EchoPowerCommands::giveTestKit))
            .then(Commands.literal("set_energy")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                    .executes(EchoPowerCommands::setEnergy)))
            .then(Commands.literal("reset_network")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(EchoPowerCommands::resetNetwork))
        );
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        var pos = player.blockPosition();
        var level = (ServerLevel) player.level();
        PowerGridSnapshot snap = EchoPowerGridApi.getSnapshot(level, pos);
        source.sendSuccess(() -> Component.literal("ECHO GRID // Server Status"), false);
        source.sendSuccess(() -> Component.literal("  Generation: " + snap.totalGeneration() + " EP/t"), false);
        source.sendSuccess(() -> Component.literal("  Demand: " + snap.totalDemand() + " EP/t"), false);
        source.sendSuccess(() -> Component.literal("  Stored: " + snap.totalStored() + "/" + snap.totalCapacity()), false);
        source.sendSuccess(() -> Component.literal("  State: " + snap.state()), false);
        return 1;
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        var pos = player.blockPosition();
        var level = (ServerLevel) player.level();
        var storage = EchoPowerGridApi.getEnergyStorage(level, pos);
        if (storage.isPresent()) {
            var s = storage.get();
            source.sendSuccess(() -> Component.literal("ECHO GRID // Inspect Target"), false);
            source.sendSuccess(() -> Component.literal("  Energy: " + s.getEnergyStored() + "/" + s.getMaxEnergyStored()), false);
            source.sendSuccess(() -> Component.literal("  Max I/O: " + s.getMaxInput() + "/" + s.getMaxOutput()), false);
        } else {
            source.sendFailure(Component.literal("No power node at your position."));
        }
        return 1;
    }

    private static int networks(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var level = source.getLevel();
        int count = 0;
        // We don't expose the internal network map directly; just give a generic message
        source.sendSuccess(() -> Component.literal("ECHO GRID // Loaded networks are managed per-dimension."), false);
        source.sendSuccess(() -> Component.literal("  Use /echo_power inspect or /echo_power debug_chunk for details."), false);
        return 1;
    }

    private static int debugChunk(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        var level = (ServerLevel) player.level();
        var chunkPos = player.blockPosition();
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(chunkPos.offset(-8, -64, -8), chunkPos.offset(8, 320, 8))) {
            if (EchoPowerGridApi.getEnergyStorage(level, pos).isPresent()) {
                count++;
            }
        }
        int finalCount = count;
        source.sendSuccess(() -> Component.literal("ECHO GRID // Power nodes near current position: " + finalCount), false);
        return 1;
    }

    private static int giveTestKit(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        player.getInventory().add(new ItemStack(ModBlocks.CREATIVE_POWER_SOURCE.get()));
        player.getInventory().add(new ItemStack(ModBlocks.CREATIVE_POWER_SINK.get()));
        player.getInventory().add(new ItemStack(ModBlocks.POWER_METER.get()));
        player.getInventory().add(new ItemStack(ModBlocks.LOW_VOLTAGE_CABLE.get(), 32));
        player.getInventory().add(new ItemStack(ModBlocks.SMALL_BATTERY_BANK.get()));
        player.getInventory().add(new ItemStack(ModBlocks.TEST_POWER_CONSUMER.get()));
        source.sendSuccess(() -> Component.literal("ECHO GRID // Test kit delivered."), false);
        return 1;
    }

    private static int setEnergy(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        long amount = LongArgumentType.getLong(ctx, "amount");
        var level = (ServerLevel) player.level();
        var pos = player.blockPosition();
        var be = level.getBlockEntity(pos);
        if (be instanceof BatteryBlockEntity bat) {
            bat.receiveEnergy(amount - bat.getEnergyStored(), false);
            source.sendSuccess(() -> Component.literal("ECHO GRID // Battery energy set to " + amount), false);
        } else {
            source.sendFailure(Component.literal("Target block is not a battery."));
        }
        return 1;
    }

    private static int resetNetwork(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be used by a player."));
            return 0;
        }
        var level = (ServerLevel) player.level();
        var pos = player.blockPosition();
        PowerNetworkManager.get(level).markDirty(pos);
        source.sendSuccess(() -> Component.literal("ECHO GRID // Network marked dirty. Rebuild queued."), false);
        return 1;
    }
}
