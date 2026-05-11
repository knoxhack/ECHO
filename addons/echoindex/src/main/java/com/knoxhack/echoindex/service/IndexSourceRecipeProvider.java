package com.knoxhack.echoindex.service;

import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echoindex.EchoIndex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum IndexSourceRecipeProvider implements IIndexRecipeProvider {
    INSTANCE;

    public static final Identifier CATEGORY = EchoIndex.id("recipe/sources");
    private static final Identifier PROVIDER_ID = EchoIndex.id("provider/sources");
    private volatile List<SourceFact> sources = List.of();

    @Override
    public Identifier id() {
        return PROVIDER_ID;
    }

    @Override
    public List<IndexRecipeCategory> recipeCategories(Player player) {
        return List.of(new IndexRecipeCategory(
                CATEGORY,
                "Sources",
                new ItemStack(Items.SPYGLASS),
                0xFFFFD166,
                90));
    }

    @Override
    public List<IndexRecipeView> recipes(Player player) {
        List<IndexRecipeView> views = new ArrayList<>();
        for (SourceFact source : sources) {
            BuiltInRegistries.ITEM.getOptional(source.itemId()).ifPresent(item -> {
                ItemStack output = new ItemStack(item);
                ItemStack icon = source.icon().isEmpty() ? output : source.icon();
                List<IndexRecipeSlot> slots = new ArrayList<>();
                slots.add(IndexRecipeSlot.machine(icon));
                slots.add(IndexRecipeSlot.output(output));
                views.add(new IndexRecipeView(
                        viewId(source),
                        CATEGORY,
                        source.title(),
                        icon,
                        slots,
                        source.notes(),
                        0,
                        false,
                        source.sourceModId()));
            });
        }
        views.sort(Comparator.comparing(view -> view.id().toString()));
        return views;
    }

    public void replaceSources(Collection<SourceFact> newSources) {
        sources = newSources == null ? List.of() : newSources.stream()
                .filter(source -> source != null && source.itemId() != null)
                .sorted(Comparator.comparing((SourceFact source) -> source.itemId().toString())
                        .thenComparing(source -> source.sourceId().toString())
                        .thenComparing(SourceFact::title))
                .toList();
    }

    public int sourceFactCount() {
        return sources.size();
    }

    public int sourceRecipeCount(Player player) {
        return recipes(player).size();
    }

    private static Identifier viewId(SourceFact source) {
        return EchoIndex.id("source/" + sanitize(source.itemId().getNamespace()) + "/"
                + sanitize(source.itemId().getPath()) + "/"
                + sanitize(source.sourceId().getNamespace()) + "/"
                + sanitize(source.sourceId().getPath()) + "/"
                + sanitize(source.title().toLowerCase(Locale.ROOT)));
    }

    private static String sanitize(String value) {
        StringBuilder builder = new StringBuilder();
        String safe = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (int i = 0; i < safe.length(); i++) {
            char c = safe.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.isEmpty() ? "unknown" : builder.toString();
    }

    public record SourceFact(
            Identifier itemId,
            Identifier sourceId,
            String title,
            List<String> notes,
            ItemStack icon,
            String sourceModId) {
        public SourceFact {
            title = title == null || title.isBlank() ? "Source" : title.strip();
            notes = notes == null ? List.of() : notes.stream()
                    .filter(note -> note != null && !note.isBlank())
                    .map(String::strip)
                    .toList();
            icon = icon == null ? ItemStack.EMPTY : icon.copy();
            sourceModId = sourceModId == null || sourceModId.isBlank() ? itemId.getNamespace() : sourceModId.strip();
        }

        public static SourceFact of(Identifier itemId, Identifier sourceId, String title, List<String> notes,
                Item icon, String sourceModId) {
            return new SourceFact(itemId, sourceId, title, notes, new ItemStack(icon), sourceModId);
        }
    }
}
