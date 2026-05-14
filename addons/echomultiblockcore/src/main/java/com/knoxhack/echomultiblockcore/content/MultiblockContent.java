package com.knoxhack.echomultiblockcore.content;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.api.MultiblockDefinition;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class MultiblockContent {
    private static volatile Map<Identifier, MultiblockDefinition> definitions = Map.of();

    private MultiblockContent() {
    }

    public static void replaceDefinitions(Map<Identifier, MultiblockDefinition> loaded) {
        definitions = Map.copyOf(loaded == null ? Map.of() : loaded);
        EchoMultiblockCore.LOGGER.info("ECHO MultiblockCore loaded {} multiblock definition(s).", definitions.size());
    }

    public static List<MultiblockDefinition> definitions() {
        return definitions.values().stream()
                .sorted(Comparator.comparing(definition -> definition.id().toString()))
                .toList();
    }

    public static Optional<MultiblockDefinition> definition(Identifier id) {
        return Optional.ofNullable(id == null ? null : definitions.get(id));
    }

    public static Map<Identifier, MultiblockDefinition> snapshot() {
        return new LinkedHashMap<>(definitions);
    }
}
