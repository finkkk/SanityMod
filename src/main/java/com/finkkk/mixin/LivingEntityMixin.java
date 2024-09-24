package com.finkkk.mixin;

import com.finkkk.sanity.SanityAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{
    @Inject(method = "onDamaged",at = @At("TAIL"))
    public void onDamaged(DamageSource damageSource, CallbackInfo ci) {
        // 判断当前玩家是否是攻击者
        if (damageSource.getAttacker() instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) damageSource.getAttacker();
            if(attacker instanceof SanityAccess){
                ((SanityAccess) attacker).incrementSanity();
            }
            System.out.println(attacker.getName().getString() + "发起了攻击"+"属于玩家发起的攻击");

        }
        // 判断当前实体是否受到敌对生物攻击
        if (damageSource.getAttacker() instanceof HostileEntity) {
            HostileEntity attacker = (HostileEntity) damageSource.getAttacker();
            System.out.println(attacker.getName().getString() + "发起了攻击"+"属于敌对生物发起的攻击");
        }
    }
}
