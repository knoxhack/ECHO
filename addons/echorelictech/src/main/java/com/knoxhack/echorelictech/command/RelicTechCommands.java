package com.knoxhack.echorelictech.command;

import com.knoxhack.echorelictech.api.RelicTechApi;
import com.knoxhack.echorelictech.api.relic.RelicCondition;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import com.knoxhack.echorelictech.data.RelicDefinitionLoader;
import com.knoxhack.echorelictech.data.RelicVaultLoader;
import com.knoxhack.echorelictech.registry.ModDataComponents;
import com.knoxhack.echorelictech.registry.ModItems;
import com.knoxhack.echorelictech.server.RelicInstabilityManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public final class RelicTechCommands {
    private RelicTechCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("echorelictech")
            .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
            .then(Commands.literal("give_relic")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("relicId", StringArgumentType.string())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            String idStr = StringArgumentType.getString(ctx, "relicId");
                            Identifier id = Identifier.tryParse(idStr);
                            if (id == null) {
                                ctx.getSource().sendFailure(Component.literal("Invalid relic ID."));
                                return 0;
                            }
                            ItemStack stack = new ItemStack(ModItems.PHASE_ANCHOR.get());
                            if (id.getPath().equals("null_battery")) stack = new ItemStack(ModItems.NULL_BATTERY.get());
                            else if (id.getPath().equals("guardian_lens")) stack = new ItemStack(ModItems.GUARDIAN_LENS.get());
                            else if (id.getPath().equals("echo_mirror")) stack = new ItemStack(ModItems.ECHO_MIRROR.get());
                            else if (id.getPath().equals("matter_stitcher")) stack = new ItemStack(ModItems.MATTER_STITCHER.get());

                            stack.set(ModDataComponents.RELIC_DATA.get(), new RelicInstanceData(
                                id, RelicCondition.DAMAGED, 0, BlockPos.ZERO, "", 0, false, false, false, true, 0));
                            if (!target.getInventory().add(stack)) target.drop(stack, false);
                            ctx.getSource().sendSuccess(() -> Component.literal("Gave relic " + id + " to " + target.getName().getString()), true);
                            return 1;
                        }))))
            .then(Commands.literal("instability")
                .then(Commands.literal("get")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            int val = RelicInstabilityManager.getInstability(target);
                            int level = RelicInstabilityManager.getInstabilityLevel(target);
                            ctx.getSource().sendSuccess(() -> Component.literal("Instability: " + val + " (Level " + level + " - " + RelicInstabilityManager.levelName(level) + ")"), false);
                            return 1;
                        })))
                .then(Commands.literal("set")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                RelicInstabilityManager.setInstability(target, amount);
                                ctx.getSource().sendSuccess(() -> Component.literal("Set instability to " + amount), true);
                                return 1;
                            }))))
                .then(Commands.literal("add")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                RelicInstabilityManager.addInstability(target, amount);
                                ctx.getSource().sendSuccess(() -> Component.literal("Added " + amount + " instability."), true);
                                return 1;
                            })))))
            .then(Commands.literal("identify_held")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) {
                        ctx.getSource().sendFailure(Component.literal("Must be run by a player."));
                        return 0;
                    }
                    ItemStack held = player.getMainHandItem();
                    RelicTechApi.identifyRelic(player, held);
                    ctx.getSource().sendSuccess(() -> Component.literal("Held item identified."), true);
                    return 1;
                }))
            .then(Commands.literal("corrupt_held")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                    RelicTechApi.setRelicCondition(player.getMainHandItem(), RelicCondition.CORRUPTED);
                    ctx.getSource().sendSuccess(() -> Component.literal("Held relic corrupted."), true);
                    return 1;
                }))
            .then(Commands.literal("stabilize_held")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                    RelicTechApi.setRelicCondition(player.getMainHandItem(), RelicCondition.STABILIZED);
                    ctx.getSource().sendSuccess(() -> Component.literal("Held relic stabilized."), true);
                    return 1;
                }))
            .then(Commands.literal("contain_held")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                    RelicTechApi.setRelicCondition(player.getMainHandItem(), RelicCondition.CONTAINED);
                    ctx.getSource().sendSuccess(() -> Component.literal("Held relic contained."), true);
                    return 1;
                }))
            .then(Commands.literal("overclock_held")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                    RelicTechApi.setRelicCondition(player.getMainHandItem(), RelicCondition.OVERCLOCKED);
                    ctx.getSource().sendSuccess(() -> Component.literal("Held relic overclocked."), true);
                    return 1;
                }))
            .then(Commands.literal("trigger_failure")
                .then(Commands.argument("severity", StringArgumentType.string())
                    .executes(ctx -> {
                        if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                        String sev = StringArgumentType.getString(ctx, "severity");
                        player.sendSystemMessage(Component.literal("Forced failure triggered: " + sev));
                        return 1;
                    })))
            .then(Commands.literal("bind_phase_anchor")
                .executes(ctx -> {
                    if (!(ctx.getSource().getPlayer() instanceof ServerPlayer player)) return 0;
                    RelicTechApi.bindPhaseAnchor(player, player.getMainHandItem(), player.blockPosition());
                    return 1;
                }))
            .then(Commands.literal("locate_vault")
                .executes(ctx -> {
                    var vaults = RelicVaultLoader.all();
                    ctx.getSource().sendSuccess(() -> Component.literal("ECHO: RelicTech // Known Vaults (" + vaults.size() + "):"), false);
                    for (var vault : vaults) {
                        ctx.getSource().sendSuccess(() -> Component.literal("  " + vault.displayName() + " [" + vault.tier() + "] Security: " + vault.securityLevel()), false);
                    }
                    return 1;
                }))
            .then(Commands.literal("debug")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("RelicTech debug info: Definitions=" + RelicDefinitionLoader.all().size()), false);
                    return 1;
                }))
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Data reload handled automatically via reload listener."), false);
                    return 1;
                }))
        );
    }
}
