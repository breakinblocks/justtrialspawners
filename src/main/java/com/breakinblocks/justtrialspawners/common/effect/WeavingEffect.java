package com.breakinblocks.justtrialspawners.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Weaving effect - on death, places 2-3 cobwebs in random nearby air blocks.
 */
public class WeavingEffect extends MobEffect {
    public WeavingEffect() {
        super(MobEffectCategory.HARMFUL, 0x665544);
    }
}
