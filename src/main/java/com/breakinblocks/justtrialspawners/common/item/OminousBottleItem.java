package com.breakinblocks.justtrialspawners.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Ominous Bottle - consumable that applies Bad Omen when drunk.
 * Has an amplifier level (0-4) stored in NBT that determines the Bad Omen level.
 * Duration: 120 seconds per amplifier level (2400 ticks * (amplifier + 1)).
 */
public class OminousBottleItem extends Item {
    private static final int DRINK_DURATION = 32;
    public static final String TAG_AMPLIFIER = "ominous_bottle_amplifier";
    public static final int MAX_AMPLIFIER = 4;
    private static final int TICKS_PER_LEVEL = 2400; // 120 seconds

    public OminousBottleItem(Properties properties) {
        super(properties);
    }

    public static int getAmplifier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_AMPLIFIER)) {
            return Math.min(tag.getInt(TAG_AMPLIFIER), MAX_AMPLIFIER);
        }
        return 0;
    }

    public static ItemStack createWithAmplifier(Item item, int amplifier) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_AMPLIFIER, Math.min(amplifier, MAX_AMPLIFIER));
        return stack;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        if (!level.isClientSide) {
            int amplifier = getAmplifier(stack);
            int duration = TICKS_PER_LEVEL * (amplifier + 1);
            entity.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, duration, amplifier));
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int amplifier = getAmplifier(stack);
        int seconds = (TICKS_PER_LEVEL * (amplifier + 1)) / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        MutableComponent effectLine = Component.translatable("effect.minecraft.bad_omen");
        if (amplifier > 0) {
            effectLine.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
        }
        tooltip.add(effectLine.withStyle(ChatFormatting.BLUE));

        String timeStr = remainingSeconds > 0
                ? String.format("%d:%02d", minutes, remainingSeconds)
                : String.format("%d:00", minutes);
        tooltip.add(Component.literal(timeStr).withStyle(ChatFormatting.BLUE));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
