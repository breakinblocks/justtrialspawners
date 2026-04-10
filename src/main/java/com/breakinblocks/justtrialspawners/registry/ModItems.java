package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.item.OminousBottleItem;
import com.breakinblocks.justtrialspawners.common.item.WindChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<Item> TRIAL_KEY = ITEMS.register("trial_key",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> OMINOUS_TRIAL_KEY = ITEMS.register("ominous_trial_key",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BREEZE_ROD = ITEMS.register("breeze_rod",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WIND_CHARGE = ITEMS.register("wind_charge",
            () -> new WindChargeItem(new Item.Properties()));

    public static final RegistryObject<Item> OMINOUS_BOTTLE = ITEMS.register("ominous_bottle",
            () -> new OminousBottleItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(64)));

    public static final RegistryObject<ForgeSpawnEggItem> BREEZE_SPAWN_EGG = ITEMS.register("breeze_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BREEZE, 0x957B9D, 0x553B56, new Item.Properties()));

    public static final RegistryObject<ForgeSpawnEggItem> BOGGED_SPAWN_EGG = ITEMS.register("bogged_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BOGGED, 0x8A6B4F, 0x5A7339, new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
