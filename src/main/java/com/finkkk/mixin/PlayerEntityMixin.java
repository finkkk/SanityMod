package com.finkkk.mixin;

import com.finkkk.status.SanityManager;
import com.finkkk.status.StatAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
        System.out.println("已读取到san值NBT 值为"+nbt.getInt(SanityManager.SANITY_NBT));
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull NbtCompound nbt, CallbackInfo info) {
        if (this.getWorld().isClient) return;
        nbt.putInt(SanityManager.SANITY_NBT, this.sanityManager.getSanity());
        System.out.println("已写入san值NBT 值为"+this.sanityManager.getSanity());
    }
    @Inject(method = "onKilledOther",at = @At("HEAD"))
    public void onKilledOther(ServerWorld world, LivingEntity other, CallbackInfoReturnable<Boolean> cir){
        if (other instanceof HostileEntity || other instanceof PlayerEntity){
            this.sanityManager.addSanity(10);
            this.syncSanityWithClient((ServerPlayerEntity) (Object) this);
        }
    }
    @Inject(method = "getHurtSound", at = @At("HEAD"))
    public void onGetHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
        // 检查受伤对象是否为玩家
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) (Object) this;

            // 检查伤害来源是否是生物攻击
            if (source.getAttacker() instanceof MobEntity) {
                // 获取玩家的 SanityManager 并减少 Sanity
                SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
                sanityManager.addSanity(-5);  // 每次受到攻击减少 5 点 Sanity
                this.syncSanityWithClient((ServerPlayerEntity) (Object) this);
                System.out.println("玩家 " + player.getName().getString() + " 受到了生物攻击，Sanity 减少！");
            } else {
                // 忽略环境伤害
                System.out.println("玩家受到了自然伤害或非生物攻击，Sanity 不变。");
            }
        }
    }
    @Inject(method = "initDataTracker",at = @At("TAIL"))
    public void onInit(CallbackInfo ci){
        // 注册玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // 确保在玩家加入时将服务端的 Sanity 数据同步到客户端
            syncSanityWithClient(player);
        });
    }
    // 在 sanity 值变化的地方，比如 onKilledOther 和 onDamage 方法里添加：
    public void syncSanityWithClient(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(this.sanityManager.getSanity());  // 把当前的 sanity 值发送给客户端
        ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
    }

}
