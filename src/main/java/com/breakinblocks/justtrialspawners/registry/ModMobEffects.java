package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.effect.InfestedEffect;
import com.breakinblocks.justtrialspawners.common.effect.OozingEffect;
import com.breakinblocks.justtrialspawners.common.effect.TrialOmenEffect;
import com.breakinblocks.justtrialspawners.common.effect.WeavingEffect;
import com.breakinblocks.justtrialspawners.common.effect.WindChargedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(
            ForgeRegistries.MOB_EFFECTS, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<MobEffect> TRIAL_OMEN = MOB_EFFECTS.register("trial_omen",
            TrialOmenEffect::new);

    public static final RegistryObject<MobEffect> WIND_CHARGED = MOB_EFFECTS.register("wind_charged",
            WindChargedEffect::new);

    public static final RegistryObject<MobEffect> WEAVING = MOB_EFFECTS.register("weaving",
            WeavingEffect::new);

    public static final RegistryObject<MobEffect> OOZING = MOB_EFFECTS.register("oozing",
            OozingEffect::new);

    public static final RegistryObject<MobEffect> INFESTED = MOB_EFFECTS.register("infested",
            InfestedEffect::new);

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
