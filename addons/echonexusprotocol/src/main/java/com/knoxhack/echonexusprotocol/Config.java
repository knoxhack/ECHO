package com.knoxhack.echonexusprotocol;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class Config {
   public static final ModConfigSpec SPEC;
   public static final BooleanValue FORCE_NEXUS_UNLOCK;
   public static final ConfigValue<String> BALANCE_PRESET;
   public static final IntValue MACHINE_CAPACITY;
   public static final IntValue MACHINE_TRANSFER;
   public static final IntValue MACHINE_DURATION_PERCENT;
   public static final IntValue MACHINE_CHARGE_COST_PERCENT;
   public static final IntValue MACHINE_CHARGE_OUTPUT_PERCENT;
   public static final IntValue FILTER_CORRUPTION_REDUCTION;
   public static final IntValue FILTER_LEAK_PRESSURE;
   public static final IntValue STABILIZER_FIELD_GAIN;
   public static final IntValue STABILIZER_CORRUPTION_REDUCTION;
   public static final IntValue REACTOR_OUTPUT_PERCENT;
   public static final IntValue FIELD_TICK_INTERVAL;
   public static final BooleanValue CHUNK_CORRUPTION_TICK_ENABLED;
   public static final IntValue FIELD_DECAY_LOW_PRESSURE;
   public static final IntValue FIELD_DECAY_HIGH_PRESSURE;
   public static final IntValue CORRUPTION_SPREAD_FIELD_THRESHOLD;
   public static final IntValue CORRUPTION_SPREAD_PRESSURE_THRESHOLD;
   public static final IntValue STORM_WINDOW_MULTIPLIER;
   public static final IntValue REALITY_TEAR_FIELD_THRESHOLD;
   public static final IntValue FIELD_MAP_RADIUS;
   public static final IntValue SEAL_TICK_INTERVAL;
   public static final IntValue SEAL_RADIUS;
   public static final IntValue SEAL_RELAY_AMOUNT;
   public static final IntValue SEAL_DEFENSE_DAMAGE;
   public static final IntValue SEAL_PURIFY_PRESSURE_REDUCTION;
   public static final IntValue SEAL_COLLAPSE_FIELD_LOSS;
   public static final IntValue ARMOR_LOCK_COOLDOWN;
   public static final IntValue ARMOR_LOCK_FIELD_GAIN;
   public static final IntValue ARMOR_LOCK_CORRUPTION_REDUCTION;
   public static final IntValue STABILIZED_PURITY_FIELD_GAIN;
   public static final IntValue STABILIZED_PURITY_CORRUPTION_REDUCTION;
   public static final IntValue FIELD_ANCHOR_TICKS;
   public static final IntValue FIELD_ANCHOR_FIELD_GAIN;
   public static final IntValue FIELD_ANCHOR_CORRUPTION_REDUCTION;
   public static final IntValue BOSS_HEALTH_PERCENT;
   public static final IntValue BOSS_DAMAGE_PERCENT;
   public static final IntValue DUNGEON_LOOT_SCALE_PERCENT;

   private Config() {
   }

   static {
      Builder builder = new Builder();
      builder.push("progression");
      FORCE_NEXUS_UNLOCK = builder.comment("Development override. When true, Nexus progression ignores stationfall:blackbox_recovered.")
         .define("forceNexusUnlock", false);
      BALANCE_PRESET = builder.comment("Documentation preset label for pack/admin tuning. Valid labels: casual, standard, hardcore. Numeric values below remain authoritative.")
         .define("balancePreset", "standard");
      builder.pop();
      builder.push("machines");
      MACHINE_CAPACITY = builder.defineInRange("machineCapacity", 16000, 1000, 1000000);
      MACHINE_TRANSFER = builder.defineInRange("machineTransfer", 512, 1, 100000);
      MACHINE_DURATION_PERCENT = builder.comment("Percent multiplier for Nexus processing recipe duration. Beta default keeps machines responsive without making charge free.")
         .defineInRange("machineDurationPercent", 90, 10, 500);
      MACHINE_CHARGE_COST_PERCENT = builder.comment("Percent multiplier for machine Nexus Charge costs.")
         .defineInRange("machineChargeCostPercent", 90, 0, 500);
      MACHINE_CHARGE_OUTPUT_PERCENT = builder.comment("Percent multiplier for machine Nexus Charge output.")
         .defineInRange("machineChargeOutputPercent", 100, 0, 500);
      FILTER_CORRUPTION_REDUCTION = builder.defineInRange("filterCorruptionReduction", 4, 0, 100);
      FILTER_LEAK_PRESSURE = builder.defineInRange("filterLeakPressure", 2, 0, 100);
      STABILIZER_FIELD_GAIN = builder.defineInRange("stabilizerFieldGain", 6, 0, 100);
      STABILIZER_CORRUPTION_REDUCTION = builder.defineInRange("stabilizerCorruptionReduction", 4, 0, 100);
      REACTOR_OUTPUT_PERCENT = builder.comment("Extra percent multiplier applied to passive Corruption Reactor generation.")
         .defineInRange("reactorOutputPercent", 100, 0, 500);
      builder.pop();
      builder.push("field");
      FIELD_TICK_INTERVAL = builder.defineInRange("fieldTickInterval", 80, 20, 1200);
      CHUNK_CORRUPTION_TICK_ENABLED = builder.define("chunkCorruptionTickEnabled", true);
      FIELD_DECAY_LOW_PRESSURE = builder.defineInRange("fieldDecayLowPressure", 1, 0, 20);
      FIELD_DECAY_HIGH_PRESSURE = builder.defineInRange("fieldDecayHighPressure", 2, 0, 20);
      CORRUPTION_SPREAD_FIELD_THRESHOLD = builder.defineInRange("corruptionSpreadFieldThreshold", 40, 0, 100);
      CORRUPTION_SPREAD_PRESSURE_THRESHOLD = builder.defineInRange("corruptionSpreadPressureThreshold", 15, 0, 100);
      STORM_WINDOW_MULTIPLIER = builder.defineInRange("stormWindowMultiplier", 4, 1, 20);
      REALITY_TEAR_FIELD_THRESHOLD = builder.defineInRange("realityTearFieldThreshold", 20, 0, 100);
      FIELD_MAP_RADIUS = builder.comment("Fixed Terminal field-map display radius in chunks. Nexus sync stores a stable 5x5 grid for this release, so valid values are intentionally capped at 2.")
         .defineInRange("fieldMapRadius", 2, 1, 2);
      builder.pop();
      builder.push("seals");
      SEAL_TICK_INTERVAL = builder.defineInRange("sealTickInterval", 35, 10, 1200);
      SEAL_RADIUS = builder.defineInRange("sealRadius", 5, 1, 16);
      SEAL_RELAY_AMOUNT = builder.defineInRange("sealRelayAmount", 96, 1, 10000);
      SEAL_DEFENSE_DAMAGE = builder.defineInRange("sealDefenseDamage", 6, 1, 100);
      SEAL_PURIFY_PRESSURE_REDUCTION = builder.defineInRange("sealPurifyPressureReduction", 5, 0, 100);
      SEAL_COLLAPSE_FIELD_LOSS = builder.defineInRange("sealCollapseFieldLoss", 6, 0, 100);
      builder.pop();
      builder.push("gear");
      ARMOR_LOCK_COOLDOWN = builder.defineInRange("armorLockCooldown", 2400, 0, 72000);
      ARMOR_LOCK_FIELD_GAIN = builder.defineInRange("armorLockFieldGain", 8, 0, 100);
      ARMOR_LOCK_CORRUPTION_REDUCTION = builder.defineInRange("armorLockCorruptionReduction", 8, 0, 100);
      STABILIZED_PURITY_FIELD_GAIN = builder.comment("Field recovery from the stronger collapsed-chunk purity charge.")
         .defineInRange("stabilizedPurityFieldGain", 16, 0, 100);
      STABILIZED_PURITY_CORRUPTION_REDUCTION = builder.defineInRange("stabilizedPurityCorruptionReduction", 32, 0, 100);
      FIELD_ANCHOR_TICKS = builder.comment("Quarantine duration applied by Field Anchor recovery tool.")
         .defineInRange("fieldAnchorTicks", 2400, 0, 72000);
      FIELD_ANCHOR_FIELD_GAIN = builder.defineInRange("fieldAnchorFieldGain", 12, 0, 100);
      FIELD_ANCHOR_CORRUPTION_REDUCTION = builder.defineInRange("fieldAnchorCorruptionReduction", 24, 0, 100);
      builder.pop();
      builder.push("bosses");
      BOSS_HEALTH_PERCENT = builder.defineInRange("bossHealthPercent", 115, 10, 1000);
      BOSS_DAMAGE_PERCENT = builder.defineInRange("bossDamagePercent", 105, 10, 1000);
      builder.pop();
      builder.push("dungeons");
      DUNGEON_LOOT_SCALE_PERCENT = builder.comment("Loot intensity hint used by dungeon templates and tests. Datapacks can tune loot tables separately.")
         .defineInRange("dungeonLootScalePercent", 110, 0, 500);
      builder.pop();
      SPEC = builder.build();
   }
}
