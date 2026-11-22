package com.example.grizzlymod.registry;

import com.example.grizzlymod.GrizzlyMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GrizzlyMod.MOD_ID);

    public static final RegistryObject<Item> GRIZZLY_BEAR_SPAWN_EGG = ITEMS.register("grizzly_bear_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GRIZZLY_BEAR, 0x7A4424, 0xB2825A, new Item.Properties()));
}
