package de.nexusbay.forge.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class NexusShipModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation("nexusbay", "nexus_ship"), "main");

    public final ModelPart hull;
    public final ModelPart deck;
    public final ModelPart mast;
    public final ModelPart sail;

    public NexusShipModel(ModelPart root) {
        hull = root.getChild("hull");
        deck = root.getChild("deck");
        mast = root.getChild("mast");
        sail = root.getChild("sail");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("hull",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-24, -2, -9, 48, 10, 18),
                net.minecraft.client.model.geom.PartPose.ZERO);
        root.addOrReplaceChild("deck",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-21, -4, -7, 42, 2, 14),
                net.minecraft.client.model.geom.PartPose.ZERO);
        root.addOrReplaceChild("mast",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1, -48, -1, 2, 46, 2),
                net.minecraft.client.model.geom.PartPose.ZERO);
        root.addOrReplaceChild("sail",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-15, -43, -0.5F, 30, 30, 1),
                net.minecraft.client.model.geom.PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }
}
