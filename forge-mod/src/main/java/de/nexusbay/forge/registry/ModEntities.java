package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.boss.KaidoBossEntity;
import de.nexusbay.forge.ship.NexusShipEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NexusBayForge.MOD_ID);

    public static final RegistryObject<EntityType<KaidoBossEntity>> KAIDO =
            ENTITY_TYPES.register("kaido", () -> EntityType.Builder
                    .of(KaidoBossEntity::new, MobCategory.MONSTER)
                    .sized(2.7F, 6.4F)
                    .clientTrackingRange(12)
                    .build("kaido"));

    public static final RegistryObject<EntityType<NexusShipEntity>> NEXUS_SHIP =
            ENTITY_TYPES.register("nexus_ship", () -> EntityType.Builder
                    .of(NexusShipEntity::new, MobCategory.MISC)
                    .sized(4.5F, 1.8F)
                    .clientTrackingRange(10)
                    .build("nexus_ship"));

    private ModEntities() {}

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
