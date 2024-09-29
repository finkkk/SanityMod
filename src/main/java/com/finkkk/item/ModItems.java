package com.finkkk.item;

import com.finkkk.SanityMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item sanity_potion = regitsterItems("sanity_potion",new SanityPotionItem(new FabricItemSettings().food(ModFoodComponents.SANITY_POTION)));

    private static void addItemsToGroup(FabricItemGroupEntries groupEntries) {
        groupEntries.add(sanity_potion);
    }

    private static Item regitsterItems(String name,Item item){
        return Registry.register(Registries.ITEM,new Identifier(SanityMod.MOD_ID,name),item);
    }
    public static void regitsterModItems(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(ModItems::addItemsToGroup);
    }
}
