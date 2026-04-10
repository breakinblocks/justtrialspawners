package com.breakinblocks.justtrialspawners.common.block.entity.trialspawner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.SpawnData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for a trial spawner - defines spawn behavior, mob counts, and loot.
 */
public class TrialSpawnerConfig {
    public static final TrialSpawnerConfig DEFAULT = new TrialSpawnerConfig();
    public static final String TAG_SPAWN_RANGE = "spawn_range";
    public static final String TAG_TOTAL_MOBS = "total_mobs";
    public static final String TAG_SIMULTANEOUS_MOBS = "simultaneous_mobs";
    public static final String TAG_TOTAL_MOBS_ADDED_PER_PLAYER = "total_mobs_added_per_player";
    public static final String TAG_SIMULTANEOUS_MOBS_ADDED_PER_PLAYER = "simultaneous_mobs_added_per_player";
    public static final String TAG_TICKS_BETWEEN_SPAWN = "ticks_between_spawn";
    public static final String TAG_SPAWN_POTENTIALS = "spawn_potentials";
    public static final String TAG_LOOT_TABLES_TO_EJECT = "loot_tables_to_eject";
    public static final String TAG_ITEMS_TO_DROP_WHEN_OMINOUS = "items_to_drop_when_ominous";

    private int spawnRange = 4;
    private float totalMobs = 1.0f;
    private float simultaneousMobs = 1.0f;
    private float totalMobsAddedPerPlayer = 1.0f;
    private float simultaneousMobsAddedPerPlayer = 1.0f;
    private int ticksBetweenSpawn = 40;
    private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
    private List<ResourceLocation> lootTablesToEject = new ArrayList<>();
    private ResourceLocation itemsToDropWhenOminous = new ResourceLocation("justtrialspawners", "spawners/trial_chamber/items_to_drop_when_ominous");

    public TrialSpawnerConfig() {
        // Default spawn potential - zombie with weight 1
        SpawnData defaultSpawnData = new SpawnData();
        defaultSpawnData.getEntityToSpawn().putString("id", "minecraft:zombie");
        SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
        builder.add(defaultSpawnData, 1);
        this.spawnPotentials = builder.build();

        // Spawner ejects consumables + keys (NOT vault rewards - those come from the vault block)
        lootTablesToEject.add(new ResourceLocation("justtrialspawners", "spawners/trial_chamber/consumables"));
        lootTablesToEject.add(new ResourceLocation("justtrialspawners", "spawners/trial_chamber/key"));
    }

    /**
     * Creates a config for ominous trial spawners.
     * Uses ominous consumables + ominous key tables for ejection.
     */
    public static TrialSpawnerConfig createOminousDefault() {
        TrialSpawnerConfig config = new TrialSpawnerConfig();
        config.lootTablesToEject.clear();
        config.lootTablesToEject.add(new ResourceLocation("justtrialspawners", "spawners/ominous/trial_chamber/consumables"));
        config.lootTablesToEject.add(new ResourceLocation("justtrialspawners", "spawners/ominous/trial_chamber/key"));
        return config;
    }

    public int spawnRange() { return spawnRange; }
    public float totalMobs() { return totalMobs; }
    public float simultaneousMobs() { return simultaneousMobs; }
    public float totalMobsAddedPerPlayer() { return totalMobsAddedPerPlayer; }
    public float simultaneousMobsAddedPerPlayer() { return simultaneousMobsAddedPerPlayer; }
    public int ticksBetweenSpawn() { return ticksBetweenSpawn; }
    public SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition() { return spawnPotentials; }
    public List<ResourceLocation> lootTablesToEject() { return lootTablesToEject; }
    public ResourceLocation itemsToDropWhenOminous() { return itemsToDropWhenOminous; }
    public long ticksBetweenItemSpawners() { return 160L; }

    public int calculateTargetTotalMobs(int additionalPlayers) {
        return (int) Math.floor(this.totalMobs + this.totalMobsAddedPerPlayer * additionalPlayers);
    }

    public int calculateTargetSimultaneousMobs(int additionalPlayers) {
        return (int) Math.floor(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * additionalPlayers);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_SPAWN_RANGE, spawnRange);
        tag.putFloat(TAG_TOTAL_MOBS, totalMobs);
        tag.putFloat(TAG_SIMULTANEOUS_MOBS, simultaneousMobs);
        tag.putFloat(TAG_TOTAL_MOBS_ADDED_PER_PLAYER, totalMobsAddedPerPlayer);
        tag.putFloat(TAG_SIMULTANEOUS_MOBS_ADDED_PER_PLAYER, simultaneousMobsAddedPerPlayer);
        tag.putInt(TAG_TICKS_BETWEEN_SPAWN, ticksBetweenSpawn);

        if (!spawnPotentials.isEmpty()) {
            ListTag potentialsTag = new ListTag();
            spawnPotentials.unwrap().forEach(entry -> {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putInt("weight", entry.getWeight().asInt());
                CompoundTag dataTag = new CompoundTag();
                CompoundTag entityTag = entry.getData().getEntityToSpawn();
                dataTag.put("entity", entityTag.copy());
                entry.getData().getCustomSpawnRules().ifPresent(rules -> {
                    CompoundTag rulesTag = new CompoundTag();
                    dataTag.put("custom_spawn_rules", rulesTag);
                });
                entryTag.put("data", dataTag);
                potentialsTag.add(entryTag);
            });
            tag.put(TAG_SPAWN_POTENTIALS, potentialsTag);
        }

        if (!lootTablesToEject.isEmpty()) {
            ListTag lootTag = new ListTag();
            for (ResourceLocation rl : lootTablesToEject) {
                lootTag.add(StringTag.valueOf(rl.toString()));
            }
            tag.put(TAG_LOOT_TABLES_TO_EJECT, lootTag);
        }

        tag.putString(TAG_ITEMS_TO_DROP_WHEN_OMINOUS, itemsToDropWhenOminous.toString());

        return tag;
    }

    public static TrialSpawnerConfig load(CompoundTag tag) {
        TrialSpawnerConfig config = new TrialSpawnerConfig();
        if (tag.contains(TAG_SPAWN_RANGE)) config.spawnRange = tag.getInt(TAG_SPAWN_RANGE);
        if (tag.contains(TAG_TOTAL_MOBS)) config.totalMobs = tag.getFloat(TAG_TOTAL_MOBS);
        if (tag.contains(TAG_SIMULTANEOUS_MOBS)) config.simultaneousMobs = tag.getFloat(TAG_SIMULTANEOUS_MOBS);
        if (tag.contains(TAG_TOTAL_MOBS_ADDED_PER_PLAYER)) config.totalMobsAddedPerPlayer = tag.getFloat(TAG_TOTAL_MOBS_ADDED_PER_PLAYER);
        if (tag.contains(TAG_SIMULTANEOUS_MOBS_ADDED_PER_PLAYER)) config.simultaneousMobsAddedPerPlayer = tag.getFloat(TAG_SIMULTANEOUS_MOBS_ADDED_PER_PLAYER);
        if (tag.contains(TAG_TICKS_BETWEEN_SPAWN)) config.ticksBetweenSpawn = tag.getInt(TAG_TICKS_BETWEEN_SPAWN);

        if (tag.contains(TAG_SPAWN_POTENTIALS)) {
            ListTag potentialsTag = tag.getList(TAG_SPAWN_POTENTIALS, 10);
            SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
            for (int i = 0; i < potentialsTag.size(); i++) {
                CompoundTag entryTag = potentialsTag.getCompound(i);
                int weight = entryTag.getInt("weight");
                CompoundTag dataTag = entryTag.getCompound("data");
                CompoundTag entityTag = dataTag.getCompound("entity");
                SpawnData spawnData = new SpawnData();
                spawnData.getEntityToSpawn().merge(entityTag);
                builder.add(spawnData, weight);
            }
            config.spawnPotentials = builder.build();
        }

        if (tag.contains(TAG_LOOT_TABLES_TO_EJECT)) {
            config.lootTablesToEject = new ArrayList<>();
            ListTag lootTag = tag.getList(TAG_LOOT_TABLES_TO_EJECT, 8);
            for (int i = 0; i < lootTag.size(); i++) {
                config.lootTablesToEject.add(new ResourceLocation(lootTag.getString(i)));
            }
        }

        if (tag.contains(TAG_ITEMS_TO_DROP_WHEN_OMINOUS)) {
            config.itemsToDropWhenOminous = new ResourceLocation(tag.getString(TAG_ITEMS_TO_DROP_WHEN_OMINOUS));
        }

        return config;
    }

    public void setSpawnPotentials(SimpleWeightedRandomList<SpawnData> potentials) {
        this.spawnPotentials = potentials;
    }
}
