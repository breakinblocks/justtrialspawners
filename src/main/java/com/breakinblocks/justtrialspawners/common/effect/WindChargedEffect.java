package com.breakinblocks.justtrialspawners.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Wind Charged effect - on death, creates a wind burst at the entity's position.
 */
public class WindChargedEffect extends MobEffect {
    public WindChargedEffect() {
        super(MobEffectCategory.HARMFUL, 0x99CBFF);
    }
}
