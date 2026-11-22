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
                    .sized(1.7F, 1.9F)
                    .clientTrackingRange(10)
                    .build("grizzly_bear"));
}
