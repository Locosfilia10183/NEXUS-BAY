package de.nexusbay.forge.client;

import de.nexusbay.forge.entity.QuestGiverEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class QuestGiverRenderer extends GeoEntityRenderer<QuestGiverEntity> {
    public QuestGiverRenderer(EntityRendererProvider.Context context) {
        super(context, new QuestGiverModel());
        this.shadowRadius = 0.45F;
    }
}
