package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.client.model.BreezeModel;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the glowing eyes of the Breeze using the emissive eyes render type.
 */
public class BreezeEyesLayer extends RenderLayer<BreezeEntity, BreezeModel<BreezeEntity>> {
    private static final ResourceLocation EYES_TEXTURE = new ResourceLocation(
            JustTrialSpawners.MOD_ID, "textures/entity/breeze/breeze_eyes.png");

    public BreezeEyesLayer(RenderLayerParent<BreezeEntity, BreezeModel<BreezeEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BreezeEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        BreezeModel<BreezeEntity> model = this.getParentModel();

        // Only show eyes
        model.setVisibility(false, true, false);

        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(EYES_TEXTURE));
        model.renderToBuffer(poseStack, consumer, 15728640, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        // Restore default visibility
        model.setVisibility(true, false, false);
    }
}
