package com.finkkk.item;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent SANITY_POTION = new FoodComponent.Builder()
            .hunger(0)
            .saturationModifier(0F)
            .alwaysEdible()  // 允许玩家总是可以食用
            .build();
}
