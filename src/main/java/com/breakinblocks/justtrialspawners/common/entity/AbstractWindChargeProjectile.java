package com.breakinblocks.justtrialspawners.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Base class for wind charge projectiles. Adapted from 1.21's AbstractWindCharge.
 * Does not use acceleration (flies in a straight line with no slowdown).
 */
public abstract class AbstractWindChargeProjectile extends AbstractHurtingProjectile {

    public AbstractWindChargeProjectile(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }

    public AbstractWindChargeProjectile(EntityType<? extends AbstractHurtingProjectile> type,
                                         Level level, Entity owner,
                                         double x, double y, double z) {
        super(type, x, y, z, 0, 0, 0, level);
        this.setOwner(owner);
    }

    public AbstractWindChargeProjectile(EntityType<? extends AbstractHurtingProjectile> type,
                                         double x, double y, double z,
                                         double dx, double dy, double dz,
                                         Level level) {
        super(type, x, y, z, dx, dy, dz, level);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !(other instanceof AbstractWindChargeProjectile) && super.canCollideWith(other);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof AbstractWindChargeProjectile) return false;
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            Entity entity = result.getEntity();
            if (owner != null) {
                owner.setLastHurtMob(entity);
            }
            entity.hurt(this.damageSources().mobProjectile(this, owner), 1.0F);
            this.explode(this.position());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            Vec3i normal = result.getDirection().getNormal();
            Vec3 offset = Vec3.atLowerCornerOf(normal).multiply(0.25, 0.25, 0.25);
            Vec3 impactPos = result.getLocation().add(offset);
            this.explode(impactPos);
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    protected abstract void explode(Vec3 position);

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0F; // No slowdown
    }

    @Nullable
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.getY() > this.level().getMaxBuildHeight() + 30) {
            this.explode(this.position());
            this.discard();
        } else {
            // Skip AbstractHurtingProjectile.tick() which doesn't null-check getTrailParticle()
            // in 1.20.1. Call the projectile movement logic directly instead.
            baseTick();
            HitResult hitresult = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnMoveVector(
                    this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                this.onHit(hitresult);
            }
            this.checkInsideBlocks();
            Vec3 vec3 = this.getDeltaMovement();
            double x = this.getX() + vec3.x;
            double y = this.getY() + vec3.y;
            double z = this.getZ() + vec3.z;
            net.minecraft.world.entity.projectile.ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            this.setPos(x, y, z);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
