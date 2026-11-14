package de.nexusbay.forge.registry;

import de.nexusbay.forge.NexusBayForge;
import de.nexusbay.forge.worldgen.IslandAtlasDensityFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.serialization.Codec;

public final class ModWorldgen {
    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, NexusBayForge.MOD_ID);

    public static final RegistryObject<Codec<? extends DensityFunction>> ISLAND_ATLAS =
            DENSITY_FUNCTION_TYPES.register("island_atlas",
                    () -> IslandAtlasDensityFunction.CODEC_HOLDER.codec().codec());

    private ModWorldgen() {}

    public static void register(net.minecraftforge.eventbus.api.IEventBus modBus) {
        DENSITY_FUNCTION_TYPES.register(modBus);
    }
}
