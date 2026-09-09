package com.breakinblocks.justtrialspawners.common.entity;

import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Breeze-fired wind charge - larger radius, no extra knockback multiplier.
 * Also triggers redstone components (buttons, levers, trapdoors, doors, fence gates).
 */
public class BreezeWindChargeProjectile extends AbstractWindChargeProjectile {
    private static final float RADIUS = 3.0F;

    public BreezeWindChargeProjectile(EntityType<? extends BreezeWindChargeProjectile> type, Level level) {
        super(type, level);
    }

    public BreezeWindChargeProjectile(BreezeEntity breeze, Level level) {
        super(ModEntities.BREEZE_WIND_CHARGE.get(), level, breeze,
                breeze.getX(), breeze.getSnoutYPosition(), breeze.getZ());
    }

    @Override
    protected void explode(Vec3 position) {
        WindChargeProjectile.windBurst(this.level(), position, RADIUS,
                JTSConfig.windChargeKnockbackMultiplier(), this);
        this.level().playSound(null, position.x(), position.y(), position.z(),
                ModSounds.BREEZE_WIND_CHARGE_BURST.get(), this.getSoundSource(), 1.0F, 1.0F);

        // Trigger nearby redstone-interactive blocks (buttons, levers, trapdoors, doors, fence gates)
        triggerNearbyBlocks(position);
    }

    private void triggerNearbyBlocks(Vec3 pos) {
        if (this.level().isClientSide) return;
        int range = 3;
        net.minecraft.core.BlockPos center = net.minecraft.core.BlockPos.containing(pos);
        for (net.minecraft.core.BlockPos blockPos : net.minecraft.core.BlockPos.betweenClosed(
                center.offset(-range, -range, -range), center.offset(range, range, range))) {
            net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(blockPos);
            net.minecraft.world.level.block.Block block = state.getBlock();

            // Toggle buttons
            if (block instanceof net.minecraft.world.level.block.ButtonBlock button) {
                if (!state.getValue(net.minecraft.world.level.block.ButtonBlock.POWERED)) {
                    button.press(state, this.level(), blockPos);
                }
            }
            // Toggle levers
            else if (block instanceof net.minecraft.world.level.block.LeverBlock lever) {
                net.minecraft.world.level.block.state.BlockState toggled = state.cycle(net.minecraft.world.level.block.LeverBlock.POWERED);
                this.level().setBlock(blockPos, toggled, 3);
                this.level().updateNeighborsAt(blockPos, block);
            }
            // Toggle trapdoors (non-iron only)
            else if (block instanceof net.minecraft.world.level.block.TrapDoorBlock
                    && !(block instanceof net.minecraft.world.level.block.IronBarsBlock)) {
                net.minecraft.world.level.block.state.BlockState toggled = state.cycle(net.minecraft.world.level.block.TrapDoorBlock.OPEN);
                this.level().setBlock(blockPos, toggled, 3);
                this.level().levelEvent(null, toggled.getValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN) ? 1013 : 1014, blockPos, 0);
            }
            // Toggle doors (non-iron)
            else if (block instanceof net.minecraft.world.level.block.DoorBlock
                    && !(block instanceof net.minecraft.world.level.block.IronBarsBlock)
                    && state.hasProperty(net.minecraft.world.level.block.DoorBlock.OPEN)
                    && !state.getValue(net.minecraft.world.level.block.DoorBlock.POWERED)) {
                net.minecraft.world.level.block.state.BlockState toggled = state.cycle(net.minecraft.world.level.block.DoorBlock.OPEN);
                this.level().setBlock(blockPos, toggled, 10);
                this.level().levelEvent(null, toggled.getValue(net.minecraft.world.level.block.DoorBlock.OPEN) ? 1012 : 1013, blockPos, 0);
            }
            // Toggle fence gates
            else if (block instanceof net.minecraft.world.level.block.FenceGateBlock) {
                net.minecraft.world.level.block.state.BlockState toggled = state.cycle(net.minecraft.world.level.block.FenceGateBlock.OPEN);
                this.level().setBlock(blockPos, toggled, 10);
                this.level().levelEvent(null, toggled.getValue(net.minecraft.world.level.block.FenceGateBlock.OPEN) ? 1008 : 1014, blockPos, 0);
            }
        }
    }
}
