package de.nexusbay.forge.client;
import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.RobLucciBossEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public class RobLucciModel extends GeoModel<RobLucciBossEntity> {
 private static final ResourceLocation MODEL=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/models/entity/lucci.geo.json");
 private static final ResourceLocation TEXTURE=new ResourceLocation(NexusBayForge.MOD_ID,"textures/entity/lucci.png");
 private static final ResourceLocation ANIMATION=new ResourceLocation(NexusBayForge.MOD_ID,"geckolib/animations/entity/lucci.animation.json");
 @Override public ResourceLocation getModelResource(RobLucciBossEntity entity){return MODEL;}
 @Override public ResourceLocation getTextureResource(RobLucciBossEntity entity){return TEXTURE;}
 @Override public ResourceLocation getAnimationResource(RobLucciBossEntity entity){return ANIMATION;}
}
