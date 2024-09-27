package com.finkkk.item;

import com.finkkk.SanityMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup SANITY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(SanityMod.MOD_ID,"sanity_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.sanity_group"))
                    .icon(() -> new ItemStack(ModItems.sanity_indicator)).entries((displayContext, entries) -> {
                        entries.add(ModItems.sanity_indicator);
                        entries.add(ModItems.sanity_potion);
                    }).build());
    public static void registerItemGroup() {
    }

}
