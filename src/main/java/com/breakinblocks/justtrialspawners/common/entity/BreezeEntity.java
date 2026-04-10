package com.breakinblocks.justtrialspawners.common.entity;

import com.breakinblocks.justtrialspawners.common.entity.ai.BreezeLongJumpGoal;
import com.breakinblocks.justtrialspawners.common.entity.ai.BreezeShootGoal;
import com.breakinblocks.justtrialspawners.common.entity.ai.BreezeSlideGoal;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Breeze - a wind-themed monster that attacks with wind charges.
 * Uses goal-based AI adapted from 1.21's brain-based system.
 */
public class BreezeEntity extends Monster {
    private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
    private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;

    private int soundTick = 0;

    public BreezeEntity(EntityType<? extends BreezeEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.63)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreezeShootGoal(this));
        this.goalSelector.addGoal(2, new BreezeLongJumpGoal(this));
        this.goalSelector.addGoal(3, new BreezeSlideGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // Emit ground particles
        if (this.level().isClientSide) {
            if (this.onGround()) {
                emitGroundParticles(1 + this.getRandom().nextInt(1));
            }
        }

        // Whirl sound
        this.soundTick = this.soundTick == 0
                ? this.random.nextIntBetweenInclusive(WHIRL_SOUND_FREQUENCY_MIN, WHIRL_SOUND_FREQUENCY_MAX)
                : this.soundTick - 1;
        if (this.soundTick == 0) {
            this.playWhirlSound();
        }
    }

    public void emitGroundParticles(int count) {
        if (this.isPassenger()) return;
        Vec3 center = this.getBoundingBox().getCenter();
        Vec3 pos = new Vec3(center.x, this.position().y, center.z);
        BlockState blockState = !this.getFeetBlockState().isAir() ? this.getFeetBlockState() : this.getBlockStateOn();
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < count; i++) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }

    private void playWhirlSound() {
        float volume = 0.8F + 0.2F * this.random.nextFloat();
        float pitch = 0.7F + 0.4F * this.random.nextFloat();
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                ModSounds.BREEZE_WHIRL.get(), this.getSoundSource(), volume, pitch, false);
    }

    /**
     * The Y position of the breeze's "snout" where wind charges originate.
     */
    public double getSnoutYPosition() {
        return this.getEyeY() - 0.4;
    }

    public boolean withinInnerCircleRange(Vec3 target) {
        Vec3 center = this.blockPosition().getCenter();
        return target.closerThan(center, 4.0);
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type == EntityType.PLAYER || type == EntityType.IRON_GOLEM;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // Immune to damage from other Breezes
        if (source.getEntity() instanceof BreezeEntity) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (distance > 3.0F) {
            this.playSound(ModSounds.BREEZE_LAND.get(), 1.0F, 1.0F);
        }
        return false; // Immune to fall damage
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public int getHeadRotSpeed() {
        return 25;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BREEZE_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BREEZE_HURT.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.onGround() ? ModSounds.BREEZE_IDLE_GROUND.get() : ModSounds.BREEZE_IDLE_AIR.get();
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    /**
     * Deflect non-wind-charge projectiles.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Projectile projectile) {
            // Don't deflect wind charges
            if (projectile instanceof AbstractWindChargeProjectile) {
                return super.hurt(source, amount);
            }
            // Deflect other projectiles
            Vec3 motion = projectile.getDeltaMovement();
            projectile.setDeltaMovement(motion.reverse());
            projectile.setOwner(this);
            this.level().playSound(null, this, ModSounds.BREEZE_DEFLECT.get(), this.getSoundSource(), 1.0F, 1.0F);
            return false;
        }
        return super.hurt(source, amount);
    }
}
