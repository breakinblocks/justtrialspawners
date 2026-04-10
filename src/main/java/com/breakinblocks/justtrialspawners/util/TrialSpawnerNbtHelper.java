package com.breakinblocks.justtrialspawners.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class TrialSpawnerNbtHelper {

    @Nullable
    public static String getEntityIdFromItemStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockEntityTag")) return null;

        CompoundTag beTag = tag.getCompound("BlockEntityTag");
        if (!beTag.contains("trial_spawner")) return null;

        CompoundTag spawnerTag = beTag.getCompound("trial_spawner");

        // Check spawn_data first (direct next spawn)
        if (spawnerTag.contains("data")) {
            CompoundTag dataTag = spawnerTag.getCompound("data");
            if (dataTag.contains("spawn_data")) {
                CompoundTag spawnData = dataTag.getCompound("spawn_data");
                if (spawnData.contains("entity")) {
                    String id = spawnData.getCompound("entity").getString("id");
                    if (!id.isEmpty()) return id;
                }
            }
        }

        // Check normal_config spawn_potentials
        if (spawnerTag.contains("normal_config")) {
            CompoundTag config = spawnerTag.getCompound("normal_config");
            if (config.contains("spawn_potentials")) {
                ListTag potentials = config.getList("spawn_potentials", 10);
                if (!potentials.isEmpty()) {
                    CompoundTag first = potentials.getCompound(0);
                    CompoundTag data = first.getCompound("data");
                    String id = data.getCompound("entity").getString("id");
                    if (!id.isEmpty()) return id;
                }
            }
        }

        return null;
    }
}
