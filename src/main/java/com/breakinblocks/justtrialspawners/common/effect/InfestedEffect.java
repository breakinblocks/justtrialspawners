package com.breakinblocks.justtrialspawners.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Infested effect - when the entity takes damage, 10% chance to spawn 1-2 silverfish.
 */
public class InfestedEffect extends MobEffect {
    public InfestedEffect() {
        super(MobEffectCategory.HARMFUL, 0x99CC99);
    }
}
