package com.knoxhack.echolens.provider;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echolens.EchoLens;
import com.knoxhack.echolens.api.LensContext;
import com.knoxhack.echolens.api.LensDataCategory;
import com.knoxhack.echolens.api.LensInfoRow;
import com.knoxhack.echolens.api.LensInfoSection;
import com.knoxhack.echolens.api.LensTone;
import com.knoxhack.echolens.api.LensVisibility;
import com.knoxhack.echolens.api.ServerLensProvider;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

public enum ServerProgressionProvider implements ServerLensProvider {
    INSTANCE;

    @Override
    public Identifier id() {
        return EchoLens.id("server_progression");
    }

    @Override
    public int priority() {
        return 740;
    }

    @Override
    public LensDataCategory category() {
        return LensDataCategory.INTEGRATION;
    }

    @Override
    public List<LensInfoSection> inspect(LensContext context) {
        ModList mods = ModList.get();
        List<LensInfoRow> rows = new ArrayList<>();
        rows.add(serviceRow("MissionCore", EchoCoreServices.missionCoreAvailable()));
        rows.add(serviceRow("DataCore", dataCoreAvailable()));
        rows.add(LensInfoRow.of("Ashfall", mods.isLoaded("echoashfallprotocol") ? "Installed" : "Not installed",
                "A", mods.isLoaded("echoashfallprotocol") ? LensTone.GOOD : LensTone.MUTED, LensVisibility.DEEP));
        rows.add(LensInfoRow.of("Relevance", "Public progression context only", "R",
                LensTone.INFO, LensVisibility.DEEP));
        return List.of(LensInfoSection.of(EchoLens.id("section/server_progression"),
                LensDataCategory.INTEGRATION, "Server Relevance", "S", LensTone.ECHO, LensVisibility.DEEP, rows));
    }

    @Override
    public List<LensInfoRow> deepScanSignals(LensContext context) {
        boolean relevant = EchoCoreServices.missionCoreAvailable()
                || dataCoreAvailable()
                || ModList.get().isLoaded("echoashfallprotocol");
        return List.of(LensInfoRow.of("Progression",
                relevant ? "ECHO context available" : "Public baseline only",
                "E", relevant ? LensTone.GOOD : LensTone.MUTED, LensVisibility.DEEP));
    }

    private static LensInfoRow serviceRow(String label, boolean available) {
        return LensInfoRow.of(label, available ? "Available" : "No service",
                available ? "+" : "-", available ? LensTone.GOOD : LensTone.MUTED, LensVisibility.DEEP);
    }

    private static boolean dataCoreAvailable() {
        try {
            return !EchoCoreServices.dataService().registeredKeys().isEmpty()
                    || ModList.get().isLoaded("echodatacore");
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
