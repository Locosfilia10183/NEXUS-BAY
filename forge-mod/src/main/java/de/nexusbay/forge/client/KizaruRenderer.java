package de.nexusbay.forge.client;
import de.nexusbay.forge.boss.KizaruBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class KizaruRenderer extends GeoEntityRenderer<KizaruBossEntity> {
 public KizaruRenderer(EntityRendererProvider.Context context){super(context,new KizaruModel());this.shadowRadius=0.9F;}
}