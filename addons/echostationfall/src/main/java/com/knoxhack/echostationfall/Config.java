package com.knoxhack.echostationfall;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue PANIC_GAIN_MULTIPLIER;
    public static final ModConfigSpec.IntValue PANIC_DECAY_TICKS;
    public static final ModConfigSpec.IntValue HALLUCINATION_TICKS;
    public static final ModConfigSpec.IntValue PRESSURE_EVENT_TICKS;
    public static final ModConfigSpec.BooleanValue REDUCED_HORROR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("stationfall");
        PANIC_GAIN_MULTIPLIER = builder.defineInRange("panicGainMultiplier", 1.0D, 0.0D, 10.0D);
        PANIC_DECAY_TICKS = builder.defineInRange("panicDecayTicks", 80, 10, 1200);
        HALLUCINATION_TICKS = builder.defineInRange("hallucinationTicks", 220, 20, 2400);
        PRESSURE_EVENT_TICKS = builder.defineInRange("pressureEventTicks", 260, 20, 2400);
        REDUCED_HORROR = builder.define("reducedHorror", false);
        builder.pop();
        SPEC = builder.build();
    }

    private Config() {
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoStationfall.MODID, () -> new EchoConfigModule(
                EchoStationfall.MODID,
                "Stationfall",
                List.of(new EchoConfigCategory("stationfall", "Stationfall", List.of(
                        EchoConfigEntry.doubleSpec("panic_gain_multiplier", "Panic Gain Multiplier",
                                "Multiplier for panic gained from station events.",
                                EchoConfigSide.COMMON, PANIC_GAIN_MULTIPLIER, 0.0D, 10.0D,
                                true, false, false),
                        EchoConfigEntry.intSpec("panic_decay_ticks", "Panic Decay Ticks",
                                "Ticks between panic decay pulses.",
                                EchoConfigSide.COMMON, PANIC_DECAY_TICKS, 10, 1200,
                                true, false, false),
                        EchoConfigEntry.intSpec("hallucination_ticks", "Hallucination Ticks",
                                "Duration of hallucination pressure events.",
                                EchoConfigSide.COMMON, HALLUCINATION_TICKS, 20, 2400,
                                true, false, false),
                        EchoConfigEntry.intSpec("pressure_event_ticks", "Pressure Event Ticks",
                                "Ticks between station pressure event checks.",
                                EchoConfigSide.COMMON, PRESSURE_EVENT_TICKS, 20, 2400,
                                true, false, false),
                        EchoConfigEntry.booleanSpec("reduced_horror", "Reduced Horror",
                                "Tone down horror pressure effects.",
                                EchoConfigSide.COMMON, REDUCED_HORROR, true, false, false)))))));
    }
}
