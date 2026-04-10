package com.breakinblocks.justtrialspawners.common.block.entity.trialspawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public enum TrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, ParticleEmission.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmission.SMALL_FLAMES, 200.0, true),
    ACTIVE("active", 8, ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    COOLDOWN("cooldown", 0, ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

    public static final int DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40;
    public static final int TIME_BETWEEN_EACH_EJECTION = 30;

    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final ParticleEmission particleEmission;
    private final boolean capableOfSpawning;

    TrialSpawnerState(String name, int lightLevel, ParticleEmission particleEmission, double spinningMobSpeed, boolean capableOfSpawning) {
        this.name = name;
        this.lightLevel = lightLevel;
        this.particleEmission = particleEmission;
        this.spinningMobSpeed = spinningMobSpeed;
        this.capableOfSpawning = capableOfSpawning;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0;
    }

    public boolean isCapableOfSpawning() {
        return this.capableOfSpawning;
    }

    public void emitParticles(Level level, BlockPos pos, boolean isOminous) {
        this.particleEmission.emit(level, level.getRandom(), pos, isOminous);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @FunctionalInterface
    interface ParticleEmission {
        ParticleEmission NONE = (level, random, pos, ominous) -> {};

        ParticleEmission SMALL_FLAMES = (level, random, pos, ominous) -> {
            if (random.nextInt(2) == 0) {
                Vec3 vec3 = pos.getCenter().offsetRandom(random, 0.9F);
                addParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3, level);
            }
        };

        ParticleEmission FLAMES_AND_SMOKE = (level, random, pos, ominous) -> {
            Vec3 vec3 = pos.getCenter().offsetRandom(random, 1.0F);
            addParticle(ParticleTypes.SMOKE, vec3, level);
            addParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3, level);
        };

        ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (level, random, pos, ominous) -> {
            Vec3 vec3 = pos.getCenter().offsetRandom(random, 0.9F);
            if (random.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, vec3, level);
            }
            if (level.getGameTime() % 20L == 0L) {
                Vec3 topVec = pos.getCenter().add(0.0, 0.5, 0.0);
                int count = level.getRandom().nextInt(4) + 20;
                for (int j = 0; j < count; j++) {
                    addParticle(ParticleTypes.SMOKE, topVec, level);
                }
            }
        };

        static void addParticle(SimpleParticleType type, Vec3 pos, Level level) {
            level.addParticle(type, pos.x(), pos.y(), pos.z(), 0.0, 0.0, 0.0);
        }

        void emit(Level level, RandomSource random, BlockPos pos, boolean isOminous);
    }
}
