package com.breakinblocks.justtrialspawners.common.block.entity.vault;

import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.PlayerDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Shared vault data synced to clients - connected players, display item.
 */
public class VaultSharedData {
    private final Set<UUID> connectedPlayers = new HashSet<>();
    private ItemStack displayItem = ItemStack.EMPTY;
    private double spin;
    private double oSpin;

    public Set<UUID> getConnectedPlayers() { return this.connectedPlayers; }
    public ItemStack getDisplayItem() { return this.displayItem; }
    public void setDisplayItem(ItemStack item) { this.displayItem = item; }
    public double getSpin() { return this.spin; }
    public double getOSpin() { return this.oSpin; }

    public void updateSpin(float speedMultiplier) {
        this.oSpin = this.spin;
        this.spin = (this.spin + speedMultiplier) % 360.0;
    }

    public void updateConnectedPlayersWithinRange(ServerLevel level, BlockPos pos, VaultConfig config,
                                                   VaultServerData serverData, double range) {
        List<UUID> detected = PlayerDetector.INCLUDING_CREATIVE_PLAYERS.detect(level, pos, range, true);
        this.connectedPlayers.clear();
        for (UUID uuid : detected) {
            if (!serverData.hasRewardedPlayer(level.getPlayerByUUID(uuid) != null
                    ? level.getPlayerByUUID(uuid) : null)) {
                // Only include unrewarded players
                var player = level.getPlayerByUUID(uuid);
                if (player != null && !serverData.hasRewardedPlayer(player)) {
                    this.connectedPlayers.add(uuid);
                }
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!displayItem.isEmpty()) {
            tag.put("display_item", displayItem.save(new CompoundTag()));
        }
        if (!connectedPlayers.isEmpty()) {
            ListTag list = new ListTag();
            for (UUID uuid : connectedPlayers) {
                list.add(NbtUtils.createUUID(uuid));
            }
            tag.put("connected_players", list);
        }
        return tag;
    }

    public static VaultSharedData load(CompoundTag tag) {
        VaultSharedData data = new VaultSharedData();
        if (tag.contains("display_item")) {
            data.displayItem = ItemStack.of(tag.getCompound("display_item"));
        }
        if (tag.contains("connected_players")) {
            ListTag list = tag.getList("connected_players", 11);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.connectedPlayers.add(NbtUtils.loadUUID(list.get(i)));
                } catch (Exception ignored) {}
            }
        }
        return data;
    }
}
