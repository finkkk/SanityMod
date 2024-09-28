package com.finkkk.item;

import com.finkkk.status.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SanityPotionItem extends Item {
    public SanityPotionItem(Settings settings) {
        super(settings);
    }
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // 检查用户是否是玩家
        if (user instanceof PlayerEntity player) {
            // 获取玩家的 SanityManager 并增加 Sanity
            ((StatAccessor) player).getSanityManager().addSanity(10);  // 增加 10 点 sanity
        }

        // 调用超类的方法让食物的其他效果生效
        return super.finishUsing(stack, world, user);
    }
}
