package com.finkkk.mixin;

import com.finkkk.sanity.SanityAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin{
    @Shadow private @Nullable LivingEntity attacker;

    @Shadow public abstract @Nullable LivingEntity getAttacker();

    @Inject(method = "onDamaged",at = @At("TAIL"))
    public void onDamaged(DamageSource damageSource, CallbackInfo ci) {
        // 判断当前玩家是否是攻击者
        if (damageSource.getAttacker() instanceof PlayerEntity) {
            PlayerEntity attacker = (PlayerEntity) damageSource.getAttacker();
            if (attacker instanceof SanityAccess) {
                ((SanityAccess) attacker).incrementSanity(); // 玩家理智值增加
            }
            attacker.sendMessage(Text.of("玩家攻击了敌对生物"+(Object)this.getAttacker().getName()));
            System.out.println(attacker.getName().getString() + " 攻击了敌对生物，理智值增加");
        }
    }
}
