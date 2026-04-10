package com.breakinblocks.justtrialspawners.common.block.entity.vault;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Server-side vault data - rewarded players, ejection queue, timing.
 */
public class VaultServerData {
    private static final int MAX_REWARDED_PLAYERS = 128;

    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet<>();
    private final List<ItemStack> itemsToEject = new ArrayList<>();
    private long stateUpdatingResumesAt;
    private int lastInsertFailSoundTick;

    public boolean hasRewardedPlayer(Player player) {
        return this.rewardedPlayers.contains(player.getUUID());
    }

    public void addToRewardedPlayers(Player player) {
        this.rewardedPlayers.add(player.getUUID());
        // Evict oldest if over capacity
        while (this.rewardedPlayers.size() > MAX_REWARDED_PLAYERS) {
            Iterator<UUID> iter = this.rewardedPlayers.iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }
    }

    public List<ItemStack> getItemsToEject() { return this.itemsToEject; }

    public void setItemsToEject(List<ItemStack> items) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(items);
    }

    public boolean isPaused(long gameTime) {
        return gameTime < this.stateUpdatingResumesAt;
    }

    public void pauseStateUpdating(long gameTime, int ticks) {
        this.stateUpdatingResumesAt = gameTime + ticks;
    }

    public boolean canPlayInsertFailSound(int tickCount) {
        if (tickCount - this.lastInsertFailSoundTick >= 15) {
            this.lastInsertFailSoundTick = tickCount;
            return true;
        }
        return false;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag playersTag = new ListTag();
        for (UUID uuid : rewardedPlayers) {
            playersTag.add(NbtUtils.createUUID(uuid));
        }
        tag.put("rewarded_players", playersTag);
        tag.putLong("state_updating_resumes_at", stateUpdatingResumesAt);

        if (!itemsToEject.isEmpty()) {
            ListTag itemsTag = new ListTag();
            for (ItemStack stack : itemsToEject) {
                itemsTag.add(stack.save(new CompoundTag()));
            }
            tag.put("items_to_eject", itemsTag);
        }
        return tag;
    }

    public static VaultServerData load(CompoundTag tag) {
        VaultServerData data = new VaultServerData();
        if (tag.contains("rewarded_players")) {
            ListTag list = tag.getList("rewarded_players", 11);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.rewardedPlayers.add(NbtUtils.loadUUID(list.get(i)));
                } catch (Exception ignored) {}
            }
        }
        data.stateUpdatingResumesAt = tag.getLong("state_updating_resumes_at");
        if (tag.contains("items_to_eject")) {
            ListTag items = tag.getList("items_to_eject", 10);
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = ItemStack.of(items.getCompound(i));
                if (!stack.isEmpty()) data.itemsToEject.add(stack);
            }
        }
        return data;
    }
}
