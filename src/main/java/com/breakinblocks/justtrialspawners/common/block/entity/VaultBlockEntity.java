package com.breakinblocks.justtrialspawners.common.block.entity;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.block.VaultBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultConfig;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultServerData;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultSharedData;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultState;
import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.migration.TrialsNbtConverter;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import com.breakinblocks.justtrialspawners.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class VaultBlockEntity extends BlockEntity {
    private static final int UNLOCKING_DELAY_TICKS = 14;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;

    private VaultConfig config;
    private VaultServerData serverData = new VaultServerData();
    private VaultSharedData sharedData = new VaultSharedData();
    private boolean pendingOminousFixup = false;

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VAULT.get(), pos, state);
        this.config = state.getValue(VaultBlock.OMINOUS)
                ? VaultConfig.createOminous() : new VaultConfig();
    }

    public VaultConfig getConfig() { return config; }
    public VaultSharedData getSharedData() { return sharedData; }

    // ========== Ticking ==========

    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        // Fix ominous state from Trials mod migration
        if (pendingOminousFixup) {
            pendingOminousFixup = false;
            if (!state.getValue(VaultBlock.OMINOUS)) {
                state = state.setValue(VaultBlock.OMINOUS, true);
                level.setBlockAndUpdate(pos, state);
                this.config = VaultConfig.createOminous();
                this.setChanged();
                JustTrialSpawners.LOGGER.info("Applied ominous fixup to migrated vault at {}", pos);
            }
        }

        VaultState vaultState = state.getValue(VaultBlock.STATE);

        if (serverData.isPaused(level.getGameTime())) return;

        switch (vaultState) {
            case INACTIVE -> {
                // Check for nearby players with keys
                if (level.getGameTime() % UPDATE_CONNECTED_PLAYERS_TICK_RATE == 0L) {
                    sharedData.updateConnectedPlayersWithinRange(level, pos, config, serverData, config.activationRange());
                    if (!sharedData.getConnectedPlayers().isEmpty()) {
                        setState(level, pos, state, VaultState.ACTIVE);
                        // Set display item from loot table
                        updateDisplayItem(level);
                        markUpdated();
                    }
                }
            }
            case ACTIVE -> {
                if (level.getGameTime() % UPDATE_CONNECTED_PLAYERS_TICK_RATE == 0L) {
                    sharedData.updateConnectedPlayersWithinRange(level, pos, config, serverData, config.deactivationRange());
                    if (sharedData.getConnectedPlayers().isEmpty()) {
                        setState(level, pos, state, VaultState.INACTIVE);
                        sharedData.setDisplayItem(ItemStack.EMPTY);
                        markUpdated();
                    } else if (level.getGameTime() % 20L == 0L) {
                        // Cycle display item periodically
                        updateDisplayItem(level);
                        markUpdated();
                    }
                }
            }
            case UNLOCKING -> {
                // Transition to ejecting after delay
                setState(level, pos, state, VaultState.EJECTING);
            }
            case EJECTING -> {
                List<ItemStack> items = serverData.getItemsToEject();
                if (items.isEmpty()) {
                    // Done ejecting
                    level.playSound(null, pos, ModSounds.VAULT_DEACTIVATE.get(),
                            SoundSource.BLOCKS, 1.0F, 1.0F);

                    sharedData.updateConnectedPlayersWithinRange(level, pos, config, serverData, config.deactivationRange());
                    if (sharedData.getConnectedPlayers().isEmpty()) {
                        setState(level, pos, state, VaultState.INACTIVE);
                        sharedData.setDisplayItem(ItemStack.EMPTY);
                    } else {
                        setState(level, pos, state, VaultState.ACTIVE);
                        updateDisplayItem(level);
                    }
                    markUpdated();
                } else {
                    // Eject one item
                    ItemStack toEject = items.remove(0);
                    net.minecraft.core.dispenser.DefaultDispenseItemBehavior.spawnItem(
                            level, toEject, 2, Direction.UP,
                            Vec3.atBottomCenterOf(pos).add(0, 1.2, 0));

                    float pitch = 0.8F + 0.4F * (items.isEmpty() ? 1.0F : 0.5F);
                    level.playSound(null, pos, ModSounds.VAULT_EJECT.get(),
                            SoundSource.BLOCKS, 1.0F, pitch);

                    if (!items.isEmpty()) {
                        sharedData.setDisplayItem(items.get(0));
                        serverData.pauseStateUpdating(level.getGameTime(), DELAY_BETWEEN_EJECTIONS_TICKS);
                    } else {
                        serverData.pauseStateUpdating(level.getGameTime(), DELAY_BETWEEN_EJECTIONS_TICKS);
                    }
                    markUpdated();
                }
            }
        }
    }

    public void clientTick(BlockPos pos, BlockState state) {
        VaultState vaultState = state.getValue(VaultBlock.STATE);
        if (vaultState == VaultState.ACTIVE || vaultState == VaultState.EJECTING) {
            sharedData.updateSpin(2.0F);
        }
    }

    // ========== Key Insertion ==========

    public void tryInsertKey(ServerLevel level, BlockPos pos, BlockState state, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;

        // Check if correct key
        if (!ItemStack.isSameItem(heldItem, config.keyItem())) {
            if (serverData.canPlayInsertFailSound(player.tickCount)) {
                level.playSound(null, pos, ModSounds.VAULT_INSERT_FAIL.get(),
                        SoundSource.BLOCKS, 1.0F, 0.5F);
            }
            return;
        }

        // Check if already rewarded
        if (serverData.hasRewardedPlayer(player)) {
            if (serverData.canPlayInsertFailSound(player.tickCount)) {
                level.playSound(null, pos, ModSounds.VAULT_REJECT_REWARDED_PLAYER.get(),
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return;
        }

        // Resolve loot
        List<ItemStack> loot = resolveLoot(level, player);
        if (loot.isEmpty()) return;

        // Consume key
        player.awardStat(Stats.ITEM_USED.get(heldItem.getItem()));
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(config.keyItem().getCount());
        }

        // Set up ejection
        serverData.setItemsToEject(loot);
        sharedData.setDisplayItem(loot.get(0));
        serverData.addToRewardedPlayers(player);
        serverData.pauseStateUpdating(level.getGameTime(), UNLOCKING_DELAY_TICKS);

        // Play insert sound and transition to unlocking
        level.playSound(null, pos, ModSounds.VAULT_INSERT.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
        setState(level, pos, state, VaultState.UNLOCKING);
        markUpdated();
    }

    private List<ItemStack> resolveLoot(ServerLevel level, Player player) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(config.lootTable());
        LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        return lootTable.getRandomItems(lootParams);
    }

    private void updateDisplayItem(ServerLevel level) {
        LootTable lootTable = level.getServer().getLootData().getLootTable(config.lootTable());
        LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        List<ItemStack> items = lootTable.getRandomItems(lootParams);
        if (!items.isEmpty()) {
            sharedData.setDisplayItem(items.get(level.getRandom().nextInt(items.size())));
        }
    }

    // ========== State Management ==========

    private void setState(ServerLevel level, BlockPos pos, BlockState state, VaultState newState) {
        level.setBlockAndUpdate(pos, state.setValue(VaultBlock.STATE, newState));
        this.setChanged();
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ========== NBT ==========

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Migrate old Trials mod vault NBT format
        if (JTSConfig.SERVER.enableTrialsMigration.get() && TrialsNbtConverter.isTrialsVaultFormat(tag)) {
            this.pendingOminousFixup = TrialsNbtConverter.convertVault(tag);
            this.setChanged();
        }
        if (tag.contains("config")) this.config = VaultConfig.load(tag.getCompound("config"));
        if (tag.contains("server_data")) this.serverData = VaultServerData.load(tag.getCompound("server_data"));
        if (tag.contains("shared_data")) this.sharedData = VaultSharedData.load(tag.getCompound("shared_data"));
        // Check for ominous fixup marker from migration
        if (tag.contains("_jts_ominous_fixup")) {
            this.pendingOminousFixup = tag.getBoolean("_jts_ominous_fixup");
            tag.remove("_jts_ominous_fixup");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("config", config.save());
        tag.put("server_data", serverData.save());
        tag.put("shared_data", sharedData.save());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("shared_data", sharedData.save());
        tag.put("config", config.save());
        return tag;
    }
}
