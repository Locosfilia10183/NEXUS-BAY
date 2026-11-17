package de.nexusbay.forge.client;

import de.nexusbay.forge.boss.KaidoBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KaidoRenderer extends GeoEntityRenderer<KaidoBossEntity> {
    public KaidoRenderer(EntityRendererProvider.Context context) {
        super(context, new KaidoModel());
        this.shadowRadius = 2.2F;
    }
}
