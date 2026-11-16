package de.nexusbay.forge.client;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KizaruBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public class KizaruModel extends GeoModel<KizaruBossEntity> {
 private static final ResourceLocation MODEL=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/models/entity/kizaru.geo.json");
 private static final ResourceLocation TEXTURE=new ResourceLocation(NexusBayForge.MOD_ID,"textures/entity/kizaru.png");
 private static final ResourceLocation ANIMATION=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/animations/entity/kizaru.animation.json");
 @Override public ResourceLocation getModelResource(KizaruBossEntity entity){return MODEL;}
 @Override public ResourceLocation getTextureResource(KizaruBossEntity entity){return TEXTURE;}
 @Override public ResourceLocation getAnimationResource(KizaruBossEntity entity){return ANIMATION;}
}