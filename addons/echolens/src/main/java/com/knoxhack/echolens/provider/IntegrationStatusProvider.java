package com.knoxhack.echolens.provider;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.IntegrationLensProvider;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

public enum IntegrationStatusProvider implements IntegrationLensProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return EchoLens.id("integration_status");
    }

    @Override
    public int priority() {
        return 700;
    }

    @Override
    public List<LensInfoSection> inspect(LensContext context) {
        ModList mods = ModList.get();
        List<LensInfoRow> rows = new ArrayList<>();
        rows.add(modRow("Terminal", mods.isLoaded("echoterminal"), "Archive/context hooks"));
        rows.add(modRow("Index", mods.isLoaded("echoindex"), "Recipe, use, and tracking shortcuts"));
        rows.add(modRow("Ashfall", mods.isLoaded("echoashfallprotocol"), "Hazard/progression relevance"));
        rows.add(LensInfoRow.of("Missions", EchoCoreServices.missionCoreAvailable() ? "Available" : "No service",
                "◆", EchoCoreServices.missionCoreAvailable() ? LensTone.GOOD : LensTone.MUTED, LensVisibility.DEEP));
        return List.of(LensInfoSection.of(EchoLens.id("section/integrations"), LensDataCategory.INTEGRATION,
                "ECHO Links", "⌁", LensTone.ECHO, LensVisibility.DEEP, rows));
    }

    private static LensInfoRow modRow(String label, boolean loaded, String detail) {
        return LensInfoRow.of(label, loaded ? "Online - " + detail : "Offline", loaded ? "✓" : "-",
                loaded ? LensTone.GOOD : LensTone.MUTED, LensVisibility.DEEP);
    }
}
