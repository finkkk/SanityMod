package com.finkkk.mixin;

import com.finkkk.status.SanityManager;
import com.finkkk.status.StatAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements StatAccessor {

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
                sanityManager.addSanity(-5);// 每次受到攻击减少 5 点 Sanity
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
            if (player.networkHandler != null) {  // 确保 networkHandler 已经初始化
                syncSanityWithClient(player);  // 同步 sanity
            }
            else {
                System.out.println("无法在 JOIN 事件中同步 Sanity，因为 networkHandler 为空！");
            }
        });
    }
    @Inject(method = "attack", at = @At("HEAD"))
    public void onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        sanityManager.setAttackDamage(player);  // 根据San值调整攻击力
    }
    @Inject(method = "damage", at = @At("TAIL"), cancellable = true)
    public void modifyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // 获取玩家的SanityManager
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();

        // 获取当前San值
        int sanity = sanityManager.getSanity();

        // 如果San值为-45，翻倍伤害
        if (sanity == -45) {
            amount *= 2.0F;
            player.sendMessage(Text.of("伤害已翻倍"));
        }
        // 继续执行原始damage方法，传入修改后的amount
        if (!this.isDead() && amount > 0.0F) {
            boolean result = super.damage(source, amount);
            cir.setReturnValue(result);  // 返回damage方法的执行结果
        } else {
            cir.setReturnValue(false);  // 如果玩家已死亡或伤害为0，返回false
        }
        // 如果需要取消原有逻辑或者进一步操作，你可以使用 `cir.setReturnValue` 或 `cir.cancel()`，否则继续执行原来的方法。
    }
    // 在 sanity 值变化的地方，比如 onKilledOther 和 onDamage 方法里添加：
    public void syncSanityWithClient(ServerPlayerEntity player) {
        if (player.networkHandler != null) {  // 检查 networkHandler 是否为 null
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(this.sanityManager.getSanity());  // 把当前的 sanity 值发送给客户端
            ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
        } else {
            System.out.println("无法同步 Sanity，因为 networkHandler 为空！");
        }
    }

}
