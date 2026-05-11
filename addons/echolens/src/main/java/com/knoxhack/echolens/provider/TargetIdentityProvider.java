package com.knoxhack.echolens.provider;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoProvider;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

public enum TargetIdentityProvider implements LensInfoProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return EchoLens.id("identity");
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public LensDataCategory category() {
        return LensDataCategory.IDENTITY;
    }

    @Override
    public List<LensInfoSection> inspect(LensContext context) {
        Identifier targetId = targetId(context);
        List<LensInfoRow> rows = new ArrayList<>();
        rows.add(LensInfoRow.of("Type", context.targetKind().name(), "◇", LensTone.INFO, LensVisibility.COMPACT));
        rows.add(LensInfoRow.of("Mod", modName(targetId), "◈", LensTone.ECHO, LensVisibility.COMPACT));
        rows.add(LensInfoRow.of("Registry", targetId.toString(), "#", LensTone.MUTED, LensVisibility.EXPANDED));
        if (context.hasBlock()) {
            rows.add(LensInfoRow.of("Block Entity", context.level().getBlockEntity(context.blockPos()) == null
                    ? "No" : "Yes", "▣", LensTone.NEUTRAL, LensVisibility.EXPANDED));
        }
        return List.of(LensInfoSection.of(EchoLens.id("section/identity"), LensDataCategory.IDENTITY, "Target",
                "◎", LensTone.ECHO, LensVisibility.COMPACT, rows));
    }

    private static Identifier targetId(LensContext context) {
        if (context.hasEntity()) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(context.entity().getType());
        }
        if (context.hasBlock()) {
            return BuiltInRegistries.BLOCK.getKey(context.blockState().getBlock());
        }
        if (context.hasFluid()) {
            return BuiltInRegistries.FLUID.getKey(context.fluidState().getType());
        }
        return EchoLens.id("unknown");
    }

    private static String modName(Identifier id) {
        if (id == null) {
            return "Unknown";
        }
        return ModList.get().getModContainerById(id.getNamespace())
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(id.getNamespace());
    }
}
