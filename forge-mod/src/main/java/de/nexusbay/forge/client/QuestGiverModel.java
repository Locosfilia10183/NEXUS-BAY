package de.nexusbay.forge.client;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.entity.QuestGiverEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class QuestGiverModel extends GeoModel<QuestGiverEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(NexusBayForge.MOD_ID,
            "geckolib/models/entity/quest_giver.geo.json");
    private static final ResourceLocation ANIMATION = new ResourceLocation(NexusBayForge.MOD_ID,
            "geckolib/animations/entity/quest_giver.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/entity/villager/villager.png");

    @Override public ResourceLocation getModelResource(QuestGiverEntity entity) { return MODEL; }
    @Override public ResourceLocation getTextureResource(QuestGiverEntity entity) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(QuestGiverEntity entity) { return ANIMATION; }
}
