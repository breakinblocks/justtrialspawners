package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.client.model.BreezeModel;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the swirling wind effect around the Breeze using the 128x128 wind texture.
 * In 1.21 this uses a custom RenderType.breezeWind() with UV scrolling.
 * We approximate with entityTranslucent for 1.20.1 compatibility.
 */
public class BreezeWindLayer extends RenderLayer<BreezeEntity, BreezeModel<BreezeEntity>> {
    private static final ResourceLocation WIND_TEXTURE = new ResourceLocation(
            JustTrialSpawners.MOD_ID, "textures/entity/breeze/breeze_wind.png");

    private final BreezeModel<BreezeEntity> windModel;

    public BreezeWindLayer(RenderLayerParent<BreezeEntity, BreezeModel<BreezeEntity>> parent,
                           EntityRendererProvider.Context context) {
        super(parent);
        this.windModel = new BreezeModel<>(context.bakeLayer(BreezeModel.WIND_LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       BreezeEntity entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        // Animate the wind model
        this.windModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Only show wind parts
        this.windModel.setVisibility(false, false, true);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(WIND_TEXTURE));
        this.windModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 0.6F);

        // Restore main model visibility
        this.getParentModel().setVisibility(true, false, false);
    }
}
