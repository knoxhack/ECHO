package com.knoxhack.echocore.api;

import com.knoxhack.echocore.EchoCore;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class EchoDiscoveryRegistry {
    private static final List<EchoDiscoveryProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final Set<Identifier> WARNED_DUPLICATE_ENTRY_IDS = ConcurrentHashMap.newKeySet();

    private EchoDiscoveryRegistry() {
    }

    public static void register(EchoDiscoveryProvider provider) {
        if (provider != null && !PROVIDERS.contains(provider)) {
            PROVIDERS.add(provider);
        }
    }

    public static List<EchoDiscoveryProvider> providers() {
        return List.copyOf(PROVIDERS);
    }

    public static List<EchoDiscoveryEntry> entries(Player player) {
        Map<Identifier, EchoDiscoveryEntry> entries = new LinkedHashMap<>();
        for (EchoDiscoveryProvider provider : PROVIDERS) {
            List<EchoDiscoveryEntry> provided;
            try {
                provided = provider.entries(player);
            } catch (RuntimeException exception) {
                EchoCore.LOGGER.warn("Discovery provider {} failed while listing entries.",
                        provider.getClass().getName(), exception);
                continue;
            }
            for (EchoDiscoveryEntry entry : provided == null ? List.<EchoDiscoveryEntry>of() : provided) {
                if (entry == null) {
                    EchoCore.LOGGER.warn("Discovery provider {} returned a null entry.",
                            provider.getClass().getName());
                    continue;
                }
                EchoDiscoveryEntry existing = entries.putIfAbsent(entry.id(), entry);
                if (existing != null && !existing.equals(entry) && WARNED_DUPLICATE_ENTRY_IDS.add(entry.id())) {
                    EchoCore.LOGGER.warn("Discovery entry id {} was already registered; keeping the first entry.",
                            entry.id());
                }
            }
        }
        return entries.values().stream()
                .sorted(Comparator.comparing(EchoDiscoveryEntry::chapterId)
                        .thenComparing(EchoDiscoveryEntry::category)
                        .thenComparingInt(EchoDiscoveryEntry::sortOrder)
                        .thenComparing(EchoDiscoveryEntry::revealedTitle)
                        .thenComparing(entry -> entry.id().toString()))
                .toList();
    }

    public static Optional<EchoDiscoveryEntry> entry(Player player, Identifier id) {
        if (id == null) {
            return Optional.empty();
        }
        return entries(player).stream()
                .filter(entry -> id.equals(entry.id()))
                .findFirst();
    }

    public static EchoDiscoveryState state(Player player, EchoDiscoveryEntry entry) {
        if (entry == null) {
            return EchoDiscoveryState.LOCKED;
        }
        EchoDiscoveryState best = EchoDiscoveryState.LOCKED;
        for (EchoDiscoveryProvider provider : PROVIDERS) {
            try {
                if (!providerOwns(provider, player, entry.id())) {
                    continue;
                }
                EchoDiscoveryState state = provider.state(player, entry);
                if (state == EchoDiscoveryState.CHECKED) {
                    return EchoDiscoveryState.CHECKED;
                }
                if (state == EchoDiscoveryState.DISCOVERED) {
                    best = EchoDiscoveryState.DISCOVERED;
                }
            } catch (RuntimeException exception) {
                EchoCore.LOGGER.warn("Discovery provider {} failed while resolving {}.",
                        provider.getClass().getName(), entry.id(), exception);
            }
        }
        return best;
    }

    public static int providerCount() {
        return PROVIDERS.size();
    }

    public static int registeredEntryCount() {
        return entries(null).size();
    }

    public static void clearForTests() {
        PROVIDERS.clear();
        WARNED_DUPLICATE_ENTRY_IDS.clear();
    }

    private static boolean providerOwns(EchoDiscoveryProvider provider, Player player, Identifier id) {
        if (provider == null || id == null) {
            return false;
        }
        List<EchoDiscoveryEntry> provided;
        try {
            provided = provider.entries(player);
        } catch (RuntimeException exception) {
            EchoCore.LOGGER.warn("Discovery provider {} failed while checking ownership for {}.",
                    provider.getClass().getName(), id, exception);
            return false;
        }
        for (EchoDiscoveryEntry candidate : provided == null ? List.<EchoDiscoveryEntry>of() : provided) {
            if (candidate != null && id.equals(candidate.id())) {
                return true;
            }
        }
        return false;
    }
}
