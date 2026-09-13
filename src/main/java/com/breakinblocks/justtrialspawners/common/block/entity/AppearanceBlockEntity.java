package com.breakinblocks.justtrialspawners.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.Objects;

/** Stores and synchronizes a resource-pack appearance without changing block behavior. */
public abstract class AppearanceBlockEntity extends BlockEntity {
    public static final String TAG_APPEARANCE = "appearance";
    public static final ModelProperty<ResourceLocation> APPEARANCE = new ModelProperty<>();
    private volatile ModelData appearanceData = ModelData.EMPTY;

    protected AppearanceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public ResourceLocation getAppearance() {
        return appearanceData.get(APPEARANCE);
    }

    public void setAppearance(@Nullable ResourceLocation appearance) {
        if (Objects.equals(getAppearance(), appearance)) return;
        appearanceData = appearance == null ? ModelData.EMPTY
                : ModelData.builder().with(APPEARANCE, appearance).build();
        setChanged();
        requestModelDataUpdate();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ModelData getModelData() {
        return appearanceData;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        String model = tag.getString(TAG_APPEARANCE);
        setAppearance(model.isBlank() ? null : ResourceLocation.tryParse(model));
    }

    protected void saveAppearance(CompoundTag tag) {
        ResourceLocation appearance = getAppearance();
        if (appearance != null) tag.putString(TAG_APPEARANCE, appearance.toString());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveAppearance(tag);
    }
}
