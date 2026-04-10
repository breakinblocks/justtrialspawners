package com.breakinblocks.justtrialspawners.client.renderer;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.client.model.BreezeModel;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BreezeRenderer extends MobRenderer<BreezeEntity, BreezeModel<BreezeEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(JustTrialSpawners.MOD_ID,
            "textures/entity/breeze/breeze.png");

    public BreezeRenderer(EntityRendererProvider.Context context) {
        super(context, new BreezeModel<>(context.bakeLayer(BreezeModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new BreezeEyesLayer(this));
        this.addLayer(new BreezeWindLayer(this, context));
    }

    @Override
    public void render(BreezeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Main pass: only head and rods (no eyes, no wind)
        this.getModel().setVisibility(true, false, false);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BreezeEntity entity) {
        return TEXTURE;
    }
}
