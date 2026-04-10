package com.breakinblocks.justtrialspawners.common.item;

import com.breakinblocks.justtrialspawners.common.entity.WindChargeProjectile;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WindChargeItem extends Item {
    private static final int COOLDOWN = 10;

    public WindChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            WindChargeProjectile windCharge = new WindChargeProjectile(
                    player, level,
                    player.getX(), player.getEyeY(), player.getZ());
            windCharge.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(windCharge);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.WIND_CHARGE_THROW.get(), SoundSource.NEUTRAL,
                0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        player.getCooldowns().addCooldown(this, COOLDOWN);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
