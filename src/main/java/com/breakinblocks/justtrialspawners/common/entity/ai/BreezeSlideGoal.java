package com.breakinblocks.justtrialspawners.common.entity.ai;

import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal for the Breeze to slide along the ground to reposition.
 * Adapted from 1.21's brain-based Slide behavior.
 */
public class BreezeSlideGoal extends Goal {
    private static final float SLIDE_SPEED = 0.6F;

    private final BreezeEntity breeze;
    private Vec3 slideTarget;
    private int slideDuration;

    public BreezeSlideGoal(BreezeEntity breeze) {
        this.breeze = breeze;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.breeze.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!this.breeze.onGround() || this.breeze.isInWater()) return false;

        boolean withinInner = this.breeze.withinInnerCircleRange(target.position());

        Vec3 destination = null;

        if (withinInner) {
            // Move away from the target
            Vec3 awayPos = DefaultRandomPos.getPosAway(this.breeze, 5, 5, target.position());
            if (awayPos != null && target.distanceToSqr(awayPos.x, awayPos.y, awayPos.z) > target.distanceToSqr(this.breeze)) {
                destination = awayPos;
            }
        }

        if (destination == null) {
            // Move to a random point behind the target or in the middle circle
            if (this.breeze.getRandom().nextBoolean()) {
                destination = randomPointBehindTarget(target);
            } else {
                destination = randomPointInMiddleCircle(target);
            }
        }

        if (destination == null) return false;

        this.slideTarget = destination;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.slideDuration > 0 && this.breeze.getTarget() != null;
    }

    @Override
    public void start() {
        this.breeze.playSound(ModSounds.BREEZE_SLIDE.get(), 1.0F, 1.0F);
        this.breeze.getNavigation().moveTo(
                this.slideTarget.x, this.slideTarget.y, this.slideTarget.z, SLIDE_SPEED);
        this.slideDuration = 20 + this.breeze.getRandom().nextInt(20);
    }

    @Override
    public void tick() {
        this.slideDuration--;
        if (this.breeze.getNavigation().isDone() && this.slideDuration > 0) {
            this.slideDuration = 0;
        }
    }

    @Override
    public void stop() {
        this.slideTarget = null;
    }

    private Vec3 randomPointBehindTarget(LivingEntity target) {
        Vec3 look = target.getLookAngle().normalize();
        double angle = (this.breeze.getRandom().nextDouble() - 0.5) * Math.PI;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dist = 4.0 + this.breeze.getRandom().nextDouble() * 4.0;

        double dx = (-look.x * cos - look.z * sin) * dist;
        double dz = (-look.z * cos + look.x * sin) * dist;

        return target.position().add(dx, 0, dz);
    }

    private Vec3 randomPointInMiddleCircle(LivingEntity target) {
        Vec3 diff = target.position().subtract(this.breeze.position());
        double dist = diff.length() - Mth.lerp(this.breeze.getRandom().nextDouble(), 8.0, 4.0);
        Vec3 direction = diff.normalize().scale(dist);
        return this.breeze.position().add(direction);
    }
}
