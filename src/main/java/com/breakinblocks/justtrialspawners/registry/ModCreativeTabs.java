package com.breakinblocks.justtrialspawners.registry;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.item.OminousBottleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, JustTrialSpawners.MOD_ID);

    public static final RegistryObject<CreativeModeTab> JTS_TAB = TABS.register("justtrialspawners",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.TRIAL_SPAWNER.get()))
                    .title(Component.translatable("itemGroup.justtrialspawners"))
                    .displayItems((params, output) -> {
                        // Blocks and keys
                        output.accept(ModBlocks.TRIAL_SPAWNER_ITEM.get());
                        output.accept(ModBlocks.VAULT_ITEM.get());
                        output.accept(ModItems.TRIAL_KEY.get());
                        output.accept(ModItems.OMINOUS_TRIAL_KEY.get());
                        output.accept(ModItems.BREEZE_ROD.get());
                        output.accept(ModItems.WIND_CHARGE.get());
                        for (int i = 0; i <= OminousBottleItem.MAX_AMPLIFIER; i++) {
                            output.accept(OminousBottleItem.createWithAmplifier(ModItems.OMINOUS_BOTTLE.get(), i));
                        }
                        output.accept(ModItems.BREEZE_SPAWN_EGG.get());
                        output.accept(ModItems.BOGGED_SPAWN_EGG.get());

                        // Pre-configured trial spawners for all hostile mobs
                        output.accept(createConfiguredSpawner("minecraft:zombie"));
                        output.accept(createConfiguredSpawner("minecraft:skeleton"));
                        output.accept(createConfiguredSpawner("minecraft:creeper"));
                        output.accept(createConfiguredSpawner("minecraft:spider"));
                        output.accept(createConfiguredSpawner("minecraft:cave_spider"));
                        output.accept(createConfiguredSpawner("minecraft:enderman"));
                        output.accept(createConfiguredSpawner("minecraft:witch"));
                        output.accept(createConfiguredSpawner("minecraft:slime"));
                        output.accept(createConfiguredSpawner("minecraft:magma_cube"));
                        output.accept(createConfiguredSpawner("minecraft:blaze"));
                        output.accept(createConfiguredSpawner("minecraft:ghast"));
                        output.accept(createConfiguredSpawner("minecraft:wither_skeleton"));
                        output.accept(createConfiguredSpawner("minecraft:stray"));
                        output.accept(createConfiguredSpawner("minecraft:husk"));
                        output.accept(createConfiguredSpawner("minecraft:drowned"));
                        output.accept(createConfiguredSpawner("minecraft:zombie_villager"));
                        output.accept(createConfiguredSpawner("minecraft:phantom"));
                        output.accept(createConfiguredSpawner("minecraft:pillager"));
                        output.accept(createConfiguredSpawner("minecraft:vindicator"));
                        output.accept(createConfiguredSpawner("minecraft:evoker"));
                        output.accept(createConfiguredSpawner("minecraft:ravager"));
                        output.accept(createConfiguredSpawner("minecraft:vex"));
                        output.accept(createConfiguredSpawner("minecraft:guardian"));
                        output.accept(createConfiguredSpawner("minecraft:elder_guardian"));
                        output.accept(createConfiguredSpawner("minecraft:shulker"));
                        output.accept(createConfiguredSpawner("minecraft:hoglin"));
                        output.accept(createConfiguredSpawner("minecraft:zoglin"));
                        output.accept(createConfiguredSpawner("minecraft:piglin_brute"));
                        output.accept(createConfiguredSpawner("minecraft:piglin"));
                        output.accept(createConfiguredSpawner("minecraft:zombified_piglin"));
                        output.accept(createConfiguredSpawner("minecraft:endermite"));
                        output.accept(createConfiguredSpawner("minecraft:silverfish"));
                        output.accept(createConfiguredSpawner("minecraft:warden"));
                        output.accept(createConfiguredSpawner("justtrialspawners:breeze"));
                        output.accept(createConfiguredSpawner("justtrialspawners:bogged"));
                        output.accept(createConfiguredSpawner("minecraft:wither"));
                    })
                    .build());

    /**
     * Creates a trial spawner item pre-configured with the given entity type.
     */
    public static ItemStack createConfiguredSpawner(String entityId) {
        ItemStack stack = new ItemStack(ModBlocks.TRIAL_SPAWNER_ITEM.get());

        CompoundTag configTag = new CompoundTag();
        configTag.putInt("spawn_range", 4);
        configTag.putFloat("total_mobs", 1.0f);
        configTag.putFloat("simultaneous_mobs", 1.0f);
        configTag.putFloat("total_mobs_added_per_player", 1.0f);
        configTag.putFloat("simultaneous_mobs_added_per_player", 1.0f);
        configTag.putInt("ticks_between_spawn", 40);

        ListTag potentials = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putInt("weight", 1);
        CompoundTag dataTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", entityId);
        dataTag.put("entity", entityTag);
        entry.put("data", dataTag);
        potentials.add(entry);
        configTag.put("spawn_potentials", potentials);

        CompoundTag spawnerTag = new CompoundTag();
        spawnerTag.put("normal_config", configTag);
        spawnerTag.put("ominous_config", configTag.copy());
        spawnerTag.put("data", new CompoundTag());
        spawnerTag.putBoolean("is_ominous", false);
        spawnerTag.putInt("target_cooldown_length", 36000);
        spawnerTag.putInt("required_player_range", 14);

        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.put("trial_spawner", spawnerTag);
        stack.getOrCreateTag().put("BlockEntityTag", blockEntityTag);

        return stack;
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
