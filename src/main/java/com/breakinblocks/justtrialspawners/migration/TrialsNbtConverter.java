package com.breakinblocks.justtrialspawners.migration;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * Converts NBT data from the Trials mod format to the Just Trial Spawners format.
 * This runs lazily when block entities load from chunks that still have old data.
 *
 * Trials mod trial_spawner NBT (root level):
 *   SpawnEgg: {id:"minecraft:zombie_spawn_egg", Count:1b, ...}
 *   LootTable: "trials:chests/trial_loot"
 *   DisableEffects: 0b
 *   isActive: 1b
 *   Difficulty: 2
 *   Cooldown: 0
 *   Enemies: 6
 *   Killed: 3
 *
 * Our format (nested under "trial_spawner"):
 *   trial_spawner.normal_config.spawn_potentials, trial_spawner.data, etc.
 */
public final class TrialsNbtConverter {

    /**
     * Detects whether a block entity tag contains old Trials mod data.
     */
    public static boolean isTrialsSpawnerFormat(CompoundTag tag) {
        // Old format has SpawnEgg or isActive at root level, no trial_spawner key
        return !tag.contains("trial_spawner")
                && (tag.contains("SpawnEgg") || tag.contains("isActive") || tag.contains("Enemies"));
    }

    /**
     * Detects whether a block entity tag contains old Trials mod vault data.
     */
    public static boolean isTrialsVaultFormat(CompoundTag tag) {
        return !tag.contains("config") && !tag.contains("server_data")
                && (tag.contains("Ominous") || (tag.contains("LootTable") && tag.contains("Cooldown") && !tag.contains("SpawnEgg")));
    }

    /**
     * Converts a Trials mod trial spawner NBT tag to our format.
     * Writes the result into the provided tag under the "trial_spawner" key.
     */
    public static void convertTrialSpawner(CompoundTag tag) {
        String entityId = extractEntityIdFromSpawnEgg(tag);
        if (entityId == null) {
            entityId = "minecraft:zombie"; // Fallback
        }

        int enemies = tag.contains("Enemies") ? tag.getInt("Enemies") : 1;
        int killed = tag.contains("Killed") ? tag.getInt("Killed") : 0;

        // Build spawn_potentials
        ListTag potentials = new ListTag();
        CompoundTag entry = new CompoundTag();
        entry.putInt("weight", 1);
        CompoundTag dataTag = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("id", entityId);
        dataTag.put("entity", entityTag);
        entry.put("data", dataTag);
        potentials.add(entry);

        // Build normal_config
        CompoundTag configTag = new CompoundTag();
        configTag.putInt("spawn_range", 4);
        configTag.putFloat("total_mobs", Math.max(1.0f, enemies));
        configTag.putFloat("simultaneous_mobs", 1.0f);
        configTag.putFloat("total_mobs_added_per_player", 1.0f);
        configTag.putFloat("simultaneous_mobs_added_per_player", 1.0f);
        configTag.putInt("ticks_between_spawn", 40);
        configTag.put("spawn_potentials", potentials);

        ListTag lootTables = new ListTag();
        lootTables.add(StringTag.valueOf("justtrialspawners:spawners/trial_chamber/consumables"));
        lootTables.add(StringTag.valueOf("justtrialspawners:spawners/trial_chamber/key"));
        configTag.put("loot_tables_to_eject", lootTables);

        // Build ominous_config
        CompoundTag ominousConfig = configTag.copy();
        ListTag ominousLoot = new ListTag();
        ominousLoot.add(StringTag.valueOf("justtrialspawners:spawners/ominous/trial_chamber/consumables"));
        ominousLoot.add(StringTag.valueOf("justtrialspawners:spawners/ominous/trial_chamber/key"));
        ominousConfig.put("loot_tables_to_eject", ominousLoot);

        // Build data
        CompoundTag spawnerData = new CompoundTag();
        spawnerData.putInt("total_mobs_spawned", killed);
        spawnerData.putLong("cooldown_ends_at", 0L);
        spawnerData.putLong("next_mob_spawns_at", 0L);

        // Build the trial_spawner compound
        CompoundTag spawnerTag = new CompoundTag();
        spawnerTag.put("normal_config", configTag);
        spawnerTag.put("ominous_config", ominousConfig);
        spawnerTag.put("data", spawnerData);
        spawnerTag.putInt("target_cooldown_length", 36000);
        spawnerTag.putInt("required_player_range", 14);
        spawnerTag.putBoolean("is_ominous", false);

        // Clean old keys and write new format
        tag.remove("SpawnEgg");
        tag.remove("LootTable");
        tag.remove("DisableEffects");
        tag.remove("isActive");
        tag.remove("Difficulty");
        tag.remove("Cooldown");
        tag.remove("Enemies");
        tag.remove("Killed");
        tag.put("trial_spawner", spawnerTag);

        JustTrialSpawners.LOGGER.info("Converted Trials mod spawner NBT (entity: {}, enemies: {})", entityId, enemies);
    }

    /**
     * Converts a Trials mod vault NBT tag to our format.
     * Returns true if the vault was ominous (for blockstate fixup).
     */
    public static boolean convertVault(CompoundTag tag) {
        boolean isOminous = tag.contains("Ominous") && tag.getBoolean("Ominous");

        // Build config
        CompoundTag configTag = new CompoundTag();
        String lootTable = isOminous
                ? "justtrialspawners:chests/trial_chambers/reward_ominous"
                : "justtrialspawners:chests/trial_chambers/reward";
        configTag.putString("loot_table", lootTable);
        configTag.putString("key_item", "justtrialspawners:trial_key");

        // Build server_data (empty - will reset)
        CompoundTag serverData = new CompoundTag();

        // Build shared_data (empty)
        CompoundTag sharedData = new CompoundTag();

        // Clean old keys
        tag.remove("LootTable");
        tag.remove("Cooldown");
        tag.remove("Ominous");

        tag.put("config", configTag);
        tag.put("server_data", serverData);
        tag.put("shared_data", sharedData);
        if (isOminous) {
            tag.putBoolean("_jts_ominous_fixup", true);
        }

        JustTrialSpawners.LOGGER.info("Converted Trials mod vault NBT (ominous: {})", isOminous);
        return isOminous;
    }

    @Nullable
    private static String extractEntityIdFromSpawnEgg(CompoundTag tag) {
        if (!tag.contains("SpawnEgg")) return null;

        try {
            CompoundTag eggTag = tag.getCompound("SpawnEgg");
            ItemStack eggStack = ItemStack.of(eggTag);
            if (eggStack.isEmpty()) return null;

            if (eggStack.getItem() instanceof SpawnEggItem spawnEgg) {
                EntityType<?> entityType = spawnEgg.getType(eggStack.getTag());
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
                if (id != null) {
                    return id.toString();
                }
            }
        } catch (Exception e) {
            JustTrialSpawners.LOGGER.warn("Failed to extract entity type from Trials SpawnEgg", e);
        }

        return null;
    }
}
