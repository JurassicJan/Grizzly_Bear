package com.example.grizzlymod.client;

import com.example.grizzlymod.GrizzlyMod;
import com.example.grizzlymod.entity.GrizzlyBear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Blocky grizzly model: shoulder hump, broad head with snout and movable lower jaw.
 * Texture: 128x128, see textures/entity/grizzly_bear.png
 */
public class GrizzlyBearModel<T extends GrizzlyBear> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(GrizzlyMod.MOD_ID, "grizzly_bear"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart legFrontRight;
    private final ModelPart legFrontLeft;
    private final ModelPart legBackRight;
    private final ModelPart legBackLeft;

    public GrizzlyBearModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legBackRight = root.getChild("leg_back_right");
        this.legBackLeft = root.getChild("leg_back_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // body + shoulder hump + tail
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -7.0F, -11.0F, 16.0F, 13.0F, 22.0F)
                        .texOffs(0, 36).addBox(-5.0F, -10.0F, -10.0F, 10.0F, 4.0F, 9.0F)
                        .texOffs(52, 52).addBox(-2.0F, -5.0F, 11.0F, 4.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        // head with snout and ears
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-5.0F, -5.0F, -8.0F, 10.0F, 8.0F, 8.0F)
                        .texOffs(80, 18).addBox(-2.5F, 0.0F, -12.0F, 5.0F, 3.0F, 4.0F)
                        .texOffs(80, 27).addBox(-5.0F, -7.0F, -4.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(80, 27).addBox(3.0F, -7.0F, -4.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 10.0F, -10.0F));

        // lower jaw (opens while attacking)
        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(100, 18).addBox(-2.5F, 0.0F, -5.0F, 5.0F, 2.0F, 5.0F),
                PartPose.offset(0.0F, 3.0F, -7.0F));

        // legs
        CubeListBuilder front = CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F);
        CubeListBuilder back = CubeListBuilder.create().texOffs(26, 52).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 8.0F, 6.0F);
        root.addOrReplaceChild("leg_front_right", front, PartPose.offset(-5.0F, 16.0F, -7.0F));
        root.addOrReplaceChild("leg_front_left", front, PartPose.offset(5.0F, 16.0F, -7.0F));
        root.addOrReplaceChild("leg_back_right", back, PartPose.offset(-5.0F, 16.0F, 8.0F));
        root.addOrReplaceChild("leg_back_left", back, PartPose.offset(5.0F, 16.0F, 8.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F);

        // walk cycle
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        // claw swipe while the attack swing is running
        float swipe = entity.swinging ? Mth.sin(entity.swingTime / 6.0F * (float) Math.PI) * 1.5F : 0.0F;

        this.legFrontRight.xRot = swing - swipe;
        this.legFrontLeft.xRot = -swing - swipe;
        this.legBackRight.xRot = -swing;
        this.legBackLeft.xRot = swing;

        // growl: mouth open while aggressive
        this.jaw.xRot = entity.isAggressive() ? 0.55F + Mth.sin(ageInTicks * 0.3F) * 0.08F : 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
