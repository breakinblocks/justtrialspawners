package com.breakinblocks.justtrialspawners.common.block.entity;

import com.breakinblocks.justtrialspawners.common.block.TrialSpawnerBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawner;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawnerState;
import com.breakinblocks.justtrialspawners.config.JTSConfig;
import com.breakinblocks.justtrialspawners.migration.TrialsNbtConverter;
import com.breakinblocks.justtrialspawners.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TrialSpawnerBlockEntity extends BlockEntity implements TrialSpawner.StateAccessor {
    private TrialSpawner trialSpawner;

    public TrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRIAL_SPAWNER.get(), pos, state);
        this.trialSpawner = new TrialSpawner(this);
    }

    public TrialSpawner getTrialSpawner() {
        return this.trialSpawner;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (JTSConfig.SERVER.enableTrialsMigration.get() && TrialsNbtConverter.isTrialsSpawnerFormat(tag)) {
            TrialsNbtConverter.convertTrialSpawner(tag);
            this.setChanged();
        }
        if (tag.contains("trial_spawner")) {
            this.trialSpawner.load(tag.getCompound("trial_spawner"));
        }
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("trial_spawner", this.trialSpawner.save());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public TrialSpawnerState getState() {
        if (!this.getBlockState().hasProperty(TrialSpawnerBlock.STATE)) {
            return TrialSpawnerState.INACTIVE;
        }
        return this.getBlockState().getValue(TrialSpawnerBlock.STATE);
    }

    @Override
    public void setState(Level level, TrialSpawnerState state) {
        this.setChanged();
        level.setBlockAndUpdate(this.worldPosition,
                this.getBlockState().setValue(TrialSpawnerBlock.STATE, state));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
