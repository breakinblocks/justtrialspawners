package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.common.block.TrialSpawnerBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.TrialSpawnerBlockEntity;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawner;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawnerData;
import com.breakinblocks.justtrialspawners.common.block.entity.trialspawner.TrialSpawnerState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Renders a spinning mob preview inside the trial spawner, similar to vanilla spawner renderer.
 */
public class TrialSpawnerRenderer implements BlockEntityRenderer<TrialSpawnerBlockEntity> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(TrialSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        TrialSpawner spawner = blockEntity.getTrialSpawner();
        TrialSpawnerData data = spawner.getData();
        TrialSpawnerState state = blockEntity.getBlockState().getValue(TrialSpawnerBlock.STATE);

        if (!state.hasSpinningMob()) return;

        Entity displayEntity = data.getOrCreateDisplayEntity(spawner, blockEntity.getLevel(), state);
        if (displayEntity == null) return;

        float scale = 0.53125F;
        float maxDim = Math.max(displayEntity.getBbWidth(), displayEntity.getBbHeight());
        if (maxDim > 1.0) {
            scale /= maxDim;
        }

        double spin = Mth.lerp(partialTick, data.getOSpin(), data.getSpin());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.4, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) spin));
        poseStack.translate(0.0, -0.2, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        poseStack.scale(scale, scale, scale);

        this.entityRenderer.render(displayEntity, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();
    }
}
