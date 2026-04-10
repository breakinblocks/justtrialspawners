package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.entity.AbstractWindChargeProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Renders the wind charge as a simple billboard sprite.
 */
public class WindChargeRenderer extends EntityRenderer<AbstractWindChargeProjectile> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            JustTrialSpawners.MOD_ID, "textures/entity/projectiles/wind_charge.png");
    private static final float SIZE = 0.5F;

    public WindChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AbstractWindChargeProjectile entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(SIZE, SIZE, SIZE);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        vertex(consumer, matrix, normal, packedLight, 0.0F, 0, 0, 1);
        vertex(consumer, matrix, normal, packedLight, 1.0F, 0, 1, 1);
        vertex(consumer, matrix, normal, packedLight, 1.0F, 1, 1, 0);
        vertex(consumer, matrix, normal, packedLight, 0.0F, 1, 0, 0);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                                int light, float x, int y, int u, int v) {
        consumer.vertex(matrix, x - 0.5F, y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractWindChargeProjectile entity) {
        return TEXTURE;
    }
}
