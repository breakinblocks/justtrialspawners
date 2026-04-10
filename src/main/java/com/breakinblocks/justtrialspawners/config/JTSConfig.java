package com.breakinblocks.justtrialspawners.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class JTSConfig {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;

    static {
        var serverPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = serverPair.getRight();
        SERVER = serverPair.getLeft();
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
                    .defineInRange("cooldown_ticks", 36000, 0, Integer.MAX_VALUE);

            trialSpawnerDetectionRange = builder
                    .comment("Player detection range in blocks")
                    .defineInRange("detection_range", 14, 1, 128);

            trialSpawnerSpawnRange = builder
                    .comment("Mob spawn range around the spawner")
                    .defineInRange("spawn_range", 4, 1, 32);

            ominousTrialEnabled = builder
                    .comment("Enable ominous trial spawner mode (triggered by Bad Omen)")
                    .define("ominous_enabled", true);

            builder.pop();
            builder.push("wind_charge");

            windChargeKnockbackMultiplier = builder
                    .comment("Multiplier for wind charge knockback strength")
                    .defineInRange("knockback_multiplier", 1.0, 0.0, 10.0);

            builder.pop();
            builder.push("migration");

            enableTrialsMigration = builder
                    .comment("Enable automatic migration from the Trials mod (trials:trial_spawner -> justtrialspawners:trial_spawner)")
                    .define("enable_trials_migration", true);

            builder.pop();
        }
    }
}
