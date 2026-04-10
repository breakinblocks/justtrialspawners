package com.breakinblocks.justtrialspawners.integration;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Integration with FTB Library's NBT editor.
 * Isolated in its own class to avoid classloading FTB Library when it's not present.
 */
public class FTBLibraryIntegration {

    public static void openItemNBTEditor(ServerPlayer player, ItemStack heldItem) {
        try {
            CompoundTag itemTag = heldItem.save(new CompoundTag());

            CompoundTag info = new CompoundTag();
            info.putString("type", "item");

            dev.ftb.mods.ftblibrary.FTBLibraryCommands.EDITING_NBT.put(player.getUUID(), info);
            new dev.ftb.mods.ftblibrary.net.EditNBTPacket(info, itemTag).sendTo(player);
        } catch (Exception e) {
            JustTrialSpawners.LOGGER.error("Failed to open FTB Library item NBT editor", e);
            player.sendSystemMessage(Component.literal("Failed to open NBT editor: " + e.getMessage()));
        }
    }

    public static void openNBTEditor(ServerPlayer player, TrialSpawnerBlockEntity spawnerBE, BlockPos pos) {
        try {
            // Match FTB Library's own nbtedit block format exactly
            CompoundTag info = new CompoundTag();
            info.putString("type", "block");
            info.putInt("x", pos.getX());
            info.putInt("y", pos.getY());
            info.putInt("z", pos.getZ());

            CompoundTag tag = spawnerBE.saveWithoutMetadata();

            dev.ftb.mods.ftblibrary.FTBLibraryCommands.EDITING_NBT.put(player.getUUID(), info);
            new dev.ftb.mods.ftblibrary.net.EditNBTPacket(info, tag).sendTo(player);

        } catch (Exception e) {
            JustTrialSpawners.LOGGER.error("Failed to open FTB Library NBT editor", e);
            player.sendSystemMessage(Component.literal("Failed to open NBT editor: " + e.getMessage()));
        }
    }
}
