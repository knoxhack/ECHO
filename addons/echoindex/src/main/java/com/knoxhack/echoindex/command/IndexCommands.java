package com.knoxhack.echoindex.command;

import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.service.IndexDiscoveryStore;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echoindex.service.IndexSourceRecipeProvider;
import com.knoxhack.echoindex.service.VanillaIndexRecipeProvider;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = EchoIndex.MODID)
public final class IndexCommands {
    private IndexCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("echoindex")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("categories").executes(ctx -> categories(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("recipes").executes(ctx -> recipes(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("entry", StringArgumentType.string())
                                .executes(ctx -> unlock(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "entry")))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("entry", StringArgumentType.string())
                                .executes(ctx -> reset(ctx.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(ctx, "entry")))))
                .then(Commands.literal("validate").executes(ctx -> validate(ctx.getSource().getPlayerOrException()))));
    }

    private static int list(ServerPlayer player) {
        int recipeCategoryCount = IndexService.INSTANCE.recipeCategories(player).size();
        int recipeCount = IndexService.INSTANCE.recipes(player).size();
        tell(player, "ECHO Index // Categories " + IndexService.INSTANCE.categories(player).size()
                + ", entries " + IndexService.INSTANCE.entries(player).size()
                + ", recipe categories " + recipeCategoryCount
                + ", recipes " + recipeCount + ".", ChatFormatting.AQUA);
        return Command.SINGLE_SUCCESS;
    }

    private static int categories(ServerPlayer player) {
        for (IndexCategory category : IndexService.INSTANCE.categories(player)) {
            tell(player, " - " + category.id() + " | " + category.titleKey(), ChatFormatting.GRAY);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int recipes(ServerPlayer player) {
        int rawVanilla = VanillaIndexRecipeProvider.rawRecipeCount(player);
        int adaptedVanilla = VanillaIndexRecipeProvider.adaptedRecipeCount(player);
        int sourceFacts = IndexSourceRecipeProvider.INSTANCE.sourceFactCount();
        int sourceCards = IndexSourceRecipeProvider.INSTANCE.sourceRecipeCount(player);
        tell(player, "ECHO Index // Recipe providers " + IndexService.INSTANCE.providerCount()
                + ", recipes " + IndexService.INSTANCE.recipes(player).size() + ".", ChatFormatting.AQUA);
        tell(player, " - Vanilla raw " + rawVanilla + ", adapted " + adaptedVanilla + ".", ChatFormatting.GRAY);
        tell(player, " - Source facts " + sourceFacts + ", source cards " + sourceCards + ".", ChatFormatting.GRAY);
        return Command.SINGLE_SUCCESS;
    }

    private static int unlock(ServerPlayer player, String rawEntry) {
        if (!Config.DEBUG_COMMANDS.get()) {
            tell(player, "ECHO Index // Debug commands are disabled in config.", ChatFormatting.RED);
            return 0;
        }
        Identifier id = Identifier.tryParse(rawEntry);
        if (id == null) {
            tell(player, "ECHO Index // Invalid entry id: " + rawEntry, ChatFormatting.RED);
            return 0;
        }
        IndexDiscoveryStore.INSTANCE.discover(player, id);
        tell(player, "ECHO Index // Entry unlocked: " + id + ".", ChatFormatting.GREEN);
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(ServerPlayer player, String rawEntry) {
        if (!Config.DEBUG_COMMANDS.get()) {
            tell(player, "ECHO Index // Debug commands are disabled in config.", ChatFormatting.RED);
            return 0;
        }
        Identifier id = Identifier.tryParse(rawEntry);
        if (id == null) {
            tell(player, "ECHO Index // Invalid entry id: " + rawEntry, ChatFormatting.RED);
            return 0;
        }
        IndexDiscoveryStore.INSTANCE.reset(player, id);
        tell(player, "ECHO Index // Entry state reset: " + id + ".", ChatFormatting.YELLOW);
        return Command.SINGLE_SUCCESS;
    }

    private static int validate(ServerPlayer player) {
        List<Identifier> categoryIds = IndexService.INSTANCE.categories(player).stream().map(IndexCategory::id).toList();
        List<String> warnings = IndexService.INSTANCE.entries(player).stream()
                .filter(entry -> !categoryIds.contains(entry.categoryId()))
                .map(IndexEntry::id)
                .map(Identifier::toString)
                .toList();
        if (warnings.isEmpty()) {
            tell(player, "ECHO Index // Validation passed.", ChatFormatting.GREEN);
        } else {
            tell(player, "ECHO Index // Validation found " + warnings.size() + " warning(s): "
                    + String.join(", ", warnings), ChatFormatting.YELLOW);
        }
        return warnings.isEmpty() ? Command.SINGLE_SUCCESS : 0;
    }

    private static void tell(ServerPlayer player, String message, ChatFormatting color) {
        player.sendSystemMessage(Component.literal(message).withStyle(color));
    }
}
