package com.breakinblocks.justtrialspawners.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Oozing effect - on death, spawns 2 small slimes at the entity's position.
 */
public class OozingEffect extends MobEffect {
    public OozingEffect() {
        super(MobEffectCategory.HARMFUL, 0x99CC00);
    }
}
