package com.knoxhack.echocore.api;

import com.knoxhack.echocore.EchoCore;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.neoforged.fml.ModList;

/**
 * Immutable snapshot of which ECHO modules are present at a given moment.
 * <p>
 * Use {@link #current()} to capture the state, then pass the snapshot around.
 * All optional integration checks should go through this class instead of
 * scattered {@code ModList.get().isLoaded(...)} calls.
 */
public final class EchoIntegrations {
    public static final String TERMINAL = "echoterminal";
    public static final String THEME_CORE = "echothemecore";
    public static final String INDEX = "echoindex";
    public static final String MISSION_CORE = "echomissioncore";
    public static final String HOLO_MAP = "echoholomap";
    public static final String SOUND_CORE = "echosoundcore";
    public static final String WORLD_CORE = "echoworldcore";
    public static final String DATA_CORE = "echodatacore";
    public static final String RUNTIME_GUARD = "echoruntimeguard";
    public static final String RENDER_CORE = "echorendercore";
    public static final String LENS = "echolens";
    public static final String ASHFALL = "echoashfallprotocol";
    public static final String SIGNALOS = "signalos";

    private static final Set<String> LOGGED_ONCE = ConcurrentHashMap.newKeySet();

    private final boolean hasTerminal;
    private final boolean hasThemeCore;
    private final boolean hasIndex;
    private final boolean hasMissionCore;
    private final boolean hasHoloMap;
    private final boolean hasSoundCore;
    private final boolean hasWorldCore;
    private final boolean hasDataCore;
    private final boolean hasRuntimeGuard;
    private final boolean hasRenderCore;
    private final boolean hasLens;
    private final boolean hasAshfall;
    private final EchoAddonMode mode;

    private EchoIntegrations() {
        ModList modList = ModList.get();
        this.hasTerminal = modList.isLoaded(TERMINAL);
        this.hasThemeCore = modList.isLoaded(THEME_CORE);
        this.hasIndex = modList.isLoaded(INDEX);
        this.hasMissionCore = modList.isLoaded(MISSION_CORE);
        this.hasHoloMap = modList.isLoaded(HOLO_MAP);
        this.hasSoundCore = modList.isLoaded(SOUND_CORE);
        this.hasWorldCore = modList.isLoaded(WORLD_CORE);
        this.hasDataCore = modList.isLoaded(DATA_CORE);
        this.hasRuntimeGuard = modList.isLoaded(RUNTIME_GUARD);
        this.hasRenderCore = modList.isLoaded(RENDER_CORE);
        this.hasLens = modList.isLoaded(LENS);
        this.hasAshfall = modList.isLoaded(ASHFALL);
        this.mode = computeMode(this.hasAshfall, hasAnyEcho(this));
    }

    private static boolean hasAnyEcho(EchoIntegrations i) {
        return i.hasTerminal || i.hasThemeCore || i.hasIndex || i.hasMissionCore
                || i.hasHoloMap || i.hasSoundCore || i.hasWorldCore || i.hasDataCore
                || i.hasRuntimeGuard || i.hasRenderCore || i.hasLens;
    }

    private static EchoAddonMode computeMode(boolean ashfall, boolean anyEcho) {
        if (ashfall && anyEcho) {
            return EchoAddonMode.ASHFALL_CONNECTED;
        }
        if (ashfall) {
            return EchoAddonMode.ASHFALL_CONNECTED;
        }
        if (anyEcho) {
            return EchoAddonMode.ECHO_CONNECTED;
        }
        return EchoAddonMode.STANDALONE;
    }

    /**
     * Returns a fresh snapshot of the current integration state.
     */
    public static EchoIntegrations current() {
        return new EchoIntegrations();
    }

    public boolean hasTerminal() {
        return hasTerminal;
    }

    public boolean hasThemeCore() {
        return hasThemeCore;
    }

    public boolean hasIndex() {
        return hasIndex;
    }

    public boolean hasMissionCore() {
        return hasMissionCore;
    }

    public boolean hasHoloMap() {
        return hasHoloMap;
    }

    public boolean hasSoundCore() {
        return hasSoundCore;
    }

    public boolean hasWorldCore() {
        return hasWorldCore;
    }

    public boolean hasDataCore() {
        return hasDataCore;
    }

    public boolean hasRuntimeGuard() {
        return hasRuntimeGuard;
    }

    public boolean hasRenderCore() {
        return hasRenderCore;
    }

    public boolean hasLens() {
        return hasLens;
    }

    public boolean hasAshfall() {
        return hasAshfall;
    }

    public EchoAddonMode mode() {
        return mode;
    }

    /**
     * Check any mod id safely.
     */
    public boolean has(String modId) {
        return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
    }

    /**
     * Safely invoke an optional integration action if a mod is present.
     * Catches exceptions, logs once, and never crashes the caller.
     */
    public static void ifPresent(String modId, Runnable action) {
        if (!ModList.get().isLoaded(modId)) {
            return;
        }
        if (action == null) {
            return;
        }
        try {
            action.run();
        } catch (RuntimeException exception) {
            logOnce("integration-" + modId, "Optional integration for '{}' failed: {}", modId, exception.getMessage());
        }
    }

    /**
     * Safely invoke an optional integration consumer if a mod is present.
     */
    public static <T> void ifPresent(String modId, T value, java.util.function.Consumer<T> action) {
        if (!ModList.get().isLoaded(modId)) {
            return;
        }
        if (action == null) {
            return;
        }
        try {
            action.accept(value);
        } catch (RuntimeException exception) {
            logOnce("integration-" + modId + "-consumer",
                    "Optional integration consumer for '{}' failed: {}", modId, exception.getMessage());
        }
    }

    private static void logOnce(String key, String message, Object... args) {
        if (LOGGED_ONCE.add(key)) {
            EchoCore.LOGGER.warn(message, args);
        }
    }
}
