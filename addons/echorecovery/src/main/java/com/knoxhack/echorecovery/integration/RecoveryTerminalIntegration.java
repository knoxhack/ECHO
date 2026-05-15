package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import java.util.List;

public final class RecoveryTerminalIntegration {
    private static boolean registered;

    private RecoveryTerminalIntegration() {}

    public static void registerCommon() {
        if (registered) {
            return;
        }
        registered = true;
        registerArchives();
        EchoRecovery.LOGGER.info("ECHO Recovery terminal integration registered.");
    }

    private static void registerArchives() {
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            id("archive/graves_basics"),
            "ECHO Recovery",
            "Grave Mechanics",
            "ACTIVE",
            List.of(
                "When a player dies, a grave block is created at the death location.",
                "All inventory items, armor, and offhand items are stored inside.",
                "Right-click the grave to open its inventory and recover items individually.",
                "Graves are protected from explosions, fire, and mob griefing by default."
            ),
            false
        ));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            id("archive/recovery_tools"),
            "ECHO Recovery",
            "Recovery Tools",
            "ONLINE",
            List.of(
                "Grave Key: binds to a specific grave; can be crafted with iron nuggets and a name tag.",
                "Recovery Compass: points to your last grave; craft with iron nuggets and a compass.",
                "Use /graves to list, locate, and recover graves remotely if enabled."
            ),
            false
        ));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            id("archive/grave_protection"),
            "ECHO Recovery",
            "Protection & Decay",
            "ACTIVE",
            List.of(
                "By default, only the grave owner can access it.",
                "Public access may be granted after a configurable time period.",
                "Graves can expire and drop items if decay is enabled in config.",
                "Admin bypass allows operators to access any grave."
            ),
            false
        ));
    }

    private static net.minecraft.resources.Identifier id(String path) {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath(EchoRecovery.MODID, path);
    }
}
