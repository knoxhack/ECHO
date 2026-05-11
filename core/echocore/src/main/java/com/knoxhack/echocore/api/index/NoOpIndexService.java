package com.knoxhack.echocore.api.index;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class NoOpIndexService implements IIndexService, IIndexRegistry, IIndexRecipeService,
        IIndexSearchService, IIndexDiscoveryService, IIndexOverlayService, IIndexTerminalBridge {
    public static final NoOpIndexService INSTANCE = new NoOpIndexService();

    private NoOpIndexService() {
    }

    @Override
    public boolean available() {
        return false;
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
        return false;
    }

    @Override
    public boolean registerEntry(IndexEntry entry) {
        return false;
    }

    @Override
    public List<IndexCategory> categories(Player player) {
        return List.of();
    }

    @Override
    public List<IndexEntry> entries(Player player) {
        return List.of();
    }

    @Override
    public Optional<IndexEntry> entry(Player player, Identifier id) {
        return Optional.empty();
    }

    @Override
    public boolean registerProvider(IIndexRecipeProvider provider) {
        return false;
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        return List.of();
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        return List.of();
    }

    @Override
    public List<IndexRecipeView> recipesFor(Player player, Item item) {
        return List.of();
    }

    @Override
    public List<IndexRecipeView> usesFor(Player player, Item item) {
        return List.of();
    }

    @Override
    public Optional<IndexRecipeView> recipe(Player player, Identifier id) {
        return Optional.empty();
    }

    @Override
    public int providerCount() {
        return 0;
    }

    @Override
    public List<IndexSearchResult> search(Player player, String query, int maxResults) {
        return List.of();
    }

    @Override
    public void invalidate() {
    }

    @Override
    public IndexEntryState state(Player player, Identifier entryId) {
        return IndexEntryState.VISIBLE;
    }

    @Override
    public boolean discover(ServerPlayer player, Identifier entryId) {
        return false;
    }

    @Override
    public boolean markRead(ServerPlayer player, Identifier entryId) {
        return false;
    }

    @Override
    public boolean setBookmarked(ServerPlayer player, Identifier entryId, boolean bookmarked) {
        return false;
    }

    @Override
    public boolean isBookmarked(Player player, Identifier entryId) {
        return false;
    }

    @Override
    public Set<Identifier> bookmarks(Player player) {
        return Set.of();
    }
}
