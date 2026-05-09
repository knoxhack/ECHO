package com.knoxhack.echoorbitalremnants;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final int DEFAULT_HAZARD_DRAIN_MULTIPLIER = 75;
    public static final int DEFAULT_ORBITAL_EVENT_FREQUENCY = 3000;
    public static final int DEFAULT_MACHINE_BASE_DURATION = 140;
    public static final int DEFAULT_MACHINE_CHARGE_REGEN_TICKS = 16;

    public static final ModConfigSpec.BooleanValue ADVENTURE_LEAN_DEFAULTS = BUILDER
            .comment("Keeps orbital survival tense but recoverable.")
            .define("adventureLeanDefaults", true);

    public static final ModConfigSpec.EnumValue<DifficultyPreset> DIFFICULTY_PRESET = BUILDER
            .comment("Release v1 tuning preset. ADVENTURE keeps hazards readable; HARD makes orbit less forgiving.")
            .defineEnum("difficultyPreset", DifficultyPreset.ADVENTURE);

    public static final ModConfigSpec.IntValue OXYGEN_DRAIN_TICKS = BUILDER
            .comment("Ticks between oxygen loss while exposed to vacuum.")
            .defineInRange("survival.oxygenDrainTicks", 40, 10, 400);

    public static final ModConfigSpec.IntValue RADIATION_GAIN_TICKS = BUILDER
            .comment("Ticks between radiation increases in orbital or Nexus exposure.")
            .defineInRange("survival.radiationGainTicks", 80, 20, 800);

    public static final ModConfigSpec.IntValue VACUUM_DAMAGE_TICKS = BUILDER
            .comment("Ticks between suffocation/pressure damage after oxygen or suit pressure fails.")
            .defineInRange("survival.vacuumDamageTicks", 80, 20, 400);

    public static final ModConfigSpec.IntValue HAZARD_DRAIN_MULTIPLIER = BUILDER
            .comment("Public beta route pacing percentage for route hazard oxygen, pressure, and radiation drain.")
            .defineInRange("balance.hazardDrainMultiplier", DEFAULT_HAZARD_DRAIN_MULTIPLIER, 25, 250);

    public static final ModConfigSpec.IntValue ARRIVAL_CACHE_SUPPORT_MULTIPLIER = BUILDER
            .comment("Balance tuning multiplier for route arrival cache survival support items.")
            .defineInRange("balance.arrivalCacheSupportMultiplier", 2, 1, 5);

    public static final ModConfigSpec.IntValue DEEP_SITE_THREAT_CHANCE = BUILDER
            .comment("Percent chance that a dense route feature, including Saturn/Titan beta sites, spawns an ambient threat when checked.")
            .defineInRange("balance.deepSiteThreatChance", 50, 0, 100);

    public static final ModConfigSpec.IntValue ORBITAL_ALTITUDE = BUILDER
            .comment("Overworld altitude treated as low orbit by the current orbital progression loop.")
            .defineInRange("launch.orbitalAltitude", 296, 256, 319);

    public static final ModConfigSpec.BooleanValue REQUIRE_FULL_LAUNCH_READINESS = BUILDER
            .comment("If true, the emergency rocket requires launch platform, suit, fuel, oxygen, and navigation parts.")
            .define("launch.requireFullLaunchReadiness", true);

    public static final ModConfigSpec.IntValue ORBITAL_EVENT_FREQUENCY = BUILDER
            .comment("Average ticks between ambient orbital event warnings; tuned for public beta route pacing.")
            .defineInRange("events.orbitalEventFrequency", DEFAULT_ORBITAL_EVENT_FREQUENCY, 200, 24000);

    public static final ModConfigSpec.BooleanValue FEATURE_THREATS_ENABLED = BUILDER
            .comment("If true, dense route features may spawn ambient orbital threats.")
            .define("events.featureThreatsEnabled", true);

    public static final ModConfigSpec.IntValue ROUTE_FEATURE_DENSITY = BUILDER
            .comment("Controls repeatable deep-site density in route dimensions. Higher values place sites more often.")
            .defineInRange("worldgen.routeFeatureDensity", 3, 1, 5);

    public static final ModConfigSpec.BooleanValue DEEP_SITE_CACHES_ENABLED = BUILDER
            .comment("If true, generated deep route sites include fixed survival caches.")
            .define("worldgen.deepSiteCachesEnabled", true);

    public static final ModConfigSpec.IntValue MACHINE_BASE_DURATION = BUILDER
            .comment("Public beta base processing duration in ticks for orbital machines. Recipes may override this.")
            .defineInRange("machines.baseDuration", DEFAULT_MACHINE_BASE_DURATION, 20, 2400);

    public static final ModConfigSpec.IntValue MACHINE_CHARGE_REGEN_TICKS = BUILDER
            .comment("Public beta ticks between one point of passive machine system charge regeneration.")
            .defineInRange("machines.chargeRegenTicks", DEFAULT_MACHINE_CHARGE_REGEN_TICKS, 1, 400);

    public static final ModConfigSpec.IntValue MACHINE_MAX_CHARGE = BUILDER
            .comment("Maximum internal system charge stored by one machine.")
            .defineInRange("machines.maxCharge", 100, 10, 1000);

    public static final ModConfigSpec.BooleanValue DIMENSION_UNLOCKS_ENABLED = BUILDER
            .comment("Controls whether later route unlock flags are enforced by ECHO Terminal progression.")
            .define("progression.dimensionUnlocksEnabled", true);

    public static final ModConfigSpec.BooleanValue MID_GAME_OBJECTIVES_ENABLED = BUILDER
            .comment("If true, Orbit, Moon, Mars, Europa, Saturn, and Titan require route objective chains before the next route opens.")
            .define("progression.midGameObjectivesEnabled", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int tunedMachineDuration(int recipeDuration) {
        int scaled = Math.max(1, recipeDuration) * MACHINE_BASE_DURATION.get() / 160;
        return switch (DIFFICULTY_PRESET.get()) {
            case CASUAL -> Math.max(1, scaled * 3 / 4);
            case HARD -> Math.max(1, scaled * 5 / 4);
            case ADVENTURE -> Math.max(1, scaled);
        };
    }

    public static int tunedMachineChargeRegenTicks() {
        int configured = MACHINE_CHARGE_REGEN_TICKS.get();
        return switch (DIFFICULTY_PRESET.get()) {
            case CASUAL -> Math.max(1, configured * 3 / 4);
            case HARD -> Math.max(1, configured * 5 / 4);
            case ADVENTURE -> Math.max(1, configured);
        };
    }

    public static int tunedSurvivalInterval(ModConfigSpec.IntValue configured) {
        int ticks = configured.get();
        return switch (DIFFICULTY_PRESET.get()) {
            case CASUAL -> Math.max(1, ticks * 3 / 2);
            case HARD -> Math.max(1, ticks * 2 / 3);
            case ADVENTURE -> Math.max(1, ticks);
        };
    }

    public static int tunedHazardDrain(int amount) {
        int scaled = Math.max(0, amount) * HAZARD_DRAIN_MULTIPLIER.get() / 100;
        scaled = amount > 0 ? Math.max(1, scaled) : 0;
        return switch (DIFFICULTY_PRESET.get()) {
            case CASUAL -> Math.max(1, scaled * 3 / 4);
            case HARD -> Math.max(1, scaled * 5 / 4);
            case ADVENTURE -> scaled;
        };
    }

    public static int tunedOrbitalEventFrequency() {
        int ticks = ORBITAL_EVENT_FREQUENCY.get();
        return switch (DIFFICULTY_PRESET.get()) {
            case CASUAL -> Math.max(1, ticks * 3 / 2);
            case HARD -> Math.max(1, ticks * 2 / 3);
            case ADVENTURE -> Math.max(1, ticks);
        };
    }

    public enum DifficultyPreset {
        CASUAL,
        ADVENTURE,
        HARD
    }
}
