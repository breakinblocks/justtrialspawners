package com.breakinblocks.justtrialspawners.common.entity.ai;

import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.breakinblocks.justtrialspawners.common.entity.BreezeWindChargeProjectile;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal for the Breeze to shoot wind charges at its target.
 * Adapted from 1.21's brain-based Shoot behavior.
 */
public class BreezeShootGoal extends Goal {
    private static final int ATTACK_RANGE_MIN_SQ = 4;
    private static final int ATTACK_RANGE_MAX_SQ = 256;
    private static final int CHARGE_TICKS = 15;
    private static final int RECOVER_TICKS = 4;
    private static final int COOLDOWN_TICKS = 10;
    private static final float PROJECTILE_SPEED = 0.7F;
    private static final int UNCERTAINTY_BASE = 5;
    private static final int UNCERTAINTY_MULTIPLIER = 4;

    private final BreezeEntity breeze;
    private int chargeTimer;
    private int cooldownTimer;
    private boolean hasShot;

    public BreezeShootGoal(BreezeEntity breeze) {
        this.breeze = breeze;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.breeze.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (this.cooldownTimer > 0) {
            this.cooldownTimer--;
            return false;
        }
        return isTargetWithinRange(target) && this.breeze.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.breeze.getTarget();
        return target != null && target.isAlive() && !this.hasShot;
    }

    @Override
    public void start() {
        this.chargeTimer = CHARGE_TICKS;
        this.hasShot = false;
        this.breeze.playSound(ModSounds.BREEZE_INHALE.get(), 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        LivingEntity target = this.breeze.getTarget();
        if (target == null) return;

        this.breeze.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.chargeTimer > 0) {
            this.chargeTimer--;
            return;
        }

        // Fire!
        if (isFacingTarget(target)) {
            double dx = target.getX() - this.breeze.getX();
            double dy = target.getY(target.isPassenger() ? 0.8 : 0.3) - this.breeze.getY(0.5);
            double dz = target.getZ() - this.breeze.getZ();

            if (this.breeze.level() instanceof ServerLevel serverLevel) {
                BreezeWindChargeProjectile charge = new BreezeWindChargeProjectile(this.breeze, serverLevel);
                int difficulty = serverLevel.getDifficulty().getId();
                float inaccuracy = Math.max(1, UNCERTAINTY_BASE - difficulty * UNCERTAINTY_MULTIPLIER);
                charge.shoot(dx, dy, dz, PROJECTILE_SPEED, inaccuracy);
                serverLevel.addFreshEntity(charge);
                this.breeze.playSound(ModSounds.BREEZE_SHOOT.get(), 1.5F, 1.0F);
            }
        }

        this.hasShot = true;
    }

    @Override
    public void stop() {
        this.cooldownTimer = COOLDOWN_TICKS;
    }

    private boolean isTargetWithinRange(LivingEntity target) {
        double distSq = this.breeze.distanceToSqr(target);
        return distSq > ATTACK_RANGE_MIN_SQ && distSq < ATTACK_RANGE_MAX_SQ;
    }

    private boolean isFacingTarget(LivingEntity target) {
        Vec3 viewVec = this.breeze.getViewVector(1.0F);
        Vec3 toTarget = target.position().subtract(this.breeze.position()).normalize();
        return viewVec.dot(toTarget) > 0.5;
    }
}
