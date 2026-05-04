package com.knoxhack.echoorbitalremnants.item;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.integration.AshfallCompat;
import com.knoxhack.echoorbitalremnants.progression.LaunchReadiness;
import com.knoxhack.echoorbitalremnants.progression.ModAdvancements;
import com.knoxhack.echoorbitalremnants.network.EchoTerminalSnapshot;
import com.knoxhack.echoorbitalremnants.network.OpenEchoTerminalPayload;
import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySite;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySites;
import com.knoxhack.echoorbitalremnants.world.ModDimensions;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class EchoTerminalItem extends Item {
    public EchoTerminalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                performScan(player);
            }
            openTerminal(player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void performScan(Player player) {
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        progress.tickFactionContractCooldown(player);
        progress.prepareFactionContract(player);
        if (progress.echoZeroEncountered() && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantEchoZeroResolved(serverPlayer);
        }
        if (progress.sealFinalNetwork(player)) {
            EchoTerminalProgress current = EchoTerminalProgress.get(player);
            report(player, current, current.lastTerminalReport());
            return;
        }
        Level level = player.level();
        if (SuitEvents.isOrbitalExposure(player)) {
            progress.scanOrbit(player);
            String midGameReport = tryMidGameRepair(player, progress);
            progress = EchoTerminalProgress.get(player);
            String surveyReport = trySurveyScan(player, progress);
            String contractReport = tryFactionContractScan(player, EchoTerminalProgress.get(player), false);
            if (midGameReport != null || surveyReport != null || contractReport != null) {
                String combined = combineReports(midGameReport, surveyReport, contractReport);
                report(player, EchoTerminalProgress.get(player), combined);
                return;
            }
            if (level.dimension() == ModDimensions.LUNAR_SCAR_ZONE && has(player, ModItems.HELIUM_3_CELL.get())) {
                if (midGameObjectivesEnabled() && !progress.lunarExtractorGateOpen() && !player.hasInfiniteMaterials()) {
                    report(player, progress, "Mars telemetry held. Restore three Helium Extractor Nodes with Helium Extractor Cores before the transfer window can hold.");
                    return;
                }
                progress.unlockMarsRoute(player);
                report(player, progress, "Mars transfer window resolved from Helium-3 telemetry. The route is thin, but real.");
            } else if (level.dimension() == ModDimensions.MARS_ASH_BASIN && has(player, ModItems.MARTIAN_SILICA.get())) {
                if (midGameObjectivesEnabled() && !progress.marsHabitatGateOpen() && !player.hasInfiniteMaterials()) {
                    report(player, progress, "Europa prep held. Repair three Mars Pressure Consoles with Pressure Regulators before the suit route can hold.");
                    return;
                }
                progress.unlockEuropaRoute(player);
                report(player, progress, "Europa cryo route triangulated through Martian terraformer dust.");
            } else if (level.dimension() == ModDimensions.EUROPA_CRYO_OCEAN && has(player, ModItems.CRYO_CRYSTAL.get())) {
                if (midGameObjectivesEnabled() && !progress.europaArrayGateOpen() && !player.hasInfiniteMaterials()) {
                    report(player, progress, "Deep Space Protocol held. Calibrate three Europa Thermal Arrays with Europa Probe Arrays before the anomaly route opens.");
                    return;
                }
                progress.unlockDeepSpaceProtocol(player);
                report(player, progress, "Deep Space Protocol unlocked. Cryo-ocean signal confirms the anomaly belt.");
            } else if (has(player, ModItems.NEXUS_DRIVE_CORE.get())) {
                progress.unlockDeepSpaceProtocol(player);
                report(player, progress, "Deep Space Protocol unlocked. Nexus Drive telemetry is no longer silent.");
            } else if (hasStationLifeSupportCore(player)) {
                progress.restoreStationLifeSupport(player);
                report(player, progress, "Station life support handshake restored. Lunar Signal unlocked.");
            } else {
                report(player, progress, missingOrbitalScanReport(player, progress));
            }
        } else {
            if (!progress.launchSiteTracked() && AshfallCompat.isOrbitalCalibrationLocked(player)) {
                report(player, progress, "Orbital link locked. Resolve an ECHO: Ashfall Protocol Nexus path before ECHO-7 can challenge quarantine beyond Earth.");
                return;
            }
            boolean seedSites = !progress.launchSiteTracked();
            if (seedSites && level instanceof ServerLevel serverLevel) {
                progress.markOrbitalContact(player);
                List<GroundRecoverySite> sites = GroundRecoverySites.seedStarterSites(serverLevel, player.blockPosition());
                progress.setGroundRecoverySites(player, sites);
                report(player, progress, "Orbital signal calibrated. Five Earth recovery sites tracked. Scan each landmark to secure launch salvage and prove the sky is answering.");
                return;
            }
            if (progress.hasGroundRecoverySites() && !progress.allGroundRecoverySitesComplete()) {
                EchoTerminalProgress.GroundSiteScanResult result = progress.scanGroundRecoverySite(player);
                report(player, EchoTerminalProgress.get(player), result.report());
                return;
            }
            if (seedSites) {
                progress.markOrbitalContact(player);
                report(player, progress, "Orbital signal calibrated. Recovery sites could not be seeded in this level.");
                return;
            }
            report(player, progress, progress.allGroundRecoverySitesComplete()
                    ? "Earth recovery complete. Build the launch chain and assemble the Emergency Rocket."
                    : "Orbital signal calibrated. Recovery sites tracked.");
        }
    }

    private static String tryMidGameRepair(Player player, EchoTerminalProgress progress) {
        if (!midGameObjectivesEnabled()) {
            return null;
        }
        Level level = player.level();
        if (level.dimension() == ModDimensions.LOW_EARTH_ORBIT) {
            String siteId = nearbyBlockSiteId(player, "station_network", ModBlocks.STATION_RELAY_NODE.get());
            if (siteId == null) {
                return null;
            }
            if (!has(player, ModItems.STATION_RELAY_FUSE.get()) && !player.hasInfiniteMaterials() && !progress.stationNetworkGateOpen()) {
                return "Station relay blocked: insert one Station Relay Fuse at this node.";
            }
            EchoTerminalProgress.RouteObjectiveResult result = progress.repairStationRelay(player, siteId);
            consumeRepairItem(player, ModItems.STATION_RELAY_FUSE.get(), result);
            if (result.newlyComplete()) {
                give(player, ModItems.STATION_POWER_MATRIX.get(), 1);
                give(player, ModItems.OXYGEN_CANISTER.get(), 2);
                give(player, ModItems.VACUUM_CIRCUIT.get(), 1);
            }
            return result.report("Station Network restored. Lunar prep is stronger and the Orbital Shuttle route is cleared.");
        }
        if (level.dimension() == ModDimensions.LUNAR_SCAR_ZONE) {
            String siteId = nearbyBlockSiteId(player, "lunar_extractors", ModBlocks.HELIUM_EXTRACTOR_NODE.get());
            if (siteId == null) {
                return null;
            }
            if (!has(player, ModItems.HELIUM_EXTRACTOR_CORE.get()) && !player.hasInfiniteMaterials() && !progress.lunarExtractorGateOpen()) {
                return "Helium extractor blocked: insert one Helium Extractor Core at this node.";
            }
            EchoTerminalProgress.RouteObjectiveResult result = progress.repairLunarExtractor(player, siteId);
            consumeRepairItem(player, ModItems.HELIUM_EXTRACTOR_CORE.get(), result);
            if (result.newlyComplete()) {
                give(player, ModItems.LUNAR_PRESSURE_MAP.get(), 1);
                give(player, ModItems.HELIUM_3_CELL.get(), 2);
                give(player, ModItems.SUIT_SEALANT_PATCH.get(), 2);
            }
            return result.report("Helium Extractor Network restored. Mars route reliability is online.");
        }
        if (level.dimension() == ModDimensions.MARS_ASH_BASIN) {
            String siteId = nearbyBlockSiteId(player, "mars_habitats", ModBlocks.MARS_PRESSURE_CONSOLE.get());
            if (siteId == null) {
                return null;
            }
            if (!has(player, ModItems.PRESSURE_REGULATOR.get()) && !player.hasInfiniteMaterials() && !progress.marsHabitatGateOpen()) {
                return "Mars console blocked: insert one Pressure Regulator at this habitat console.";
            }
            EchoTerminalProgress.RouteObjectiveResult result = progress.repairMarsPressureConsole(player, siteId);
            consumeRepairItem(player, ModItems.PRESSURE_REGULATOR.get(), result);
            if (result.newlyComplete()) {
                give(player, ModItems.MARTIAN_HABITAT_KEY.get(), 1);
                give(player, ModItems.MARTIAN_SILICA.get(), 3);
                give(player, ModItems.OXYGEN_BOOSTER.get(), 1);
            }
            return result.report("Mars habitats pressurized. Europa prep can hold through dust hazard zones.");
        }
        if (level.dimension() == ModDimensions.EUROPA_CRYO_OCEAN) {
            String siteId = nearbyBlockSiteId(player, "europa_arrays", ModBlocks.EUROPA_THERMAL_ARRAY.get());
            if (siteId == null) {
                return null;
            }
            if (!has(player, ModItems.EUROPA_PROBE_ARRAY.get()) && !player.hasInfiniteMaterials() && !progress.europaArrayGateOpen()) {
                return "Europa array blocked: insert one Europa Probe Array at this thermal array.";
            }
            EchoTerminalProgress.RouteObjectiveResult result = progress.repairEuropaThermalArray(player, siteId);
            consumeRepairItem(player, ModItems.EUROPA_PROBE_ARRAY.get(), result);
            if (result.newlyComplete()) {
                give(player, ModItems.THERMAL_STABILIZER.get(), 1);
                give(player, ModItems.CRYO_BATTERY.get(), 1);
                give(player, ModItems.NEXUS_STABILIZER_SHARD.get(), 1);
            }
            return result.report("Europa Thermal Array calibrated. Deep Space Protocol is unlocked.");
        }
        return null;
    }

    private static String trySurveyScan(Player player, EchoTerminalProgress progress) {
        Level level = player.level();
        if (level.dimension() == ModDimensions.LOW_EARTH_ORBIT) {
            String siteId = nearbyBlockSiteId(player, "orbit", ModBlocks.SIGNAL_RELAY.get());
            boolean itemScan = false;
            if (siteId == null && has(player, ModItems.ORBIT_SURVEY_DATA.get())) {
                siteId = itemSiteId(player, "orbit");
                itemScan = true;
            }
            if (siteId == null) {
                return null;
            }
            EchoTerminalProgress.SurveyResult result = progress.recordOrbitSurvey(player, siteId);
            consumeSurveyItem(player, ModItems.ORBIT_SURVEY_DATA.get(), itemScan, result);
            if (result.newlyComplete()) {
                give(player, ModItems.OXYGEN_CANISTER.get(), 2);
                give(player, ModItems.VACUUM_CIRCUIT.get(), 2);
            }
            return result.report("Orbit survey complete. Station power routing and salvage maps improved.");
        }
        if (level.dimension() == ModDimensions.LUNAR_SCAR_ZONE) {
            String siteId = nearbyBlockSiteId(player, "moon", ModBlocks.SURVEY_MARKER.get());
            boolean itemScan = false;
            if (siteId == null && has(player, ModItems.LUNAR_CORE_SAMPLE.get())) {
                siteId = itemSiteId(player, "moon");
                itemScan = true;
            }
            if (siteId == null) {
                return null;
            }
            EchoTerminalProgress.SurveyResult result = progress.recordMoonSurvey(player, siteId);
            consumeSurveyItem(player, ModItems.LUNAR_CORE_SAMPLE.get(), itemScan, result);
            if (result.newlyComplete()) {
                give(player, ModItems.HELIUM_3_CELL.get(), 2);
                give(player, ModItems.MARTIAN_PRESSURE_VALVE.get(), 1);
            }
            return result.report("Lunar survey complete. Helium telemetry and Mars transfer reliability improved.");
        }
        if (level.dimension() == ModDimensions.MARS_ASH_BASIN) {
            String siteId = nearbyBlockSiteId(player, "mars", ModBlocks.SIGNAL_RELAY.get());
            boolean itemScan = false;
            if (siteId == null && has(player, ModItems.MARTIAN_PRESSURE_VALVE.get())) {
                siteId = itemSiteId(player, "mars");
                itemScan = true;
            }
            if (siteId == null) {
                return null;
            }
            EchoTerminalProgress.SurveyResult result = progress.recordMarsSurvey(player, siteId);
            consumeSurveyItem(player, ModItems.MARTIAN_PRESSURE_VALVE.get(), itemScan, result);
            if (result.newlyComplete()) {
                give(player, ModItems.MARTIAN_SILICA.get(), 3);
                give(player, ModItems.EUROPA_THERMAL_PROBE.get(), 1);
            }
            return result.report("Mars survey complete. Pressure valves restored and Europa prep materials recovered.");
        }
        if (level.dimension() == ModDimensions.EUROPA_CRYO_OCEAN) {
            String siteId = nearbyBlockSiteId(player, "europa", ModBlocks.THERMAL_VENT.get());
            boolean itemScan = false;
            if (siteId == null && has(player, ModItems.EUROPA_THERMAL_PROBE.get())) {
                siteId = itemSiteId(player, "europa");
                itemScan = true;
            }
            if (siteId == null) {
                return null;
            }
            EchoTerminalProgress.SurveyResult result = progress.recordEuropaSurvey(player, siteId);
            consumeSurveyItem(player, ModItems.EUROPA_THERMAL_PROBE.get(), itemScan, result);
            if (result.newlyComplete()) {
                give(player, ModItems.CRYO_BATTERY.get(), 1);
                give(player, ModItems.NEXUS_STABILIZER_SHARD.get(), 1);
            }
            return result.report("Europa survey complete. Thermal vents mapped and Nexus stabilization recipes unlocked.");
        }
        if (level.dimension() == ModDimensions.NEXUS_ANOMALY_BELT) {
            String siteId = nearbyBlockSiteId(player, "nexus", ModBlocks.NEXUS_ANCHOR.get(), ModBlocks.NEXUS_GROWTH.get());
            boolean itemScan = false;
            if (siteId == null && has(player, ModItems.NEXUS_STABILIZER_SHARD.get())) {
                siteId = itemSiteId(player, "nexus");
                itemScan = true;
            }
            if (siteId == null) {
                return null;
            }
            EchoTerminalProgress.SurveyResult result = progress.recordNexusStabilization(player, siteId);
            if (!progress.echoZeroEncountered()) {
                return "Nexus stabilization locked. Resolve ECHO-0 first.";
            }
            consumeSurveyItem(player, ModItems.NEXUS_STABILIZER_SHARD.get(), itemScan, result);
            if (result.newlyComplete()) {
                give(player, ModItems.STABILIZED_ECHO_CORE.get(), 1);
                give(player, ModItems.NEXUS_DUST.get(), 8);
            }
            String report = result.report("Nexus anchors stabilized. Post-ECHO survey network is complete.");
            if (result.newlyComplete() && EchoTerminalProgress.get(player).finalNetworkSealed()) {
                report = EchoTerminalProgress.get(player).lastTerminalReport();
            }
            return report;
        }
        return null;
    }

    private static String tryFactionContractScan(Player player, EchoTerminalProgress progress, boolean reportBlocked) {
        FactionPledgeItem.Faction faction = progress.activeContractFaction();
        if (faction == null) {
            return reportBlocked ? progress.factionContractRequirement() : null;
        }
        boolean completed = switch (faction) {
            case ORBITAL_REMNANT -> completeOrbitalRemnantContract(player);
            case VOID_SALVAGERS -> completeVoidSalvagerContract(player);
            case NEXUS_CHOIR -> completeNexusChoirContract(player, progress);
        };
        if (!completed) {
            return reportBlocked ? factionBlockedReport(player, progress, faction) : null;
        }
        EchoTerminalProgress.ContractResult result = progress.completeFactionContract(player);
        grantFactionContractReward(player, faction);
        String report = result.name() + " complete. Contract rewards delivered; faction relay count " + result.completedCount() + ".";
        if (EchoTerminalProgress.get(player).finalNetworkSealed()) {
            report = EchoTerminalProgress.get(player).lastTerminalReport();
        }
        return report;
    }

    private static boolean completeOrbitalRemnantContract(Player player) {
        if (has(player, ModItems.ORBIT_SURVEY_DATA.get())) {
            if (!player.hasInfiniteMaterials()) {
                consumeOne(player, ModItems.ORBIT_SURVEY_DATA.get());
            }
            return true;
        }
        if (player.level().dimension() != ModDimensions.LOW_EARTH_ORBIT) {
            return false;
        }
        if (nearbyAnyBlock(player, ModBlocks.SIGNAL_RELAY.get(), ModBlocks.DOCKING_BEACON.get())) {
            return true;
        }
        return false;
    }

    private static boolean completeVoidSalvagerContract(Player player) {
        if (nearbyAnyBlock(player, ModBlocks.BROKEN_SOLAR_PANEL.get(), ModBlocks.SATELLITE_PLATING.get(),
                ModBlocks.ORBITAL_PLATING.get())) {
            return true;
        }
        if (count(player, ModItems.ORBITAL_ALLOY.get()) >= 1 && count(player, ModItems.VACUUM_CIRCUIT.get()) >= 1) {
            if (!player.hasInfiniteMaterials()) {
                consumeOne(player, ModItems.ORBITAL_ALLOY.get());
                consumeOne(player, ModItems.VACUUM_CIRCUIT.get());
            }
            return true;
        }
        return false;
    }

    private static String factionBlockedReport(Player player, EchoTerminalProgress progress, FactionPledgeItem.Faction faction) {
        return switch (faction) {
            case ORBITAL_REMNANT -> player.level().dimension() != ModDimensions.LOW_EARTH_ORBIT
                    ? "Faction contract blocked: wrong dimension. Go to Low Earth Orbit or spend 1 Orbit Survey Data."
                    : "Faction contract blocked: scan a Signal Relay/Docking Beacon or spend 1 Orbit Survey Data.";
            case VOID_SALVAGERS -> "Faction contract blocked: scan orbital salvage nearby or carry 1 Orbital Alloy and 1 Vacuum Circuit.";
            case NEXUS_CHOIR -> !progress.echoZeroEncountered()
                    ? "Faction contract blocked: Nexus Choir anchor readings unlock only after ECHO-0 is resolved."
                    : player.level().dimension() != ModDimensions.NEXUS_ANOMALY_BELT
                    ? "Faction contract blocked: wrong dimension. Go to the Nexus Anomaly Belt or spend 1 Nexus Stabilizer Shard."
                    : "Faction contract blocked: scan a Nexus Anchor/Growth or spend 1 Nexus Stabilizer Shard.";
        };
    }

    private static boolean completeNexusChoirContract(Player player, EchoTerminalProgress progress) {
        if (!progress.echoZeroEncountered()) {
            return false;
        }
        if (player.level().dimension() == ModDimensions.NEXUS_ANOMALY_BELT
                && nearbyAnyBlock(player, ModBlocks.NEXUS_ANCHOR.get(), ModBlocks.NEXUS_GROWTH.get())) {
            return true;
        }
        if (has(player, ModItems.NEXUS_STABILIZER_SHARD.get())) {
            if (!player.hasInfiniteMaterials()) {
                consumeOne(player, ModItems.NEXUS_STABILIZER_SHARD.get());
            }
            return true;
        }
        return false;
    }

    private static void grantFactionContractReward(Player player, FactionPledgeItem.Faction faction) {
        switch (faction) {
            case ORBITAL_REMNANT -> {
                give(player, ModItems.EMERGENCY_OXYGEN_CELL.get(), 4);
                give(player, ModItems.OXYGEN_CANISTER.get(), 2);
                give(player, ModItems.SUIT_SEALANT_PATCH.get(), 2);
            }
            case VOID_SALVAGERS -> {
                give(player, ModItems.VACUUM_CIRCUIT.get(), 2);
                give(player, ModItems.HEAT_SHIELD_PLATE.get(), 1);
                give(player, ModItems.NAVIGATION_CHIP.get(), 1);
            }
            case NEXUS_CHOIR -> {
                give(player, ModItems.NEXUS_DUST.get(), 4);
                give(player, ModItems.CRYO_BATTERY.get(), 1);
                give(player, ModItems.EMERGENCY_OXYGEN_CELL.get(), 2);
            }
        }
    }

    private static String missingOrbitalScanReport(Player player, EchoTerminalProgress progress) {
        Level level = player.level();
        if (progress.finalNetworkSealed()) {
            return progress.scanRequirement();
        }
        if (progress.activeContractFaction() != null) {
            return factionBlockedReport(player, progress, progress.activeContractFaction());
        }
        if (progress.factionContractCoolingDown()) {
            return "Faction contract cooling down. Wait for the relay sync counter to clear, then press SCAN again.";
        }
        if (progress.allSurveysComplete()) {
            return progress.completedFactionContractCount() == 0
                    ? "Survey network complete. " + progress.factionContractRequirement()
                    + " Complete one ECHO-tab contract before the final seal."
                    : "Survey network complete. Press SCAN to seal the final network.";
        }
        if (!progress.stationLifeSupportRestored()) {
            return "Scan incomplete. Carry or stand near a Station Life Support Core to unlock the Lunar Signal.";
        }
        if (level.dimension() == ModDimensions.LOW_EARTH_ORBIT) {
            if (midGameObjectivesEnabled() && !progress.stationNetworkGateOpen()) {
                return "Orbit objective needs a Station Relay Node nearby and one Station Relay Fuse.";
            }
            return "Orbit scan found no new log. Stand near a Signal Relay or carry Orbit Survey Data.";
        }
        if (level.dimension() == ModDimensions.LUNAR_SCAR_ZONE) {
            if (midGameObjectivesEnabled() && !progress.lunarExtractorGateOpen()) {
                return "Lunar objective needs a Helium Extractor Node nearby and one Helium Extractor Core.";
            }
            if (!progress.marsRouteUnlocked()) {
                return "Lunar scan incomplete. Carry a Helium-3 Cell to resolve the Mars Transfer Window.";
            }
            return "Lunar survey needs a Survey Marker nearby or one Lunar Core Sample in inventory.";
        }
        if (level.dimension() == ModDimensions.MARS_ASH_BASIN) {
            if (midGameObjectivesEnabled() && !progress.marsHabitatGateOpen()) {
                return "Mars objective needs a Pressure Console nearby and one Pressure Regulator.";
            }
            if (!progress.europaRouteUnlocked()) {
                return "Mars scan incomplete. Carry Martian Silica to resolve the Europa Transfer Window.";
            }
            return "Mars survey needs a Signal Relay nearby or one Martian Pressure Valve in inventory.";
        }
        if (level.dimension() == ModDimensions.EUROPA_CRYO_OCEAN) {
            if (midGameObjectivesEnabled() && !progress.europaArrayGateOpen()) {
                return "Europa objective needs a Thermal Array nearby and one Europa Probe Array.";
            }
            if (!progress.deepSpaceProtocolUnlocked()) {
                return "Europa scan incomplete. Carry a Cryo Crystal or Nexus Drive Core to unlock Deep Space Protocol.";
            }
            return "Europa survey needs a Thermal Vent nearby or one Europa Thermal Probe in inventory.";
        }
        if (level.dimension() == ModDimensions.NEXUS_ANOMALY_BELT) {
            if (!progress.echoZeroEncountered()) {
                return "Nexus stabilization locked. Defeat ECHO-0 before Anchor/Growth scans or shards can count.";
            }
            return "Nexus stabilization needs a new Nexus Anchor/Growth site or one Nexus Stabilizer Shard. Signal Analyzer can process Nexus Dust into shards.";
        }
        if (progress.completedFactionContractCount() == 0) {
            return "Faction contract unavailable. Use a faction pledge item first, then check the ECHO tab.";
        }
        return "Scan found no route hook. Use the ROUTES or SURVEY tab for the next required location and item.";
    }

    private static void report(Player player, EchoTerminalProgress progress, String message) {
        progress.setLastTerminalReport(player, message);
        player.sendSystemMessage(Component.literal("ECHO-7 // " + message));
    }

    private static String combineReports(String... reports) {
        StringBuilder builder = new StringBuilder();
        for (String report : reports) {
            if (report == null || report.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(report);
        }
        return builder.toString();
    }

    public static void openTerminal(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenEchoTerminalPayload(EchoTerminalSnapshot.from(player)));
        }
    }

    public static boolean hasTerminal(Player player) {
        return has(player, ModItems.ECHO_TERMINAL.get())
                || player.getMainHandItem().is(ModItems.ECHO_TERMINAL.get())
                || player.getOffhandItem().is(ModItems.ECHO_TERMINAL.get());
    }

    private static boolean has(Player player, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearbyAnyBlock(Player player, Block... blocks) {
        BlockPos center = player.blockPosition();
        int radius = 12;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            Block block = player.level().getBlockState(pos).getBlock();
            for (Block candidate : blocks) {
                if (block == candidate) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void give(Player player, ItemLike item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static boolean hasStationLifeSupportCore(Player player) {
        return has(player, ModBlocks.STATION_LIFE_SUPPORT_CORE.get().asItem())
                || hasNearbyBlock(player, ModBlocks.STATION_LIFE_SUPPORT_CORE.get());
    }

    private static boolean hasNearbyBlock(Player player, Block block) {
        BlockPos center = player.blockPosition();
        int radius = 12;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            if (player.level().getBlockState(pos).getBlock() == block) {
                return true;
            }
        }
        return false;
    }

    private static String nearbyBlockSiteId(Player player, String route, Block... blocks) {
        BlockPos center = player.blockPosition();
        int radius = 12;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            Block found = player.level().getBlockState(pos).getBlock();
            for (Block block : blocks) {
                if (found != block) {
                    continue;
                }
                return route + ":" + player.level().dimension().identifier().getPath() + ":" + (pos.getX() >> 4) + ":" + (pos.getZ() >> 4);
            }
        }
        return null;
    }

    private static String itemSiteId(Player player, String route) {
        return "item:" + route + ":" + player.getStringUUID() + ":" + player.tickCount + ":" + player.level().getGameTime();
    }

    private static void consumeSurveyItem(Player player, Item item, boolean itemScan, EchoTerminalProgress.SurveyResult result) {
        if (itemScan && result.counted() && !player.hasInfiniteMaterials()) {
            consumeOne(player, item);
        }
    }

    private static void consumeRepairItem(Player player, Item item, EchoTerminalProgress.RouteObjectiveResult result) {
        if (result.counted() && !player.hasInfiniteMaterials()) {
            consumeOne(player, item);
        }
    }

    private static boolean midGameObjectivesEnabled() {
        try {
            return Config.MID_GAME_OBJECTIVES_ENABLED.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    private static boolean consumeOne(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static int count(Player player, Item item) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == item) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
