package de.nexusbay.forge.client;
import de.nexusbay.forge.boss.CrocodileBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class CrocodileRenderer extends GeoEntityRenderer<CrocodileBossEntity> {
 public CrocodileRenderer(EntityRendererProvider.Context context){super(context,new CrocodileModel());this.shadowRadius=0.9F;}
}
