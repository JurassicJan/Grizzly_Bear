package com.example.grizzlymod.registry;

import com.example.grizzlymod.GrizzlyMod;
import com.example.grizzlymod.entity.GrizzlyBear;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GrizzlyMod.MOD_ID);

    public static final RegistryObject<EntityType<GrizzlyBear>> GRIZZLY_BEAR = ENTITIES.register("grizzly_bear",
            () -> EntityType.Builder.of(GrizzlyBear::new, MobCategory.CREATURE)
                    .sized(1.7F, 1.7F)
                    .clientTrackingRange(10)
                    .build("grizzly_bear"));

    /** Cub: no biome spawn of its own, it appears with normal grizzlies (34%). Same class, different type. */
    public static final RegistryObject<EntityType<GrizzlyBear>> GRIZZLY_CUB = ENTITIES.register("grizzly_cub",
            () -> EntityType.Builder.of(GrizzlyBear::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .build("grizzly_cub"));
}
