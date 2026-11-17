package de.nexusbay.forge.client;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.EnelBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public class EnelModel extends GeoModel<EnelBossEntity> {
 private static final ResourceLocation MODEL=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/models/entity/enel.geo.json");
 private static final ResourceLocation TEXTURE=new ResourceLocation(NexusBayForge.MOD_ID,"textures/entity/enel.png");
 private static final ResourceLocation ANIMATION=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/animations/entity/enel.animation.json");
 @Override public ResourceLocation getModelResource(EnelBossEntity entity){return MODEL;}
 @Override public ResourceLocation getTextureResource(EnelBossEntity entity){return TEXTURE;}
 @Override public ResourceLocation getAnimationResource(EnelBossEntity entity){return ANIMATION;}
}
