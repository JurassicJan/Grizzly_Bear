package com.example.grizzlymod.registry;

import com.example.grizzlymod.GrizzlyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GrizzlyMod.MOD_ID);

    /** Played when the bear rears up and roars (sounds/entity/grizzly/roar.ogg). */
    public static final RegistryObject<SoundEvent> GRIZZLY_ROAR = register("grizzly_roar");
    /** Played when the bear takes damage (sounds/entity/grizzly/hurt.ogg). */
    public static final RegistryObject<SoundEvent> GRIZZLY_HURT = register("grizzly_hurt");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(GrizzlyMod.MOD_ID, name)));
    }
}
