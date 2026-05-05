package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDiagnosticBlocker;
import com.knoxhack.echocore.api.EchoHazardTelemetry;
import com.knoxhack.echocore.api.EchoPackMode;
import com.knoxhack.echocore.api.EchoRouteRecord;
import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.lore.OrbitalLore;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.LaunchReadiness;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.suit.SuitState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class AshfallCompat {
    private static final String CHAPTER_ID = "orbital_remnants";
    private static final String MIRROR_ROOT = "echoorbitalremnants_ashfall_mirror";

    private AshfallCompat() {
    }

    public static void registerAddonChapter() {
        if (EchoAddonRegistry.isRegistered(CHAPTER_ID)) {
            return;
        }
        EchoAddonRegistry.register(new EchoAddonChapter() {
            @Override
            public String id() {
                return CHAPTER_ID;
            }

            @Override
            public String modId() {
                return EchoOrbitalRemnants.MODID;
            }

            @Override
            public String displayName() {
                return "ECHO: Orbital Remnants";
            }

            @Override
            public String summary() {
                return "Post-Nexus orbital survival chapter: launch chain, Station ECHO debris, route surveys, and ECHO-0 quarantine.";
            }

            @Override
            public boolean isAvailable(Player player) {
                try {
                    return hasPostNexusChoice(player);
                } catch (RuntimeException exception) {
                    EchoOrbitalRemnants.LOGGER.warn("Orbital chapter availability failed; treating chapter as unavailable.",
                            exception);
                    return false;
                }
            }

            @Override
            public String statusLine(Player player) {
                try {
                    EchoPackMode mode = EchoCoreServices.packMode(player);
                    if (mode == EchoPackMode.ORBITAL_STANDALONE) {
                        return "ORBITAL REMNANTS: Standalone recovered handoff file active.";
                    }
                    return isAvailable(player)
                            ? "ORBITAL REMNANTS: Nexus choice confirmed. Earth calibration can challenge quarantine."
                            : "ORBITAL REMNANTS: Locked until a Nexus choice gives orbit a field answer.";
                } catch (RuntimeException exception) {
                    EchoOrbitalRemnants.LOGGER.warn("Orbital chapter status failed; using recovered handoff fallback.",
                            exception);
                    return "ORBITAL REMNANTS: Standalone recovered handoff file active.";
                }
            }
        });
        OrbitalFactions.register();
        EchoCoreServices.registerHazardTelemetryService(AshfallCompat::hazardTelemetry);
        EchoCoreServices.registerDiagnosticService(AshfallCompat::diagnostics);
        EchoCoreServices.registerRouteRecordService(AshfallCompat::routeRecords);
        EchoOrbitalRemnants.LOGGER.info("ECHO platform providers after Orbital setup: {}",
                EchoCoreServices.platformProviderSummary());
    }

    public static boolean isAshfallLoaded() {
        try {
            return ModList.get().isLoaded(OrbitalLore.ASHFALL_MODID);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital Ashfall presence check failed; assuming standalone mode.", exception);
            return false;
        }
    }

    public static boolean isOrbitalCalibrationLocked(Player player) {
        try {
            return isAshfallLoaded() && !hasPostNexusChoice(player);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital calibration lock check failed; keeping route unlocked.", exception);
            return false;
        }
    }

    public static boolean hasPostNexusChoice(Player player) {
        if (player == null) {
            return false;
        }
        try {
            if (!isAshfallLoaded()) {
                return true;
            }
            return EchoCoreServices.hasPostNexusChoice(player);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital Nexus handoff check failed; treating handoff as unresolved.", exception);
            return false;
        }
    }

    public static void mirrorMilestone(Player player, String id, String title, String content) {
        if (!isAshfallLoaded() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        try {
            CompoundTag mirrored = player.getPersistentData().getCompoundOrEmpty(MIRROR_ROOT);
            if (mirrored.getBooleanOr(id, false)) {
                return;
            }
            mirrored.putBoolean(id, true);
            player.getPersistentData().put(MIRROR_ROOT, mirrored);

            EchoCoreServices.mirrorIntel(serverPlayer, EchoOrbitalRemnants.MODID, "orbital_" + id, title, content);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital milestone mirror failed for {}.", id, exception);
        }
    }

    private static EchoHazardTelemetry hazardTelemetry(Player player) {
        if (player == null) {
            return EchoHazardTelemetry.nominal();
        }
        try {
            SuitState suit = SuitState.get(player);
            int exposure = SuitEvents.isOrbitalExposure(player) ? 85 : 0;
            String status = SuitEvents.isOrbitalExposure(player)
                    ? "Orbital exposure active. Maintain oxygen, pressure, and seal integrity."
                    : "Orbital suit telemetry nominal.";
            return new EchoHazardTelemetry(
                    100,
                    suit.radiation(),
                    0,
                    suit.oxygen(),
                    suit.pressure(),
                    0,
                    0,
                    exposure,
                    status);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital hazard telemetry failed; using nominal telemetry.", exception);
            return EchoHazardTelemetry.nominal();
        }
    }

    private static List<EchoDiagnosticBlocker> diagnostics(Player player) {
        if (player == null) {
            return List.of();
        }
        try {
            List<EchoDiagnosticBlocker> blockers = new ArrayList<>();
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            if (isOrbitalCalibrationLocked(player)) {
                blockers.add(blocker("orbital_ashfall_handoff_locked", EchoDiagnosticBlocker.Severity.BLOCKED,
                        "Orbital calibration locked",
                        "Ashfall is installed and ECHO-0 still treats Earth as unresolved quarantine.",
                        "Complete an ECHO: Ashfall Protocol Nexus path before opening the orbital route."));
            }
            if (!progress.launchSiteTracked()) {
                blockers.add(blocker("orbital_calibration_needed", EchoDiagnosticBlocker.Severity.INFO,
                        "Earth calibration needed",
                        "No launch site is tracked for the Orbital route.",
                        "Sneak-use ECHO-7 on Earth to seed recovery sites and start launch preparation."));
            } else if (!progress.lowOrbitReached()) {
                LaunchReadiness launch = LaunchReadiness.evaluateForLaunch(player);
                LaunchReadiness assembly = LaunchReadiness.evaluateForAssembly(player);
                if (!launch.ready() || !assembly.ready()) {
                    blockers.add(blocker("orbital_launch_readiness", EchoDiagnosticBlocker.Severity.BLOCKED,
                            "Launch readiness incomplete",
                            "Launch or rocket assembly requirements are missing.",
                            "Open Orbital Command and complete the listed launch systems before using the Emergency Rocket."));
                }
            }
            SuitState suit = SuitState.get(player);
            if (SuitEvents.isOrbitalExposure(player) && (suit.oxygen() <= 25 || suit.pressure() <= 25)) {
                blockers.add(blocker("orbital_suit_critical", EchoDiagnosticBlocker.Severity.CRITICAL,
                        "Suit telemetry critical",
                        "Oxygen or pressure is below safe orbital range.",
                        "Return to a pressurized route or use oxygen/seal support immediately."));
            }
            return List.copyOf(blockers);
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital diagnostic provider failed; returning no blockers.", exception);
            return List.of();
        }
    }

    private static List<EchoRouteRecord> routeRecords(Player player) {
        if (player == null) {
            return List.of();
        }
        try {
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            String dimension = player.level().dimension().identifier().toString();
            return List.of(
                    route("orbital_earth_recontact", "Earth Recontact", "Calibration", dimension,
                            progress.launchSiteTracked() ? "CALIBRATED" : "SCAN REQUIRED",
                            progress.launchSiteTracked()
                                    ? "Earth recovery sites are seeded; the broken fall path has ground proof."
                                    : "Sneak-use ECHO-7 on Earth to rebuild the launch salvage map.",
                            progress.launchSiteTracked()),
                    route("orbital_launch_chain", "Launch Chain", "Launch", "Overworld",
                            progress.lowOrbitReached() ? "COMPLETE" : progress.launchPrepared() ? "READY" : "IN PROGRESS",
                            "Launch platform, pressure gear, oxygen support, and rocket assembly feed this route.",
                            progress.lowOrbitReached()),
                    route("orbital_route_worlds", "Route Worlds", "Survey", "Orbit/Moon/Mars/Europa",
                            progress.allSurveysComplete() ? "MAPPED" : progress.surveyStatus(),
                            "Shared route record for Station ECHO, Moon, Mars, Europa, and the Nexus Anomaly Belt.",
                            progress.allSurveysComplete()),
                    route("orbital_echo_zero", "ECHO-0 Quarantine", "Endgame", "Nexus Anomaly Belt",
                            progress.finalNetworkSealed() ? "SEALED" : progress.echoZeroEncountered() ? "CONTACT" : "LOCKED",
                            "Final quarantine route record for ECHO-0 and the orbital network seal.",
                            progress.finalNetworkSealed()));
        } catch (RuntimeException exception) {
            EchoOrbitalRemnants.LOGGER.warn("Orbital route provider failed; returning no routes.", exception);
            return List.of();
        }
    }

    private static EchoRouteRecord route(String path, String title, String category, String dimension,
            String status, String summary, boolean complete) {
        return new EchoRouteRecord(id(path), CHAPTER_ID, title, category, dimension, status, summary, complete);
    }

    private static EchoDiagnosticBlocker blocker(String path, EchoDiagnosticBlocker.Severity severity,
            String title, String detail, String nextAction) {
        return new EchoDiagnosticBlocker(id(path), CHAPTER_ID, severity, title, detail, nextAction);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, path);
    }
}
