package com.breakinblocks.justtrialspawners.migration;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import com.breakinblocks.justtrialspawners.registry.ModBlocks;
import com.breakinblocks.justtrialspawners.registry.ModEnchantments;
import com.breakinblocks.justtrialspawners.registry.ModEntities;
import com.breakinblocks.justtrialspawners.registry.ModItems;
import com.breakinblocks.justtrialspawners.registry.ModMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles registry remapping from the Trials mod (modid: "trials") to Just Trial Spawners.
 * MissingMappingsEvent fires on the Forge event bus (NOT the mod bus), so this handler
 * is registered manually in the main mod class.
 */
public class TrialsMigrationHandler {

    private static final String TRIALS_NAMESPACE = "trials";

    /**
     * Mapping of old Trials mod block entity type IDs to our block entity type IDs.
     * Used by the ChunkDataEvent.Load handler to rewrite block entity NBT before vanilla
     * tries to look them up in the registry.
     */
    private static final Map<String, String> BLOCK_ENTITY_ID_REMAPS = new HashMap<>();
    static {
        BLOCK_ENTITY_ID_REMAPS.put("trials:trials_spawner", "justtrialspawners:trial_spawner");
        BLOCK_ENTITY_ID_REMAPS.put("trials:trials_vault", "justtrialspawners:vault");
    }

    /**
     * Mapping of old Trials mod block IDs (in chunk palette Name fields) to our block IDs.
     * Used by the ChunkDataEvent.Load handler to rewrite block palette entries before vanilla
     * tries to look them up in the registry.
     */
    private static final Map<String, String> BLOCK_PALETTE_REMAPS = new HashMap<>();
    static {
        BLOCK_PALETTE_REMAPS.put("trials:trial_spawner", "justtrialspawners:trial_spawner");
        BLOCK_PALETTE_REMAPS.put("trials:trial_vault", "justtrialspawners:vault");
        BLOCK_PALETTE_REMAPS.put("trials:trial_vault_ominous", "justtrialspawners:vault");
    }

    /**
     * Mapping of old Trials mod spawn egg item IDs to our spawn egg item IDs.
     * Used when extracting entity type from SpawnEgg NBT in trial spawner conversion.
     */
    private static final Map<String, String> SPAWN_EGG_ITEM_REMAPS = new HashMap<>();
    static {
        SPAWN_EGG_ITEM_REMAPS.put("trials:bogged_spawn_egg", "justtrialspawners:bogged_spawn_egg");
        SPAWN_EGG_ITEM_REMAPS.put("trials:breeze_spawn_egg", "justtrialspawners:breeze_spawn_egg");
    }

    public static Map<String, String> getSpawnEggItemRemaps() {
        return SPAWN_EGG_ITEM_REMAPS;
    }

    @SubscribeEvent
    public static void onMissingBlockMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "trial_spawner" -> {
                    mapping.remap(ModBlocks.TRIAL_SPAWNER.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_spawner -> justtrialspawners:trial_spawner");
                }
                case "trial_vault", "trial_vault_ominous" -> {
                    mapping.remap(ModBlocks.VAULT.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:{} -> justtrialspawners:vault", path);
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "trial_spawner" -> {
                    mapping.remap(ModBlocks.TRIAL_SPAWNER_ITEM.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_spawner item -> justtrialspawners:trial_spawner");
                }
                case "trial_vault", "trial_vault_ominous" -> {
                    mapping.remap(ModBlocks.VAULT_ITEM.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:{} item -> justtrialspawners:vault", path);
                }
                case "trial_key" -> {
                    mapping.remap(ModItems.TRIAL_KEY.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_key -> justtrialspawners:trial_key");
                }
                case "trial_key_ominous" -> {
                    mapping.remap(ModItems.OMINOUS_TRIAL_KEY.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_key_ominous -> justtrialspawners:ominous_trial_key");
                }
                case "breeze_rod" -> {
                    mapping.remap(ModItems.BREEZE_ROD.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:breeze_rod -> justtrialspawners:breeze_rod");
                }
                case "wind_charge" -> {
                    mapping.remap(ModItems.WIND_CHARGE.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:wind_charge -> justtrialspawners:wind_charge");
                }
                case "trial_bottle" -> {
                    mapping.remap(ModItems.OMINOUS_BOTTLE.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_bottle -> justtrialspawners:ominous_bottle");
                }
                case "breeze_spawn_egg" -> {
                    mapping.remap(ModItems.BREEZE_SPAWN_EGG.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:breeze_spawn_egg -> justtrialspawners:breeze_spawn_egg");
                }
                case "bogged_spawn_egg" -> {
                    mapping.remap(ModItems.BOGGED_SPAWN_EGG.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:bogged_spawn_egg -> justtrialspawners:bogged_spawn_egg");
                }
            }
        }

        for (MissingMappingsEvent.Mapping<EntityType<?>> mapping : event.getMappings(ForgeRegistries.Keys.ENTITY_TYPES, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "breeze" -> {
                    mapping.remap(ModEntities.BREEZE.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:breeze entity -> justtrialspawners:breeze");
                }
                case "bogged" -> {
                    mapping.remap(ModEntities.BOGGED.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:bogged entity -> justtrialspawners:bogged");
                }
                case "wind_charge" -> {
                    mapping.remap(ModEntities.WIND_CHARGE.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:wind_charge entity -> justtrialspawners:wind_charge");
                }
            }
        }

        for (MissingMappingsEvent.Mapping<BlockEntityType<?>> mapping : event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "trials_spawner" -> {
                    mapping.remap(ModBlockEntities.TRIAL_SPAWNER.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trials_spawner block entity -> justtrialspawners:trial_spawner");
                }
                case "trials_vault" -> {
                    mapping.remap(ModBlockEntities.VAULT.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trials_vault block entity -> justtrialspawners:vault");
                }
            }
        }

        for (MissingMappingsEvent.Mapping<MobEffect> mapping : event.getMappings(ForgeRegistries.Keys.MOB_EFFECTS, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "winded" -> {
                    mapping.remap(ModMobEffects.WIND_CHARGED.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:winded -> justtrialspawners:wind_charged");
                }
                case "trial_curse" -> {
                    mapping.remap(ModMobEffects.TRIAL_OMEN.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:trial_curse -> justtrialspawners:trial_omen");
                }
                case "oozing" -> {
                    mapping.remap(ModMobEffects.OOZING.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:oozing -> justtrialspawners:oozing");
                }
                case "infested" -> {
                    mapping.remap(ModMobEffects.INFESTED.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:infested -> justtrialspawners:infested");
                }
                case "weaving" -> {
                    mapping.remap(ModMobEffects.WEAVING.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:weaving -> justtrialspawners:weaving");
                }
            }
        }

        for (MissingMappingsEvent.Mapping<Enchantment> mapping : event.getMappings(ForgeRegistries.Keys.ENCHANTMENTS, TRIALS_NAMESPACE)) {
            String path = mapping.getKey().getPath();
            switch (path) {
                case "density" -> {
                    mapping.remap(ModEnchantments.DENSITY.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:density -> justtrialspawners:density");
                }
                case "breach" -> {
                    mapping.remap(ModEnchantments.BREACH.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:breach -> justtrialspawners:breach");
                }
                case "wind_burst" -> {
                    mapping.remap(ModEnchantments.WIND_BURST.get());
                    JustTrialSpawners.LOGGER.info("Remapped trials:wind_burst -> justtrialspawners:wind_burst");
                }
            }
        }
    }

    /**
     * Rewrites block palette entries and block entity NBT IDs in raw chunk NBT.
     * Called from ChunkSerializerMixin BEFORE vanilla deserializes the chunk.
     * Vanilla looks up blocks and block entity types by string, and MissingMappingsEvent
     * remapping doesn't expose the old key as an alias, so we rewrite the raw chunk NBT
     * before the palette is processed.
     */
    public static void rewriteChunkNbt(CompoundTag chunkTag) {
        if (chunkTag == null) return;

        // Rewrite block palette Names in each section
        Tag sectionsTag = chunkTag.get("sections");
        if (sectionsTag instanceof ListTag sections) {
            for (int i = 0; i < sections.size(); i++) {
                CompoundTag section = sections.getCompound(i);
                if (!section.contains("block_states")) continue;
                CompoundTag blockStates = section.getCompound("block_states");
                if (!blockStates.contains("palette")) continue;
                ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
                for (int j = 0; j < palette.size(); j++) {
                    CompoundTag paletteEntry = palette.getCompound(j);
                    String oldName = paletteEntry.getString("Name");
                    String newName = BLOCK_PALETTE_REMAPS.get(oldName);
                    if (newName != null) {
                        paletteEntry.putString("Name", newName);
                        boolean wasOminousVault = "trials:trial_vault_ominous".equals(oldName);
                        paletteEntry.remove("Properties");
                        if (wasOminousVault) {
                            CompoundTag props = new CompoundTag();
                            props.putString("ominous", "true");
                            paletteEntry.put("Properties", props);
                        }
                    }
                }
            }
        }

        // Rewrite block entity IDs
        Tag blockEntitiesTag = chunkTag.get("block_entities");
        if (blockEntitiesTag instanceof ListTag blockEntities) {
            for (int i = 0; i < blockEntities.size(); i++) {
                CompoundTag be = blockEntities.getCompound(i);
                String oldId = be.getString("id");
                String newId = BLOCK_ENTITY_ID_REMAPS.get(oldId);
                if (newId != null) {
                    be.putString("id", newId);
                }
            }
        }
    }
}
