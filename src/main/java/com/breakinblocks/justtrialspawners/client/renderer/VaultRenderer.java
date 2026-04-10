package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.common.block.VaultBlock;
import com.breakinblocks.justtrialspawners.common.block.entity.VaultBlockEntity;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultSharedData;
import com.breakinblocks.justtrialspawners.common.block.entity.vault.VaultState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders a spinning item display inside the vault when active.
 */
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {

    public VaultRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VaultBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        VaultState state = blockEntity.getBlockState().getValue(VaultBlock.STATE);
        if (state == VaultState.INACTIVE) return;

        VaultSharedData sharedData = blockEntity.getSharedData();
        ItemStack displayItem = sharedData.getDisplayItem();
        if (displayItem.isEmpty()) return;

        float spin = (float) Mth.lerp(partialTick, sharedData.getOSpin(), sharedData.getSpin());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(displayItem, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
