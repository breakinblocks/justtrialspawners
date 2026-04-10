package com.breakinblocks.justtrialspawners.client.model;

import com.breakinblocks.justtrialspawners.JustTrialSpawners;
import com.breakinblocks.justtrialspawners.common.entity.BreezeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Faithful recreation of the 1.21 Breeze model with head, jaw, eyes, rods, and wind body.
 */
public class BreezeModel<T extends BreezeEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(JustTrialSpawners.MOD_ID, "breeze"), "main");
    public static final ModelLayerLocation WIND_LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(JustTrialSpawners.MOD_ID, "breeze"), "wind");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart rods;
    private final ModelPart windBody;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;

    public BreezeModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.eyes = this.head.getChild("eyes");
        this.rods = this.body.getChild("rods");
        this.windBody = root.getChild("wind_body");
        this.windBottom = this.windBody.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
    }

    public static LayerDefinition createBodyLayer(int texWidth, int texHeight) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootPart = mesh.getRoot();

        // Body (empty group)
        PartDefinition body = rootPart.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.ZERO);

        // Head
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, 0.0F));

        head.addOrReplaceChild("skull",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(4, 24).addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F),
                PartPose.ZERO);

        // Eyes (duplicate geometry for separate render layer)
        PartDefinition eyes = head.addOrReplaceChild("eyes",
                CubeListBuilder.create(), PartPose.ZERO);

        eyes.addOrReplaceChild("eyes_skull",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);

        eyes.addOrReplaceChild("eyes_jaw",
                CubeListBuilder.create()
                        .texOffs(4, 24).addBox(-5.0F, -5.0F, -4.2F, 10.0F, 3.0F, 4.0F),
                PartPose.ZERO);

        // Rods (three spinning rods around the body)
        PartDefinition rods = body.addOrReplaceChild("rods",
                CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        rods.addOrReplaceChild("rod_1",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(2.5981F, -3.0F, 1.5F,
                        -2.7489F, -1.0472F, (float) Math.PI));

        rods.addOrReplaceChild("rod_2",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(-2.5981F, -3.0F, 1.5F,
                        -2.7489F, 1.0472F, (float) Math.PI));

        rods.addOrReplaceChild("rod_3",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -3.0F,
                        0.3927F, 0.0F, 0.0F));

        // Wind body (empty group)
        PartDefinition windBody = rootPart.addOrReplaceChild("wind_body",
                CubeListBuilder.create(), PartPose.ZERO);

        // Wind bottom
        PartDefinition windBottom = windBody.addOrReplaceChild("wind_bottom",
                CubeListBuilder.create()
                        .texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // Wind mid
        PartDefinition windMid = windBottom.addOrReplaceChild("wind_mid",
                CubeListBuilder.create()
                        .texOffs(74, 28).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F)
                        .texOffs(78, 32).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F)
                        .texOffs(49, 71).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, -7.0F, 0.0F));

        // Wind top
        windMid.addOrReplaceChild("wind_top",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -8.0F, -9.0F, 18.0F, 8.0F, 18.0F)
                        .texOffs(6, 6).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 12.0F)
                        .texOffs(105, 57).addBox(-2.5F, -8.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.offset(0.0F, -6.0F, 0.0F));

        return LayerDefinition.create(mesh, texWidth, texHeight);
    }

    public static LayerDefinition createMainLayer() {
        return createBodyLayer(32, 32);
    }

    public static LayerDefinition createWindLayer() {
        return createBodyLayer(128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float f = ageInTicks * (float) Math.PI * -0.1F;

        // Wind orbiting animation
        this.windTop.x = Mth.cos(f) * 0.6F;
        this.windTop.z = Mth.sin(f) * 0.6F;

        this.windMid.x = Mth.sin(f) * 0.4F;
        this.windMid.z = Mth.cos(f) * 0.8F;

        this.windBottom.x = Mth.cos(f) * -0.25F;
        this.windBottom.z = Mth.sin(f) * -0.25F;

        // Head bobbing
        this.head.y = 4.0F + Mth.cos(f) / 4.0F;

        // Rods spinning
        this.rods.yRot = ageInTicks * (float) Math.PI * 0.1F;

        // Head look direction
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    /**
     * Enable/disable parts for layer-specific rendering.
     * @param headVisible show head and rods
     * @param eyesVisible show eyes overlay
     * @param windVisible show wind body
     */
    public void setVisibility(boolean headVisible, boolean eyesVisible, boolean windVisible) {
        this.head.getChild("skull").visible = headVisible;
        this.head.getChild("jaw").visible = headVisible;
        this.eyes.visible = eyesVisible;
        this.rods.visible = headVisible;
        this.windBody.visible = windVisible;
    }
}
