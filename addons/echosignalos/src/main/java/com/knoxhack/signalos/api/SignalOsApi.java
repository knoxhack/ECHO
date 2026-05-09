package com.knoxhack.signalos.api;

import com.knoxhack.signalos.content.SignalOsContentRegistry;
import net.minecraft.resources.Identifier;

/**
 * Public entry point for mods that want to publish content into SignalOS.
 */
public final class SignalOsApi {
    private SignalOsApi() {
    }

    public static void registerChapter(TerminalChapter chapter) {
        SignalOsContentRegistry.registerChapter(chapter);
    }

    public static void registerPage(TerminalPage page) {
        SignalOsContentRegistry.registerPage(page);
    }

    public static void registerMission(TerminalMission mission) {
        SignalOsContentRegistry.registerMission(mission);
    }

    public static void registerArchive(TerminalArchiveRecord record) {
        SignalOsContentRegistry.registerArchive(record);
    }

    public static void registerDiagnostics(TerminalDiagnosticProvider provider) {
        SignalOsContentRegistry.registerDiagnostics(provider);
    }

    public static Identifier id(String id) {
        return TerminalIds.parse(id, "SignalOS identifier");
    }
}
