package de.nexusbay.forge.client;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public class CrocodileModel extends GeoModel<CrocodileBossEntity> {
 private static final ResourceLocation MODEL=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/models/entity/crocodile.geo.json");
 private static final ResourceLocation TEXTURE=new ResourceLocation(NexusBayForge.MOD_ID,"textures/entity/crocodile.png");
 private static final ResourceLocation ANIMATION=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/animations/entity/crocodile.animation.json");
 @Override public ResourceLocation getModelResource(CrocodileBossEntity entity){return MODEL;}
 @Override public ResourceLocation getTextureResource(CrocodileBossEntity entity){return TEXTURE;}
 @Override public ResourceLocation getAnimationResource(CrocodileBossEntity entity){return ANIMATION;}
}
