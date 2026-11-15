package de.nexusbay.forge.client;
import de.nexusbay.forge.boss.RobLucciBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
public class RobLucciRenderer extends GeoEntityRenderer<RobLucciBossEntity> {
 public RobLucciRenderer(EntityRendererProvider.Context context){super(context,new RobLucciModel());this.shadowRadius=0.85F;}
}
