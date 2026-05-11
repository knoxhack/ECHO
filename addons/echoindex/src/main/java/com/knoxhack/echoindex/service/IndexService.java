package com.knoxhack.echoindex.service;

import com.knoxhack.echocore.api.index.IIndexDiscoveryService;
import com.knoxhack.echocore.api.index.IIndexOverlayService;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IIndexRecipeService;
import com.knoxhack.echocore.api.index.IIndexRegistry;
import com.knoxhack.echocore.api.index.IIndexSearchService;
import com.knoxhack.echocore.api.index.IIndexService;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.IndexEntryState;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSearchResult;
import com.knoxhack.echoindex.Config;
import com.knoxhack.echoindex.EchoIndex;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IndexService implements IIndexService, IIndexRegistry, IIndexRecipeService,
        IIndexSearchService, IIndexDiscoveryService, IIndexOverlayService {
    public static final IndexService INSTANCE = new IndexService();

    private final Map<Identifier, IndexCategory> categories = new LinkedHashMap<>();
    private final Map<Identifier, IndexEntry> entries = new LinkedHashMap<>();
    private final Map<Identifier, IndexCategory> dataCategories = new LinkedHashMap<>();
    private final Map<Identifier, IndexEntry> dataEntries = new LinkedHashMap<>();
    private final List<IIndexRecipeProvider> recipeProviders = new CopyOnWriteArrayList<>();
    private volatile List<ItemStack> cachedItems = List.of();

    private IndexService() {
    }

    @Override
    public IIndexRegistry registry() {
        return this;
    }

    @Override
    public IIndexRecipeService recipes() {
        return this;
    }

    @Override
    public IIndexSearchService search() {
        return this;
    }

    @Override
    public IIndexDiscoveryService discovery() {
        return this;
    }

    @Override
    public IIndexOverlayService overlay() {
        return this;
    }

    @Override
    public boolean registerCategory(IndexCategory category) {
        if (category == null || category.id() == null) {
            return false;
        }
        synchronized (categories) {
            return categories.putIfAbsent(category.id(), category) == null;
        }
    }

    @Override
    public boolean registerEntry(IndexEntry entry) {
        if (entry == null || entry.id() == null) {
            return false;
        }
        synchronized (entries) {
            return entries.putIfAbsent(entry.id(), entry) == null;
        }
    }

    public void replaceDataDriven(Map<Identifier, IndexCategory> newCategories, Map<Identifier, IndexEntry> newEntries) {
        synchronized (categories) {
            for (Identifier id : dataCategories.keySet()) {
                categories.remove(id);
            }
            dataCategories.clear();
            if (newCategories != null) {
                dataCategories.putAll(newCategories);
                newCategories.values().forEach(category -> categories.put(category.id(), category));
            }
        }
        synchronized (entries) {
            for (Identifier id : dataEntries.keySet()) {
                entries.remove(id);
            }
            dataEntries.clear();
            if (newEntries != null) {
                dataEntries.putAll(newEntries);
                newEntries.values().forEach(entry -> entries.put(entry.id(), entry));
            }
        }
        refresh();
    }

    @Override
    public List<IndexCategory> categories(Player player) {
        synchronized (categories) {
            return categories.values().stream()
                    .sorted(Comparator.comparingInt(IndexCategory::sortOrder)
                            .thenComparing(category -> category.id().toString()))
                    .toList();
        }
    }

    @Override
    public List<IndexEntry> entries(Player player) {
        synchronized (entries) {
            return entries.values().stream()
                    .filter(entry -> visible(player, entry))
                    .sorted(Comparator.comparingInt(IndexEntry::sortOrder)
                            .thenComparing(entry -> entry.id().toString()))
                    .toList();
        }
    }

    @Override
    public Optional<IndexEntry> entry(Player player, Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        synchronized (entries) {
            return Optional.ofNullable(entries.get(id));
        }
    }

    @Override
    public boolean registerProvider(IIndexRecipeProvider provider) {
        if (provider == null || provider.id() == null) {
            return false;
        }
        for (IIndexRecipeProvider existing : recipeProviders) {
            if (provider.id().equals(existing.id())) {
                return existing == provider;
            }
        }
        recipeProviders.add(provider);
        return true;
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        Map<Identifier, IndexRecipeCategory> merged = new LinkedHashMap<>();
        for (IIndexRecipeProvider provider : recipeProviders) {
            try {
                for (IndexRecipeCategory category : provider.recipeCategories(player)) {
                    if (category != null) {
                        merged.putIfAbsent(category.id(), category);
                    }
                }
            } catch (RuntimeException exception) {
                EchoIndex.LOGGER.warn("Index recipe provider {} failed while listing categories.", provider.id(), exception);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparingInt(IndexRecipeCategory::order)
                        .thenComparing(category -> category.id().toString()))
                .toList();
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        List<IndexRecipeView> all = new ArrayList<>();
        Set<Identifier> seen = new LinkedHashSet<>();
        for (IIndexRecipeProvider provider : recipeProviders) {
            try {
                for (IndexRecipeView recipe : provider.recipes(player)) {
                    if (recipe != null && seen.add(recipe.id())) {
                        all.add(recipe);
                    }
                }
            } catch (RuntimeException exception) {
                EchoIndex.LOGGER.warn("Index recipe provider {} failed while listing recipes.", provider.id(), exception);
            }
        }
        all.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        return all;
    }

    @Override
    public List<IndexRecipeView> recipesFor(Player player, Item item) {
        if (item == null) {
            return List.of();
        }
        return recipes(player).stream().filter(recipe -> recipe.outputs(item)).toList();
    }

    @Override
    public List<IndexRecipeView> usesFor(Player player, Item item) {
        if (item == null) {
            return List.of();
        }
        return recipes(player).stream().filter(recipe -> recipe.uses(item)).toList();
    }

    @Override
    public Optional<IndexRecipeView> recipe(Player player, Identifier id) {
        return recipes(player).stream().filter(recipe -> recipe.id().equals(id)).findFirst();
    }

    @Override
    public int providerCount() {
        return recipeProviders.size();
    }

    public List<ItemStack> itemCatalog(Player player) {
        List<ItemStack> cached = cachedItems;
        if (Config.SEARCH_CACHE_ENABLED.get() && !cached.isEmpty()) {
            return cached;
        }
        List<ItemStack> built = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .map(item -> new ItemStack(item))
                .sorted(Comparator.<ItemStack, String>comparing(stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT))
                        .thenComparing(stack -> itemId(stack.getItem()).toString()))
                .toList();
        cachedItems = built;
        return built;
    }

    public List<ItemStack> filteredItems(Player player, String query, int maxResults) {
        String normalized = normalize(query);
        int limit = Math.max(1, Math.min(maxResults, Config.SEARCH_MAX_RESULTS.get()));
        return itemCatalog(player).stream()
                .filter(stack -> itemMatches(player, stack, normalized))
                .limit(limit)
                .toList();
    }

    @Override
    public List<IndexSearchResult> search(Player player, String query, int maxResults) {
        String normalized = normalize(query);
        int limit = Math.max(1, Math.min(maxResults, Config.SEARCH_MAX_RESULTS.get()));
        List<IndexSearchResult> results = new ArrayList<>();
        for (ItemStack stack : itemCatalog(player)) {
            if (itemMatches(player, stack, normalized)) {
                Identifier id = itemId(stack.getItem());
                results.add(new IndexSearchResult(id, "item", stack.getHoverName().getString(), stack, score(id, normalized)));
            }
            if (results.size() >= limit) {
                return results;
            }
        }
        for (IndexEntry entry : entries(player)) {
            if (entryMatches(player, entry, normalized)) {
                results.add(new IndexSearchResult(entry.id(), "entry", entry.titleKey(), entry.icon(), score(entry.id(), normalized)));
            }
            if (results.size() >= limit) {
                break;
            }
        }
        results.sort(Comparator.comparingInt(IndexSearchResult::score).reversed()
                .thenComparing(result -> result.title().toLowerCase(Locale.ROOT)));
        return results;
    }

    @Override
    public void invalidate() {
        cachedItems = List.of();
    }

    @Override
    public IndexEntryState state(Player player, Identifier entryId) {
        return IndexDiscoveryStore.INSTANCE.state(player, entryId);
    }

    @Override
    public boolean discover(ServerPlayer player, Identifier entryId) {
        return IndexDiscoveryStore.INSTANCE.discover(player, entryId);
    }

    @Override
    public boolean markRead(ServerPlayer player, Identifier entryId) {
        return IndexDiscoveryStore.INSTANCE.markRead(player, entryId);
    }

    @Override
    public boolean setBookmarked(ServerPlayer player, Identifier entryId, boolean bookmarked) {
        return IndexDiscoveryStore.INSTANCE.setBookmarked(player, entryId, bookmarked);
    }

    @Override
    public boolean isBookmarked(Player player, Identifier entryId) {
        if (player == null) {
            return ClientIndexState.isBookmarked(entryId);
        }
        return IndexDiscoveryStore.INSTANCE.isBookmarked(player, entryId);
    }

    @Override
    public Set<Identifier> bookmarks(Player player) {
        return player == null ? ClientIndexState.bookmarks() : IndexDiscoveryStore.INSTANCE.bookmarks(player);
    }

    @Override
    public boolean overlayEnabled(Player player) {
        return Config.OVERLAY_ENABLED.get();
    }

    @Override
    public boolean excludedScreen(String screenClassName) {
        if (screenClassName == null) {
            return false;
        }
        return screenClassName.contains("EchoTerminalScreen") || screenClassName.contains("IndexRecipeScreen");
    }

    private boolean visible(Player player, IndexEntry entry) {
        if (!Config.DISCOVERY_ENABLED.get()) {
            return true;
        }
        IndexEntryState state = IndexDiscoveryStore.INSTANCE.state(player, entry.id());
        return state != IndexEntryState.HIDDEN
                && (!Config.DISCOVERY_HIDE_LOCKED.get() || state != IndexEntryState.LOCKED);
    }

    private boolean itemMatches(Player player, ItemStack stack, String query) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (query.isBlank()) {
            return true;
        }
        Identifier id = itemId(stack.getItem());
        for (String token : query.split("\\s+")) {
            if (!itemMatchesToken(player, stack, id, token)) {
                return false;
            }
        }
        return true;
    }

    private boolean itemMatchesToken(Player player, ItemStack stack, Identifier id, String token) {
        if (token.isBlank()) {
            return true;
        }
        if (token.startsWith("@")) {
            return id.getNamespace().contains(token.substring(1));
        }
        if (token.startsWith("#")) {
            if (!Config.SEARCH_TAG_SEARCH.get()) {
                return false;
            }
            String needle = token.substring(1);
            return stack.getItem().builtInRegistryHolder().tags()
                    .map(TagKey::location)
                    .map(Identifier::toString)
                    .anyMatch(tag -> tag.contains(needle));
        }
        if (token.startsWith("$")) {
            return categoryToken(stack, id, token.substring(1));
        }
        if ("bookmarked".equals(token) || "favorite".equals(token)) {
            return bookmarks(player).contains(id);
        }
        String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        return name.contains(token)
                || (Config.SEARCH_REGISTRY_SEARCH.get() && id.toString().contains(token))
                || (Config.SEARCH_TOOLTIP_SEARCH.get()
                        && stack.getItem().getDescriptionId().toLowerCase(Locale.ROOT).contains(token));
    }

    private static boolean categoryToken(ItemStack stack, Identifier id, String category) {
        String token = category == null ? "" : category.toLowerCase(Locale.ROOT);
        Item item = stack.getItem();
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        String namespace = id == null ? "" : id.getNamespace().toLowerCase(Locale.ROOT);
        String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        return switch (token) {
            case "block", "blocks" -> item instanceof BlockItem;
            case "machine", "machines" -> item instanceof BlockItem && hasAny(path + " " + name,
                    "machine", "station", "bench", "workbench", "forge", "fabricator", "generator",
                    "press", "grinder", "compressor", "refinery", "smelter", "reclaimer", "scanner",
                    "terminal", "console", "dock", "beacon", "scrubber", "purifier", "hopper",
                    "condenser", "charger", "array", "core");
            case "tool", "tools" -> stack.isDamageableItem();
            case "combat", "weapon", "weapons" -> hasAny(path + " " + name,
                    "sword", "bow", "armor", "shield", "rifle", "gun", "blade", "hammer", "staff",
                    "dagger", "chakram", "gauntlet", "launcher", "lance", "knife");
            case "echo" -> namespace.startsWith("echo") || name.contains("echo") || path.contains("echo");
            default -> false;
        };
    }

    private static boolean hasAny(String value, String... needles) {
        String haystack = value == null ? "" : value;
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private boolean entryMatches(Player player, IndexEntry entry, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = (entry.id() + " " + entry.categoryId() + " " + entry.titleKey() + " "
                + entry.subtitleKey() + " " + entry.summaryKey() + " " + String.join(" ", entry.tags()))
                .toLowerCase(Locale.ROOT);
        for (String token : query.split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            if (token.startsWith("@")) {
                String mod = token.substring(1);
                if (!entry.id().getNamespace().contains(mod) && !entry.sourceModId().toLowerCase(Locale.ROOT).contains(mod)) {
                    return false;
                }
                continue;
            }
            if (token.startsWith("$")) {
                String category = token.substring(1);
                String categoryId = entry.categoryId().toString().toLowerCase(Locale.ROOT);
                boolean matched = categoryId.contains(category)
                        || ("machines".equals(category) && entry.tags().stream().anyMatch(value -> value.equalsIgnoreCase("machine")))
                        || ("machine".equals(category) && entry.tags().stream().anyMatch(value -> value.equalsIgnoreCase("machine")))
                        || ("echo".equals(category) && entry.sourceModId().toLowerCase(Locale.ROOT).startsWith("echo"));
                if (!matched) {
                    return false;
                }
                continue;
            }
            if (token.startsWith("#")) {
                String tag = token.substring(1);
                if (entry.tags().stream().noneMatch(value -> value.toLowerCase(Locale.ROOT).contains(tag))) {
                    return false;
                }
                continue;
            }
            if ("bookmarked".equals(token) || "favorite".equals(token)) {
                if (!bookmarks(player).contains(entry.id())) {
                    return false;
                }
                continue;
            }
            if ("discovered".equals(token)) {
                IndexEntryState state = state(player, entry.id());
                if (state != IndexEntryState.DISCOVERED
                        && state != IndexEntryState.COMPLETED
                        && state != IndexEntryState.ARCHIVED) {
                    return false;
                }
                continue;
            }
            if ("locked".equals(token)) {
                if (state(player, entry.id()) != IndexEntryState.LOCKED) {
                    return false;
                }
                continue;
            }
            if (!haystack.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static int score(Identifier id, String query) {
        if (query.isBlank()) {
            return 1;
        }
        String value = id.toString();
        if (value.equals(query)) {
            return 1000;
        }
        if (value.endsWith(query)) {
            return 500;
        }
        return value.contains(query) ? 100 : 10;
    }

    private static String normalize(String query) {
        return query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    }

    public static Identifier itemId(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? Identifier.fromNamespaceAndPath("minecraft", "air") : id;
    }
}
