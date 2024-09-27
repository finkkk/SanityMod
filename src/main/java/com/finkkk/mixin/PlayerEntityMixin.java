package com.finkkk.mixin;

import com.finkkk.status.SanityManager;
import com.finkkk.status.StatAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements StatAccessor {
    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Unique
    private int sanity;
    @Unique
    protected SanityManager sanityManager = new SanityManager();
    @Unique
    @Override
    public SanityManager getSanityManager() {
        return this.sanityManager;
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        if (this.getWorld().isClient) return;
        this.sanityManager.setSanity(nbt.contains(SanityManager.SANITY_NBT, NbtElement.INT_TYPE) ? nbt.getInt(SanityManager.SANITY_NBT) : 0);
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull NbtCompound nbt, CallbackInfo info) {
        if (this.getWorld().isClient) return;
        nbt.putInt(SanityManager.SANITY_NBT, this.sanityManager.getSanity());
    }
    @Inject(method = "onDeath",at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci){
        this.sanityManager.resetSanity();
    }
    @Inject(method = "onKilledOther",at = @At("HEAD"))
    public void onKilledOther(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> cir){
        if (other instanceof HostileEntity){
            this.sanityManager.addSanity(1);
            this.sendMessage(Text.of("你杀死了一个敌对生物"+other.getName()),false);
        }
        else {
            this.sanityManager.addSanity(5);
            this.sendMessage(Text.of("你杀死了一个友好生物"+other.getName()),false);
        }
    }
    @Inject(method = "tick",at = @At("HEAD"))
    public void onTick(CallbackInfo ci){
        this.sendMessage(Text.of("san值为"+this.sanityManager.getSanity()),true);
        System.out.println("san值为"+this.sanityManager.getSanity());
    }
}
