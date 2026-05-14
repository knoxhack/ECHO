package com.knoxhack.echocore.api;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.api.network.EchoDiscoveryToast;
import com.knoxhack.echocore.api.network.INetworkBridge;
import com.knoxhack.echocore.api.network.INetworkService;
import com.knoxhack.echocore.api.network.NoOpNetworkService;
import com.knoxhack.echocore.api.index.IIndexEntryProvider;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IIndexService;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.NoOpIndexService;
import com.knoxhack.echocore.api.mission.IMissionRegistry;
import com.knoxhack.echocore.api.mission.IMissionService;
import com.knoxhack.echocore.api.mission.MissionContentRegistrar;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echocore.api.mission.NoOpMissionService;
import com.knoxhack.echocore.discovery.EchoDiscoveryData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

/**
 * Convenience accessors around optional cross-mod ECHO services.
 */
public final class EchoCoreServices {
    private static final EchoProfileService DEFAULT_PROFILE_SERVICE = new PersistentProfileService();
    private static final INetworkService DEFAULT_NETWORK_SERVICE = NoOpNetworkService.INSTANCE;
    private static final IMissionService DEFAULT_MISSION_SERVICE = NoOpMissionService.INSTANCE;
    private static final IIndexService DEFAULT_INDEX_SERVICE = NoOpIndexService.INSTANCE;
    private static final List<EchoRouteRecordService> ROUTE_RECORD_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<EchoDiagnosticService> DIAGNOSTIC_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<EchoHazardTelemetryService> HAZARD_TELEMETRY_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<EchoRecoveryService> RECOVERY_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<EchoFactionStandingService> FACTION_STANDING_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<EchoFactionActionHandlerService> FACTION_ACTION_SERVICES = new CopyOnWriteArrayList<>();
    private static final List<IMapDataProvider> MAP_DATA_PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<IIndexEntryProvider> INDEX_ENTRY_PROVIDERS = new CopyOnWriteArrayList<>();
    private static final List<IIndexRecipeProvider> INDEX_RECIPE_PROVIDERS = new CopyOnWriteArrayList<>();
    private static final Map<String, List<MissionContentRegistrar>> MISSION_CONTENT_REGISTRARS = new LinkedHashMap<>();
    private static final List<ExpectedEchoModule> ECHO_MODULE_CATALOG = List.of(
            new ExpectedEchoModule("echoashfallprotocol", "ECHO: Ashfall Protocol", "root",
                    "Main Ashfall campaign addon."),
            new ExpectedEchoModule("echocore", "ECHO: Core", "core/echocore",
                    "Shared services, profiles, diagnostics, hazards, route records, rewards, factions, and mirrors."),
            new ExpectedEchoModule("echonetcore", "ECHO: NetCore", "addons/echonetcore",
                    "Shared packet registration, sync, action validation, and debug network contracts."),
            new ExpectedEchoModule("echodatacore", "ECHO: DataCore", "addons/echodatacore",
                    "Shared persistent player, world, and team progression data."),
            new ExpectedEchoModule("echomissioncore", "ECHO: MissionCore", "addons/echomissioncore",
                    "Shared mission, objective, reward, and Terminal feed engine."),
            new ExpectedEchoModule("echoworldcore", "ECHO: WorldCore", "addons/echoworldcore",
                    "Shared world regions, markers, hazards, discoveries, and world event contracts."),
            new ExpectedEchoModule("echoterminal", "ECHO: Terminal", "addons/echoterminal",
                    "Player-facing terminal shell, mission graph, archives, rewards, route records, and recipe index."),
            new ExpectedEchoModule("signalos", "SignalOS", "addons/echosignalos",
                    "Reusable chapter, mission, archive, reward, diagnostics, JSON, and KubeJS-friendly framework."),
            new ExpectedEchoModule("signalosexample", "SignalOS Example Addon", "addons/signalosexample",
                    "Example-only SignalOS integration addon."),
            new ExpectedEchoModule("echoorbitalremnants", "ECHO: Orbital Remnants", "addons/echoorbitalremnants",
                    "Post-Nexus orbital route continuation."),
            new ExpectedEchoModule("echonexusprotocol", "ECHO: Nexus Protocol", "addons/echonexusprotocol",
                    "Nexus corruption and memory escalation chapter."),
            new ExpectedEchoModule("echoagriculturereclamation", "ECHO: Agriculture Reclamation",
                    "addons/echoagriculturereclamation", "Ecology, agriculture, hydroponics, and restoration chapter."),
            new ExpectedEchoModule("echostationfall", "ECHO: Stationfall", "addons/echostationfall",
                    "Station ECHO horror chapter."),
            new ExpectedEchoModule("echoblackboxprotocol", "ECHO: Blackbox Protocol",
                    "addons/echoblackboxprotocol", "Late-game memory finale."),
            new ExpectedEchoModule("echoindustrialnexus", "ECHO: Industrial Nexus",
                    "addons/echoindustrialnexus", "Machines, Thermal Flux, salvage processing, ducts, and filters."),
            new ExpectedEchoModule("echologisticsnetwork", "ECHO: Logistics Network",
                    "addons/echologisticsnetwork", "Storage, loadouts, remote requests, courier delivery, and depots."),
            new ExpectedEchoModule("echorendercore", "ECHO: RenderCore",
                    "addons/echorendercore", "Shared rendering, animation, overlays, particles, previews, and profile composition."),
            new ExpectedEchoModule("echoconvoyprotocol", "ECHO: Convoy Protocol",
                    "addons/echoconvoyprotocol", "Vehicles, fuel, cargo, checkpoint gates, and travel routes."),
            new ExpectedEchoModule("echoholomap", "ECHO: HoloMap",
                    "addons/echoholomap", "Terminal-integrated command map, telemetry layers, and marker registry."),
            new ExpectedEchoModule("echoindex", "ECHO: Index",
                    "addons/echoindex", "Shared item, recipe, usage, discovery, and archive browser."),
            new ExpectedEchoModule("echoarmory", "ECHO: Armory", "addons/echoarmory",
                    "Weapons, armor, modules, energy recharge, faction locks, and loadout hooks."),
            new ExpectedEchoModule("echolens", "ECHO: Lens", "addons/echolens",
                    "Smart scanner HUD with local inspection, server-assisted Deep Scan, and addon context."),
            new ExpectedEchoModule("echoblockworks", "ECHO Blockworks", "addons/echoblockworks",
                    "First-party decorative, structural, and themed block library for ECHO structures."));
    public static final Identifier ACCEPT_FACTION_CONTRACT_ACTION =
            Identifier.fromNamespaceAndPath(EchoCore.MODID, "accept_contract");
    public static final Identifier COMPLETE_FACTION_CONTRACT_ACTION =
            Identifier.fromNamespaceAndPath(EchoCore.MODID, "complete_contract");

    private EchoCoreServices() {
    }

    public static void registerMissionService(IMissionService service) {
        if (service == null) {
            return;
        }
        EchoServiceRegistry.register(IMissionService.class, service);
        replayMissionContent(service);
    }

    public static IMissionService missionService() {
        return EchoServiceRegistry.getOrDefault(IMissionService.class, DEFAULT_MISSION_SERVICE);
    }

    public static boolean missionCoreAvailable() {
        try {
            return missionService().available();
        } catch (RuntimeException exception) {
            warnProviderFailure("mission service availability", missionService(), exception);
            return false;
        }
    }

    public static void registerMissionContent(String source, MissionContentRegistrar registrar) {
        if (registrar == null) {
            return;
        }
        String safeSource = source == null || source.isBlank() ? "unknown" : source;
        synchronized (MISSION_CONTENT_REGISTRARS) {
            MISSION_CONTENT_REGISTRARS.computeIfAbsent(safeSource, ignored -> new ArrayList<>()).add(registrar);
        }
        IMissionService service = missionService();
        if (service.available()) {
            applyMissionRegistrar(service, safeSource, registrar);
        }
    }

    public static void replayMissionContent(IMissionRegistry registry) {
        if (registry == null) {
            return;
        }
        List<Map.Entry<String, List<MissionContentRegistrar>>> entries;
        synchronized (MISSION_CONTENT_REGISTRARS) {
            entries = MISSION_CONTENT_REGISTRARS.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), List.copyOf(entry.getValue())))
                    .toList();
        }
        for (Map.Entry<String, List<MissionContentRegistrar>> entry : entries) {
            for (MissionContentRegistrar registrar : entry.getValue()) {
                applyMissionRegistrar(registry, entry.getKey(), registrar);
            }
        }
    }

    public static boolean startMission(ServerPlayer player, Identifier missionId) {
        try {
            return player != null && missionId != null && missionService().startMission(player, missionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission start", missionService(), exception);
            return false;
        }
    }

    public static boolean completeMission(ServerPlayer player, Identifier missionId) {
        try {
            return player != null && missionId != null && missionService().completeMission(player, missionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission completion", missionService(), exception);
            return false;
        }
    }

    public static boolean claimMissionReward(ServerPlayer player, Identifier missionId) {
        try {
            return player != null && missionId != null && missionService().claimReward(player, missionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission reward claim", missionService(), exception);
            return false;
        }
    }

    public static boolean handleMissionAction(ServerPlayer player, Identifier missionId, String actionId) {
        try {
            return player != null && missionId != null && missionService().handleAction(player, missionId, actionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission action", missionService(), exception);
            return false;
        }
    }

    public static boolean recordMissionObjective(
            ServerPlayer player,
            MissionObjectiveType type,
            Identifier target,
            int amount,
            Map<String, String> context) {
        try {
            return player != null && missionService().recordObjective(player, type, target, amount, context);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission objective", missionService(), exception);
            return false;
        }
    }

    public static boolean recordMissionObjective(
            ServerPlayer player,
            MissionObjectiveType type,
            Identifier target,
            int amount) {
        return recordMissionObjective(player, type, target, amount, Map.of());
    }

    public static void registerMissionHookCoverage(String source, Identifier missionId, Identifier objectiveTarget) {
        if (missionId == null || objectiveTarget == null) {
            return;
        }
        try {
            missionService().registerHookCoverage(source, missionId, objectiveTarget);
        } catch (RuntimeException exception) {
            warnProviderFailure("mission hook coverage", missionService(), exception);
        }
    }

    public static Map<String, String> missionHookCoverageSummary() {
        try {
            Map<String, String> coverage = missionService().missionHookCoverageBySource();
            return coverage == null ? Map.of() : coverage;
        } catch (RuntimeException exception) {
            warnProviderFailure("mission hook coverage summary", missionService(), exception);
            return Map.of();
        }
    }

    private static void applyMissionRegistrar(IMissionRegistry registry, String source, MissionContentRegistrar registrar) {
        try {
            registrar.register(registry);
        } catch (RuntimeException exception) {
            EchoCore.LOGGER.warn("ECHO mission content registrar '{}' failed; continuing without its output.", source, exception);
        }
    }

    public static void registerNetworkService(INetworkService service) {
        EchoServiceRegistry.register(INetworkService.class, service);
    }

    public static INetworkService networkService() {
        return EchoServiceRegistry.getOrDefault(INetworkService.class, DEFAULT_NETWORK_SERVICE);
    }

    public static INetworkBridge networkBridge() {
        INetworkBridge bridge = networkService().bridge();
        return bridge == null ? INetworkBridge.NOOP : bridge;
    }

    public static void registerDataService(IDataService service) {
        EchoServiceRegistry.register(IDataService.class, service);
    }

    public static IDataService dataService() {
        return EchoServiceRegistry.getOrDefault(IDataService.class, NoOpDataService.INSTANCE);
    }

    public static <T> IDataKey<T> registerDataKey(IDataKey<T> key) {
        try {
            return dataService().registerKey(key);
        } catch (RuntimeException exception) {
            warnProviderFailure("data key registration", dataService(), exception);
            return NoOpDataService.INSTANCE.registerKey(key);
        }
    }

    public static IPlayerDataView playerData(Player player) {
        IDataService service = dataService();
        try {
            IPlayerDataView view = service.player(player);
            return view == null ? NoOpDataService.INSTANCE.player(player) : view;
        } catch (RuntimeException exception) {
            warnProviderFailure("player data", service, exception);
            return NoOpDataService.INSTANCE.player(player);
        }
    }

    public static IWorldDataView worldData(Level level) {
        IDataService service = dataService();
        try {
            IWorldDataView view = service.world(level);
            return view == null ? NoOpDataService.INSTANCE.world(level) : view;
        } catch (RuntimeException exception) {
            warnProviderFailure("world data", service, exception);
            return NoOpDataService.INSTANCE.world(level);
        }
    }

    public static ITeamDataView teamData(Level level, Identifier teamId) {
        IDataService service = dataService();
        try {
            ITeamDataView view = service.team(level, teamId);
            return view == null ? NoOpDataService.INSTANCE.team(level, teamId) : view;
        } catch (RuntimeException exception) {
            warnProviderFailure("team data", service, exception);
            return NoOpDataService.INSTANCE.team(level, teamId);
        }
    }

    public static IDataSyncBridge dataSyncBridge() {
        IDataService service = dataService();
        try {
            IDataSyncBridge bridge = service.syncBridge();
            return bridge == null ? IDataSyncBridge.NOOP : bridge;
        } catch (RuntimeException exception) {
            warnProviderFailure("data sync bridge", service, exception);
            return IDataSyncBridge.NOOP;
        }
    }

    public static void registerIndexService(IIndexService service) {
        IIndexService safe = service == null ? NoOpIndexService.INSTANCE : service;
        EchoServiceRegistry.register(IIndexService.class, safe);
        for (IIndexEntryProvider provider : INDEX_ENTRY_PROVIDERS) {
            safeRegisterIndexProvider(safe, provider);
        }
        for (IIndexRecipeProvider provider : INDEX_RECIPE_PROVIDERS) {
            safeRegisterIndexRecipeProvider(safe, provider);
        }
    }

    public static IIndexService indexService() {
        return EchoServiceRegistry.getOrDefault(IIndexService.class, DEFAULT_INDEX_SERVICE);
    }

    public static boolean indexAvailable() {
        try {
            return indexService().available();
        } catch (RuntimeException exception) {
            warnProviderFailure("index service availability", indexService(), exception);
            return false;
        }
    }

    public static void invalidateIndexRecipes(String reason) {
        IIndexService service = indexService();
        try {
            service.recipes().invalidateRecipes(reason);
        } catch (RuntimeException exception) {
            warnProviderFailure("index recipe invalidation", service, exception);
        }
    }

    public static void registerIndexProvider(IIndexEntryProvider provider) {
        if (provider == null) {
            return;
        }
        Identifier providerId = safeIndexProviderId(provider);
        if (providerId == null) {
            warnInvalidProviderOutput("index entry", provider, "provider id is null");
            return;
        }
        for (IIndexEntryProvider existing : INDEX_ENTRY_PROVIDERS) {
            Identifier existingId = safeIndexProviderId(existing);
            if (providerId.equals(existingId)) {
                if (existing != provider) {
                    warnInvalidProviderOutput("index entry", provider,
                            "duplicate provider id " + providerId + " ignored");
                }
                return;
            }
        }
        INDEX_ENTRY_PROVIDERS.add(provider);
        safeRegisterIndexProvider(indexService(), provider);
    }

    public static void registerIndexRecipeProvider(IIndexRecipeProvider provider) {
        if (provider == null) {
            return;
        }
        Identifier providerId = safeIndexRecipeProviderId(provider);
        if (providerId == null) {
            warnInvalidProviderOutput("index recipe", provider, "provider id is null");
            return;
        }
        for (IIndexRecipeProvider existing : INDEX_RECIPE_PROVIDERS) {
            Identifier existingId = safeIndexRecipeProviderId(existing);
            if (providerId.equals(existingId)) {
                if (existing != provider) {
                    warnInvalidProviderOutput("index recipe", provider,
                            "duplicate provider id " + providerId + " ignored");
                }
                return;
            }
        }
        INDEX_RECIPE_PROVIDERS.add(provider);
        safeRegisterIndexRecipeProvider(indexService(), provider);
    }

    public static List<IndexCategory> indexCategories(Player player) {
        IIndexService service = indexService();
        try {
            List<IndexCategory> categories = service.registry().categories(player);
            return categories == null ? List.of() : categories;
        } catch (RuntimeException exception) {
            warnProviderFailure("index categories", service, exception);
            return List.of();
        }
    }

    public static List<IndexEntry> indexEntries(Player player) {
        IIndexService service = indexService();
        try {
            List<IndexEntry> entries = service.registry().entries(player);
            return entries == null ? List.of() : entries;
        } catch (RuntimeException exception) {
            warnProviderFailure("index entries", service, exception);
            return List.of();
        }
    }

    public static void registerPackModeService(EchoPackModeService service) {
        EchoServiceRegistry.register(EchoPackModeService.class, service);
    }

    public static EchoPackMode packMode(Player player) {
        EchoPackModeService service = EchoServiceRegistry.getOrDefault(EchoPackModeService.class, EchoCoreServices::detectPackMode);
        try {
            EchoPackMode mode = service.packMode(player);
            return mode == null ? EchoPackMode.UNKNOWN : mode;
        } catch (RuntimeException exception) {
            warnProviderFailure("pack mode", service, exception);
            return detectPackMode(player);
        }
    }

    public static List<EchoChapterCapability> chapterCapabilities(Player player) {
        ModList modList = ModList.get();
        return List.of(
                chapterCapability(player, modList, "ashfall_protocol", "echoashfallprotocol", "Ashfall Protocol"),
                chapterCapability(player, modList, "orbital_remnants", "echoorbitalremnants", "Orbital Remnants"),
                chapterCapability(player, modList, "stationfall", "echostationfall", "Stationfall"),
                chapterCapability(player, modList, "nexus_protocol", "echonexusprotocol", "Nexus Protocol"),
                chapterCapability(player, modList, "blackbox_protocol", "echoblackboxprotocol", "Blackbox Protocol"),
                chapterCapability(player, modList, "agriculture_reclamation", "echoagriculturereclamation", "Agriculture Reclamation"),
                chapterCapability(player, modList, "industrial_nexus", "echoindustrialnexus", "Industrial Nexus"),
                chapterCapability(player, modList, "logistics_network", "echologisticsnetwork", "Logistics Network"),
                chapterCapability(player, modList, "convoy_protocol", "echoconvoyprotocol", "Convoy Protocol"),
                chapterCapability(player, modList, "armory", "echoarmory", "Armory"),
                chapterCapability(player, modList, "lens", "echolens", "Lens"),
                chapterCapability(player, modList, "blockworks", "echoblockworks", "Blockworks"));
    }

    public static List<EchoModuleInfo> moduleReport() {
        ModList modList = ModList.get();
        return ECHO_MODULE_CATALOG.stream()
                .map(module -> runtimeModuleInfo(modList, module))
                .toList();
    }

    public static String moduleReportSummary() {
        List<EchoModuleInfo> modules = moduleReport();
        long loaded = modules.stream().filter(EchoModuleInfo::loaded).count();
        String loadedModules = modules.stream()
                .filter(EchoModuleInfo::loaded)
                .map(module -> module.modId() + ":" + (module.version().isBlank() ? "unknown" : module.version()))
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
        return "modules=" + loaded + "/" + modules.size() + " [" + loadedModules + "]";
    }

    public static void registerProfileService(EchoProfileService service) {
        EchoServiceRegistry.register(EchoProfileService.class, service);
    }

    public static EchoProfile profile(Player player) {
        EchoProfileService service = profileService();
        try {
            EchoProfile profile = service.profile(player);
            return profile == null ? EchoProfile.empty() : profile;
        } catch (RuntimeException exception) {
            warnProviderFailure("profile", service, exception);
            return EchoProfile.empty();
        }
    }

    public static void saveProfile(ServerPlayer player, EchoProfile profile) {
        EchoProfileService service = profileService();
        try {
            service.saveProfile(player, profile == null ? EchoProfile.empty() : profile);
        } catch (RuntimeException exception) {
            warnProviderFailure("profile save", service, exception);
        }
    }

    public static void unlockArchive(ServerPlayer player, String recordId) {
        if (player == null || recordId == null || recordId.isBlank()) {
            return;
        }
        saveProfile(player, profile(player).discoverRecord(recordId));
    }

    public static boolean isArchiveUnlocked(Player player, String recordId) {
        return player != null && profile(player).hasDiscoveredRecord(recordId);
    }

    public static EchoProgressLedger progressLedger(Player player) {
        EchoProfileService service = profileService();
        try {
            EchoProgressLedger ledger = service.progressLedger(player);
            return ledger == null ? EchoProgressLedger.empty() : ledger;
        } catch (RuntimeException exception) {
            warnProviderFailure("progress ledger", service, exception);
            return EchoProgressLedger.empty();
        }
    }

    public static void saveProgressLedger(ServerPlayer player, EchoProgressLedger ledger) {
        EchoProfileService service = profileService();
        try {
            service.saveProgressLedger(player, ledger == null ? EchoProgressLedger.empty() : ledger);
        } catch (RuntimeException exception) {
            warnProviderFailure("progress ledger save", service, exception);
        }
    }

    public static void recordMilestone(ServerPlayer player, String milestoneId) {
        if (player == null || milestoneId == null || milestoneId.isBlank()) {
            return;
        }
        saveProgressLedger(player, progressLedger(player).withMilestone(milestoneId));
        discoverVisibleRouteRecords(player);
    }

    public static void registerDiscoveryProvider(EchoDiscoveryProvider provider) {
        EchoDiscoveryRegistry.register(provider);
    }

    public static List<EchoDiscoveryEntry> discoveryEntries(Player player) {
        return EchoDiscoveryRegistry.entries(player);
    }

    public static Optional<EchoDiscoveryEntry> discoveryEntry(Player player, Identifier id) {
        return EchoDiscoveryRegistry.entry(player, id);
    }

    public static EchoDiscoveryState discoveryState(Player player, EchoDiscoveryEntry entry) {
        if (entry == null) {
            return EchoDiscoveryState.LOCKED;
        }
        EchoDiscoveryState providerState = EchoDiscoveryRegistry.state(player, entry);
        if (providerState == EchoDiscoveryState.CHECKED) {
            return EchoDiscoveryState.CHECKED;
        }
        if (player != null && EchoDiscoveryData.get(player).contains(entry.id())) {
            return EchoDiscoveryState.DISCOVERED;
        }
        return providerState;
    }

    public static boolean hasDiscoveredFeature(Player player, Identifier id) {
        return player != null && id != null && EchoDiscoveryData.get(player).contains(id);
    }

    public static boolean discoverFeature(ServerPlayer player, Identifier id) {
        if (player == null || id == null) {
            return false;
        }
        Optional<EchoDiscoveryEntry> entry = EchoDiscoveryRegistry.entry(player, id);
        if (entry.isEmpty()) {
            return false;
        }
        EchoDiscoveryData data = EchoDiscoveryData.get(player);
        if (!data.discover(id)) {
            return false;
        }
        EchoDiscoveryData.saveAndSync(player, data);
        networkBridge().sendDiscoveryToast(player, new EchoDiscoveryToast(entry.get()));
        return true;
    }

    public static boolean discoverFeature(ServerPlayer player, String id) {
        return discoverFeature(player, Identifier.tryParse(id == null ? "" : id));
    }

    public static Identifier routeDiscoveryId(Identifier routeRecordId) {
        if (routeRecordId == null) {
            return Identifier.fromNamespaceAndPath(EchoCore.MODID, "route/unknown");
        }
        return Identifier.fromNamespaceAndPath(routeRecordId.getNamespace(), "route/" + routeRecordId.getPath());
    }

    public static int discoverVisibleRouteRecords(ServerPlayer player) {
        if (player == null) {
            return 0;
        }
        int discovered = 0;
        for (EchoRouteRecord record : routeRecords(player)) {
            if (record == null || !routeRecordVisible(record)) {
                continue;
            }
            if (discoverFeature(player, routeDiscoveryId(record.id()))) {
                discovered++;
            }
        }
        return discovered;
    }

    public static void registerWorldRegionService(IWorldRegionService service) {
        IWorldRegionService safe = service == null ? NoOpWorldService.INSTANCE : service;
        EchoServiceRegistry.register(IWorldRegionService.class, safe);
        EchoServiceRegistry.register(IRegionService.class, safe);
        EchoServiceRegistry.register(IHazardService.class, safe);
        EchoServiceRegistry.register(IWorldMarkerService.class, safe);
        EchoServiceRegistry.register(IStructureDiscoveryService.class, safe);
    }

    public static IWorldRegionService worldRegions() {
        return EchoServiceRegistry.getOrDefault(IWorldRegionService.class, NoOpWorldService.INSTANCE);
    }

    public static IRegionService regionService() {
        return EchoServiceRegistry.getOrDefault(IRegionService.class, worldRegions());
    }

    public static IHazardService hazardService() {
        return EchoServiceRegistry.getOrDefault(IHazardService.class, worldRegions());
    }

    public static IWorldMarkerService worldMarkerService() {
        return EchoServiceRegistry.getOrDefault(IWorldMarkerService.class, worldRegions());
    }

    public static IStructureDiscoveryService structureDiscoveryService() {
        return EchoServiceRegistry.getOrDefault(IStructureDiscoveryService.class, worldRegions());
    }

    public static void registerMapMarkerService(IMapMarkerService service) {
        IMapMarkerService safe = service == null ? NoOpMapService.INSTANCE : service;
        EchoServiceRegistry.register(IMapMarkerService.class, safe);
        for (IMapDataProvider provider : MAP_DATA_PROVIDERS) {
            safeRegisterMapProvider(safe, provider);
        }
    }

    public static IMapMarkerService mapMarkerService() {
        return EchoServiceRegistry.getOrDefault(IMapMarkerService.class, NoOpMapService.INSTANCE);
    }

    public static void registerMapDataProvider(IMapDataProvider provider) {
        if (provider == null) {
            return;
        }
        Identifier providerId = safeMapProviderId(provider);
        if (providerId == null) {
            warnInvalidProviderOutput("map data", provider, "provider id is null");
            return;
        }
        for (IMapDataProvider existing : MAP_DATA_PROVIDERS) {
            Identifier existingId = safeMapProviderId(existing);
            if (providerId.equals(existingId)) {
                if (existing != provider) {
                    warnInvalidProviderOutput("map data", provider,
                            "duplicate provider id " + providerId + " ignored");
                }
                return;
            }
        }
        MAP_DATA_PROVIDERS.add(provider);
        safeRegisterMapProvider(mapMarkerService(), provider);
    }

    public static List<IMapLayer> mapLayers(Player player) {
        IMapMarkerService service = mapMarkerService();
        try {
            List<IMapLayer> layers = service.layers(player);
            return layers == null ? List.of() : layers;
        } catch (RuntimeException exception) {
            warnProviderFailure("map layer", service, exception);
            return List.of();
        }
    }

    public static List<IMapMarker> mapMarkers(Player player) {
        IMapMarkerService service = mapMarkerService();
        try {
            List<IMapMarker> markers = service.markers(player);
            return markers == null ? List.of() : markers;
        } catch (RuntimeException exception) {
            warnProviderFailure("map marker", service, exception);
            return List.of();
        }
    }

    public static boolean refreshMapMarkers(ServerPlayer player, String reason) {
        IMapMarkerService service = mapMarkerService();
        try {
            return player != null && service.refresh(player, reason == null ? "" : reason);
        } catch (RuntimeException exception) {
            warnProviderFailure("map refresh", service, exception);
            return false;
        }
    }

    public static int mapDataProviderCount() {
        return MAP_DATA_PROVIDERS.size();
    }

    public static void syncDiscoveryDataToClient(ServerPlayer player) {
        if (player != null) {
            player.syncData(com.knoxhack.echocore.registry.ModAttachments.DISCOVERY_DATA.get());
        }
    }

    public static void syncDiscoveryDataToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            syncDiscoveryDataToClient(serverPlayer);
        }
    }

    public static void registerRouteRecordService(EchoRouteRecordService service) {
        if (service != null && !ROUTE_RECORD_SERVICES.contains(service)) {
            ROUTE_RECORD_SERVICES.add(service);
        }
    }

    public static List<EchoRouteRecord> routeRecords(Player player) {
        Map<Identifier, EchoRouteRecord> records = new LinkedHashMap<>();
        for (EchoRouteRecordService service : ROUTE_RECORD_SERVICES) {
            List<EchoRouteRecord> provided = safeRouteRecords(service, player);
            for (EchoRouteRecord record : provided) {
                if (record == null) {
                    warnInvalidProviderOutput("route record", service, "null record");
                    continue;
                }
                EchoRouteRecord existing = records.putIfAbsent(record.id(), record);
                if (existing != null && !existing.equals(record)) {
                    warnInvalidProviderOutput("route record", service,
                            "duplicate id " + record.id() + " ignored");
                }
            }
        }
        return records.values().stream()
                .sorted(Comparator.comparing(EchoRouteRecord::chapterId)
                        .thenComparing(EchoRouteRecord::title)
                        .thenComparing(record -> record.id().toString()))
                .toList();
    }

    public static void registerDiagnosticService(EchoDiagnosticService service) {
        if (service != null && !DIAGNOSTIC_SERVICES.contains(service)) {
            DIAGNOSTIC_SERVICES.add(service);
        }
    }

    public static List<EchoDiagnosticBlocker> diagnostics(Player player) {
        Map<Identifier, EchoDiagnosticBlocker> blockers = new LinkedHashMap<>();
        for (EchoDiagnosticService service : DIAGNOSTIC_SERVICES) {
            List<EchoDiagnosticBlocker> provided = safeDiagnostics(service, player);
            for (EchoDiagnosticBlocker blocker : provided) {
                if (blocker == null) {
                    warnInvalidProviderOutput("diagnostic", service, "null diagnostic");
                    continue;
                }
                EchoDiagnosticBlocker existing = blockers.putIfAbsent(blocker.id(), blocker);
                if (existing != null && !existing.equals(blocker)) {
                    warnInvalidProviderOutput("diagnostic", service,
                            "duplicate id " + blocker.id() + " ignored");
                }
            }
        }
        return blockers.values().stream()
                .sorted(Comparator.comparingInt((EchoDiagnosticBlocker blocker) -> severityRank(blocker.severity()))
                        .thenComparing(EchoDiagnosticBlocker::chapterId)
                        .thenComparing(EchoDiagnosticBlocker::title)
                        .thenComparing(blocker -> blocker.id().toString()))
                .toList();
    }

    public static void registerHazardTelemetryService(EchoHazardTelemetryService service) {
        if (service != null && !HAZARD_TELEMETRY_SERVICES.contains(service)) {
            HAZARD_TELEMETRY_SERVICES.add(service);
        }
    }

    public static EchoHazardTelemetry hazardTelemetry(Player player) {
        EchoHazardTelemetry telemetry = EchoHazardTelemetry.nominal();
        for (EchoHazardTelemetryService service : HAZARD_TELEMETRY_SERVICES) {
            try {
                telemetry = telemetry.merge(service.telemetry(player));
            } catch (RuntimeException exception) {
                warnProviderFailure("hazard telemetry", service, exception);
            }
        }
        return telemetry;
    }

    public static void registerRecoveryService(EchoRecoveryService service) {
        if (service != null && !RECOVERY_SERVICES.contains(service)) {
            RECOVERY_SERVICES.add(service);
        }
    }

    public static boolean recover(ServerPlayer player, String recoveryId) {
        for (EchoRecoveryService service : RECOVERY_SERVICES) {
            try {
                if (service.recover(player, recoveryId)) {
                    return true;
                }
            } catch (RuntimeException exception) {
                warnProviderFailure("recovery", service, exception);
            }
        }
        return false;
    }

    public static void registerFactionStandingService(EchoFactionStandingService service) {
        if (service != null && !FACTION_STANDING_SERVICES.contains(service)) {
            FACTION_STANDING_SERVICES.add(service);
        }
    }

    public static void registerFactionActionHandler(EchoFactionActionHandlerService service) {
        if (service != null && !FACTION_ACTION_SERVICES.contains(service)) {
            FACTION_ACTION_SERVICES.add(service);
        }
    }

    public static EchoFactionDefinition registerFaction(EchoFactionDefinition definition) {
        return EchoFactionRegistry.register(definition);
    }

    public static Optional<EchoFactionDefinition> factionDefinition(Identifier factionId) {
        if (factionId == null) {
            return Optional.empty();
        }
        try {
            return EchoFactionRegistry.definition(factionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("faction registry", EchoFactionRegistry.class, exception);
            return Optional.empty();
        }
    }

    public static List<EchoFactionDefinition> factionDefinitions() {
        try {
            return EchoFactionRegistry.definitions();
        } catch (RuntimeException exception) {
            warnProviderFailure("faction registry", EchoFactionRegistry.class, exception);
            return List.of();
        }
    }

    public static List<EchoFactionProfile> factionProfiles(Player player) {
        if (player == null) {
            return List.of();
        }
        try {
            List<EchoFactionProfile> profiles = EchoFactionDataService.profiles(player);
            return profiles == null ? List.of() : profiles;
        } catch (RuntimeException exception) {
            warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            return List.of();
        }
    }

    public static Optional<EchoFactionProfile> factionProfile(Player player, Identifier factionId) {
        if (player == null || factionId == null) {
            return Optional.empty();
        }
        try {
            return EchoFactionDataService.profile(player, factionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            return Optional.empty();
        }
    }

    public static void markFactionContacted(Player player, Identifier factionId) {
        if (player != null && factionId != null) {
            try {
                EchoFactionDataService.markContacted(player, factionId);
                syncFactionDataToClient(player);
                if (player instanceof ServerPlayer serverPlayer) {
                    discoverFeature(serverPlayer,
                            Identifier.fromNamespaceAndPath(factionId.getNamespace(), "faction/" + factionId.getPath()));
                    discoverVisibleRouteRecords(serverPlayer);
                }
            } catch (RuntimeException exception) {
                warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            }
        }
    }

    public static void recordFactionInteraction(Player player, Identifier factionId, String roleId, long gameTime) {
        if (player != null && factionId != null) {
            try {
                EchoFactionDataService.recordInteraction(player, factionId, roleId, gameTime);
                syncFactionDataToClient(player);
            } catch (RuntimeException exception) {
                warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            }
        }
    }

    public static void setFactionReputation(Player player, Identifier factionId, int reputation) {
        if (player != null && factionId != null) {
            try {
                EchoFactionDataService.setReputation(player, factionId, reputation);
                syncFactionDataToClient(player);
            } catch (RuntimeException exception) {
                warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            }
        }
    }

    public static void addFactionReputation(Player player, Identifier factionId, int delta) {
        if (player != null && factionId != null && delta != 0) {
            try {
                EchoFactionDataService.addReputation(player, factionId, delta);
                syncFactionDataToClient(player);
            } catch (RuntimeException exception) {
                warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            }
        }
    }

    public static boolean acceptFactionContract(Player player, Identifier factionId, Identifier contractId) {
        if (player == null || factionId == null || contractId == null) {
            return false;
        }
        try {
            boolean accepted = EchoFactionDataService.acceptContract(player, factionId, contractId);
            if (accepted) {
                syncFactionDataToClient(player);
            }
            return accepted;
        } catch (RuntimeException exception) {
            warnProviderFailure("faction contract", EchoFactionDataService.class, exception);
            return false;
        }
    }

    public static boolean completeFactionContract(Player player, Identifier factionId, Identifier contractId) {
        if (player == null || factionId == null || contractId == null) {
            return false;
        }
        try {
            boolean completed = EchoFactionDataService.completeContract(player, factionId, contractId);
            if (completed) {
                syncFactionDataToClient(player);
            }
            return completed;
        } catch (RuntimeException exception) {
            warnProviderFailure("faction contract", EchoFactionDataService.class, exception);
            return false;
        }
    }

    public static void rememberFactionNpc(Player player, Identifier factionId, String memoryLine) {
        if (player != null && factionId != null) {
            try {
                EchoFactionDataService.rememberNpc(player, factionId, memoryLine);
                syncFactionDataToClient(player);
            } catch (RuntimeException exception) {
                warnProviderFailure("faction data", EchoFactionDataService.class, exception);
            }
        }
    }

    public static void syncFactionDataToClient(ServerPlayer player) {
        if (player != null) {
            networkBridge().syncFactionData(player, EchoFactionDataService.exportRoot(player));
        }
    }

    public static void syncFactionDataToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            syncFactionDataToClient(serverPlayer);
        }
    }

    public static Optional<EchoFactionInteractionSnapshot> factionInteractionSnapshot(
            Player player, Identifier factionId, String roleId) {
        Optional<EchoFactionProfile> profile = factionProfile(player, factionId);
        if (profile.isEmpty()) {
            return Optional.empty();
        }

        EchoFactionProfile resolvedProfile = profile.get();
        EchoFactionDefinition definition = resolvedProfile.definition();
        Map<Identifier, EchoFactionAction> actions = new LinkedHashMap<>();
        for (EchoFactionAction action : definition.actions()) {
            actions.putIfAbsent(action.id(), action);
        }
        String localContext = "";
        for (EchoFactionActionHandlerService service : FACTION_ACTION_SERVICES) {
            if (!safeSupports(service, factionId)) {
                continue;
            }
            List<EchoFactionAction> provided = safeFactionActions(service, player, resolvedProfile, roleId);
            for (EchoFactionAction action : provided) {
                if (action == null) {
                    warnInvalidProviderOutput("faction action", service, "null action");
                    continue;
                }
                EchoFactionAction existing = actions.putIfAbsent(action.id(), action);
                if (existing != null && !existing.equals(action)) {
                    warnInvalidProviderOutput("faction action", service,
                            "duplicate id " + action.id() + " ignored");
                }
            }
            if (localContext.isBlank()) {
                String context = safeLocalContext(service, player, resolvedProfile, roleId);
                if (context != null && !context.isBlank()) {
                    localContext = context.trim();
                }
            }
        }

        String greeting = definition.dialogue().greeting();
        if (greeting == null || greeting.isBlank()) {
            greeting = definition.displayName() + " contact online.";
        }
        return Optional.of(new EchoFactionInteractionSnapshot(
                resolvedProfile,
                roleId,
                roleName(definition, roleId),
                greeting,
                localContext,
                List.copyOf(actions.values()),
                definition.contracts()));
    }

    public static EchoFactionContractState factionContractState(Player player, Identifier factionId,
            Identifier contractId, String roleId) {
        Optional<EchoFactionProfile> profile = factionProfile(player, factionId);
        if (profile.isEmpty()) {
            return EchoFactionContractState.unavailable(contractId, "Faction signal unavailable.");
        }
        EchoFactionContract contract = findContract(profile.get().definition(), contractId).orElse(null);
        if (contract == null) {
            return EchoFactionContractState.unavailable(contractId, "No clean field record for this faction contract.");
        }
        EchoFactionContractState fallback = EchoFactionContractState.fromProfile(profile.get(), contract);
        for (EchoFactionActionHandlerService service : FACTION_ACTION_SERVICES) {
            if (!safeSupports(service, factionId)) {
                continue;
            }
            EchoFactionContractState state = safeContractState(service, player, profile.get(), contract, roleId);
            if (state != null) {
                return state;
            }
        }
        return fallback;
    }

    public static EchoFactionActionResult performFactionAction(ServerPlayer player, Identifier factionId,
            Identifier actionId, String roleId, Identifier targetId) {
        if (player == null || factionId == null || actionId == null) {
            return EchoFactionActionResult.failure("Field Request Incomplete", "The faction signal was missing required data.");
        }

        recordFactionInteraction(player, factionId, roleId, player.level().getGameTime());

        if (ACCEPT_FACTION_CONTRACT_ACTION.equals(actionId)) {
            if (targetId == null) {
                return EchoFactionActionResult.failure("Contract Signal Missing", "No contract target is tuned on this request.");
            }
            Optional<EchoFactionProfile> profile = factionProfile(player, factionId);
            EchoFactionContract contract = profile.flatMap(value -> findContract(value.definition(), targetId)).orElse(null);
            EchoFactionContractState state = factionContractState(player, factionId, targetId, roleId);
            if (!state.canAccept()) {
                return EchoFactionActionResult.failure("Contract Sealed",
                        state.lockedReason().isBlank() ? "That contract is not accepting field proof." : state.lockedReason());
            }
            boolean accepted = acceptFactionContract(player, factionId, targetId);
            EchoFactionActionResult handlerResult = accepted && profile.isPresent() && contract != null
                    ? notifyContractAccepted(player, factionId, profile.get(), contract, roleId)
                    : null;
            return accepted
                    ? handlerResult != null
                            ? handlerResult
                            : EchoFactionActionResult.success("Contract Accepted", "Contract added to your active faction work.")
                    : EchoFactionActionResult.failure("Contract Sealed", "That contract is sealed or another contract is active.");
        }

        if (COMPLETE_FACTION_CONTRACT_ACTION.equals(actionId)) {
            if (targetId == null) {
                return EchoFactionActionResult.failure("Contract Signal Missing", "No contract target is tuned on this request.");
            }
            Optional<EchoFactionProfile> profile = factionProfile(player, factionId);
            EchoFactionContract contract = profile.flatMap(value -> findContract(value.definition(), targetId)).orElse(null);
            EchoFactionContractState state = factionContractState(player, factionId, targetId, roleId);
            if (!state.canComplete()) {
                return EchoFactionActionResult.failure("Contract Pending",
                        state.lockedReason().isBlank() ? "That contract is still waiting on field proof." : state.lockedReason());
            }
            EchoFactionActionResult handlerResult = profile.isPresent() && contract != null
                    ? notifyContractCompleted(player, factionId, profile.get(), contract, roleId)
                    : null;
            if (handlerResult != null && !handlerResult.success()) {
                return handlerResult;
            }
            boolean completed = completeFactionContract(player, factionId, targetId);
            if (completed && handlerResult != null && handlerResult.reputationDelta() != 0) {
                addFactionReputation(player, factionId, handlerResult.reputationDelta());
            }
            return completed
                    ? handlerResult != null
                            ? handlerResult
                            : EchoFactionActionResult.success("Contract Archived", "Faction standing updated from field proof.")
                    : EchoFactionActionResult.failure("Contract Pending", "That contract is still waiting on field proof.");
        }

        for (EchoFactionActionHandlerService service : FACTION_ACTION_SERVICES) {
            if (safeSupports(service, factionId)) {
                try {
                    EchoFactionActionResult result = service.handle(player, factionId, actionId, roleId, targetId);
                    if (result != null) {
                        if (result.reputationDelta() != 0) {
                            addFactionReputation(player, factionId, result.reputationDelta());
                        }
                        return result;
                    }
                } catch (RuntimeException exception) {
                    warnProviderFailure("faction action handler", service, exception);
                    return EchoFactionActionResult.failure("Signal Failed",
                            "The owning chapter could not complete that field action.");
                }
            }
        }

        Optional<EchoFactionDefinition> definition = factionDefinition(factionId);
        if (definition.isPresent() && definition.get().actions().stream().anyMatch(action -> action.id().equals(actionId))) {
            return EchoFactionActionResult.info("Signal Logged", "The contact acknowledges the request.");
        }
        return EchoFactionActionResult.failure("Signal Unknown", "No faction contact recognized this request.");
    }

    public static List<String> factionStandingLines(Player player, String factionId) {
        LinkedHashSet<String> lines = new LinkedHashSet<>();
        if (player != null && factionId != null && !factionId.isBlank()) {
            try {
                EchoFactionDataService.profile(player, Identifier.parse(factionId))
                        .map(EchoFactionProfile::standingLine)
                        .ifPresent(lines::add);
            } catch (RuntimeException ignored) {
                // Keep the legacy string hook tolerant of non-namespaced legacy IDs.
            }
        }
        for (EchoFactionStandingService service : FACTION_STANDING_SERVICES) {
            try {
                String line = service.standingLine(player, factionId);
                if (line != null && !line.isBlank()) {
                    lines.add(line);
                }
            } catch (RuntimeException exception) {
                warnProviderFailure("faction standing", service, exception);
            }
        }
        return List.copyOf(lines);
    }

    public static void registerNexusPathService(NexusPathService service) {
        EchoServiceRegistry.register(NexusPathService.class, service);
    }

    public static boolean hasPostNexusChoice(Player player) {
        Optional<NexusPathService> service = EchoServiceRegistry.find(NexusPathService.class);
        if (service.isEmpty()) {
            return false;
        }
        try {
            return service.get().hasPostNexusChoice(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus path", service.get(), exception);
            return false;
        }
    }

    public static void registerNexusCampaignService(NexusCampaignService service) {
        EchoServiceRegistry.register(NexusCampaignService.class, service);
    }

    public static String nexusCampaignPathId(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            String pathId = service.pathId(player);
            return pathId == null ? "" : pathId;
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return "";
        }
    }

    public static int nexusInstability(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            return Math.max(0, service.instability(player));
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return 0;
        }
    }

    public static boolean isNexusWarfrontComplete(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            return service.isWarfrontComplete(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return false;
        }
    }

    public static boolean isNexusFinalProtocolComplete(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            return service.isFinalProtocolComplete(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return false;
        }
    }

    public static List<String> nexusRelaySummary(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            List<String> summary = service.relaySummary(player);
            return summary == null ? List.of() : summary;
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return List.of();
        }
    }

    public static boolean isNexusFinalBossDefeated(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            return service.isFinalBossDefeated(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return false;
        }
    }

    public static String nexusCampaignStatusLine(Player player) {
        NexusCampaignService service = nexusCampaignService();
        try {
            String status = service.statusLine(player);
            return status == null || status.isBlank() ? NexusCampaignService.NOOP.statusLine(player) : status;
        } catch (RuntimeException exception) {
            warnProviderFailure("nexus campaign", service, exception);
            return NexusCampaignService.NOOP.statusLine(player);
        }
    }

    public static void registerIntelMirrorService(IntelMirrorService service) {
        EchoServiceRegistry.register(IntelMirrorService.class, service);
    }

    public static void mirrorIntel(ServerPlayer player, String sourceModId, String id, String title, String content) {
        EchoServiceRegistry.find(IntelMirrorService.class)
                .ifPresent(service -> {
                    try {
                        service.mirrorIntel(player, sourceModId, id, title, content);
                    } catch (RuntimeException exception) {
                        warnProviderFailure("intel mirror", service, exception);
                    }
                });
    }

    public static void registerTerminalPlacementService(TerminalPlacementService service) {
        EchoServiceRegistry.register(TerminalPlacementService.class, service);
    }

    public static boolean placeTerminal(Level level, BlockPos pos, Player owner) {
        TerminalPlacementService service = terminalPlacementService();
        try {
            return service.placeTerminal(level, pos, owner);
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal placement", service, exception);
            return false;
        }
    }

    public static BlockState terminalStructureBlockState() {
        TerminalPlacementService service = terminalPlacementService();
        try {
            BlockState state = service.structureBlockState();
            return state == null ? TerminalPlacementService.NOOP.structureBlockState() : state;
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal placement", service, exception);
            return TerminalPlacementService.NOOP.structureBlockState();
        }
    }

    public static boolean isTerminalBlock(BlockState state) {
        TerminalPlacementService service = terminalPlacementService();
        try {
            return service.isTerminalBlock(state);
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal placement", service, exception);
            return false;
        }
    }

    public static void registerTerminalRewardService(TerminalRewardService service) {
        EchoServiceRegistry.register(TerminalRewardService.class, service);
    }

    public static boolean storeTerminalRewards(ServerPlayer player, String missionId, List<ItemStack> rewards) {
        TerminalRewardService service = terminalRewardService();
        try {
            return service.storeRewards(player, missionId, rewards == null ? List.of() : rewards);
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal rewards", service, exception);
            return false;
        }
    }

    public static boolean claimTerminalRewards(ServerPlayer player) {
        TerminalRewardService service = terminalRewardService();
        try {
            return service.claimRewards(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal rewards", service, exception);
            return false;
        }
    }

    public static int pendingTerminalRewardCount(Player player) {
        TerminalRewardService service = terminalRewardService();
        try {
            return Math.max(0, service.pendingRewardCount(player));
        } catch (RuntimeException exception) {
            warnProviderFailure("terminal rewards", service, exception);
            return 0;
        }
    }

    public static void clearPlatformServicesForTests() {
        ROUTE_RECORD_SERVICES.clear();
        DIAGNOSTIC_SERVICES.clear();
        HAZARD_TELEMETRY_SERVICES.clear();
        RECOVERY_SERVICES.clear();
        FACTION_STANDING_SERVICES.clear();
        FACTION_ACTION_SERVICES.clear();
        MAP_DATA_PROVIDERS.clear();
        EchoDiscoveryRegistry.clearForTests();
        EchoDataBus.clearForTests();
        NoOpDataService.INSTANCE.clearRegisteredKeysForTests();
    }

    public static String platformProviderSummary() {
        IDataService dataService = dataService();
        return "packMode=" + detectPackMode(null).name()
                + ", " + moduleReportSummary()
                + ", dataService=" + providerName(dataService)
                + ", dataKeys=" + dataService.registeredKeys().size()
                + ", routes=" + ROUTE_RECORD_SERVICES.size()
                + ", diagnostics=" + DIAGNOSTIC_SERVICES.size()
                + ", hazards=" + HAZARD_TELEMETRY_SERVICES.size()
                + ", recovery=" + RECOVERY_SERVICES.size()
                + ", factionStanding=" + FACTION_STANDING_SERVICES.size()
                + ", factionActions=" + FACTION_ACTION_SERVICES.size()
                + ", mapService=" + providerName(mapMarkerService())
                + ", mapProviders=" + MAP_DATA_PROVIDERS.size()
                + ", worldRegions=" + (EchoServiceRegistry.find(IWorldRegionService.class).isPresent() ? 1 : 0)
                + ", discoveryProviders=" + EchoDiscoveryRegistry.providerCount()
                + ", factions=" + factionDefinitions().size();
    }

    private static boolean routeRecordVisible(EchoRouteRecord record) {
        if (record == null) {
            return false;
        }
        if (record.complete()) {
            return true;
        }
        String status = record.status() == null ? "" : record.status().toLowerCase(Locale.ROOT);
        return !(status.contains("locked")
                || status.contains("sealed")
                || status.contains("pending")
                || status.contains("unresolved")
                || status.contains("waiting"));
    }

    private static List<EchoRouteRecord> safeRouteRecords(EchoRouteRecordService service, Player player) {
        try {
            List<EchoRouteRecord> records = service.routeRecords(player);
            return records == null ? List.of() : records;
        } catch (RuntimeException exception) {
            warnProviderFailure("route record", service, exception);
            return List.of();
        }
    }

    private static List<EchoDiagnosticBlocker> safeDiagnostics(EchoDiagnosticService service, Player player) {
        try {
            List<EchoDiagnosticBlocker> diagnostics = service.diagnostics(player);
            return diagnostics == null ? List.of() : diagnostics;
        } catch (RuntimeException exception) {
            warnProviderFailure("diagnostic", service, exception);
            return List.of();
        }
    }

    private static boolean safeSupports(EchoFactionActionHandlerService service, Identifier factionId) {
        try {
            return service.supports(factionId);
        } catch (RuntimeException exception) {
            warnProviderFailure("faction action support", service, exception);
            return false;
        }
    }

    private static void safeRegisterMapProvider(IMapMarkerService service, IMapDataProvider provider) {
        if (service == null || provider == null || service == NoOpMapService.INSTANCE) {
            return;
        }
        try {
            service.registerProvider(provider);
        } catch (RuntimeException exception) {
            warnProviderFailure("map data registration", provider, exception);
        }
    }

    private static Identifier safeMapProviderId(IMapDataProvider provider) {
        try {
            return provider == null ? null : provider.providerId();
        } catch (RuntimeException exception) {
            warnProviderFailure("map provider id", provider, exception);
            return null;
        }
    }

    private static void safeRegisterIndexProvider(IIndexService service, IIndexEntryProvider provider) {
        if (service == null || provider == null || service == NoOpIndexService.INSTANCE) {
            return;
        }
        try {
            provider.register(service.registry());
        } catch (RuntimeException exception) {
            warnProviderFailure("index entry registration", provider, exception);
        }
    }

    private static void safeRegisterIndexRecipeProvider(IIndexService service, IIndexRecipeProvider provider) {
        if (service == null || provider == null || service == NoOpIndexService.INSTANCE) {
            return;
        }
        try {
            service.recipes().registerProvider(provider);
        } catch (RuntimeException exception) {
            warnProviderFailure("index recipe registration", provider, exception);
        }
    }

    private static Identifier safeIndexProviderId(IIndexEntryProvider provider) {
        try {
            return provider == null ? null : provider.id();
        } catch (RuntimeException exception) {
            warnProviderFailure("index entry provider id", provider, exception);
            return null;
        }
    }

    private static Identifier safeIndexRecipeProviderId(IIndexRecipeProvider provider) {
        try {
            return provider == null ? null : provider.id();
        } catch (RuntimeException exception) {
            warnProviderFailure("index recipe provider id", provider, exception);
            return null;
        }
    }

    private static List<EchoFactionAction> safeFactionActions(EchoFactionActionHandlerService service,
            Player player, EchoFactionProfile profile, String roleId) {
        try {
            List<EchoFactionAction> actions = service.actions(player, profile, roleId);
            return actions == null ? List.of() : actions;
        } catch (RuntimeException exception) {
            warnProviderFailure("faction actions", service, exception);
            return List.of();
        }
    }

    private static String safeLocalContext(EchoFactionActionHandlerService service,
            Player player, EchoFactionProfile profile, String roleId) {
        try {
            return service.localContext(player, profile, roleId);
        } catch (RuntimeException exception) {
            warnProviderFailure("faction local context", service, exception);
            return "";
        }
    }

    private static EchoFactionContractState safeContractState(EchoFactionActionHandlerService service,
            Player player, EchoFactionProfile profile, EchoFactionContract contract, String roleId) {
        try {
            return service.contractState(player, profile, contract, roleId);
        } catch (RuntimeException exception) {
            warnProviderFailure("faction contract state", service, exception);
            return null;
        }
    }

    private static EchoFactionActionResult notifyContractAccepted(ServerPlayer player, Identifier factionId,
            EchoFactionProfile profile, EchoFactionContract contract, String roleId) {
        EchoFactionActionResult result = null;
        for (EchoFactionActionHandlerService service : FACTION_ACTION_SERVICES) {
            if (!safeSupports(service, factionId)) {
                continue;
            }
            try {
                EchoFactionActionResult candidate = service.acceptContract(player, profile, contract, roleId);
                if (candidate != null && result == null) {
                    result = candidate;
                }
            } catch (RuntimeException exception) {
                warnProviderFailure("faction contract accept", service, exception);
            }
        }
        return result;
    }

    private static EchoFactionActionResult notifyContractCompleted(ServerPlayer player, Identifier factionId,
            EchoFactionProfile profile, EchoFactionContract contract, String roleId) {
        EchoFactionActionResult result = null;
        for (EchoFactionActionHandlerService service : FACTION_ACTION_SERVICES) {
            if (!safeSupports(service, factionId)) {
                continue;
            }
            try {
                EchoFactionActionResult candidate = service.completeContract(player, profile, contract, roleId);
                if (candidate != null) {
                    if (!candidate.success()) {
                        return candidate;
                    }
                    if (result == null) {
                        result = candidate;
                    }
                }
            } catch (RuntimeException exception) {
                warnProviderFailure("faction contract complete", service, exception);
                return EchoFactionActionResult.failure("Contract Failed",
                        "The owning chapter could not complete that faction contract.");
            }
        }
        return result;
    }

    private static Optional<EchoFactionContract> findContract(EchoFactionDefinition definition, Identifier contractId) {
        if (definition == null || contractId == null) {
            return Optional.empty();
        }
        return definition.contracts().stream()
                .filter(contract -> contract.id().equals(contractId))
                .findFirst();
    }

    private static int severityRank(EchoDiagnosticBlocker.Severity severity) {
        return switch (severity == null ? EchoDiagnosticBlocker.Severity.INFO : severity) {
            case CRITICAL -> 0;
            case BLOCKED -> 1;
            case WARNING -> 2;
            case INFO -> 3;
        };
    }

    private static void warnProviderFailure(String surface, Object provider, RuntimeException exception) {
        EchoCore.LOGGER.warn("ECHO platform {} provider {} failed; ignoring provider output.",
                surface, providerName(provider), exception);
    }

    private static void warnInvalidProviderOutput(String surface, Object provider, String detail) {
        EchoCore.LOGGER.warn("ECHO platform {} provider {} returned invalid output: {}.",
                surface, providerName(provider), detail);
    }

    private static String providerName(Object provider) {
        return provider == null ? "<null>" : provider.getClass().getName();
    }

    private static String roleName(EchoFactionDefinition definition, String roleId) {
        if (definition == null) {
            return "Contact";
        }
        String requested = roleId == null ? "" : roleId.trim();
        if (!requested.isBlank()) {
            for (EchoNpcRole role : definition.roles()) {
                if (requested.equals(role.id())) {
                    return role.displayName();
                }
            }
        }
        return definition.roles().isEmpty() ? "Contact" : definition.roles().get(0).displayName();
    }

    private static EchoPackMode detectPackMode(Player player) {
        ModList modList = ModList.get();
        boolean ashfall = modList.isLoaded("echoashfallprotocol");
        boolean orbital = modList.isLoaded("echoorbitalremnants");
        boolean stationfall = modList.isLoaded("echostationfall");
        boolean nexus = modList.isLoaded("echonexusprotocol");
        boolean blackbox = modList.isLoaded("echoblackboxprotocol");
        boolean industrial = modList.isLoaded("echoindustrialnexus");
        boolean logistics = modList.isLoaded("echologisticsnetwork");
        boolean convoy = modList.isLoaded("echoconvoyprotocol");
        boolean armory = modList.isLoaded("echoarmory");
        if (ashfall && orbital && stationfall && nexus && blackbox && industrial && logistics && convoy && armory) {
            return EchoPackMode.FULL_SAGA_WITH_EXTENSIONS;
        }
        if (ashfall && orbital && stationfall && nexus && blackbox) {
            return EchoPackMode.COMPLETE_SAGA;
        }
        if (ashfall && orbital) {
            return EchoPackMode.FULL_SAGA;
        }
        if (ashfall) {
            return EchoPackMode.ASHFALL_STANDALONE;
        }
        if (orbital) {
            return EchoPackMode.ORBITAL_STANDALONE;
        }
        if (industrial) {
            return EchoPackMode.INDUSTRIAL_EXTENSION;
        }
        return EchoPackMode.UNKNOWN;
    }

    private static EchoChapterCapability chapterCapability(
            Player player, ModList modList, String chapterId, String modId, String displayName) {
        boolean installed = modList.isLoaded(modId);
        Optional<EchoAddonChapter> chapter = EchoAddonRegistry.chapters().stream()
                .filter(candidate -> chapterId.equals(candidate.id()))
                .findFirst();
        if (chapter.isEmpty()) {
            return new EchoChapterCapability(chapterId, displayName, installed, false,
                    installed ? "Installed, but no chapter provider is registered." : "Mod not installed.");
        }
        EchoAddonChapter provider = chapter.get();
        boolean available = false;
        String status = "";
        try {
            available = provider.isAvailable(player);
            status = provider.statusLine(player);
        } catch (RuntimeException exception) {
            warnProviderFailure("chapter capability " + chapterId, provider, exception);
            status = "Chapter provider failed while reporting availability.";
        }
        return new EchoChapterCapability(chapterId, provider.displayName(), installed, available, status);
    }

    private static EchoModuleInfo runtimeModuleInfo(ModList modList, ExpectedEchoModule expected) {
        boolean loaded = modList.isLoaded(expected.modId());
        String displayName = expected.displayName();
        String version = "";
        if (loaded) {
            Optional<? extends net.neoforged.fml.ModContainer> container = modList.getModContainerById(expected.modId());
            if (container.isPresent()) {
                try {
                    displayName = container.get().getModInfo().getDisplayName();
                    version = container.get().getModInfo().getVersion().toString();
                } catch (RuntimeException exception) {
                    warnProviderFailure("module metadata " + expected.modId(), container.get(), exception);
                }
            }
        }
        return new EchoModuleInfo(expected.modId(), displayName, version, expected.projectPath(),
                expected.ownership(), loaded, true);
    }

    private static TerminalPlacementService terminalPlacementService() {
        return EchoServiceRegistry.getOrDefault(TerminalPlacementService.class, TerminalPlacementService.NOOP);
    }

    private static TerminalRewardService terminalRewardService() {
        return EchoServiceRegistry.getOrDefault(TerminalRewardService.class, TerminalRewardService.NOOP);
    }

    private static NexusCampaignService nexusCampaignService() {
        return EchoServiceRegistry.getOrDefault(NexusCampaignService.class, NexusCampaignService.NOOP);
    }

    private static EchoProfileService profileService() {
        return EchoServiceRegistry.getOrDefault(EchoProfileService.class, DEFAULT_PROFILE_SERVICE);
    }

    private static final class PersistentProfileService implements EchoProfileService {
        private static final String PROFILE_ROOT = "echocore_profile";
        private static final String LEDGER_ROOT = "echocore_progress_ledger";

        @Override
        public EchoProfile profile(Player player) {
            if (player == null) {
                return EchoProfile.empty();
            }
            CompoundTag root = player.getPersistentData().getCompoundOrEmpty(PROFILE_ROOT);
            return new EchoProfile(
                    root.getStringOr("callsign", "ECHO Operator"),
                    EchoDifficultyProfile.byId(root.getStringOr("difficulty", EchoDifficultyProfile.GUIDED.name())),
                    root.getStringOr("nexus_path", ""),
                    readSet(root, "completed_arc"),
                    readSet(root, "discovered_record"),
                    readSet(root, "dismissed_tutorial"));
        }

        @Override
        public void saveProfile(ServerPlayer player, EchoProfile profile) {
            if (player == null) {
                return;
            }
            EchoProfile safe = profile == null ? EchoProfile.empty() : profile;
            CompoundTag root = new CompoundTag();
            root.putString("callsign", safe.callsign());
            root.putString("difficulty", safe.difficulty().name());
            root.putString("nexus_path", safe.nexusPath());
            writeSet(root, "completed_arc", safe.completedArcs());
            writeSet(root, "discovered_record", safe.discoveredRecords());
            writeSet(root, "dismissed_tutorial", safe.dismissedTutorials());
            player.getPersistentData().put(PROFILE_ROOT, root);
        }

        @Override
        public EchoProgressLedger progressLedger(Player player) {
            if (player == null) {
                return EchoProgressLedger.empty();
            }
            CompoundTag root = player.getPersistentData().getCompoundOrEmpty(LEDGER_ROOT);
            return new EchoProgressLedger(
                    readSet(root, "milestone"),
                    readMap(root, "flag"),
                    readSet(root, "active_objective"));
        }

        @Override
        public void saveProgressLedger(ServerPlayer player, EchoProgressLedger ledger) {
            if (player == null) {
                return;
            }
            EchoProgressLedger safe = ledger == null ? EchoProgressLedger.empty() : ledger;
            CompoundTag root = new CompoundTag();
            writeSet(root, "milestone", safe.milestones());
            writeMap(root, "flag", safe.flags());
            writeSet(root, "active_objective", safe.activeObjectives());
            player.getPersistentData().put(LEDGER_ROOT, root);
        }

        private static Set<String> readSet(CompoundTag root, String prefix) {
            int count = root.getIntOr(prefix + "_count", 0);
            LinkedHashSet<String> values = new LinkedHashSet<>();
            for (int i = 0; i < count; i++) {
                String value = root.getStringOr(prefix + "_" + i, "");
                if (!value.isBlank()) {
                    values.add(value);
                }
            }
            return Set.copyOf(values);
        }

        private static void writeSet(CompoundTag root, String prefix, Set<String> values) {
            int index = 0;
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    root.putString(prefix + "_" + index++, value);
                }
            }
            root.putInt(prefix + "_count", index);
        }

        private static Map<String, String> readMap(CompoundTag root, String prefix) {
            int count = root.getIntOr(prefix + "_count", 0);
            LinkedHashMap<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                String key = root.getStringOr(prefix + "_" + i + "_key", "");
                String value = root.getStringOr(prefix + "_" + i + "_value", "");
                if (!key.isBlank()) {
                    values.put(key, value);
                }
            }
            return Map.copyOf(values);
        }

        private static void writeMap(CompoundTag root, String prefix, Map<String, String> values) {
            int index = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().isBlank()) {
                    root.putString(prefix + "_" + index + "_key", entry.getKey());
                    root.putString(prefix + "_" + index + "_value", entry.getValue() == null ? "" : entry.getValue());
                    index++;
                }
            }
            root.putInt(prefix + "_count", index);
        }
    }

    private record ExpectedEchoModule(String modId, String displayName, String projectPath, String ownership) {
    }
}
