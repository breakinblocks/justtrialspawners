package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.entity.BoggedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

/**
 * Renderer for the Bogged entity - skeleton with custom texture and mushroom overlay.
 */
public class BoggedRenderer extends SkeletonRenderer {
    private static final ResourceLocation BOGGED_TEXTURE = new ResourceLocation(
            JustTrialSpawners.MOD_ID, "textures/entity/skeleton/bogged.png");

    public BoggedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.addLayer(new BoggedOverlayLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
        return BOGGED_TEXTURE;
    }

    /**
     * Renders the mushroom overlay on the Bogged when not sheared.
     */
    private static class BoggedOverlayLayer extends RenderLayer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> {
        private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(
                JustTrialSpawners.MOD_ID, "textures/entity/skeleton/bogged_overlay.png");
        private final SkeletonModel<AbstractSkeleton> overlayModel;

        public BoggedOverlayLayer(RenderLayerParent<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> parent,
                                   EntityModelSet modelSet) {
            super(parent);
            this.overlayModel = new SkeletonModel<>(modelSet.bakeLayer(ModelLayers.SKELETON));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                           AbstractSkeleton entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity instanceof BoggedEntity bogged && bogged.isSheared()) {
                return;
            }
            coloredCutoutModelCopyLayerRender(
                    this.getParentModel(), this.overlayModel, OVERLAY_TEXTURE,
                    poseStack, buffer, packedLight, entity,
                    limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                    partialTick, 1.0F, 1.0F, 1.0F);
        }
    }
}
