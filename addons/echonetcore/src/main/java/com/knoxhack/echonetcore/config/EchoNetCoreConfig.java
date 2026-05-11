package com.knoxhack.echonetcore.config;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echonetcore.EchoNetCore;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class EchoNetCoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_PACKET_LOGGING;
    public static final ModConfigSpec.BooleanValue LOG_DROPPED_PACKETS;
    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_PACKETS;
    public static final ModConfigSpec.IntValue DEFAULT_ACTION_RATE_LIMIT_TICKS;
    public static final ModConfigSpec.IntValue TERMINAL_ACTION_RATE_LIMIT_TICKS;
    public static final ModConfigSpec.IntValue DEBUG_ACTION_RATE_LIMIT_TICKS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("network");
        DEBUG_PACKET_LOGGING = BUILDER
                .comment("Logs accepted ECHO packets. Keep disabled outside debugging.")
                .define("debugPacketLogging", false);
        LOG_DROPPED_PACKETS = BUILDER
                .comment("Logs dropped or rate-limited ECHO packets.")
                .define("logDroppedPackets", false);
        ENABLE_DEBUG_PACKETS = BUILDER
                .comment("Allows registered debug/dev packet handlers to run for permissioned operators.")
                .define("enableDebugPackets", false);
        DEFAULT_ACTION_RATE_LIMIT_TICKS = BUILDER
                .comment("Default spam guard for serverbound gameplay action packets.")
                .defineInRange("defaultActionRateLimitTicks", 10, 0, 200);
        TERMINAL_ACTION_RATE_LIMIT_TICKS = BUILDER
                .comment("Shared terminal action spam guard. Terminal keeps its existing per-action throttle.")
                .defineInRange("terminalActionRateLimitTicks", 4, 0, 200);
        DEBUG_ACTION_RATE_LIMIT_TICKS = BUILDER
                .comment("Spam guard for debug/dev packets.")
                .defineInRange("debugActionRateLimitTicks", 20, 0, 400);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private EchoNetCoreConfig() {
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoNetCore.MODID, () -> {
            EchoConfigCategory network = new EchoConfigCategory("network", "Network", List.of(
                    EchoConfigEntry.intSpec("default_action_rate_limit", "Default Action Rate Limit",
                            "Default spam guard in ticks for serverbound gameplay action packets.",
                            EchoConfigSide.COMMON, DEFAULT_ACTION_RATE_LIMIT_TICKS, 0, 200,
                            true, false, false),
                    EchoConfigEntry.intSpec("terminal_action_rate_limit", "Terminal Action Rate Limit",
                            "Shared terminal action spam guard in ticks.",
                            EchoConfigSide.COMMON, TERMINAL_ACTION_RATE_LIMIT_TICKS, 0, 200,
                            true, false, false)));
            return new EchoConfigModule(EchoNetCore.MODID, "NetCore", List.of(network));
        }));
    }
}
