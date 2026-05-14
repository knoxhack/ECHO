package com.knoxhack.echoindex.test;

import com.knoxhack.echocore.api.mission.InMemoryMissionRegistry;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.integration.IndexMissionCoreIntegration;
import com.knoxhack.echoindex.integration.IndexTerminalImportRecipeProvider;
import com.knoxhack.echoindex.integration.IndexTerminalRecipeProvider;
import com.knoxhack.echoindex.service.IndexRecipeDisplayMetadata;
import com.knoxhack.echoindex.service.IndexRecipeLayoutType;
import com.knoxhack.echoindex.service.IndexRecipeSnapshot;
import com.knoxhack.echoindex.service.IndexRecipeSnapshotCodec;
import com.knoxhack.echoindex.service.IndexService;
import com.knoxhack.echoindex.service.VanillaIndexRecipeProvider;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeCategory;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeEntry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeNote;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeProvider;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeRegistry;
import com.knoxhack.echoterminal.api.recipe.TerminalRecipeSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoIndex.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MISSION_CORE_CONTENT =
            TEST_FUNCTIONS.register("missioncore_content_registration", () -> ModGameTests::missionCoreContentRegistration);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_IMPORT_CONVERSION =
            TEST_FUNCTIONS.register("terminal_import_recipe_conversion", () -> ModGameTests::terminalImportRecipeConversion);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TERMINAL_REGISTRY_REVISION =
            TEST_FUNCTIONS.register("terminal_recipe_registry_revision", () -> ModGameTests::terminalRecipeRegistryRevision);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEMANTIC_DUPLICATE_FILTERING =
            TEST_FUNCTIONS.register("semantic_duplicate_filtering", () -> ModGameTests::semanticDuplicateFiltering);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPE_LIKE_PROVIDER_CARDS =
            TEST_FUNCTIONS.register("recipe_like_provider_cards", () -> ModGameTests::recipeLikeProviderCards);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> QUERY_RESULT_LIMITING =
            TEST_FUNCTIONS.register("recipe_query_result_limiting", () -> ModGameTests::recipeQueryResultLimiting);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VANILLA_RECIPE_GRID_METADATA =
            TEST_FUNCTIONS.register("vanilla_recipe_grid_metadata", () -> ModGameTests::vanillaRecipeGridMetadata);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("index_missioncore"));
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                200,
                0,
                true,
                Rotation.NONE,
                false,
                1,
                1,
                false,
                2);
        event.registerTest(id("missioncore_content_registration"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, MISSION_CORE_CONTENT.getId()), data));
        event.registerTest(id("terminal_import_recipe_conversion"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, TERMINAL_IMPORT_CONVERSION.getId()), data));
        event.registerTest(id("terminal_recipe_registry_revision"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, TERMINAL_REGISTRY_REVISION.getId()), data));
        event.registerTest(id("semantic_duplicate_filtering"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, SEMANTIC_DUPLICATE_FILTERING.getId()), data));
        event.registerTest(id("recipe_like_provider_cards"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, RECIPE_LIKE_PROVIDER_CARDS.getId()), data));
        event.registerTest(id("recipe_query_result_limiting"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, QUERY_RESULT_LIMITING.getId()), data));
        event.registerTest(id("vanilla_recipe_grid_metadata"),
                new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, VANILLA_RECIPE_GRID_METADATA.getId()), data));
    }

    private static void missionCoreContentRegistration(GameTestHelper helper) {
        InMemoryMissionRegistry registry = new InMemoryMissionRegistry();
        IndexMissionCoreIntegration.registerContent(registry);
        helper.assertTrue(registry.chapter(id("index")).isPresent(), "Index MissionCore chapter should be owned by Index.");
        assertMission(helper, registry, "open_search_entry", "open", MissionObjectiveType.UNLOCK_RESEARCH);
        assertMission(helper, registry, "inspect_recipe_source", "recipe", MissionObjectiveType.UNLOCK_RESEARCH);
        assertMission(helper, registry, "follow_source_note", "source", MissionObjectiveType.UNLOCK_RESEARCH);
        helper.succeed();
    }

    private static void terminalImportRecipeConversion(GameTestHelper helper) {
        TerminalRecipeEntry entry = new TerminalRecipeEntry(
                id("terminal/custom_recipe"),
                id("terminal/category"),
                "Terminal Recipe",
                new ItemStack(Items.FURNACE),
                List.of(
                        TerminalRecipeSlot.input(Items.IRON_ORE),
                        TerminalRecipeSlot.catalyst(Items.COAL),
                        TerminalRecipeSlot.output(Items.IRON_INGOT),
                        TerminalRecipeSlot.text(TerminalRecipeSlot.Role.OUTPUT, "Output fluid #2 x100")),
                List.of(TerminalRecipeNote.info("Process note"), TerminalRecipeNote.warning("Careful")),
                80,
                true);

        IndexRecipeView view = IndexTerminalImportRecipeProvider.convertForTests(entry);
        helper.assertTrue(view.id().equals(id("terminal_import/echoindex/terminal/custom_recipe")),
                "Terminal import should namespace recipe ids under EchoIndex.");
        helper.assertTrue(view.categoryId().equals(entry.categoryId()), "Terminal import should preserve category ids.");
        helper.assertTrue(view.processTicks() == 80 && view.locked(), "Terminal import should preserve timing and lock state.");
        helper.assertTrue(view.machine().is(Items.FURNACE), "Terminal import should preserve machine stack.");
        helper.assertTrue(view.itemsForRole(IndexSlotRole.INPUT).contains(Items.IRON_ORE),
                "Terminal import should index inputs.");
        helper.assertTrue(view.itemsForRole(IndexSlotRole.CATALYST).contains(Items.COAL),
                "Terminal import should index catalysts.");
        helper.assertTrue(view.itemsForRole(IndexSlotRole.OUTPUT).contains(Items.IRON_INGOT),
                "Terminal import should index item outputs.");
        helper.assertTrue(view.slots().stream().anyMatch(slot -> slot.role() == IndexSlotRole.OUTPUT
                        && slot.stacks().isEmpty()
                        && slot.label().equals("Output fluid #2 x100")),
                "Terminal import should preserve text-only output slots.");
        helper.assertTrue(view.notes().contains("Warning: Careful"), "Terminal warnings should remain visible in Index notes.");
        helper.assertFalse(IndexTerminalImportRecipeProvider.importableForTests(IndexTerminalRecipeProvider.INSTANCE),
                "Terminal import should skip the Index-to-Terminal export provider.");
        helper.succeed();
    }

    private static void terminalRecipeRegistryRevision(GameTestHelper helper) {
        long before = TerminalRecipeRegistry.revision();
        AtomicInteger changes = new AtomicInteger();
        Runnable listener = changes::incrementAndGet;
        TerminalRecipeRegistry.addChangeListener(listener);
        try {
            TerminalRecipeRegistry.withClearedForTests(() ->
                    TerminalRecipeRegistry.register(new DummyTerminalRecipeProvider(id("dummy_terminal_provider"))));
        } finally {
            TerminalRecipeRegistry.removeChangeListener(listener);
        }
        helper.assertTrue(TerminalRecipeRegistry.revision() > before,
                "Terminal recipe registry revision should advance after provider mutations.");
        helper.assertTrue(changes.get() > 0, "Terminal recipe registry listeners should fire on provider mutations.");
        helper.succeed();
    }

    private static void semanticDuplicateFiltering(GameTestHelper helper) {
        Identifier categoryId = id("duplicate/category");
        Identifier directRecipeId = id("duplicate/direct_recipe");
        Identifier importedRecipeId = id("terminal_import/echoindex/duplicate/recipe");
        IIndexRecipeProvider directProvider = new DummyIndexRecipeProvider(
                id("provider/direct_duplicate"),
                new IndexRecipeCategory(categoryId, "Duplicate Recipes", new ItemStack(Items.CRAFTING_TABLE), 0xFF66E8FF, 1),
                new IndexRecipeView(
                        directRecipeId,
                        categoryId,
                        "Direct Duplicate",
                        new ItemStack(Items.CRAFTING_TABLE),
                        duplicateSlots(),
                        List.of("Direct provider owns this semantic recipe."),
                        20,
                        false,
                        EchoIndex.MODID));

        TerminalRecipeRegistry.withClearedForTests(() -> {
            TerminalRecipeRegistry.register(new DuplicateTerminalRecipeProvider(id("duplicate_terminal_provider")));
            IndexRecipeSnapshot importFirst = IndexService.INSTANCE.recipeSnapshotForTests(null,
                    List.of(IndexTerminalImportRecipeProvider.INSTANCE, directProvider));
            helper.assertTrue(importFirst.recipe(directRecipeId).isPresent(),
                    "Direct Index recipe should replace an earlier Terminal-import duplicate.");
            helper.assertFalse(importFirst.recipe(importedRecipeId).isPresent(),
                    "Terminal-import duplicate should be removed when a direct provider owns the same recipe.");
            helper.assertTrue(importFirst.warnings().stream()
                            .anyMatch(warning -> warning.contains("Terminal-import duplicate replaced")),
                    "Replacement should be visible in Index recipe diagnostics.");

            IndexRecipeSnapshot directFirst = IndexService.INSTANCE.recipeSnapshotForTests(null,
                    List.of(directProvider, IndexTerminalImportRecipeProvider.INSTANCE));
            helper.assertTrue(directFirst.recipe(directRecipeId).isPresent(),
                    "Direct Index recipe should remain when it is seen before the Terminal import.");
            helper.assertFalse(directFirst.recipe(importedRecipeId).isPresent(),
                    "Terminal import should be skipped when the direct recipe already exists.");
            helper.assertTrue(directFirst.recipes().size() == 1,
                    "Semantic duplicate filtering should leave one visible recipe card.");
        });
        helper.succeed();
    }

    private static void recipeLikeProviderCards(GameTestHelper helper) {
        List<IIndexRecipeProvider> providers = List.of(
                fixtureProvider("echologisticsnetwork", "recipe/logistics_loadouts", "Logistics Loadouts",
                        "recipe/loadout/test", "Loadout Delivery", "Delivery/restock request"),
                fixtureProvider("echoconvoyprotocol", "recipe/convoy_routes", "Convoy Routes",
                        "recipe/route/test", "Convoy Route", "Route readiness"),
                fixtureProvider("echoagriculturereclamation", "recipe/agriculture_reclamation", "Agriculture Reclamation",
                        "recipe/crop/test", "Hydroponic Growth", "Accelerated crop growth"),
                fixtureProvider("echomultiblockcore", "recipe/multiblock_automation", "Multiblock Automation",
                        "recipe/automation/test", "Workcell Automation", "Capability output"),
                fixtureProvider("echomissioncore", "recipe/mission_rewards", "Mission Rewards",
                        "recipe/mission/test", "Mission Reward", "Unlock progress"),
                fixtureProvider("echoworldcore", "recipe/world_sources", "World Sources",
                        "recipe/world_source/test", "World Source", "Hazard/source discovery"));

        IndexRecipeSnapshot snapshot = IndexService.INSTANCE.recipeSnapshotForTests(null, providers);
        helper.assertTrue(snapshot.providerCount() == providers.size(),
                "Each recipe-like Echo fixture provider should be represented in provider stats.");
        helper.assertTrue(snapshot.recipes().size() == providers.size(),
                "Each recipe-like Echo fixture provider should publish one recipe card.");
        for (IIndexRecipeProvider provider : providers) {
            Identifier providerId = provider.id();
            helper.assertTrue(snapshot.recipesForProvider(providerId).size() == 1,
                    "Provider should own exactly one recipe card: " + providerId);
        }
        helper.assertTrue(snapshot.usesFor(Items.IRON_INGOT).size() == providers.size(),
                "Item-backed inputs should index use lookups across recipe-like cards.");
        helper.assertTrue(snapshot.recipes().stream()
                        .allMatch(recipe -> IndexRecipeSnapshot.hasRole(recipe, IndexSlotRole.OUTPUT)),
                "Recipe-like cards with text-only outputs should still satisfy output role coverage.");
        helper.assertTrue(snapshot.recipes().stream().anyMatch(ModGameTests::hasTextOnlyOutput),
                "Recipe-like cards should render non-item outputs as labeled text slots.");
        helper.succeed();
    }

    private static void recipeQueryResultLimiting(GameTestHelper helper) {
        List<IndexRecipeView> uses = new ArrayList<>();
        for (int i = 0; i < 520; i++) {
            uses.add(queryFixtureRecipe(i));
        }
        CompoundTag tag = IndexRecipeSnapshotCodec.encodeQueryResult(
                id("query/test_item"),
                IndexRecipeSnapshot.empty(),
                List.of(),
                uses,
                List.of(),
                "");
        int totalUses = tag.getIntOr("use_count", 0);
        int visibleUses = tag.getIntOr("visible_use_count", 0);
        helper.assertTrue(totalUses == uses.size(), "Query result should keep the full use count.");
        helper.assertTrue(visibleUses > 0 && visibleUses < totalUses,
                "Query result should cap the visible use payload.");
        helper.assertTrue(IndexRecipeSnapshotCodec.decodeRecipeViews(tag.getListOrEmpty("uses")).size() == visibleUses,
                "Visible use count should match the encoded payload.");
        helper.assertTrue(tag.getStringOr("query_warning", "").contains("Showing first"),
                "Limited query result should include a user-visible warning.");
        helper.succeed();
    }

    private static void vanillaRecipeGridMetadata(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        IndexRecipeSnapshot snapshot = IndexService.INSTANCE.recipeSnapshotForTests(
                player, List.of(VanillaIndexRecipeProvider.INSTANCE));
        Identifier recipeId = Identifier.withDefaultNamespace("diamond_pickaxe");
        IndexRecipeDisplayMetadata metadata = snapshot.metadata(recipeId)
                .orElseThrow(() -> helper.assertionException("Diamond pickaxe display metadata should be present."));
        assertDiamondPickaxeGrid(helper, metadata, "provider metadata");

        IndexRecipeSnapshot decoded = IndexRecipeSnapshotCodec.decode(IndexRecipeSnapshotCodec.encode(snapshot));
        IndexRecipeDisplayMetadata decodedMetadata = decoded.metadata(recipeId)
                .orElseThrow(() -> helper.assertionException("Diamond pickaxe display metadata should survive codec."));
        assertDiamondPickaxeGrid(helper, decodedMetadata, "decoded metadata");

        IndexRecipeDisplayMetadata blank = new IndexRecipeDisplayMetadata(
                id("test/blank_grid"),
                IndexRecipeLayoutType.CRAFTING_SHAPED,
                3,
                3,
                List.of(),
                new ItemStack(Items.CRAFTING_TABLE),
                new ItemStack(Items.DIAMOND_PICKAXE));
        IndexRecipeDisplayMetadata fallback = blank.withFallbackInputCellsFromSlots(List.of(
                IndexRecipeSlot.input(new ItemStack(Items.DIAMOND)),
                IndexRecipeSlot.input(new ItemStack(Items.STICK))));
        helper.assertTrue(fallback.hasRenderableInputCells(),
                "Slot-derived fallback metadata should prevent a blank vanilla grid.");
        assertCell(helper, fallback, 0, Items.DIAMOND, "fallback cell 0 should render the first input.");
        assertCell(helper, fallback, 1, Items.STICK, "fallback cell 1 should render the second input.");
        helper.succeed();
    }

    private static void assertMission(
            GameTestHelper helper,
            InMemoryMissionRegistry registry,
            String missionPath,
            String objectiveKey,
            MissionObjectiveType type) {
        Identifier missionId = id(missionPath);
        MissionDefinition mission = registry.missionDefinition(missionId)
                .orElseThrow(() -> new AssertionError("Missing MissionCore mission: " + missionId));
        helper.assertTrue(mission.kind() == MissionKind.SIDE_OP, "Index MissionCore missions should be side ops.");
        helper.assertTrue(!mission.rewards().isEmpty(), "Index MissionCore mission should have a claimable reward: " + missionId);
        helper.assertTrue(mission.objectives().size() == 1, "Index MissionCore mission should have one direct objective: " + missionId);
        helper.assertTrue(mission.objectives().getFirst().type() == type, "Index objective type should stay stable: " + missionId);
        String target = mission.objectives().getFirst().criteria().get("target");
        helper.assertTrue(MissionHookTargets.objectiveTarget(EchoIndex.MODID, missionId, objectiveKey).toString().equals(target),
                "Index MissionCore objective target should use MissionHookTargets: " + missionId);
    }

    private static void assertDiamondPickaxeGrid(GameTestHelper helper, IndexRecipeDisplayMetadata metadata, String source) {
        helper.assertTrue(metadata.type() == IndexRecipeLayoutType.CRAFTING_SHAPED,
                "Diamond pickaxe " + source + " should use shaped crafting layout.");
        helper.assertTrue(metadata.width() == 3 && metadata.height() == 3,
                "Diamond pickaxe " + source + " should keep a 3x3 grid.");
        helper.assertTrue(metadata.hasRenderableInputCells(),
                "Diamond pickaxe " + source + " should have renderable ingredient cells.");
        assertCell(helper, metadata, 0, Items.DIAMOND, "Diamond pickaxe top-left cell should contain diamond.");
        assertCell(helper, metadata, 1, Items.DIAMOND, "Diamond pickaxe top-middle cell should contain diamond.");
        assertCell(helper, metadata, 2, Items.DIAMOND, "Diamond pickaxe top-right cell should contain diamond.");
        assertEmptyCell(helper, metadata, 3, "Diamond pickaxe middle-left cell should remain empty.");
        assertCell(helper, metadata, 4, Items.STICK, "Diamond pickaxe center cell should contain stick.");
        assertEmptyCell(helper, metadata, 5, "Diamond pickaxe middle-right cell should remain empty.");
        assertEmptyCell(helper, metadata, 6, "Diamond pickaxe bottom-left cell should remain empty.");
        assertCell(helper, metadata, 7, Items.STICK, "Diamond pickaxe bottom-middle cell should contain stick.");
        assertEmptyCell(helper, metadata, 8, "Diamond pickaxe bottom-right cell should remain empty.");
    }

    private static void assertCell(GameTestHelper helper, IndexRecipeDisplayMetadata metadata, int index,
            Item item, String message) {
        helper.assertTrue(index >= 0 && index < metadata.cells().size(), message + " Cell index exists.");
        helper.assertTrue(metadata.cells().get(index).stream().anyMatch(stack -> stack.is(item)), message);
    }

    private static void assertEmptyCell(GameTestHelper helper, IndexRecipeDisplayMetadata metadata, int index,
            String message) {
        helper.assertTrue(index >= 0 && index < metadata.cells().size(), message + " Cell index exists.");
        helper.assertTrue(metadata.cells().get(index).stream().noneMatch(stack -> stack != null && !stack.isEmpty()), message);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoIndex.MODID, path);
    }

    private static Identifier namespacedId(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    private static List<IndexRecipeSlot> duplicateSlots() {
        return List.of(
                IndexRecipeSlot.input(new ItemStack(Items.IRON_INGOT)),
                IndexRecipeSlot.machine(new ItemStack(Items.CRAFTING_TABLE)),
                IndexRecipeSlot.output(new ItemStack(Items.STICK)));
    }

    private static IIndexRecipeProvider fixtureProvider(
            String namespace,
            String categoryPath,
            String categoryTitle,
            String recipePath,
            String recipeTitle,
            String textOutput) {
        Identifier categoryId = namespacedId(namespace, categoryPath);
        ItemStack machine = new ItemStack(Items.CRAFTING_TABLE);
        return new DummyIndexRecipeProvider(
                namespacedId(namespace, "provider/index_recipes"),
                new IndexRecipeCategory(categoryId, categoryTitle, machine, 0xFF66E8FF, 500),
                new IndexRecipeView(
                        namespacedId(namespace, recipePath),
                        categoryId,
                        recipeTitle,
                        machine,
                        List.of(
                                IndexRecipeSlot.input(new ItemStack(Items.IRON_INGOT)),
                                IndexRecipeSlot.machine(machine),
                                new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), textOutput)),
                        List.of("Fixture coverage for " + categoryTitle),
                        0,
                        false,
                        namespace));
    }

    private static IndexRecipeView queryFixtureRecipe(int index) {
        return new IndexRecipeView(
                id("query/recipe_" + index),
                id("query/category"),
                "Query Fixture " + index,
                new ItemStack(Items.CRAFTING_TABLE),
                List.of(
                        IndexRecipeSlot.input(new ItemStack(Items.IRON_INGOT)),
                        IndexRecipeSlot.machine(new ItemStack(Items.CRAFTING_TABLE)),
                        IndexRecipeSlot.output(new ItemStack(Items.STICK))),
                List.of("Fixture recipe for packet-size limiting."),
                0,
                false,
                EchoIndex.MODID);
    }

    private static boolean hasTextOnlyOutput(IndexRecipeView recipe) {
        return recipe.slots().stream()
                .anyMatch(slot -> slot.role() == IndexSlotRole.OUTPUT
                        && slot.stacks().isEmpty()
                        && !slot.label().isBlank());
    }

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoIndex.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    private record DummyIndexRecipeProvider(
            Identifier providerId,
            IndexRecipeCategory category,
            IndexRecipeView recipe) implements IIndexRecipeProvider {
        @Override
        public Identifier id() {
            return providerId;
        }

        @Override
        public List<IndexRecipeCategory> recipeCategories(Player player) {
            return List.of(category);
        }

        @Override
        public List<IndexRecipeView> recipes(Player player) {
            return List.of(recipe);
        }
    }

    private record DuplicateTerminalRecipeProvider(Identifier providerId) implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return providerId;
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            return List.of(new TerminalRecipeCategory(ModGameTests.id("duplicate/category"),
                    "Duplicate Recipes", new ItemStack(Items.CRAFTING_TABLE), 0xFF66E8FF, 1));
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            return List.of(new TerminalRecipeEntry(
                    ModGameTests.id("duplicate/recipe"),
                    ModGameTests.id("duplicate/category"),
                    "Imported Duplicate",
                    new ItemStack(Items.CRAFTING_TABLE),
                    List.of(
                            TerminalRecipeSlot.input(Items.IRON_INGOT),
                            TerminalRecipeSlot.machine(new ItemStack(Items.CRAFTING_TABLE)),
                            TerminalRecipeSlot.output(Items.STICK)),
                    List.of(),
                    20,
                    false));
        }
    }

    private record DummyTerminalRecipeProvider(Identifier providerId) implements TerminalRecipeProvider {
        @Override
        public Identifier id() {
            return providerId;
        }

        @Override
        public List<TerminalRecipeCategory> categories(Player player) {
            return List.of(new TerminalRecipeCategory(ModGameTests.id("dummy_category"), "Dummy", new ItemStack(Items.CRAFTING_TABLE), 0xFF66E8FF, 1));
        }

        @Override
        public List<TerminalRecipeEntry> recipes(Player player) {
            return List.of(new TerminalRecipeEntry(
                    ModGameTests.id("dummy_recipe"),
                    ModGameTests.id("dummy_category"),
                    "Dummy",
                    new ItemStack(Items.CRAFTING_TABLE),
                    List.of(TerminalRecipeSlot.output(Items.STICK)),
                    List.of(),
                    0,
                    false));
        }
    }
}
