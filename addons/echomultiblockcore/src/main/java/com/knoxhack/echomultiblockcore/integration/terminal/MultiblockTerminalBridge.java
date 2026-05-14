package com.knoxhack.echomultiblockcore.integration.terminal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.api.AutomationIngredient;
import com.knoxhack.echomultiblockcore.api.AutomationOutput;
import com.knoxhack.echomultiblockcore.api.AutomationRecipeRegistry;
import com.knoxhack.echomultiblockcore.api.MultiblockAutomationRecipe;
import com.knoxhack.echomultiblockcore.api.MultiblockIntegrationServices;
import com.knoxhack.echomultiblockcore.api.MultiblockProgressionDefinition;
import com.knoxhack.echomultiblockcore.api.MultiblockProgressionRegistry;
import com.knoxhack.echomultiblockcore.api.MultiblockStatusSnapshot;
import com.knoxhack.echomultiblockcore.block.entity.MultiblockControllerBlockEntity;
import com.knoxhack.echomultiblockcore.registry.ModBlocks;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonInfo;
import com.knoxhack.echoterminal.api.TerminalAddonInfoProvider;
import com.knoxhack.echoterminal.api.TerminalAddonInfoRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonMetric;
import com.knoxhack.echoterminal.api.TerminalAddonSection;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeCategory;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeEntry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeNote;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeProvider;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeRegistry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeSlot;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MultiblockTerminalBridge {
    public static final Identifier TAB_ID = EchoMultiblockCore.id("terminal");
    public static final Identifier START_TASK = EchoMultiblockCore.id("start_task");
    public static final Identifier CLEAR_QUEUE = EchoMultiblockCore.id("clear_queue");
    public static final Identifier RETRY_BLOCKED = EchoMultiblockCore.id("retry_blocked");
    public static final Identifier PAUSE_QUEUE = EchoMultiblockCore.id("pause_queue");
    public static final Identifier RESUME_QUEUE = EchoMultiblockCore.id("resume_queue");
    private static final int ACCENT = 0xFF66E8FF;
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private MultiblockTerminalBridge() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalRecipeRegistry.register(new RecipeProvider());
        TerminalAddonInfoRegistry.register(new AddonInfoProvider());
        TerminalActionRegistry.register(TAB_ID, START_TASK, MultiblockTerminalBridge::startTask);
        TerminalActionRegistry.register(TAB_ID, CLEAR_QUEUE, (player, payload) -> withController(player, payload,
                controller -> controller.clearQueue(player)));
        TerminalActionRegistry.register(TAB_ID, RETRY_BLOCKED, (player, payload) -> withController(player, payload,
                controller -> controller.retryBlocked(player)));
        TerminalActionRegistry.register(TAB_ID, PAUSE_QUEUE, (player, payload) -> withController(player, payload,
                controller -> controller.pauseQueue(player)));
        TerminalActionRegistry.register(TAB_ID, RESUME_QUEUE, (player, payload) -> withController(player, payload,
                controller -> controller.resumeQueue(player)));
        EchoMultiblockCore.LOGGER.info("ECHO MultiblockCore terminal automation bridge registered.");
    }

    private static void startTask(ServerPlayer player, String payload) {
        ActionPayload parsed = ActionPayload.parse(player, payload);
        if (parsed == null || parsed.recipeId() == null) {
            return;
        }
        withController(player, parsed, controller -> controller.queueRecipe(parsed.recipeId(), player));
    }

    private static void withController(ServerPlayer player, String payload, ControllerConsumer consumer) {
        ActionPayload parsed = ActionPayload.parse(player, payload);
        if (parsed == null) {
            return;
        }
        withController(player, parsed, consumer);
    }

    private static void withController(ServerPlayer player, ActionPayload payload, ControllerConsumer consumer) {
        if (player == null || payload == null || payload.controllerPos() == null || consumer == null) {
            return;
        }
        ServerLevel level = payload.level(player);
        if (level == null || !level.isLoaded(payload.controllerPos())) {
            return;
        }
        if (!player.level().dimension().equals(level.dimension()) || player.blockPosition().distManhattan(payload.controllerPos()) > 32) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(payload.controllerPos());
        if (blockEntity instanceof MultiblockControllerBlockEntity controller) {
            consumer.accept(controller);
        }
    }

    private record RecipeProvider() implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return EchoMultiblockCore.id("automation_recipes");
        }

        @Override
        public String displayName() {
            return "Multiblock Automation";
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            Map<Identifier, TerminalRecipeCategory> categories = new LinkedHashMap<>();
            for (MultiblockAutomationRecipe recipe : AutomationRecipeRegistry.all()) {
                categories.putIfAbsent(recipe.category(), new TerminalRecipeCategory(
                        recipe.category(),
                        title(recipe.category()),
                        new ItemStack(ModBlocks.MULTIBLOCK_CONTROLLER.asItem()),
                        ACCENT,
                        categories.size() * 10));
            }
            return List.copyOf(categories.values());
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            return AutomationRecipeRegistry.all().stream()
                    .map(this::entry)
                    .sorted(Comparator.comparing(entry -> entry.id().toString()))
                    .toList();
        }

        private TerminalRecipeEntry entry(MultiblockAutomationRecipe recipe) {
            List<TerminalRecipeSlot> slots = java.util.stream.Stream.concat(
                    recipe.inputs().stream().map(this::inputSlot),
                    recipe.outputs().stream().map(this::outputSlot))
                    .toList();
            List<TerminalRecipeNote> notes = java.util.stream.Stream.concat(
                    java.util.stream.Stream.of(TerminalRecipeNote.info("Workcell: " + recipe.requiredWorkcell().name()),
                            TerminalRecipeNote.info("Tools: " + (recipe.requiredTools().isEmpty()
                                    ? "Any" : recipe.requiredTools().stream().map(Enum::name).reduce((a, b) -> a + ", " + b).orElse("Any")))),
                    recipe.notes().stream().map(TerminalRecipeNote::info))
                    .toList();
            return new TerminalRecipeEntry(
                    recipe.id(),
                    recipe.category(),
                    recipe.displayName(),
                    new ItemStack(ModBlocks.MULTIBLOCK_CONTROLLER.asItem()),
                    slots,
                    notes,
                    recipe.durationTicks(),
                    false);
        }

        private TerminalRecipeSlot inputSlot(AutomationIngredient ingredient) {
            List<ItemStack> examples = ingredient.exampleStacks();
            return examples.isEmpty()
                    ? TerminalRecipeSlot.text(TerminalRecipeSlot.Role.INPUT, ingredient.summary())
                    : new TerminalRecipeSlot(TerminalRecipeSlot.Role.INPUT, examples, ingredient.summary());
        }

        private TerminalRecipeSlot outputSlot(AutomationOutput output) {
            ItemStack stack = output.stack();
            return stack.isEmpty()
                    ? TerminalRecipeSlot.text(TerminalRecipeSlot.Role.OUTPUT, output.summary())
                    : TerminalRecipeSlot.output(stack);
        }

        private static String title(Identifier id) {
            return id.getPath().replace('_', ' ');
        }
    }

    private record AddonInfoProvider() implements TerminalAddonInfoProvider {
        @Override
        public String chapterId() {
            return EchoMultiblockCore.CHAPTER_ID;
        }

        @Override
        public TerminalAddonInfo info(Player player) {
            List<MultiblockStatusSnapshot> snapshots = MultiblockIntegrationServices.terminalSnapshots(player);
            long active = snapshots.stream().filter(snapshot -> snapshot.state().name().equals("ACTIVE")).count();
            int queued = snapshots.stream().mapToInt(snapshot -> snapshot.currentTasks().size()).sum();
            return new TerminalAddonInfo(
                    "Shared facility automation, robotics, and multiblock runtime services.",
                    List.of(
                            new TerminalAddonMetric("Definitions", String.valueOf(com.knoxhack.echomultiblockcore.content.MultiblockContent.definitions().size()), "loaded structures", ACCENT),
                            new TerminalAddonMetric("Recipes", String.valueOf(AutomationRecipeRegistry.all().size()), "automation tasks", 0xFF92F7A6),
                            new TerminalAddonMetric("Progression", String.valueOf(MultiblockProgressionRegistry.all().size()), "facility steps", 0xFFB7F7FF),
                            new TerminalAddonMetric("Formed", String.valueOf(snapshots.size()), "known runtimes", 0xFFFFD166),
                            new TerminalAddonMetric("Active", String.valueOf(active), "running workcells", 0xFFFF8FA3)),
                    List.of(new TerminalAddonSection("Task Queue", List.of(
                            "Queued tasks: " + queued,
                            "Terminal actions: start, clear, retry, pause, resume",
                            "Payload: dimension + controller_pos + recipe_id")),
                            new TerminalAddonSection("Facility Progression", progressionLines())),
                    List.of());
        }

        private static List<String> progressionLines() {
            List<String> lines = MultiblockProgressionRegistry.all().stream()
                    .limit(8)
                    .map(MultiblockTerminalBridge.AddonInfoProvider::progressionLine)
                    .toList();
            return lines.isEmpty() ? List.of("No progression entries loaded.") : lines;
        }

        private static String progressionLine(MultiblockProgressionDefinition progression) {
            return "T" + progression.tier() + " " + progression.title()
                    + (progression.featuredRecipes().isEmpty() ? "" : " // " + progression.featuredRecipeSummary());
        }
    }

    private record ActionPayload(Identifier dimension, BlockPos controllerPos, Identifier recipeId) {
        static ActionPayload parse(ServerPlayer player, String payload) {
            try {
                JsonObject json = payload == null || payload.isBlank()
                        ? new JsonObject()
                        : JsonParser.parseString(payload).getAsJsonObject();
                Identifier dimension = json.has("dimension")
                        ? Identifier.parse(json.get("dimension").getAsString())
                        : (player == null ? Level.OVERWORLD.identifier() : player.level().dimension().identifier());
                BlockPos pos = pos(json.get("controller_pos"));
                Identifier recipe = json.has("recipe_id") ? Identifier.parse(json.get("recipe_id").getAsString()) : null;
                return new ActionPayload(dimension, pos, recipe);
            } catch (RuntimeException exception) {
                EchoMultiblockCore.LOGGER.warn("Ignoring malformed MultiblockCore terminal action payload: {}", payload);
                return null;
            }
        }

        ServerLevel level(ServerPlayer player) {
            if (player == null) {
                return null;
            }
            MinecraftServer server = player.level().getServer();
            if (server == null) {
                return null;
            }
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimension);
            return server.getLevel(key);
        }

        private static BlockPos pos(com.google.gson.JsonElement element) {
            if (element == null || element.isJsonNull()) {
                return null;
            }
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                if (array.size() < 3) {
                    return null;
                }
                return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
            }
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return BlockPos.of(element.getAsLong());
            }
            String raw = element.getAsString().replace(',', ' ').trim();
            String[] parts = raw.split("\\s+");
            if (parts.length >= 3) {
                return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
            return BlockPos.of(Long.parseLong(raw));
        }
    }

    @FunctionalInterface
    private interface ControllerConsumer {
        void accept(MultiblockControllerBlockEntity controller);
    }
}
