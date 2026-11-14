package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KaidoModel extends GeoModel<KaidoBossEntity> {
    private static final ResourceLocation HUMAN_MODEL =
            new ResourceLocation(NexusBayForge.MOD_ID, "geo/entity/kaido.geo.json");
    private static final ResourceLocation HYBRID_MODEL =
            new ResourceLocation(NexusBayForge.MOD_ID, "geo/entity/kaido_hybrid.geo.json");
    private static final ResourceLocation DRAGON_MODEL =
            new ResourceLocation(NexusBayForge.MOD_ID, "geo/entity/kaido_dragon.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NexusBayForge.MOD_ID, "textures/entity/kaido.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(NexusBayForge.MOD_ID, "animations/entity/kaido.animation.json");

    @Override
    public ResourceLocation getModelResource(KaidoBossEntity entity) {
        return switch (entity.getCurrentPhase()) {
            case 2 -> HYBRID_MODEL;
            case 3 -> DRAGON_MODEL;
            default -> HUMAN_MODEL;
        };
    }

    @Override
    public ResourceLocation getTextureResource(KaidoBossEntity entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationResource(KaidoBossEntity entity) { return ANIMATION; }
}
