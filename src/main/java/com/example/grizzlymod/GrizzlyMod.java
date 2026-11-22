package com.example.grizzlymod;

import com.example.grizzlymod.registry.ModEntities;
import com.example.grizzlymod.registry.ModItems;
import com.example.grizzlymod.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GrizzlyMod.MOD_ID)
public class GrizzlyMod {
    public static final String MOD_ID = "grizzlymod";

    public GrizzlyMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
    }
}
