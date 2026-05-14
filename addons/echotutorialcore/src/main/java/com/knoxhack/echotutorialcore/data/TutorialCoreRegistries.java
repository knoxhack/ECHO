package com.knoxhack.echotutorialcore.data;

import com.knoxhack.echotutorialcore.api.card.TutorialCard;
import com.knoxhack.echotutorialcore.api.hint.TutorialHint;
import com.knoxhack.echotutorialcore.api.tooltip.TutorialTooltip;
import com.knoxhack.echotutorialcore.api.trigger.TutorialFlow;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public final class TutorialCoreRegistries {
    private static final Map<Identifier, TutorialCard> CARDS = new LinkedHashMap<>();
    private static final Map<Identifier, TutorialHint> HINTS = new LinkedHashMap<>();
    private static final Map<Identifier, TutorialFlow> FLOWS = new LinkedHashMap<>();
    private static final Map<Identifier, TutorialTooltip> TOOLTIPS = new LinkedHashMap<>();

    private TutorialCoreRegistries() {}

    public static void registerCard(TutorialCard card) {
        if (card != null && card.id() != null) {
            CARDS.put(card.id(), card);
        }
    }

    public static void registerHint(TutorialHint hint) {
        if (hint != null && hint.id() != null) {
            HINTS.put(hint.id(), hint);
        }
    }

    public static void registerFlow(TutorialFlow flow) {
        if (flow != null && flow.id() != null) {
            FLOWS.put(flow.id(), flow);
        }
    }

    public static void registerTooltip(TutorialTooltip tooltip) {
        if (tooltip != null && tooltip.targetItem() != null) {
            TOOLTIPS.put(tooltip.targetItem(), tooltip);
        }
    }

    public static Optional<TutorialCard> getCard(Identifier id) {
        return Optional.ofNullable(CARDS.get(id));
    }

    public static Optional<TutorialHint> getHint(Identifier id) {
        return Optional.ofNullable(HINTS.get(id));
    }

    public static Optional<TutorialFlow> getFlow(Identifier id) {
        return Optional.ofNullable(FLOWS.get(id));
    }

    public static List<TutorialCard> getCardsByCategory(com.knoxhack.echotutorialcore.api.TutorialCategory category) {
        return CARDS.values().stream()
                .filter(c -> c.category() == category)
                .sorted((a, b) -> Integer.compare(b.priority(), a.priority()))
                .collect(Collectors.toList());
    }

    public static List<TutorialCard> allCards() {
        return List.copyOf(CARDS.values());
    }

    public static List<TutorialHint> allHints() {
        return List.copyOf(HINTS.values());
    }

    public static List<TutorialFlow> allFlows() {
        return List.copyOf(FLOWS.values());
    }

    public static Optional<TutorialTooltip> getTooltip(Identifier itemId) {
        return Optional.ofNullable(TOOLTIPS.get(itemId));
    }

    public static List<TutorialTooltip> allTooltips() {
        return List.copyOf(TOOLTIPS.values());
    }

    public static void clearAll() {
        CARDS.clear();
        HINTS.clear();
        FLOWS.clear();
        TOOLTIPS.clear();
    }

    public static int cardCount() {
        return CARDS.size();
    }

    public static int hintCount() {
        return HINTS.size();
    }

    public static int flowCount() {
        return FLOWS.size();
    }

    public static int tooltipCount() {
        return TOOLTIPS.size();
    }
}
