package com.example.grizzlymod.registry;

import com.example.grizzlymod.GrizzlyMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GrizzlyMod.MOD_ID);

    public static final RegistryObject<Item> GRIZZLY_BEAR_SPAWN_EGG = ITEMS.register("grizzly_bear_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GRIZZLY_BEAR, 0x4A3020, 0x8A6540, new Item.Properties()));

    public static final RegistryObject<Item> GRIZZLY_CUB_SPAWN_EGG = ITEMS.register("grizzly_cub_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.GRIZZLY_CUB, 0x8A4A28, 0xC89868, new Item.Properties()));

    public static final RegistryObject<Item> BEAR_MEAT = ITEMS.register("bear_meat",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).meat().build())));

    public static final RegistryObject<Item> COOKED_BEAR_MEAT = ITEMS.register("cooked_bear_meat",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).meat().build())));

    public static final RegistryObject<Item> BEAR_PELT = ITEMS.register("bear_pelt",
            () -> new Item(new Item.Properties()));

    /** Helmet: while worn, bears notice the player from 5 blocks less far away. */
    public static final RegistryObject<Item> BEAR_HEAD = ITEMS.register("bear_head",
            () -> new ArmorItem(ModArmorMaterials.BEAR_HEAD, ArmorItem.Type.HELMET, new Item.Properties()));
}
