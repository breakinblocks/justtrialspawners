package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.util.TrialSpawnerNbtHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Renders a spinning mob preview inside the trial spawner item if it has mob NBT data.
 */
public class TrialSpawnerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final EntityRenderDispatcher entityRenderer;
    private final BlockRenderDispatcher blockRenderer;
    @Nullable
    private Entity cachedEntity;
    @Nullable
    private String cachedEntityId;
    private double spin;

    public TrialSpawnerItemRenderer(BlockEntityWithoutLevelRenderer original) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Render the block model
        var blockItem = (net.minecraft.world.item.BlockItem) stack.getItem();
        var blockState = blockItem.getBlock().defaultBlockState();
        poseStack.pushPose();
        blockRenderer.renderSingleBlock(blockState, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        // Check for mob NBT data
        String entityId = TrialSpawnerNbtHelper.getEntityIdFromItemStack(stack);
        if (entityId == null) return;

        // Cache the entity for rendering
        if (cachedEntity == null || !entityId.equals(cachedEntityId)) {
            CompoundTag entityTag = new CompoundTag();
            entityTag.putString("id", entityId);
            cachedEntity = EntityType.loadEntityRecursive(entityTag, Minecraft.getInstance().level, Function.identity());
            cachedEntityId = entityId;
        }

        if (cachedEntity == null) return;

        // Update spin
        spin += 2.0;

        float scale = 0.53125F;
        float maxDim = Math.max(cachedEntity.getBbWidth(), cachedEntity.getBbHeight());
        if (maxDim > 1.0) {
            scale /= maxDim;
        }

        // Render the mob inside the block, centered at (0.5, 0.4, 0.5) like the placed block renderer
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) spin));
        poseStack.translate(0.0, -0.2, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(scale, scale, scale);

        entityRenderer.render(cachedEntity, 0.0, 0.0, 0.0, 0.0F, 0.0F, poseStack, buffer, packedLight);

        poseStack.popPose();
    }

}
