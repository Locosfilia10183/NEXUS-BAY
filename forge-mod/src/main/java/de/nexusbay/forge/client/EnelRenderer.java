package de.nexusbay.forge.client;
import de.nexusbay.forge.boss.EnelBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class EnelRenderer extends GeoEntityRenderer<EnelBossEntity> {
 public EnelRenderer(EntityRendererProvider.Context context){super(context,new EnelModel());this.shadowRadius=0.8F;}
}
