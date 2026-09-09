package com.breakinblocks.justtrialspawners.common.entity;

import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Player-thrown wind charge - smaller radius, applies knockback multiplier.
 */
public class WindChargeProjectile extends AbstractWindChargeProjectile {
    private static final float RADIUS = 1.2F;
    private static final float KNOCKBACK_MULTIPLIER = 1.22F;

    public WindChargeProjectile(EntityType<? extends WindChargeProjectile> type, Level level) {
        super(type, level);
    }

    public WindChargeProjectile(Player owner, Level level, double x, double y, double z) {
        super(ModEntities.WIND_CHARGE.get(), level, owner, x, y, z);
    }

    @Override
    protected void explode(Vec3 position) {
        windBurst(this.level(), position, RADIUS,
                KNOCKBACK_MULTIPLIER * JTSConfig.windChargeKnockbackMultiplier(), this);
        this.level().playSound(null, position.x(), position.y(), position.z(),
                ModSounds.WIND_CHARGE_BURST.get(), this.getSoundSource(), 1.0F, 1.0F);
    }

    /**
     * Creates a wind burst at the given position - applies knockback to nearby entities without destroying blocks.
     * Mimics vanilla explosion knockback formula for authentic wind charge behavior.
     */
    public static void windBurst(Level level, Vec3 center, float radius, float knockbackMultiplier, Entity source) {
        float diameter = radius * 2.0F;
        AABB area = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);

        List<Entity> entities = level.getEntities(source, area, e -> e.isAlive() && !(e instanceof AbstractWindChargeProjectile));

        for (Entity entity : entities) {
            // Use vanilla formula: normalize distance by diameter (not radius)
            double dist = Math.sqrt(entity.distanceToSqr(center));
            double exposure = dist / (double) diameter;
            if (exposure > 1.0) continue;

            // Skip flying creative players
            if (entity instanceof Player player && player.getAbilities().flying) continue;

            // Direction from center to entity eye height (vanilla uses eye Y for upward launch)
            double dx = entity.getX() - center.x;
            double dy = entity.getEyeY() - center.y;
            double dz = entity.getZ() - center.z;
            double dirLen = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dirLen == 0) continue;

            dx /= dirLen;
            dy /= dirLen;
            dz /= dirLen;

            // Vanilla: seen percent ray-cast for exposure through blocks
            double seenPercent = net.minecraft.world.level.Explosion.getSeenPercent(center, entity);
            double knockbackStrength = (1.0 - exposure) * seenPercent;

            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    dx * knockbackStrength * knockbackMultiplier,
                    dy * knockbackStrength * knockbackMultiplier,
                    dz * knockbackStrength * knockbackMultiplier));
            entity.hurtMarked = true;
        }
    }
}
