package com.breakinblocks.justtrialspawners.common.entity.ai;

import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal for the Breeze to perform long jumps to reposition behind its target.
 * Adapted from 1.21's brain-based LongJump behavior.
 */
public class BreezeLongJumpGoal extends Goal {
    private static final int JUMP_COOLDOWN = 10;
    private static final int JUMP_COOLDOWN_HURT = 2;
    private static final int INHALING_DURATION = 10;
    private static final float MAX_JUMP_VELOCITY = 1.4F;
    private static final int REQUIRED_AIR_BLOCKS = 4;
    private static final int[] ALLOWED_ANGLES = {40, 55, 60, 75, 80};

    private final BreezeEntity breeze;
    private int cooldownTimer;
    private int inhalingTimer;
    private Vec3 jumpTarget;
    private Phase phase = Phase.IDLE;

    private enum Phase { IDLE, INHALING, JUMPING, LANDING }

    public BreezeLongJumpGoal(BreezeEntity breeze) {
        this.breeze = breeze;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.cooldownTimer > 0) {
            this.cooldownTimer--;
            return false;
        }
        LivingEntity target = this.breeze.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!this.breeze.onGround() && !this.breeze.isInWater()) return false;

        double dist = this.breeze.distanceTo(target);
        if (dist < 4.0 || dist > 24.0) return false;

        if (!canJumpFromCurrentPosition()) return false;

        // Calculate jump target - a point behind the target
        Vec3 targetPos = randomPointBehindTarget(target);
        BlockPos landingPos = snapToSurface(targetPos);
        if (landingPos == null) return false;

        // Check that we have line of sight to landing
        if (!hasLineOfSight(landingPos.getCenter())) return false;

        this.jumpTarget = landingPos.getCenter();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.phase != Phase.IDLE;
    }

    @Override
    public void start() {
        this.phase = Phase.INHALING;
        this.inhalingTimer = INHALING_DURATION;
        this.breeze.level().playSound(null, this.breeze,
                ModSounds.BREEZE_CHARGE.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        if (this.jumpTarget == null) {
            this.phase = Phase.IDLE;
            return;
        }

        switch (this.phase) {
            case INHALING -> {
                // Look at jump target during charge-up
                this.breeze.getLookControl().setLookAt(
                        this.jumpTarget.x, this.jumpTarget.y, this.jumpTarget.z);

                if (--this.inhalingTimer <= 0) {
                    // Calculate and execute jump
                    Vec3 jumpVec = calculateJumpVector(this.jumpTarget);
                    if (jumpVec != null) {
                        this.breeze.playSound(ModSounds.BREEZE_JUMP.get(), 1.0F, 1.0F);
                        this.breeze.setDeltaMovement(jumpVec);
                        this.breeze.setNoGravity(false);
                        this.phase = Phase.JUMPING;
                    } else {
                        this.phase = Phase.IDLE;
                    }
                }
            }
            case JUMPING -> {
                if (this.breeze.onGround() || this.breeze.isInWater()) {
                    this.breeze.playSound(ModSounds.BREEZE_LAND.get(), 1.0F, 1.0F);
                    this.phase = Phase.IDLE;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.phase = Phase.IDLE;
        this.jumpTarget = null;
        boolean wasHurt = this.breeze.getLastHurtByMobTimestamp() > this.breeze.tickCount - 20;
        this.cooldownTimer = wasHurt ? JUMP_COOLDOWN_HURT : JUMP_COOLDOWN;
    }

    private Vec3 randomPointBehindTarget(LivingEntity target) {
        Vec3 lookVec = target.getLookAngle().normalize();
        // Point behind the target (opposite of where they're looking), randomized
        double angle = (this.breeze.getRandom().nextDouble() - 0.5) * Math.PI; // +/- 90 degrees
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double distance = 4.0 + this.breeze.getRandom().nextDouble() * 4.0; // 4-8 blocks behind

        double dx = (-lookVec.x * cos - lookVec.z * sin) * distance;
        double dz = (-lookVec.z * cos + lookVec.x * sin) * distance;

        return target.position().add(dx, 0, dz);
    }

    private BlockPos snapToSurface(Vec3 pos) {
        ClipContext ctx = new ClipContext(pos, pos.add(0, -10, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.breeze);
        HitResult result = this.breeze.level().clip(ctx);
        if (result.getType() == HitResult.Type.BLOCK) {
            return BlockPos.containing(result.getLocation()).above();
        }
        return null;
    }

    private boolean hasLineOfSight(Vec3 target) {
        ClipContext ctx = new ClipContext(this.breeze.getEyePosition(), target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.breeze);
        HitResult result = this.breeze.level().clip(ctx);
        return result.getType() == HitResult.Type.MISS
                || result.getLocation().distanceToSqr(target) < 4.0;
    }

    private boolean canJumpFromCurrentPosition() {
        BlockPos pos = this.breeze.blockPosition();
        for (int i = 1; i <= REQUIRED_AIR_BLOCKS; i++) {
            BlockPos above = pos.above(i);
            if (!this.breeze.level().getBlockState(above).isAir()
                    && !this.breeze.level().getFluidState(above).is(FluidTags.WATER)) {
                return false;
            }
        }
        return true;
    }

    private Vec3 calculateJumpVector(Vec3 target) {
        Vec3 from = this.breeze.position();
        Vec3 diff = target.subtract(from);
        double horizontalDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        // Try different launch angles
        int[] shuffled = ALLOWED_ANGLES.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = this.breeze.getRandom().nextInt(i + 1);
            int temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }

        for (int angleDeg : shuffled) {
            double angleRad = Math.toRadians(angleDeg);
            double sinAngle = Math.sin(angleRad);
            double cosAngle = Math.cos(angleRad);

            // Calculate required velocity for ballistic trajectory
            double vy = horizontalDist * sinAngle / cosAngle;
            double speed = horizontalDist / (cosAngle * 2.0 * sinAngle / 0.08);

            if (speed > 0) {
                double velocity = Math.sqrt(speed * 0.08);
                if (velocity <= MAX_JUMP_VELOCITY) {
                    Vec3 horizontal = new Vec3(diff.x, 0, diff.z).normalize().scale(velocity * cosAngle);
                    return new Vec3(horizontal.x, velocity * sinAngle, horizontal.z);
                }
            }
        }

        // Fallback: simple arc
        double speed = Math.min(MAX_JUMP_VELOCITY, horizontalDist * 0.1);
        Vec3 horizontal = new Vec3(diff.x, 0, diff.z).normalize().scale(speed);
        return new Vec3(horizontal.x, Math.min(MAX_JUMP_VELOCITY, 0.5 + diff.y * 0.1), horizontal.z);
    }
}
