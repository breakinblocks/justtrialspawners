package com.breakinblocks.justtrialspawners.common.block.entity.vault;

import com.breakinblocks.justtrialspawners.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Configuration for a vault block - key item, loot table, detection ranges.
 */
public class VaultConfig {
    private ResourceLocation lootTable;
    private ItemStack keyItem;
    private double activationRange;
    private double deactivationRange;

    public VaultConfig() {
        this.lootTable = new ResourceLocation("justtrialspawners", "chests/trial_chambers/reward");
        this.keyItem = new ItemStack(ModItems.TRIAL_KEY.get());
        this.activationRange = 4.0;
        this.deactivationRange = 4.5;
    }

    public static VaultConfig createOminous() {
        VaultConfig config = new VaultConfig();
        config.lootTable = new ResourceLocation("justtrialspawners", "chests/trial_chambers/reward_ominous");
        config.keyItem = new ItemStack(ModItems.OMINOUS_TRIAL_KEY.get());
        return config;
    }

    public ResourceLocation lootTable() { return lootTable; }
    public ItemStack keyItem() { return keyItem; }
    public double activationRange() { return activationRange; }
    public double deactivationRange() { return deactivationRange; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("loot_table", lootTable.toString());
        tag.put("key_item", keyItem.save(new CompoundTag()));
        tag.putDouble("activation_range", activationRange);
        tag.putDouble("deactivation_range", deactivationRange);
        return tag;
    }

    public static VaultConfig load(CompoundTag tag) {
        VaultConfig config = new VaultConfig();
        if (tag.contains("loot_table")) config.lootTable = new ResourceLocation(tag.getString("loot_table"));
        if (tag.contains("key_item")) config.keyItem = ItemStack.of(tag.getCompound("key_item"));
        if (tag.contains("activation_range")) config.activationRange = tag.getDouble("activation_range");
        if (tag.contains("deactivation_range")) config.deactivationRange = tag.getDouble("deactivation_range");
        return config;
    }
}
