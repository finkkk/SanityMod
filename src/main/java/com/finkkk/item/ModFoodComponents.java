package com.finkkk.item;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent sanity_potion = new FoodComponent.Builder().hunger(3).saturationModifier(0F)
            .build();
}
