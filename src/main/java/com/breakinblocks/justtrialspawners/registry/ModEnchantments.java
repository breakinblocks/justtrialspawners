package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.enchantment.BreachEnchantment;
import com.breakinblocks.justtrialspawners.common.enchantment.DensityEnchantment;
import com.breakinblocks.justtrialspawners.common.enchantment.WindBurstEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(
            ForgeRegistries.ENCHANTMENTS, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<Enchantment> DENSITY = ENCHANTMENTS.register("density",
            DensityEnchantment::new);

    public static final RegistryObject<Enchantment> BREACH = ENCHANTMENTS.register("breach",
            BreachEnchantment::new);

    public static final RegistryObject<Enchantment> WIND_BURST = ENCHANTMENTS.register("wind_burst",
            WindBurstEnchantment::new);

    public static void register(IEventBus modEventBus) {
        ENCHANTMENTS.register(modEventBus);
    }
}
