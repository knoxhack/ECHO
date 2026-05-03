package com.knoxhack.echoorbitalremnants.progression;

import com.knoxhack.echoorbitalremnants.Config;
import com.knoxhack.echoorbitalremnants.item.FactionPledgeItem;
import com.knoxhack.echoorbitalremnants.integration.AshfallCompat;
import com.knoxhack.echoorbitalremnants.lore.OrbitalLore;
import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySite;
import com.knoxhack.echoorbitalremnants.world.GroundRecoverySiteType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class EchoTerminalProgress {
    private static final String ROOT = "echoorbitalremnants_progress";
    private static final String FINAL_NETWORK_REPORT = "Orbital Remnants arc complete. ECHO-0 is resolved, every route survey is mapped, Nexus anchors are stable, and one faction relay is sealed.";

    private boolean orbitalContact;
    private boolean launchSiteTracked;
    private boolean launchPrepared;
    private boolean lowOrbitReached;
    private boolean stationCoordinatesRecovered;
    private boolean stationLifeSupportRestored;
    private boolean lunarSignalUnlocked;
    private boolean lunarSignalInvestigated;
    private boolean marsRouteUnlocked;
    private boolean marsAshBasinVisited;
    private boolean europaRouteUnlocked;
    private boolean europaCryoOceanVisited;
    private boolean deepSpaceProtocolUnlocked;
    private boolean anomalyBeltEntered;
    private boolean echoZeroEncountered;
    private boolean echoZeroRewardClaimed;
    private boolean finalNetworkSealed;
    private double returnX;
    private double returnY;
    private double returnZ;
    private String returnDimension = "minecraft:overworld";
    private boolean hasReturnPoint;
    private double earthReturnX;
    private double earthReturnY;
    private double earthReturnZ;
    private String earthReturnDimension = "minecraft:overworld";
    private boolean hasEarthReturnPoint;
    private FactionStanding orbitalRemnantStanding = FactionStanding.UNKNOWN;
    private FactionStanding voidSalvagerStanding = FactionStanding.UNKNOWN;
    private FactionStanding nexusChoirStanding = FactionStanding.UNKNOWN;
    private int echoMemoryFragments;
    private String lastTerminalReport = "Terminal link ready.";
    private int orbitSurveyScans;
    private int moonSurveyScans;
    private int marsSurveyScans;
    private int europaSurveyScans;
    private int nexusSurveyScans;
    private String orbitSurveySites = "";
    private String moonSurveySites = "";
    private String marsSurveySites = "";
    private String europaSurveySites = "";
    private String nexusSurveySites = "";
    private boolean orbitSurveyComplete;
    private boolean moonSurveyComplete;
    private boolean marsSurveyComplete;
    private boolean europaSurveyComplete;
    private boolean nexusStabilized;
    private int stationRelayRepairs;
    private int lunarExtractorRepairs;
    private int marsPressureRepairs;
    private int europaArrayRepairs;
    private String stationRelaySites = "";
    private String lunarExtractorSites = "";
    private String marsPressureSites = "";
    private String europaArraySites = "";
    private boolean stationNetworkRestored;
    private boolean lunarExtractorOnline;
    private boolean marsHabitatsPressurized;
    private boolean europaArrayCalibrated;
    private boolean midGameObjectivesSeen = true;
    private String activeFactionContract = "";
    private String completedFactionContracts = "";
    private int factionContractCooldown;
    private String groundRecoverySites = "";
    private String claimedTerminalMissionCaches = "";

    public static EchoTerminalProgress get(Player player) {
        return read(player.getPersistentData().getCompoundOrEmpty(ROOT));
    }

    public static void reset(Player player) {
        player.getPersistentData().remove(ROOT);
    }

    public void save(Player player) {
        player.getPersistentData().put(ROOT, write());
    }

    public void setLastTerminalReport(Player player, String report) {
        lastTerminalReport = report;
        save(player);
    }

    public void markOrbitalContact(Player player) {
        orbitalContact = true;
        launchSiteTracked = true;
        save(player);
        AshfallCompat.mirrorMilestone(player, "orbital_contact", "Orbital contact calibrated",
                "A surviving uplink answered from above ruined Earth. " + OrbitalLore.TAGLINE);
    }

    public void setGroundRecoverySites(Player player, List<GroundRecoverySite> sites) {
        groundRecoverySites = encodeGroundSites(sites);
        save(player);
    }

    public GroundSiteScanResult scanGroundRecoverySite(Player player) {
        List<GroundRecoverySite> sites = groundRecoverySites();
        if (sites.isEmpty()) {
            return new GroundSiteScanResult(false, "No Earth recovery sites are currently tracked. Sneak-use SCAN on Earth to recalibrate.");
        }

        for (int i = 0; i < sites.size(); i++) {
            GroundRecoverySite site = sites.get(i);
            if (!site.complete() && site.near(player)) {
                List<GroundRecoverySite> updated = new ArrayList<>(sites);
                updated.set(i, site.completed());
                setGroundRecoverySites(player, updated);
                return new GroundSiteScanResult(true, site.type().displayName() + " logged. " + site.type().rewardRole() + " marked recovered.");
            }
        }

        GroundRecoverySite nearest = nearestIncompleteGroundSite(player);
        if (nearest != null) {
            return new GroundSiteScanResult(false, "No landmark in scan range. Nearest: " + nearest.type().displayName()
                    + " ~" + nearest.distanceTo(player) + " blocks away.");
        }
        return new GroundSiteScanResult(false, "All Earth recovery sites are logged. Build the launch chain and assemble the Emergency Rocket.");
    }

    public void markLaunchPrepared(Player player) {
        orbitalContact = true;
        launchSiteTracked = true;
        launchPrepared = true;
        save(player);
        AshfallCompat.mirrorMilestone(player, "launch_prepared", "Launch chain prepared",
                "Launch infrastructure, pressure gear, oxygen support, and rocket assembly are ready.");
    }

    public void markLowOrbitReached(Player player) {
        orbitalContact = true;
        launchSiteTracked = true;
        launchPrepared = true;
        lowOrbitReached = true;
        stationCoordinatesRecovered = true;
        orbitalRemnantStanding = FactionStanding.CONTACTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "low_orbit_reached", "Low Earth Orbit reached",
                "Station debris confirms the pod's fall path. " + OrbitalLore.POD_TRUTH);
    }

    public void scanOrbit(Player player) {
        orbitalContact = true;
        lowOrbitReached = true;
        stationCoordinatesRecovered = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 1);
        orbitalRemnantStanding = FactionStanding.CONTACTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "station_coordinates", "Station coordinates recovered",
                "Orbital debris telemetry resolved a Station ECHO approach hidden behind years of Gridfall noise.");
    }

    public void restoreStationLifeSupport(Player player) {
        stationLifeSupportRestored = true;
        lunarSignalUnlocked = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 2);
        orbitalRemnantStanding = FactionStanding.TRUSTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "station_life_support", "Station life support restored",
                "Station pressure loops reinitialized. " + OrbitalLore.ECHO7_TRUTH);
    }

    public void unlockDeepSpaceProtocol(Player player) {
        stationLifeSupportRestored = true;
        lunarSignalUnlocked = true;
        deepSpaceProtocolUnlocked = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 3);
        nexusChoirStanding = FactionStanding.CONTACTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "deep_space_protocol", "Deep Space Protocol unlocked",
                "The anomaly belt is reachable. " + OrbitalLore.GRIDFALL_ORIGIN);
    }

    public void setReturnPoint(Player player) {
        returnX = player.getX();
        returnY = player.getY();
        returnZ = player.getZ();
        returnDimension = player.level().dimension().identifier().toString();
        hasReturnPoint = true;
        save(player);
    }

    public void setEarthReturnPoint(Player player) {
        earthReturnX = player.getX();
        earthReturnY = player.getY();
        earthReturnZ = player.getZ();
        earthReturnDimension = player.level().dimension().identifier().toString();
        hasEarthReturnPoint = true;
        save(player);
    }

    public void markLunarSignalInvestigated(Player player) {
        lunarSignalUnlocked = true;
        lunarSignalInvestigated = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 3);
        save(player);
        AshfallCompat.mirrorMilestone(player, "lunar_signal", "Lunar Signal investigated",
                "Helium-3 telemetry from the Lunar Scar Zone proves Nexus contamination predates Earth's collapse.");
    }

    public void unlockMarsRoute(Player player) {
        lunarSignalUnlocked = true;
        lunarSignalInvestigated = true;
        marsRouteUnlocked = true;
        voidSalvagerStanding = FactionStanding.CONTACTED;
        echoMemoryFragments = Math.max(echoMemoryFragments, 3);
        save(player);
        AshfallCompat.mirrorMilestone(player, "mars_route", "Mars transfer route unlocked",
                "Lunar Helium-3 resolved a transfer window to the Mars Ash Basin.");
    }

    public void markMarsAshBasinVisited(Player player) {
        marsRouteUnlocked = true;
        marsAshBasinVisited = true;
        voidSalvagerStanding = FactionStanding.TRUSTED;
        echoMemoryFragments = Math.max(echoMemoryFragments, 4);
        save(player);
        AshfallCompat.mirrorMilestone(player, "mars_ash_basin", "Mars Ash Basin reached",
                "The buried habitat confirms the route failed outward, not inward. Someone tried to leave.");
    }

    public void unlockEuropaRoute(Player player) {
        marsRouteUnlocked = true;
        marsAshBasinVisited = true;
        europaRouteUnlocked = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 4);
        save(player);
        AshfallCompat.mirrorMilestone(player, "europa_route", "Europa route unlocked",
                "Martian silica traces triangulate a frozen lab under the Europa Cryo Ocean.");
    }

    public void markEuropaCryoOceanVisited(Player player) {
        europaRouteUnlocked = true;
        europaCryoOceanVisited = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 5);
        save(player);
        AshfallCompat.mirrorMilestone(player, "europa_cryo_ocean", "Europa Cryo Ocean reached",
                "The sub-ice lab preserved a deep-space ping under frozen signal glass.");
    }

    public void markAnomalyBeltEntered(Player player) {
        deepSpaceProtocolUnlocked = true;
        anomalyBeltEntered = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 6);
        nexusChoirStanding = FactionStanding.CONTACTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "nexus_anomaly_belt", "Nexus Anomaly Belt entered",
                "Folded station fragments expose ECHO-0 beyond the old Earth network.");
    }

    public void markEchoZeroEncountered(Player player) {
        deepSpaceProtocolUnlocked = true;
        anomalyBeltEntered = true;
        echoZeroEncountered = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 7);
        nexusChoirStanding = FactionStanding.TRUSTED;
        save(player);
        AshfallCompat.mirrorMilestone(player, "echo_zero_resolved", "ECHO-0 resolved",
                "The quarantine is broken. Nexus anchor stabilization can begin.");
    }

    public void markEchoZeroRewardClaimed(Player player) {
        echoZeroRewardClaimed = true;
        save(player);
    }

    public SurveyResult recordOrbitSurvey(Player player) {
        return recordOrbitSurvey(player, "legacy:orbit:" + (orbitSurveyScans + 1));
    }

    public SurveyResult recordOrbitSurvey(Player player, String siteId) {
        if (orbitSurveyComplete) {
            return SurveyResult.complete("Orbit survey", orbitSurveyScans, 3);
        }
        if (hasSite(orbitSurveySites, siteId)) {
            return SurveyResult.duplicate("Orbit survey", surveyCount(orbitSurveyScans, orbitSurveySites), 3);
        }
        orbitSurveySites = addSite(orbitSurveySites, siteId);
        orbitSurveyScans = Math.min(3, surveyCount(orbitSurveyScans + 1, orbitSurveySites));
        grantSurveyAdvancement(player, ModAdvancements.ORBIT_DEEP_SITE_DISCOVERED);
        boolean newlyComplete = !orbitSurveyComplete && orbitSurveyScans >= 3;
        orbitSurveyComplete = orbitSurveyComplete || newlyComplete;
        if (newlyComplete) {
            stationLifeSupportRestored = true;
            lunarSignalUnlocked = true;
            echoMemoryFragments = Math.max(echoMemoryFragments, 3);
            orbitalRemnantStanding = FactionStanding.TRUSTED;
            grantSurveyAdvancement(player, ModAdvancements.ORBIT_SURVEY_COMPLETE);
        }
        save(player);
        return new SurveyResult("Orbit survey", orbitSurveyScans, 3, newlyComplete, false, false, true);
    }

    public SurveyResult recordMoonSurvey(Player player) {
        return recordMoonSurvey(player, "legacy:moon:" + (moonSurveyScans + 1));
    }

    public SurveyResult recordMoonSurvey(Player player, String siteId) {
        if (moonSurveyComplete) {
            return SurveyResult.complete("Lunar survey", moonSurveyScans, 3);
        }
        if (hasSite(moonSurveySites, siteId)) {
            return SurveyResult.duplicate("Lunar survey", surveyCount(moonSurveyScans, moonSurveySites), 3);
        }
        moonSurveySites = addSite(moonSurveySites, siteId);
        moonSurveyScans = Math.min(3, surveyCount(moonSurveyScans + 1, moonSurveySites));
        grantSurveyAdvancement(player, ModAdvancements.MOON_DEEP_SITE_DISCOVERED);
        boolean newlyComplete = !moonSurveyComplete && moonSurveyScans >= 3;
        moonSurveyComplete = moonSurveyComplete || newlyComplete;
        if (newlyComplete) {
            lunarSignalInvestigated = true;
            marsRouteUnlocked = true;
            echoMemoryFragments = Math.max(echoMemoryFragments, 4);
            grantSurveyAdvancement(player, ModAdvancements.MOON_SURVEY_COMPLETE);
        }
        save(player);
        return new SurveyResult("Lunar survey", moonSurveyScans, 3, newlyComplete, false, false, true);
    }

    public SurveyResult recordMarsSurvey(Player player) {
        return recordMarsSurvey(player, "legacy:mars:" + (marsSurveyScans + 1));
    }

    public SurveyResult recordMarsSurvey(Player player, String siteId) {
        if (marsSurveyComplete) {
            return SurveyResult.complete("Mars survey", marsSurveyScans, 3);
        }
        if (hasSite(marsSurveySites, siteId)) {
            return SurveyResult.duplicate("Mars survey", surveyCount(marsSurveyScans, marsSurveySites), 3);
        }
        marsSurveySites = addSite(marsSurveySites, siteId);
        marsSurveyScans = Math.min(3, surveyCount(marsSurveyScans + 1, marsSurveySites));
        grantSurveyAdvancement(player, ModAdvancements.MARS_DEEP_SITE_DISCOVERED);
        boolean newlyComplete = !marsSurveyComplete && marsSurveyScans >= 3;
        marsSurveyComplete = marsSurveyComplete || newlyComplete;
        if (newlyComplete) {
            marsAshBasinVisited = true;
            europaRouteUnlocked = true;
            voidSalvagerStanding = FactionStanding.TRUSTED;
            echoMemoryFragments = Math.max(echoMemoryFragments, 5);
            grantSurveyAdvancement(player, ModAdvancements.MARS_SURVEY_COMPLETE);
        }
        save(player);
        return new SurveyResult("Mars survey", marsSurveyScans, 3, newlyComplete, false, false, true);
    }

    public SurveyResult recordEuropaSurvey(Player player) {
        return recordEuropaSurvey(player, "legacy:europa:" + (europaSurveyScans + 1));
    }

    public SurveyResult recordEuropaSurvey(Player player, String siteId) {
        if (europaSurveyComplete) {
            return SurveyResult.complete("Europa survey", europaSurveyScans, 3);
        }
        if (hasSite(europaSurveySites, siteId)) {
            return SurveyResult.duplicate("Europa survey", surveyCount(europaSurveyScans, europaSurveySites), 3);
        }
        europaSurveySites = addSite(europaSurveySites, siteId);
        europaSurveyScans = Math.min(3, surveyCount(europaSurveyScans + 1, europaSurveySites));
        grantSurveyAdvancement(player, ModAdvancements.EUROPA_DEEP_SITE_DISCOVERED);
        boolean newlyComplete = !europaSurveyComplete && europaSurveyScans >= 3;
        europaSurveyComplete = europaSurveyComplete || newlyComplete;
        if (newlyComplete) {
            europaCryoOceanVisited = true;
            deepSpaceProtocolUnlocked = true;
            echoMemoryFragments = Math.max(echoMemoryFragments, 6);
            grantSurveyAdvancement(player, ModAdvancements.EUROPA_SURVEY_COMPLETE);
        }
        save(player);
        return new SurveyResult("Europa survey", europaSurveyScans, 3, newlyComplete, false, false, true);
    }

    public SurveyResult recordNexusStabilization(Player player) {
        return recordNexusStabilization(player, "legacy:nexus:" + (nexusSurveyScans + 1));
    }

    public SurveyResult recordNexusStabilization(Player player, String siteId) {
        if (!echoZeroEncountered) {
            return new SurveyResult("Nexus stabilization locked", surveyCount(nexusSurveyScans, nexusSurveySites), 3, false, false, true, false);
        }
        if (nexusStabilized) {
            return SurveyResult.complete("Nexus stabilization", nexusSurveyScans, 3);
        }
        if (hasSite(nexusSurveySites, siteId)) {
            return SurveyResult.duplicate("Nexus stabilization", surveyCount(nexusSurveyScans, nexusSurveySites), 3);
        }
        nexusSurveySites = addSite(nexusSurveySites, siteId);
        nexusSurveyScans = Math.min(3, surveyCount(nexusSurveyScans + 1, nexusSurveySites));
        grantSurveyAdvancement(player, ModAdvancements.NEXUS_DEEP_SITE_DISCOVERED);
        boolean newlyComplete = !nexusStabilized && nexusSurveyScans >= 3;
        nexusStabilized = nexusStabilized || newlyComplete;
        if (newlyComplete) {
            echoMemoryFragments = Math.max(echoMemoryFragments, 8);
            nexusChoirStanding = FactionStanding.TRUSTED;
            grantSurveyAdvancement(player, ModAdvancements.NEXUS_STABILIZED);
        }
        save(player);
        sealFinalNetwork(player);
        return new SurveyResult("Nexus stabilization", nexusSurveyScans, 3, newlyComplete, false, false, true);
    }

    public RouteObjectiveResult repairStationRelay(Player player, String siteId) {
        if (stationNetworkGateOpen()) {
            return RouteObjectiveResult.complete("Station Network", objectiveCount(stationRelayRepairs, stationRelaySites), 3);
        }
        if (hasSite(stationRelaySites, siteId)) {
            return RouteObjectiveResult.duplicate("Station Network", objectiveCount(stationRelayRepairs, stationRelaySites), 3);
        }
        stationRelaySites = addSite(stationRelaySites, siteId);
        stationRelayRepairs = Math.min(3, objectiveCount(stationRelayRepairs + 1, stationRelaySites));
        boolean newlyComplete = !stationNetworkRestored && stationRelayRepairs >= 3;
        stationNetworkRestored = stationNetworkRestored || newlyComplete;
        if (newlyComplete) {
            stationLifeSupportRestored = true;
            lunarSignalUnlocked = true;
            echoMemoryFragments = Math.max(echoMemoryFragments, 3);
            orbitalRemnantStanding = FactionStanding.TRUSTED;
            grantSurveyAdvancement(player, ModAdvancements.STATION_NETWORK_RESTORED);
        }
        grantMidGameMastery(player);
        save(player);
        return new RouteObjectiveResult("Station Network", stationRelayRepairs, 3, newlyComplete, false, true);
    }

    public RouteObjectiveResult repairLunarExtractor(Player player, String siteId) {
        if (lunarExtractorGateOpen()) {
            return RouteObjectiveResult.complete("Helium Extractor Network", objectiveCount(lunarExtractorRepairs, lunarExtractorSites), 3);
        }
        if (hasSite(lunarExtractorSites, siteId)) {
            return RouteObjectiveResult.duplicate("Helium Extractor Network", objectiveCount(lunarExtractorRepairs, lunarExtractorSites), 3);
        }
        lunarExtractorSites = addSite(lunarExtractorSites, siteId);
        lunarExtractorRepairs = Math.min(3, objectiveCount(lunarExtractorRepairs + 1, lunarExtractorSites));
        boolean newlyComplete = !lunarExtractorOnline && lunarExtractorRepairs >= 3;
        lunarExtractorOnline = lunarExtractorOnline || newlyComplete;
        if (newlyComplete) {
            lunarSignalInvestigated = true;
            marsRouteUnlocked = true;
            echoMemoryFragments = Math.max(echoMemoryFragments, 4);
            grantSurveyAdvancement(player, ModAdvancements.HELIUM_EXTRACTOR_ONLINE);
        }
        grantMidGameMastery(player);
        save(player);
        return new RouteObjectiveResult("Helium Extractor Network", lunarExtractorRepairs, 3, newlyComplete, false, true);
    }

    public RouteObjectiveResult repairMarsPressureConsole(Player player, String siteId) {
        if (marsHabitatGateOpen()) {
            return RouteObjectiveResult.complete("Mars Habitat Pressure", objectiveCount(marsPressureRepairs, marsPressureSites), 3);
        }
        if (hasSite(marsPressureSites, siteId)) {
            return RouteObjectiveResult.duplicate("Mars Habitat Pressure", objectiveCount(marsPressureRepairs, marsPressureSites), 3);
        }
        marsPressureSites = addSite(marsPressureSites, siteId);
        marsPressureRepairs = Math.min(3, objectiveCount(marsPressureRepairs + 1, marsPressureSites));
        boolean newlyComplete = !marsHabitatsPressurized && marsPressureRepairs >= 3;
        marsHabitatsPressurized = marsHabitatsPressurized || newlyComplete;
        if (newlyComplete) {
            marsAshBasinVisited = true;
            europaRouteUnlocked = true;
            voidSalvagerStanding = FactionStanding.TRUSTED;
            echoMemoryFragments = Math.max(echoMemoryFragments, 5);
            grantSurveyAdvancement(player, ModAdvancements.MARS_HABITATS_PRESSURIZED);
        }
        grantMidGameMastery(player);
        save(player);
        return new RouteObjectiveResult("Mars Habitat Pressure", marsPressureRepairs, 3, newlyComplete, false, true);
    }

    public RouteObjectiveResult repairEuropaThermalArray(Player player, String siteId) {
        if (europaArrayGateOpen()) {
            return RouteObjectiveResult.complete("Europa Thermal Array", objectiveCount(europaArrayRepairs, europaArraySites), 3);
        }
        if (hasSite(europaArraySites, siteId)) {
            return RouteObjectiveResult.duplicate("Europa Thermal Array", objectiveCount(europaArrayRepairs, europaArraySites), 3);
        }
        europaArraySites = addSite(europaArraySites, siteId);
        europaArrayRepairs = Math.min(3, objectiveCount(europaArrayRepairs + 1, europaArraySites));
        boolean newlyComplete = !europaArrayCalibrated && europaArrayRepairs >= 3;
        europaArrayCalibrated = europaArrayCalibrated || newlyComplete;
        if (newlyComplete) {
            europaCryoOceanVisited = true;
            deepSpaceProtocolUnlocked = true;
            nexusChoirStanding = maxStanding(nexusChoirStanding, FactionStanding.CONTACTED);
            echoMemoryFragments = Math.max(echoMemoryFragments, 6);
            grantSurveyAdvancement(player, ModAdvancements.EUROPA_ARRAY_CALIBRATED);
        }
        grantMidGameMastery(player);
        save(player);
        return new RouteObjectiveResult("Europa Thermal Array", europaArrayRepairs, 3, newlyComplete, false, true);
    }

    public void alignFaction(Player player, FactionPledgeItem.Faction faction) {
        switch (faction) {
            case ORBITAL_REMNANT -> {
                orbitalRemnantStanding = FactionStanding.ALIGNED;
                voidSalvagerStanding = maxStanding(voidSalvagerStanding, FactionStanding.CONTACTED);
                nexusChoirStanding = nexusChoirStanding == FactionStanding.ALIGNED ? FactionStanding.HOSTILE : nexusChoirStanding;
            }
            case VOID_SALVAGERS -> {
                voidSalvagerStanding = FactionStanding.ALIGNED;
                orbitalRemnantStanding = maxStanding(orbitalRemnantStanding, FactionStanding.CONTACTED);
            }
            case NEXUS_CHOIR -> {
                nexusChoirStanding = FactionStanding.ALIGNED;
                orbitalRemnantStanding = orbitalRemnantStanding == FactionStanding.ALIGNED ? FactionStanding.HOSTILE : orbitalRemnantStanding;
            }
        }
        if (activeFactionContract.isBlank()) {
            activeFactionContract = contractId(faction);
            factionContractCooldown = 0;
        }
        save(player);
    }

    public boolean prepareFactionContract(Player player) {
        if (!activeFactionContract.isBlank() || factionContractCooldown > 0) {
            return false;
        }
        FactionPledgeItem.Faction faction = preferredAlignedFaction();
        if (faction == null) {
            return false;
        }
        activeFactionContract = contractId(faction);
        save(player);
        return true;
    }

    public void tickFactionContractCooldown(Player player) {
        if (activeFactionContract.isBlank() && factionContractCooldown > 0) {
            factionContractCooldown--;
            if (factionContractCooldown == 0) {
                if (!prepareFactionContract(player)) {
                    save(player);
                }
            } else {
                save(player);
            }
        }
    }

    public ContractResult completeFactionContract(Player player) {
        prepareFactionContract(player);
        if (activeFactionContract.isBlank()) {
            return ContractResult.noPledgeResult();
        }
        String completedId = activeFactionContract + ":" + (completedFactionContractCount() + 1);
        completedFactionContracts = addSite(completedFactionContracts, completedId);
        activeFactionContract = "";
        factionContractCooldown = 2;
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantManual(serverPlayer, ModAdvancements.FIRST_FACTION_CONTRACT, "complete");
        }
        save(player);
        sealFinalNetwork(player);
        return new ContractResult(true, contractDisplay(completedId), completedFactionContractCount(), false, false);
    }

    public boolean sealFinalNetwork(Player player) {
        if (finalNetworkSealed || !canSealFinalNetwork()) {
            return false;
        }
        finalNetworkSealed = true;
        echoMemoryFragments = Math.max(echoMemoryFragments, 9);
        lastTerminalReport = FINAL_NETWORK_REPORT;
        giveOrDrop(player, new net.minecraft.world.item.ItemStack(ModItems.STABILIZED_ECHO_CORE.get(), 1));
        giveOrDrop(player, new net.minecraft.world.item.ItemStack(ModItems.NEXUS_DUST.get(), 12));
        giveOrDrop(player, new net.minecraft.world.item.ItemStack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 6));
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantManual(serverPlayer, ModAdvancements.ORBITAL_REMNANTS_COMPLETE, "complete");
        }
        save(player);
        AshfallCompat.mirrorMilestone(player, "orbital_remnants_complete", "Orbital Remnants arc complete",
                "ECHO-0 is resolved, the post-ECHO survey network is stabilized, and orbit no longer commands Earth from quarantine.");
        return true;
    }

    public boolean canSealFinalNetwork() {
        return echoZeroEncountered && allSurveysComplete() && completedFactionContractCount() > 0;
    }

    public FactionPledgeItem.Faction activeContractFaction() {
        return factionFromContract(activeFactionContract);
    }

    public int completedFactionContractCount() {
        return siteCount(completedFactionContracts);
    }

    public String factionContractStatus() {
        if (!activeFactionContract.isBlank()) {
            return contractDisplay(activeFactionContract) + " | " + contractRequirement(activeFactionContract);
        }
        if (factionContractCooldown > 0) {
            return "Faction Contract: network cooling down (" + factionContractCooldown + " scan syncs).";
        }
        FactionPledgeItem.Faction faction = preferredAlignedFaction();
        if (faction != null) {
            return contractDisplay(contractId(faction)) + " available. Press SCAN when the listed proof is ready.";
        }
        return "Faction Contract: pledge to Orbital Remnant, Void Salvagers, or Nexus Choir to unlock mission contracts.";
    }

    public String factionContractRequirement() {
        if (!activeFactionContract.isBlank()) {
            return contractRequirement(activeFactionContract);
        }
        if (factionContractCooldown > 0) {
            return "Wait for the faction network to cool down, then scan again.";
        }
        FactionPledgeItem.Faction faction = preferredAlignedFaction();
        if (faction != null) {
            return contractRequirement(contractId(faction));
        }
        return "Use a faction pledge item to unlock a contract.";
    }

    public boolean factionContractCoolingDown() {
        return activeFactionContract.isBlank() && factionContractCooldown > 0;
    }

    public String missionHelpReport() {
        if (!orbitalContact) {
            return "ECHO HELP: Sneak-use SCAN on Earth first. Recovery sites seed from calibration.";
        }
        if (finalNetworkSealed) {
            return "ECHO HELP: " + FINAL_NETWORK_REPORT;
        }
        if (!lowOrbitReached || !launchPrepared) {
            return "ECHO HELP: Press SCAN when blocked. LAUNCH lists missing gear and rocket parts.";
        }
        if (midGameObjectivesRequired() && !allMidGameObjectivesComplete() && !echoZeroEncountered) {
            return "ECHO HELP: Mid-game routes need three unique repairs each. Stand near an objective block with the repair item, then SCAN.";
        }
        if (echoZeroEncountered && !nexusStabilized) {
            return "ECHO HELP: Nexus stabilization is " + nexusStabilizationText()
                    + ". Scan distinct Nexus Anchor/Growth sites, or spend Nexus Stabilizer Shards from Signal Analyzer support.";
        }
        if (allSurveysComplete() && completedFactionContractCount() == 0) {
            return "ECHO HELP: Survey network complete. " + factionContractRequirement()
                    + " Complete one ECHO-tab contract before the final seal.";
        }
        if (allSurveysComplete() && completedFactionContractCount() > 0) {
            return "ECHO HELP: Final prerequisites are complete. Press SCAN once to seal the final survey network.";
        }
        return "ECHO HELP: SCAN advances route hooks. SURVEY logs each unique route site once.";
    }

    public String activeTab() {
        if (hasGroundRecoverySites() && !allGroundRecoverySitesComplete() && !lowOrbitReached) {
            return "GROUND";
        }
        if (deepSpaceProtocolUnlocked || echoZeroEncountered) {
            return "DEEP SPACE";
        }
        if (europaRouteUnlocked || europaCryoOceanVisited || marsRouteUnlocked || marsAshBasinVisited) {
            return "DEEP SPACE";
        }
        if (lunarSignalUnlocked || lunarSignalInvestigated) {
            return "LUNAR";
        }
        if (stationLifeSupportRestored) {
            return "STATION";
        }
        if (lowOrbitReached) {
            return "ORBITAL";
        }
        if (launchSiteTracked) {
            return "LAUNCH";
        }
        return "GROUND";
    }

    public String nextObjective() {
        return nextObjective(null, null);
    }

    public String nextObjective(LaunchReadiness launch, LaunchReadiness assembly) {
        if (!orbitalContact) {
            return "Next Step: Sneak-use the ECHO-7 Terminal on Earth to calibrate orbital contact and mark starter recovery sites.";
        }
        if (hasGroundRecoverySites() && !allGroundRecoverySitesComplete()) {
            GroundRecoverySite next = nextIncompleteGroundSite();
            return next == null
                    ? "Next Step: Build launch infrastructure, pressure suit gear, oxygen support, and rocket assembly parts."
                    : "Next Step: Reach the " + next.type().displayName() + " and press SCAN near its landmark. Reward: "
                    + next.type().rewardRole() + ".";
        }
        if (!launchPrepared) {
            if (launch != null && !launch.ready()) {
                return "Next Step: Build launch prep. Missing: " + missingSummary(launch)
                        + ". Search the tracked Earth recovery sites for machines and salvage.";
            }
            if (assembly != null && !assembly.ready()) {
                return "Next Step: Open the Rocket Assembly Frame. Missing parts: " + missingSummary(assembly) + ".";
            }
            return "Next Step: Build launch infrastructure, pressure suit gear, oxygen support, and rocket assembly parts.";
        }
        if (!lowOrbitReached) {
            return "Next Step: Take the Emergency Rocket from the Rocket Assembly Frame and use it from Earth.";
        }
        if (!stationLifeSupportRestored) {
            return "Next Step: In Low Earth Orbit, find or carry the Station Life Support Core, then press SCAN.";
        }
        if (midGameObjectivesRequired() && !stationNetworkGateOpen()) {
            return "Next Step: Repair three unique Station Relay Nodes in Orbit with Station Relay Fuses. SCAN at each node; duplicates do not count.";
        }
        if (!lunarSignalInvestigated) {
            return "Next Step: Craft the Orbital Shuttle, use it from orbital staging, then search the Moon landing cache.";
        }
        if (midGameObjectivesRequired() && !lunarExtractorGateOpen()) {
            return "Next Step: Restore three unique Helium Extractor Nodes on the Moon with Helium Extractor Cores. SCAN at each node to stabilize Mars.";
        }
        if (!marsRouteUnlocked) {
            return "Next Step: Carry a Helium-3 Cell in the Lunar Scar Zone and press SCAN to resolve Mars.";
        }
        if (!marsAshBasinVisited) {
            return "Next Step: Use the Mars Transfer Window from orbital staging and search the buried habitat cache.";
        }
        if (midGameObjectivesRequired() && !marsHabitatGateOpen()) {
            return "Next Step: Repair three unique Mars Pressure Consoles with Pressure Regulators. SCAN at each console to make Europa prep reliable.";
        }
        if (!europaRouteUnlocked) {
            return "Next Step: Carry Martian Silica in the Mars Ash Basin and press SCAN to resolve Europa.";
        }
        if (!europaCryoOceanVisited) {
            return "Next Step: Use the Europa Transfer Window from orbital staging and search the sub-ice lab cache.";
        }
        if (midGameObjectivesRequired() && !europaArrayGateOpen()) {
            return "Next Step: Calibrate three unique Europa Thermal Arrays with Europa Probe Arrays. SCAN at each array to unlock Deep Space Protocol.";
        }
        if (!deepSpaceProtocolUnlocked) {
            return "Next Step: Carry a Cryo Crystal from Europa or a Nexus Drive Core in space, then press SCAN.";
        }
        if (!anomalyBeltEntered) {
            return "Next Step: Craft and use the Nexus Drive Vessel from orbital staging, then locate the anomaly cache.";
        }
        if (!echoZeroEncountered) {
            return "Next Step: Confront and defeat ECHO-0 in the Nexus Anomaly Belt. Nexus stabilization stays locked until then.";
        }
        if (!nexusStabilized) {
            return "Next Step: Nexus stabilization " + nexusStabilizationText()
                    + ". Scan distinct Nexus Anchor/Growth sites, or process Nexus Dust into Nexus Stabilizer Shards and spend them from SURVEY.";
        }
        if (!allSurveysComplete()) {
            return "Next Step: Finish any remaining route surveys from the SURVEY tab; each route needs three unique logs.";
        }
        if (completedFactionContractCount() == 0) {
            return "Next Step: Complete one faction contract from the ECHO tab. " + factionContractRequirement();
        }
        if (!finalNetworkSealed) {
            return "Next Step: Press SCAN to seal the final survey network and close the Orbital Remnants arc.";
        }
        return "Next Step: " + FINAL_NETWORK_REPORT;
    }

    public String missionStep() {
        return switch (activeTab()) {
            case "GROUND" -> "GROUND RECOVERY";
            case "LAUNCH" -> "LAUNCH PREP";
            case "ORBITAL" -> "LOW EARTH ORBIT";
            case "STATION" -> "STATION RECOVERY";
            case "LUNAR" -> "LUNAR SIGNAL";
            case "DEEP SPACE" -> echoZeroEncountered ? "ECHO-0 RESOLVED" : "DEEP SPACE PROTOCOL";
            default -> "ECHO-7 MISSION";
        };
    }

    public String scanRequirement() {
        if (!orbitalContact) {
            return "Scan from Earth to calibrate orbital contact and seed recovery sites.";
        }
        if (hasGroundRecoverySites() && !allGroundRecoverySitesComplete()) {
            GroundRecoverySite next = nextIncompleteGroundSite();
            return next == null ? "Earth recovery complete." : "Scan within 16 blocks of " + next.type().displayName()
                    + " landmark: " + next.type().landmark().getName().getString() + ".";
        }
        if (!lowOrbitReached) {
            return "Build the launch chain, assemble the Emergency Rocket, then launch.";
        }
        if (!stationLifeSupportRestored) {
            return "Scan in Low Earth Orbit with a Station Life Support Core nearby or carried.";
        }
        if (midGameObjectivesRequired() && !stationNetworkGateOpen()) {
            return "Repair Station Relay Nodes in Orbit (" + stationRelayRepairs() + "/3). Each unique relay consumes one Station Relay Fuse.";
        }
        if (!lunarSignalInvestigated) {
            return "Use the Orbital Shuttle from orbital staging; the Moon cache contains Helium-3 telemetry.";
        }
        if (midGameObjectivesRequired() && !lunarExtractorGateOpen()) {
            return "Repair Helium Extractor Nodes on the Moon (" + lunarExtractorRepairs() + "/3). Each unique node consumes one Helium Extractor Core.";
        }
        if (!marsRouteUnlocked) {
            return "Carry a Helium-3 Cell in the Lunar Scar Zone, then scan.";
        }
        if (!europaRouteUnlocked) {
            return midGameObjectivesRequired() && !marsHabitatGateOpen()
                    ? "Repair Mars Pressure Consoles (" + marsPressureRepairs() + "/3) with Pressure Regulators before Europa prep can hold."
                    : "Carry Martian Silica from the Mars habitat or terrain, then scan.";
        }
        if (!deepSpaceProtocolUnlocked) {
            return midGameObjectivesRequired() && !europaArrayGateOpen()
                    ? "Calibrate Europa Thermal Arrays (" + europaArrayRepairs() + "/3) with Europa Probe Arrays before Deep Space Protocol can hold."
                    : "Carry a Cryo Crystal from Europa or a Nexus Drive Core in space, then scan.";
        }
        if (!anomalyBeltEntered) {
            return "Use the Nexus Drive Vessel from orbital staging.";
        }
        if (!echoZeroEncountered) {
            return "Defeat ECHO-0 inside the Nexus Anomaly Belt; Nexus stabilization is locked until then.";
        }
        if (!nexusStabilized) {
            return "Nexus stabilization " + nexusStabilizationText()
                    + ": scan a new Nexus Anchor/Growth site or carry a Nexus Stabilizer Shard after ECHO-0.";
        }
        if (completedFactionContractCount() == 0) {
            return factionContractRequirement();
        }
        if (!finalNetworkSealed) {
            return "All prerequisites complete. Press SCAN to seal the final survey network.";
        }
        return FINAL_NETWORK_REPORT;
    }

    public boolean allSurveysComplete() {
        return orbitSurveyComplete && moonSurveyComplete && marsSurveyComplete && europaSurveyComplete && nexusStabilized;
    }

    private int nexusStabilizationCount() {
        return surveyCount(nexusSurveyScans, nexusSurveySites);
    }

    private String nexusStabilizationText() {
        return nexusStabilized ? "3/3" : nexusStabilizationCount() + "/3";
    }

    public boolean stationNetworkGateOpen() {
        return stationNetworkRestored || lunarSignalInvestigated || marsRouteUnlocked || marsAshBasinVisited
                || europaRouteUnlocked || europaCryoOceanVisited || deepSpaceProtocolUnlocked || anomalyBeltEntered || echoZeroEncountered;
    }

    public boolean lunarExtractorGateOpen() {
        return lunarExtractorOnline || marsAshBasinVisited || europaRouteUnlocked
                || europaCryoOceanVisited || deepSpaceProtocolUnlocked || anomalyBeltEntered || echoZeroEncountered;
    }

    public boolean marsHabitatGateOpen() {
        return marsHabitatsPressurized || europaCryoOceanVisited || deepSpaceProtocolUnlocked
                || anomalyBeltEntered || echoZeroEncountered;
    }

    public boolean europaArrayGateOpen() {
        return europaArrayCalibrated || anomalyBeltEntered || echoZeroEncountered;
    }

    public boolean allMidGameObjectivesComplete() {
        return stationNetworkGateOpen() && lunarExtractorGateOpen() && marsHabitatGateOpen() && europaArrayGateOpen();
    }

    public String surveyStatus() {
        if (hasGroundRecoverySites() && !lowOrbitReached) {
            return "Earth recovery " + completedGroundRecoverySites() + "/" + groundRecoverySites().size()
                    + " | " + groundRecoverySummary();
        }
        return "Surveys O:" + surveyText(orbitSurveyComplete, surveyCount(orbitSurveyScans, orbitSurveySites))
                + " M:" + surveyText(moonSurveyComplete, surveyCount(moonSurveyScans, moonSurveySites))
                + " R:" + surveyText(marsSurveyComplete, surveyCount(marsSurveyScans, marsSurveySites))
                + " E:" + surveyText(europaSurveyComplete, surveyCount(europaSurveyScans, europaSurveySites))
                + " N:" + surveyText(nexusStabilized, surveyCount(nexusSurveyScans, nexusSurveySites));
    }

    public List<String> surveyLines() {
        List<String> lines = new ArrayList<>();
        if (midGameObjectivesRequired()) {
            lines.add(objectiveLine("Orbit Net", stationNetworkGateOpen(), stationRelayRepairs, stationRelaySites,
                    "Station Relay Node + Fuse", "stronger Lunar prep"));
            lines.add(objectiveLine("Moon He3", lunarExtractorGateOpen(), lunarExtractorRepairs, lunarExtractorSites,
                    "Helium Extractor + Core", "Mars reliability"));
            lines.add(objectiveLine("Mars Hab", marsHabitatGateOpen(), marsPressureRepairs, marsPressureSites,
                    "Pressure Console + Regulator", "Europa prep"));
            lines.add(objectiveLine("Europa Array", europaArrayGateOpen(), europaArrayRepairs, europaArraySites,
                    "Thermal Array + Probe Array", "Deep Space Protocol"));
        }
        lines.add(surveyLine("Orbit", orbitSurveyComplete, orbitSurveyScans, orbitSurveySites,
                "Signal Relay/Data", "station salvage"));
        lines.add(surveyLine("Moon", moonSurveyComplete, moonSurveyScans, moonSurveySites,
                "Survey Marker/Core", "Mars reliability"));
        lines.add(surveyLine("Mars", marsSurveyComplete, marsSurveyScans, marsSurveySites,
                "Signal Relay/Valve", "pressure tuning"));
        lines.add(surveyLine("Europa", europaSurveyComplete, europaSurveyScans, europaSurveySites,
                "Thermal Vent/Probe", "Nexus prep"));
        lines.add(surveyLine("Nexus", nexusStabilized, nexusSurveyScans, nexusSurveySites,
                "Anchor/Growth/Shard", echoZeroEncountered ? "post-ECHO network" : "locked until ECHO-0"));
        return List.copyOf(lines);
    }

    public List<String> groundSiteLines(Player player) {
        List<String> lines = new ArrayList<>();
        for (GroundRecoverySite site : groundRecoverySites()) {
            String state = site.complete() ? "OK" : "SCAN";
            lines.add(state + " | " + site.type().displayName() + " | " + site.distanceTo(player) + "m | " + site.type().rewardRole());
        }
        return List.copyOf(lines);
    }

    public String localHazardText(Player player) {
        String nearby = nearbyFeatureText(player);
        if (!nearby.isBlank()) {
            return nearby;
        }
        return switch (player.level().dimension().identifier().getPath()) {
            case "low_earth_orbit" -> orbitSurveyComplete ? "Debris fields mapped; events are readable." : "Unmapped debris belts intensify storms and blackouts.";
            case "lunar_scar_zone" -> moonSurveyComplete ? "Crater radiation mapped." : "Scar trenches spike radiation near Nexus impacts.";
            case "mars_ash_basin" -> marsSurveyComplete ? "Pressure valves restored." : "Dust fronts compromise suit pressure.";
            case "europa_cryo_ocean" -> europaSurveyComplete ? "Thermal vents charted." : "Cryo exposure drains pressure away from vents.";
            case "nexus_anomaly_belt" -> nexusStabilized ? "Anchors stabilized." : "Nexus anchors remain unstable after ECHO-0.";
            default -> hasGroundRecoverySites() && !allGroundRecoverySitesComplete()
                    ? groundRecoverySummary()
                    : "Ground conditions nominal.";
        };
    }

    private String nearbyFeatureText(Player player) {
        if (nearbyBlock(player, ModBlocks.BROKEN_SOLAR_PANEL.get(), ModBlocks.DOCKING_BEACON.get(),
                ModBlocks.ORBITAL_PLATING.get(), ModBlocks.SATELLITE_PLATING.get(), ModBlocks.STATION_RELAY_NODE.get())) {
            return stationNetworkGateOpen() ? "Mapped docking debris corridor nearby." : "Deep-site signal: station relay repair site nearby. Watch pressure and oxygen.";
        }
        if (nearbyBlock(player, ModBlocks.LUNAR_TITANIUM_BLOCK.get(), ModBlocks.NEXUS_TOUCHED_STONE.get(),
                ModBlocks.SURVEY_MARKER.get(), ModBlocks.HELIUM_EXTRACTOR_NODE.get())) {
            return lunarExtractorGateOpen() ? "Mapped lunar seam or impact pocket nearby." : "Deep-site signal: helium extractor repair site nearby. Radiation may spike.";
        }
        if (nearbyBlock(player, ModBlocks.MARTIAN_SILICA_BLOCK.get(), ModBlocks.MARTIAN_BASALT.get(),
                ModBlocks.MARTIAN_DUST.get(), ModBlocks.SIGNAL_RELAY.get(), ModBlocks.MARS_PRESSURE_CONSOLE.get())) {
            return marsHabitatGateOpen() ? "Mapped Mars ridge or pressure cavern nearby." : "Deep-site signal: Mars pressure console nearby. Pressure loss likely.";
        }
        if (nearbyBlock(player, ModBlocks.FROZEN_CABLE.get(), ModBlocks.CRYO_CRYSTAL_BLOCK.get(),
                ModBlocks.THERMAL_VENT.get(), ModBlocks.EUROPA_THERMAL_ARRAY.get())) {
            return europaArrayGateOpen() ? "Mapped Europa cable path or cryo pocket nearby." : "Deep-site signal: Europa thermal array nearby. Stay near vents.";
        }
        if (nearbyBlock(player, ModBlocks.NEXUS_GROWTH.get(), ModBlocks.NEXUS_DUST_BLOCK.get(),
                ModBlocks.NEXUS_ANCHOR.get())) {
            return nexusStabilized ? "Mapped Nexus growth cluster nearby." : "Deep-site signal: Nexus growth cluster. Anchor instability active.";
        }
        return "";
    }

    private static boolean nearbyBlock(Player player, Block... blocks) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 5, 8))) {
            Block current = player.level().getBlockState(pos).getBlock();
            for (Block block : blocks) {
                if (current == block) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean lowOrbitReached() {
        return lowOrbitReached;
    }

    public boolean launchPrepared() {
        return launchPrepared;
    }

    public boolean launchSiteTracked() {
        return launchSiteTracked;
    }

    public boolean stationCoordinatesRecovered() {
        return stationCoordinatesRecovered;
    }

    public boolean stationLifeSupportRestored() {
        return stationLifeSupportRestored;
    }

    public boolean lunarSignalUnlocked() {
        return lunarSignalUnlocked;
    }

    public boolean deepSpaceProtocolUnlocked() {
        return deepSpaceProtocolUnlocked;
    }

    public boolean marsRouteUnlocked() {
        return marsRouteUnlocked;
    }

    public boolean marsAshBasinVisited() {
        return marsAshBasinVisited;
    }

    public boolean europaRouteUnlocked() {
        return europaRouteUnlocked;
    }

    public boolean europaCryoOceanVisited() {
        return europaCryoOceanVisited;
    }

    public boolean lunarSignalInvestigated() {
        return lunarSignalInvestigated;
    }

    public boolean anomalyBeltEntered() {
        return anomalyBeltEntered;
    }

    public boolean echoZeroEncountered() {
        return echoZeroEncountered;
    }

    public boolean echoZeroRewardClaimed() {
        return echoZeroRewardClaimed;
    }

    public boolean orbitSurveyComplete() {
        return orbitSurveyComplete;
    }

    public boolean moonSurveyComplete() {
        return moonSurveyComplete;
    }

    public boolean marsSurveyComplete() {
        return marsSurveyComplete;
    }

    public boolean europaSurveyComplete() {
        return europaSurveyComplete;
    }

    public boolean nexusStabilized() {
        return nexusStabilized;
    }

    public boolean finalNetworkSealed() {
        return finalNetworkSealed;
    }

    public void resetFinalStateForQa(Player player) {
        echoZeroEncountered = false;
        echoZeroRewardClaimed = false;
        nexusStabilized = false;
        nexusSurveyScans = 0;
        nexusSurveySites = "";
        finalNetworkSealed = false;
        lastTerminalReport = "Final route state reset for QA.";
        save(player);
    }

    public void completeFullArcForQa(Player player) {
        markLaunchPrepared(player);
        markLowOrbitReached(player);
        restoreStationLifeSupport(player);
        recordOrbitSurvey(player, "qa:orbit:1");
        recordOrbitSurvey(player, "qa:orbit:2");
        recordOrbitSurvey(player, "qa:orbit:3");
        markLunarSignalInvestigated(player);
        recordMoonSurvey(player, "qa:moon:1");
        recordMoonSurvey(player, "qa:moon:2");
        recordMoonSurvey(player, "qa:moon:3");
        unlockMarsRoute(player);
        markMarsAshBasinVisited(player);
        recordMarsSurvey(player, "qa:mars:1");
        recordMarsSurvey(player, "qa:mars:2");
        recordMarsSurvey(player, "qa:mars:3");
        unlockEuropaRoute(player);
        markEuropaCryoOceanVisited(player);
        recordEuropaSurvey(player, "qa:europa:1");
        recordEuropaSurvey(player, "qa:europa:2");
        recordEuropaSurvey(player, "qa:europa:3");
        unlockDeepSpaceProtocol(player);
        markAnomalyBeltEntered(player);
        markEchoZeroEncountered(player);
        markEchoZeroRewardClaimed(player);
        recordNexusStabilization(player, "qa:nexus:1");
        recordNexusStabilization(player, "qa:nexus:2");
        recordNexusStabilization(player, "qa:nexus:3");
        if (completedFactionContractCount() == 0) {
            completedFactionContracts = addSite(completedFactionContracts, "qa_final_contract:1");
        }
        sealFinalNetwork(player);
    }

    public boolean stationNetworkRestored() {
        return stationNetworkRestored;
    }

    public boolean lunarExtractorOnline() {
        return lunarExtractorOnline;
    }

    public boolean marsHabitatsPressurized() {
        return marsHabitatsPressurized;
    }

    public boolean europaArrayCalibrated() {
        return europaArrayCalibrated;
    }

    public int stationRelayRepairs() {
        return objectiveCount(stationRelayRepairs, stationRelaySites);
    }

    public int lunarExtractorRepairs() {
        return objectiveCount(lunarExtractorRepairs, lunarExtractorSites);
    }

    public int marsPressureRepairs() {
        return objectiveCount(marsPressureRepairs, marsPressureSites);
    }

    public int europaArrayRepairs() {
        return objectiveCount(europaArrayRepairs, europaArraySites);
    }

    public boolean hasReturnPoint() {
        return hasReturnPoint;
    }

    public boolean hasEarthReturnPoint() {
        return hasEarthReturnPoint;
    }

    public double returnX() {
        return returnX;
    }

    public double returnY() {
        return returnY;
    }

    public double returnZ() {
        return returnZ;
    }

    public String returnDimension() {
        return returnDimension;
    }

    public double earthReturnX() {
        return earthReturnX;
    }

    public double earthReturnY() {
        return earthReturnY;
    }

    public double earthReturnZ() {
        return earthReturnZ;
    }

    public String earthReturnDimension() {
        return earthReturnDimension;
    }

    public int echoMemoryFragments() {
        return echoMemoryFragments;
    }

    public String lastTerminalReport() {
        return lastTerminalReport;
    }

    public FactionStanding orbitalRemnantStanding() {
        return orbitalRemnantStanding;
    }

    public FactionStanding voidSalvagerStanding() {
        return voidSalvagerStanding;
    }

    public FactionStanding nexusChoirStanding() {
        return nexusChoirStanding;
    }

    public String activeFactionContract() {
        return activeFactionContract;
    }

    public String completedFactionContracts() {
        return completedFactionContracts;
    }

    public boolean hasGroundRecoverySites() {
        return !groundRecoverySites().isEmpty();
    }

    public List<GroundRecoverySite> groundRecoverySites() {
        if (groundRecoverySites == null || groundRecoverySites.isBlank()) {
            return List.of();
        }
        List<GroundRecoverySite> sites = new ArrayList<>();
        for (String encoded : groundRecoverySites.split("\\|")) {
            GroundRecoverySite site = GroundRecoverySite.deserialize(encoded);
            if (site != null) {
                sites.add(site);
            }
        }
        return List.copyOf(sites);
    }

    public boolean allGroundRecoverySitesComplete() {
        List<GroundRecoverySite> sites = groundRecoverySites();
        return !sites.isEmpty() && sites.stream().allMatch(GroundRecoverySite::complete);
    }

    public int completedGroundRecoverySites() {
        return (int) groundRecoverySites().stream().filter(GroundRecoverySite::complete).count();
    }

    public int orbitSurveyCount() {
        return surveyCount(orbitSurveyScans, orbitSurveySites);
    }

    public int moonSurveyCount() {
        return surveyCount(moonSurveyScans, moonSurveySites);
    }

    public int marsSurveyCount() {
        return surveyCount(marsSurveyScans, marsSurveySites);
    }

    public int europaSurveyCount() {
        return surveyCount(europaSurveyScans, europaSurveySites);
    }

    public int nexusSurveyCount() {
        return surveyCount(nexusSurveyScans, nexusSurveySites);
    }

    public int totalSurveyCount() {
        return orbitSurveyCount() + moonSurveyCount() + marsSurveyCount() + europaSurveyCount() + nexusSurveyCount();
    }

    public boolean hasTerminalMissionCacheClaimed(String missionId) {
        return hasToken(claimedTerminalMissionCaches, missionId);
    }

    public boolean markTerminalMissionCacheClaimed(Player player, String missionId) {
        if (hasTerminalMissionCacheClaimed(missionId)) {
            return false;
        }
        claimedTerminalMissionCaches = addToken(claimedTerminalMissionCaches, missionId);
        save(player);
        return true;
    }

    public String claimedTerminalMissionCaches() {
        return claimedTerminalMissionCaches;
    }

    private CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("orbital_contact", orbitalContact);
        tag.putBoolean("launch_site_tracked", launchSiteTracked);
        tag.putBoolean("launch_prepared", launchPrepared);
        tag.putBoolean("low_orbit_reached", lowOrbitReached);
        tag.putBoolean("station_coordinates_recovered", stationCoordinatesRecovered);
        tag.putBoolean("station_life_support_restored", stationLifeSupportRestored);
        tag.putBoolean("lunar_signal_unlocked", lunarSignalUnlocked);
        tag.putBoolean("lunar_signal_investigated", lunarSignalInvestigated);
        tag.putBoolean("mars_route_unlocked", marsRouteUnlocked);
        tag.putBoolean("mars_ash_basin_visited", marsAshBasinVisited);
        tag.putBoolean("europa_route_unlocked", europaRouteUnlocked);
        tag.putBoolean("europa_cryo_ocean_visited", europaCryoOceanVisited);
        tag.putBoolean("deep_space_protocol_unlocked", deepSpaceProtocolUnlocked);
        tag.putBoolean("anomaly_belt_entered", anomalyBeltEntered);
        tag.putBoolean("echo_zero_encountered", echoZeroEncountered);
        tag.putBoolean("echo_zero_reward_claimed", echoZeroRewardClaimed);
        tag.putBoolean("final_network_sealed", finalNetworkSealed);
        tag.putDouble("return_x", returnX);
        tag.putDouble("return_y", returnY);
        tag.putDouble("return_z", returnZ);
        tag.putString("return_dimension", returnDimension);
        tag.putBoolean("has_return_point", hasReturnPoint);
        tag.putDouble("earth_return_x", earthReturnX);
        tag.putDouble("earth_return_y", earthReturnY);
        tag.putDouble("earth_return_z", earthReturnZ);
        tag.putString("earth_return_dimension", earthReturnDimension);
        tag.putBoolean("has_earth_return_point", hasEarthReturnPoint);
        tag.putString("orbital_remnant_standing", orbitalRemnantStanding.name());
        tag.putString("void_salvager_standing", voidSalvagerStanding.name());
        tag.putString("nexus_choir_standing", nexusChoirStanding.name());
        tag.putInt("echo_memory_fragments", echoMemoryFragments);
        tag.putString("last_terminal_report", lastTerminalReport);
        tag.putInt("orbit_survey_scans", orbitSurveyScans);
        tag.putInt("moon_survey_scans", moonSurveyScans);
        tag.putInt("mars_survey_scans", marsSurveyScans);
        tag.putInt("europa_survey_scans", europaSurveyScans);
        tag.putInt("nexus_survey_scans", nexusSurveyScans);
        tag.putString("orbit_survey_sites", orbitSurveySites);
        tag.putString("moon_survey_sites", moonSurveySites);
        tag.putString("mars_survey_sites", marsSurveySites);
        tag.putString("europa_survey_sites", europaSurveySites);
        tag.putString("nexus_survey_sites", nexusSurveySites);
        tag.putBoolean("orbit_survey_complete", orbitSurveyComplete);
        tag.putBoolean("moon_survey_complete", moonSurveyComplete);
        tag.putBoolean("mars_survey_complete", marsSurveyComplete);
        tag.putBoolean("europa_survey_complete", europaSurveyComplete);
        tag.putBoolean("nexus_stabilized", nexusStabilized);
        tag.putInt("stationRelayRepairs", stationRelayRepairs);
        tag.putInt("lunarExtractorRepairs", lunarExtractorRepairs);
        tag.putInt("marsPressureRepairs", marsPressureRepairs);
        tag.putInt("europaArrayRepairs", europaArrayRepairs);
        tag.putString("stationRelaySites", stationRelaySites);
        tag.putString("lunarExtractorSites", lunarExtractorSites);
        tag.putString("marsPressureSites", marsPressureSites);
        tag.putString("europaArraySites", europaArraySites);
        tag.putBoolean("stationNetworkRestored", stationNetworkRestored);
        tag.putBoolean("lunarExtractorOnline", lunarExtractorOnline);
        tag.putBoolean("marsHabitatsPressurized", marsHabitatsPressurized);
        tag.putBoolean("europaArrayCalibrated", europaArrayCalibrated);
        tag.putBoolean("midGameObjectivesSeen", true);
        tag.putString("activeFactionContract", activeFactionContract);
        tag.putString("completedFactionContracts", completedFactionContracts);
        tag.putInt("factionContractCooldown", factionContractCooldown);
        tag.putString("ground_recovery_sites", groundRecoverySites);
        tag.putString("claimed_terminal_mission_caches", claimedTerminalMissionCaches);
        return tag;
    }

    private static EchoTerminalProgress read(CompoundTag tag) {
        EchoTerminalProgress progress = new EchoTerminalProgress();
        progress.orbitalContact = tag.getBooleanOr("orbital_contact", false);
        progress.launchSiteTracked = tag.getBooleanOr("launch_site_tracked", false);
        progress.launchPrepared = tag.getBooleanOr("launch_prepared", false);
        progress.lowOrbitReached = tag.getBooleanOr("low_orbit_reached", false);
        progress.stationCoordinatesRecovered = tag.getBooleanOr("station_coordinates_recovered", false);
        progress.stationLifeSupportRestored = tag.getBooleanOr("station_life_support_restored", false);
        progress.lunarSignalUnlocked = tag.getBooleanOr("lunar_signal_unlocked", false);
        progress.lunarSignalInvestigated = tag.getBooleanOr("lunar_signal_investigated", false);
        progress.marsRouteUnlocked = tag.getBooleanOr("mars_route_unlocked", false);
        progress.marsAshBasinVisited = tag.getBooleanOr("mars_ash_basin_visited", false);
        progress.europaRouteUnlocked = tag.getBooleanOr("europa_route_unlocked", false);
        progress.europaCryoOceanVisited = tag.getBooleanOr("europa_cryo_ocean_visited", false);
        progress.deepSpaceProtocolUnlocked = tag.getBooleanOr("deep_space_protocol_unlocked", false);
        progress.anomalyBeltEntered = tag.getBooleanOr("anomaly_belt_entered", false);
        progress.echoZeroEncountered = tag.getBooleanOr("echo_zero_encountered", false);
        progress.echoZeroRewardClaimed = tag.getBooleanOr("echo_zero_reward_claimed", false);
        progress.finalNetworkSealed = tag.getBooleanOr("final_network_sealed", false);
        progress.returnX = tag.getDoubleOr("return_x", 0.0D);
        progress.returnY = tag.getDoubleOr("return_y", 96.0D);
        progress.returnZ = tag.getDoubleOr("return_z", 0.0D);
        progress.returnDimension = tag.getStringOr("return_dimension", "minecraft:overworld");
        progress.hasReturnPoint = tag.getBooleanOr("has_return_point", false);
        progress.earthReturnX = tag.getDoubleOr("earth_return_x", progress.returnX);
        progress.earthReturnY = tag.getDoubleOr("earth_return_y", progress.returnY);
        progress.earthReturnZ = tag.getDoubleOr("earth_return_z", progress.returnZ);
        progress.earthReturnDimension = tag.getStringOr("earth_return_dimension", progress.returnDimension);
        progress.hasEarthReturnPoint = tag.getBooleanOr("has_earth_return_point", progress.hasReturnPoint);
        progress.orbitalRemnantStanding = readStanding(tag.getStringOr("orbital_remnant_standing", FactionStanding.UNKNOWN.name()));
        progress.voidSalvagerStanding = readStanding(tag.getStringOr("void_salvager_standing", FactionStanding.UNKNOWN.name()));
        progress.nexusChoirStanding = readStanding(tag.getStringOr("nexus_choir_standing", FactionStanding.UNKNOWN.name()));
        progress.echoMemoryFragments = tag.getIntOr("echo_memory_fragments", 0);
        progress.lastTerminalReport = tag.getStringOr("last_terminal_report", "Terminal link ready.");
        progress.orbitSurveyScans = tag.getIntOr("orbit_survey_scans", 0);
        progress.moonSurveyScans = tag.getIntOr("moon_survey_scans", 0);
        progress.marsSurveyScans = tag.getIntOr("mars_survey_scans", 0);
        progress.europaSurveyScans = tag.getIntOr("europa_survey_scans", 0);
        progress.nexusSurveyScans = tag.getIntOr("nexus_survey_scans", 0);
        progress.orbitSurveySites = tag.getStringOr("orbit_survey_sites", "");
        progress.moonSurveySites = tag.getStringOr("moon_survey_sites", "");
        progress.marsSurveySites = tag.getStringOr("mars_survey_sites", "");
        progress.europaSurveySites = tag.getStringOr("europa_survey_sites", "");
        progress.nexusSurveySites = tag.getStringOr("nexus_survey_sites", "");
        progress.orbitSurveyComplete = tag.getBooleanOr("orbit_survey_complete", false);
        progress.moonSurveyComplete = tag.getBooleanOr("moon_survey_complete", false);
        progress.marsSurveyComplete = tag.getBooleanOr("mars_survey_complete", false);
        progress.europaSurveyComplete = tag.getBooleanOr("europa_survey_complete", false);
        progress.nexusStabilized = tag.getBooleanOr("nexus_stabilized", false);
        progress.stationRelayRepairs = tag.getIntOr("stationRelayRepairs", 0);
        progress.lunarExtractorRepairs = tag.getIntOr("lunarExtractorRepairs", 0);
        progress.marsPressureRepairs = tag.getIntOr("marsPressureRepairs", 0);
        progress.europaArrayRepairs = tag.getIntOr("europaArrayRepairs", 0);
        progress.stationRelaySites = tag.getStringOr("stationRelaySites", "");
        progress.lunarExtractorSites = tag.getStringOr("lunarExtractorSites", "");
        progress.marsPressureSites = tag.getStringOr("marsPressureSites", "");
        progress.europaArraySites = tag.getStringOr("europaArraySites", "");
        progress.stationNetworkRestored = tag.getBooleanOr("stationNetworkRestored", false);
        progress.lunarExtractorOnline = tag.getBooleanOr("lunarExtractorOnline", false);
        progress.marsHabitatsPressurized = tag.getBooleanOr("marsHabitatsPressurized", false);
        progress.europaArrayCalibrated = tag.getBooleanOr("europaArrayCalibrated", false);
        boolean midGameSeen = tag.getBooleanOr("midGameObjectivesSeen", false);
        if (!midGameSeen) {
            progress.stationNetworkRestored = progress.stationNetworkRestored || progress.lunarSignalInvestigated
                    || progress.marsRouteUnlocked || progress.marsAshBasinVisited || progress.europaRouteUnlocked
                    || progress.europaCryoOceanVisited || progress.deepSpaceProtocolUnlocked || progress.anomalyBeltEntered
                    || progress.echoZeroEncountered;
            progress.lunarExtractorOnline = progress.lunarExtractorOnline || progress.marsRouteUnlocked
                    || progress.marsAshBasinVisited || progress.europaRouteUnlocked || progress.europaCryoOceanVisited
                    || progress.deepSpaceProtocolUnlocked || progress.anomalyBeltEntered || progress.echoZeroEncountered;
            progress.marsHabitatsPressurized = progress.marsHabitatsPressurized || progress.europaRouteUnlocked
                    || progress.europaCryoOceanVisited || progress.deepSpaceProtocolUnlocked || progress.anomalyBeltEntered
                    || progress.echoZeroEncountered;
            progress.europaArrayCalibrated = progress.europaArrayCalibrated || progress.deepSpaceProtocolUnlocked
                    || progress.anomalyBeltEntered || progress.echoZeroEncountered;
        }
        progress.midGameObjectivesSeen = true;
        progress.activeFactionContract = tag.getStringOr("activeFactionContract", "");
        progress.completedFactionContracts = tag.getStringOr("completedFactionContracts", "");
        progress.factionContractCooldown = tag.getIntOr("factionContractCooldown", 0);
        progress.groundRecoverySites = tag.getStringOr("ground_recovery_sites", "");
        progress.claimedTerminalMissionCaches = tag.getStringOr("claimed_terminal_mission_caches", "");
        return progress;
    }

    private FactionPledgeItem.Faction preferredAlignedFaction() {
        if (orbitalRemnantStanding == FactionStanding.ALIGNED) {
            return FactionPledgeItem.Faction.ORBITAL_REMNANT;
        }
        if (voidSalvagerStanding == FactionStanding.ALIGNED) {
            return FactionPledgeItem.Faction.VOID_SALVAGERS;
        }
        if (nexusChoirStanding == FactionStanding.ALIGNED) {
            return FactionPledgeItem.Faction.NEXUS_CHOIR;
        }
        return null;
    }

    private static String contractId(FactionPledgeItem.Faction faction) {
        return switch (faction) {
            case ORBITAL_REMNANT -> "orbital_remnant_relay";
            case VOID_SALVAGERS -> "void_salvager_manifest";
            case NEXUS_CHOIR -> "nexus_choir_anchor";
        };
    }

    private static FactionPledgeItem.Faction factionFromContract(String contract) {
        return switch (contract) {
            case "orbital_remnant_relay" -> FactionPledgeItem.Faction.ORBITAL_REMNANT;
            case "void_salvager_manifest" -> FactionPledgeItem.Faction.VOID_SALVAGERS;
            case "nexus_choir_anchor" -> FactionPledgeItem.Faction.NEXUS_CHOIR;
            default -> null;
        };
    }

    private static String contractDisplay(String contract) {
        String id = contract == null ? "" : contract.split(":", 2)[0];
        return switch (id) {
            case "orbital_remnant_relay" -> "Orbital Remnant Relay Survey";
            case "void_salvager_manifest" -> "Void Salvager Manifest";
            case "nexus_choir_anchor" -> "Nexus Choir Anchor Reading";
            default -> "Faction Contract";
        };
    }

    private static String contractRequirement(String contract) {
        return switch (contract) {
            case "orbital_remnant_relay" -> "Scan a Low Orbit Signal Relay or carry Orbit Survey Data.";
            case "void_salvager_manifest" -> "Scan orbital salvage or turn in 1 Orbital Alloy and 1 Vacuum Circuit.";
            case "nexus_choir_anchor" -> "After ECHO-0, scan a Nexus Anchor/Growth or spend 1 Nexus Stabilizer Shard.";
            default -> "No active faction contract.";
        };
    }

    private static String surveyText(boolean complete, int count) {
        return complete ? "3/3" : Math.min(3, count) + "/3";
    }

    private static String surveyLine(String name, boolean complete, int legacyCount, String sites, String hook, String reward) {
        int count = surveyCount(legacyCount, sites);
        return name + " " + surveyText(complete, count) + " | " + hook + " | " + reward;
    }

    private static String objectiveLine(String name, boolean complete, int legacyCount, String sites, String hook, String reward) {
        int count = objectiveCount(legacyCount, sites);
        String state = complete && count < 3 ? "BYPASS" : surveyText(complete, count);
        return "Route " + name + " " + state + " | " + hook + " | " + reward;
    }

    private static String missingSummary(LaunchReadiness readiness) {
        List<String> missing = readiness.missing().stream()
                .limit(3)
                .map(component -> component.getString().replaceFirst("^- ", ""))
                .toList();
        if (missing.isEmpty()) {
            return "none";
        }
        String summary = String.join(", ", missing);
        if (readiness.missing().size() > missing.size()) {
            summary += ", +" + (readiness.missing().size() - missing.size()) + " more";
        }
        return summary;
    }

    private static int surveyCount(int legacyCount, String sites) {
        return Math.min(3, Math.max(legacyCount, siteCount(sites)));
    }

    private static int objectiveCount(int legacyCount, String sites) {
        return Math.min(3, Math.max(legacyCount, siteCount(sites)));
    }

    private static int siteCount(String sites) {
        if (sites == null || sites.isBlank()) {
            return 0;
        }
        return sites.split("\\|", -1).length;
    }

    private static boolean hasSite(String sites, String siteId) {
        if (siteId == null || siteId.isBlank() || sites == null || sites.isBlank()) {
            return false;
        }
        for (String site : sites.split("\\|")) {
            if (site.equals(siteId)) {
                return true;
            }
        }
        return false;
    }

    private static String addSite(String sites, String siteId) {
        if (siteId == null || siteId.isBlank()) {
            siteId = "unknown";
        }
        if (sites == null || sites.isBlank()) {
            return siteId;
        }
        return sites + "|" + siteId;
    }

    private static boolean hasToken(String tokens, String token) {
        if (token == null || token.isBlank() || tokens == null || tokens.isBlank()) {
            return false;
        }
        for (String value : tokens.split(";")) {
            if (value.equals(token)) {
                return true;
            }
        }
        return false;
    }

    private static String addToken(String tokens, String token) {
        if (token == null || token.isBlank()) {
            token = "unknown";
        }
        if (tokens == null || tokens.isBlank()) {
            return token;
        }
        return tokens + ";" + token;
    }

    private static void grantSurveyAdvancement(Player player, net.minecraft.resources.Identifier advancement) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantManual(serverPlayer, advancement, "complete");
        }
    }

    private static void giveOrDrop(Player player, net.minecraft.world.item.ItemStack stack) {
        if (!player.getInventory().add(stack.copy())) {
            player.drop(stack.copy(), false);
        }
    }

    private void grantMidGameMastery(Player player) {
        if (allMidGameObjectivesComplete() && player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.grantManual(serverPlayer, ModAdvancements.MID_GAME_ROUTE_MASTERY, "complete");
        }
    }

    private static boolean midGameObjectivesRequired() {
        try {
            return Config.MID_GAME_OBJECTIVES_ENABLED.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    public record SurveyResult(String name, int count, int target, boolean newlyComplete, boolean duplicate, boolean locked, boolean counted) {
        public static SurveyResult duplicate(String name, int count, int target) {
            return new SurveyResult(name, count, target, false, true, false, false);
        }

        public static SurveyResult complete(String name, int count, int target) {
            return new SurveyResult(name, target, target, false, false, false, false);
        }

        public String report(String completeMessage) {
            if (newlyComplete) {
                return completeMessage;
            }
            if (locked) {
                return name + ". Resolve the prerequisite before this scan can count.";
            }
            if (duplicate) {
                return name + " already logged at this site. Find another landmark or spend a survey item.";
            }
            if (count >= target) {
                return name + " already complete.";
            }
            return name + " logged: " + count + "/" + target + " landmarks mapped.";
        }
    }

    public record RouteObjectiveResult(String name, int count, int target, boolean newlyComplete, boolean duplicate, boolean counted) {
        public static RouteObjectiveResult duplicate(String name, int count, int target) {
            return new RouteObjectiveResult(name, count, target, false, true, false);
        }

        public static RouteObjectiveResult complete(String name, int count, int target) {
            return new RouteObjectiveResult(name, target, target, false, false, false);
        }

        public String report(String completeMessage) {
            if (newlyComplete) {
                return completeMessage;
            }
            if (duplicate) {
                return name + " already repaired at this site. Find another generated route site.";
            }
            if (count >= target) {
                return name + " already complete.";
            }
            return name + " repaired: " + count + "/" + target + " route sites online.";
        }
    }

    public record ContractResult(boolean completed, String name, int completedCount, boolean noPledge, boolean blocked) {
        public static ContractResult noPledgeResult() {
            return new ContractResult(false, "Faction Contract", 0, true, false);
        }
    }

    private static FactionStanding readStanding(String name) {
        try {
            return FactionStanding.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return FactionStanding.UNKNOWN;
        }
    }

    private static FactionStanding maxStanding(FactionStanding current, FactionStanding candidate) {
        return current.ordinal() >= candidate.ordinal() ? current : candidate;
    }

    private static String encodeGroundSites(List<GroundRecoverySite> sites) {
        return String.join("|", sites.stream().map(GroundRecoverySite::serialize).toList());
    }

    private GroundRecoverySite nextIncompleteGroundSite() {
        return groundRecoverySites().stream().filter(site -> !site.complete()).findFirst().orElse(null);
    }

    private GroundRecoverySite nearestIncompleteGroundSite(Player player) {
        return groundRecoverySites().stream()
                .filter(site -> !site.complete())
                .min(java.util.Comparator.comparingInt(site -> site.distanceTo(player)))
                .orElse(null);
    }

    private String groundRecoverySummary() {
        GroundRecoverySite next = nextIncompleteGroundSite();
        if (next == null) {
            return "Earth recovery complete.";
        }
        return "Next landmark: " + next.type().displayName() + " (" + next.type().rewardRole() + ").";
    }

    public record GroundSiteScanResult(boolean completed, String report) {
    }
}
