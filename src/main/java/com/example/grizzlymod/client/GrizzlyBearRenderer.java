package com.example.grizzlymod.client;

import com.example.grizzlymod.GrizzlyMod;
import com.example.grizzlymod.entity.GrizzlyBear;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GrizzlyBearRenderer extends MobRenderer<GrizzlyBear, GrizzlyBearModel<GrizzlyBear>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(GrizzlyMod.MOD_ID, "textures/entity/grizzly_bear.png");

    private static final ResourceLocation CANNIBAL_TEXTURE =
            new ResourceLocation(GrizzlyMod.MOD_ID, "textures/entity/grizzly_bear_cannibal.png");

    /** Overall size of the model (1.0 = as modelled in Blockbench). */
    private static final float MODEL_SCALE = 1.5F;
    private static final float CUB_SCALE = 0.6F;

    public GrizzlyBearRenderer(EntityRendererProvider.Context context) {
        super(context, new GrizzlyBearModel<>(context.bakeLayer(GrizzlyBearModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    protected void scale(GrizzlyBear bear, PoseStack poseStack, float partialTick) {
        float scale = bear.isCub() ? CUB_SCALE : MODEL_SCALE;
        this.shadowRadius = bear.isCub() ? 0.5F : 1.0F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(GrizzlyBear bear) {
        return bear.isCannibal() ? CANNIBAL_TEXTURE : TEXTURE;
    }
}
