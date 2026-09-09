package com.breakinblocks.justtrialspawners.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class JTSConfig {
    public static final int DEFAULT_COOLDOWN_TICKS = 36000;
    public static final int DEFAULT_DETECTION_RANGE = 14;
    public static final int DEFAULT_SPAWN_RANGE = 4;
    public static final double DEFAULT_WIND_CHARGE_KNOCKBACK = 1.0;
    public static final boolean DEFAULT_OMINOUS_ENABLED = true;
    public static final boolean DEFAULT_TRIALS_MIGRATION = false;

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        var serverPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static int cooldownTicks() {
        return SERVER_SPEC.isLoaded() ? SERVER.trialSpawnerCooldownTicks.get() : DEFAULT_COOLDOWN_TICKS;
    }

    public static int detectionRange() {
        return SERVER_SPEC.isLoaded() ? SERVER.trialSpawnerDetectionRange.get() : DEFAULT_DETECTION_RANGE;
    }

    public static int spawnRange() {
        return SERVER_SPEC.isLoaded() ? SERVER.trialSpawnerSpawnRange.get() : DEFAULT_SPAWN_RANGE;
    }

    public static boolean ominousEnabled() {
        return SERVER_SPEC.isLoaded() ? SERVER.ominousTrialEnabled.get() : DEFAULT_OMINOUS_ENABLED;
    }

    public static float windChargeKnockbackMultiplier() {
        return (float) (SERVER_SPEC.isLoaded()
                ? SERVER.windChargeKnockbackMultiplier.get()
                : DEFAULT_WIND_CHARGE_KNOCKBACK);
    }

    public static boolean trialsMigrationEnabled() {
        return SERVER_SPEC.isLoaded() ? SERVER.enableTrialsMigration.get() : DEFAULT_TRIALS_MIGRATION;
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.IntValue trialSpawnerCooldownTicks;
        public final ForgeConfigSpec.IntValue trialSpawnerDetectionRange;
        public final ForgeConfigSpec.IntValue trialSpawnerSpawnRange;
        public final ForgeConfigSpec.DoubleValue windChargeKnockbackMultiplier;
        public final ForgeConfigSpec.BooleanValue ominousTrialEnabled;
        public final ForgeConfigSpec.BooleanValue enableTrialsMigration;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.push("trial_spawner");

            trialSpawnerCooldownTicks = builder
                    .comment("Cooldown in ticks after trial spawner completes (default 36000 = 30 minutes)")
                    .defineInRange("cooldown_ticks", DEFAULT_COOLDOWN_TICKS, 0, Integer.MAX_VALUE);

            trialSpawnerDetectionRange = builder
                    .comment("Player detection range in blocks")
                    .defineInRange("detection_range", DEFAULT_DETECTION_RANGE, 1, 128);

            trialSpawnerSpawnRange = builder
                    .comment("Mob spawn range around the spawner")
                    .defineInRange("spawn_range", DEFAULT_SPAWN_RANGE, 1, 32);

            ominousTrialEnabled = builder
                    .comment("Enable ominous trial spawner mode (triggered by Bad Omen). When false, spawners never turn ominous, Bad Omen is left alone instead of being converted to Trial Omen, and any spawner that is already ominous reverts to normal.")
                    .define("ominous_enabled", DEFAULT_OMINOUS_ENABLED);

            builder.pop();
            builder.push("wind_charge");

            windChargeKnockbackMultiplier = builder
                    .comment("Multiplier for wind charge knockback strength")
                    .defineInRange("knockback_multiplier", DEFAULT_WIND_CHARGE_KNOCKBACK, 0.0, 10.0);

            builder.pop();
            builder.push("migration");

            enableTrialsMigration = builder
                    .comment("Enable automatic migration from the Trials mod (trials:trial_spawner -> justtrialspawners:trial_spawner). Disabled by default - enable when transitioning a world from the Trials mod to Just Trial Spawners.")
                    .define("enable_trials_migration", DEFAULT_TRIALS_MIGRATION);

            builder.pop();
        }
    }
}
