package com.example.grizzlymod.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Material of the bear head helmet. The vanilla armor layer is fully transparent; the head itself is drawn by BearHeadLayer. */
public enum ModArmorMaterials implements ArmorMaterial {
    BEAR_HEAD("grizzlymod:bear_head", 165, 2, 9);

    private final String name;
    private final int durability;
    private final int defense;
    private final int enchantability;

    ModArmorMaterials(String name, int durability, int defense, int enchantability) {
        this.name = name;
        this.durability = durability;
        this.defense = defense;
        this.enchantability = enchantability;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return this.durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.defense;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.BEAR_PELT.get());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
