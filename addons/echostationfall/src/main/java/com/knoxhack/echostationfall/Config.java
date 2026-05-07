package com.knoxhack.echostationfall;
import net.neoforged.neoforge.common.ModConfigSpec;
public final class Config {
    public static final ModConfigSpec SPEC; public static final ModConfigSpec.DoubleValue PANIC_GAIN_MULTIPLIER; public static final ModConfigSpec.IntValue PANIC_DECAY_TICKS; public static final ModConfigSpec.IntValue HALLUCINATION_TICKS; public static final ModConfigSpec.IntValue PRESSURE_EVENT_TICKS; public static final ModConfigSpec.BooleanValue REDUCED_HORROR;
    static { ModConfigSpec.Builder b=new ModConfigSpec.Builder(); b.push("stationfall"); PANIC_GAIN_MULTIPLIER=b.defineInRange("panicGainMultiplier",1.0D,0.0D,10.0D); PANIC_DECAY_TICKS=b.defineInRange("panicDecayTicks",80,10,1200); HALLUCINATION_TICKS=b.defineInRange("hallucinationTicks",220,20,2400); PRESSURE_EVENT_TICKS=b.defineInRange("pressureEventTicks",260,20,2400); REDUCED_HORROR=b.define("reducedHorror",false); b.pop(); SPEC=b.build(); }
    private Config(){}
}
