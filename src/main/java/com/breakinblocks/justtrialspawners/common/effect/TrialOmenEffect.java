package com.breakinblocks.justtrialspawners.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Trial Omen effect - applied when a player with Bad Omen approaches a trial spawner.
 * Converted from Bad Omen at a rate of 18000 ticks per amplifier level.
 * While active, nearby trial spawners become ominous.
 */
public class TrialOmenEffect extends MobEffect {
    public TrialOmenEffect() {
        super(MobEffectCategory.NEUTRAL, 0x1E1F21);
    }
}
