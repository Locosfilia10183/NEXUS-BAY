package de.nexusbay.forge.client;

import de.nexusbay.forge.ship.NexusShipEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

public final class NexusShipRenderer extends EntityRenderer<NexusShipEntity> {
    private static final ResourceLocation WOOD = new ResourceLocation("minecraft", "textures/block/oak_planks.png");
    private static final ResourceLocation LOG = new ResourceLocation("minecraft", "textures/block/oak_log.png");
    private static final ResourceLocation SAIL = new ResourceLocation("minecraft", "textures/block/white_wool.png");
    private final NexusShipModel model;

    public NexusShipRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new NexusShipModel(context.bakeLayer(NexusShipModel.LAYER));
        this.shadowRadius = 2.0F;
    }

    @Override
    public void render(NexusShipEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 0.35D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(1.0F, -1.0F, 1.0F);

        float wave = (entity.tickCount + partialTick) * 0.08F;
        model.sail.zRot = (float) Math.sin(wave) * 0.035F;
        model.wheel.yRot = (float) Math.sin(wave * 0.55F) * 0.12F;

        renderPart(model.hull, WOOD, pose, buffers, packedLight);
        renderPart(model.deck, WOOD, pose, buffers, packedLight);
        renderPart(model.mast, LOG, pose, buffers, packedLight);
        renderPart(model.sail, SAIL, pose, buffers, packedLight);
        renderPart(model.wheel, LOG, pose, buffers, packedLight);
        renderPart(model.portRail, LOG, pose, buffers, packedLight);
        renderPart(model.starboardRail, LOG, pose, buffers, packedLight);
        renderPart(model.bow, WOOD, pose, buffers, packedLight);

        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private static void renderPart(net.minecraft.client.model.geom.ModelPart part, ResourceLocation texture,
                                   PoseStack pose, MultiBufferSource buffers, int packedLight) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(texture));
        part.render(pose, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public ResourceLocation getTextureLocation(NexusShipEntity entity) {
        return WOOD;
    }
}
