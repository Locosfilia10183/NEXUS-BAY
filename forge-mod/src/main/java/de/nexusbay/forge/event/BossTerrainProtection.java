package de.nexusbay.forge.event;

import de.nexusbay.forge.boss.BossMob;
import net.minecraftforge.event.entity.living.MobGriefingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Prevents every NEXUS-BAY boss from changing blocks through mob griefing. */
@Mod.EventBusSubscriber(modid = "nexusbay")
public final class BossTerrainProtection {
    private BossTerrainProtection() {}

    @SubscribeEvent
    public static void onMobGriefing(MobGriefingEvent event) {
        if (event.getEntity() instanceof BossMob) {
            event.setCanGrief(false);
        }
    }
}
