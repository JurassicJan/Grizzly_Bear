package com.example.grizzlymod.client;

import com.example.grizzlymod.GrizzlyMod;
import com.example.grizzlymod.entity.GrizzlyBear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Draws the 3D bear head on players who wear the bear head helmet. */
public class BearHeadLayer extends RenderLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GrizzlyMod.MOD_ID, "textures/entity/grizzly_bear.png");

    private final BearHeadModel model;

    public BearHeadLayer(RenderLayerParent<Player, PlayerModel<Player>> parent, EntityModelSet models) {
        super(parent);
        this.model = new BearHeadModel(models.bakeLayer(BearHeadModel.LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Player player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!GrizzlyBear.wearsBearHead(player)) {
            return;
        }
        poseStack.pushPose();
        this.getParentModel().getHead().translateAndRotate(poseStack);
        poseStack.scale(1.2F, 1.2F, 1.2F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
