package com.knoxhack.echolens.provider;

import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.FluidLensProvider;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FluidState;

public enum FluidStateProvider implements FluidLensProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return EchoLens.id("fluid_state");
    }

    @Override
    public int priority() {
        return 140;
    }

    @Override
    public LensDataCategory category() {
        return LensDataCategory.FLUID;
    }

    @Override
    public List<LensInfoSection> inspectFluid(LensContext context, FluidState state) {
        Identifier id = BuiltInRegistries.FLUID.getKey(state.getType());
        List<LensInfoRow> rows = List.of(
                LensInfoRow.of("Fluid", id == null ? "unknown" : id.toString(), "≈", LensTone.INFO,
                        LensVisibility.COMPACT),
                LensInfoRow.of("Source", state.isSource() ? "Yes" : "Flowing", "◌",
                        state.isSource() ? LensTone.GOOD : LensTone.NEUTRAL, LensVisibility.EXPANDED),
                LensInfoRow.of("Level", Integer.toString(state.getAmount()), "≋", LensTone.NEUTRAL,
                        LensVisibility.EXPANDED));
        return List.of(LensInfoSection.of(EchoLens.id("section/fluid"), LensDataCategory.FLUID, "Fluid",
                "≈", LensTone.INFO, LensVisibility.COMPACT, rows));
    }
}
