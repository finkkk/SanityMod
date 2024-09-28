package com.finkkk.status;
import com.finkkk.mixin.PlayerEntityMixin;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.BiPredicate;

public class SanityManager implements ModInitializer,SanityBuff {
    public static final String SANITY_NBT = "sanity";
    private int sanity = 0;
    private final int max_sanity = 45;
    private final int min_sanity = -45;
    private int lastSanity = 0;
    private int sanDifference = 0;
    //在 InGameHud 中不要计算 sanity 和 lastSanity 的差值，因为它的刷新速度比 PlayerEntity 中的 ticks() 快得多，导致指示上升和下降趋势的箭头闪烁
    private PlayerEntity player;  // 保存 PlayerEntity 实例
    // 无参构造函数
    public SanityManager() {}
    // 带 PlayerEntity 的构造函数
    public SanityManager(PlayerEntity player) {
        this.player = player;
    }

    // 设置 PlayerEntity 实例
    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public int getSanity(){
        if (sanity > max_sanity) sanity = max_sanity;
        else if (sanity < min_sanity) sanity = min_sanity;
        return sanity;
    }

    public void setSanity(int val) {
        if (val > max_sanity) val = max_sanity;
        else if (val < min_sanity) val = min_sanity;
        sanity = val;
        updateDifference();
        if (player != null) {
            updateCombatStats(player);  // 根据当前的Sanity值更新战斗属性
        }
    }

    public void addSanity(int val) {
        setSanity(sanity + val);
        updateDifference();
        if (player != null) {
            updateCombatStats(player);  // 根据当前的Sanity值更新战斗属性
        }
    }

    public void resetSanity() {
        sanity = 0;
        lastSanity = 0;
        updateDifference();
        if (player != null) {
            updateCombatStats(player);  // 根据当前的Sanity值更新战斗属性
        }
    }
    public int getSanDifference() {
        return sanDifference;
    }
    public void setSanDifference(int val) {
        sanDifference = val;
    }
    public void updateDifference() {
        sanDifference = sanity - lastSanity;
        lastSanity = sanity;
    }
    public void handleAfterRespawn(ServerPlayerEntity player) {
        resetSanity(); // 重置 Sanity 值
        if (player.networkHandler != null) {  // 确保 networkHandler 已经初始化
            // 发送同步消息给客户端
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(this.getSanity());  // 把当前的 sanity 值发送给客户端
            ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
        }
    }
    @Override
    public void onInitialize() {
        // 注册玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // 确保在玩家加入时将服务端的 Sanity 数据同步到客户端
            if (player != null && player.networkHandler != null){
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(this.getSanity());  // 把当前的 sanity 值发送给客户端
                ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
            }
        });
        // 监听玩家复活事件
        ServerPlayerEvents.AFTER_RESPAWN.register((player,source,server) -> {
            SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
            if (player.networkHandler != null) {  // 检查 networkHandler 是否为 null
                sanityManager.handleAfterRespawn(player);
            }
        });
    }
    private static final UUID SANITY_ATTACK_MODIFIER_UUID = UUID.fromString("fa233e1c-4180-4865-b01b-bc4ab223dddd"); // 创建一个独特的UUID
    @Override
    public void setAttackDamage(PlayerEntity player) {
        int sanity = getSanity();
        // 获取攻击力属性实例
        var attributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        // 先移除之前的修饰符，以避免重复叠加
        attributeInstance.removeModifier(SANITY_ATTACK_MODIFIER_UUID);
        // 如果san值为负数，玩家造成的伤害减半
        if (sanity < 0) {
            // 创建一个新的修饰符，减少50%的攻击力
            EntityAttributeModifier modifier = new EntityAttributeModifier(SANITY_ATTACK_MODIFIER_UUID,
                    "Sanity attack reduction", -0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            attributeInstance.addPersistentModifier(modifier); // 添加修饰符
        }
        // 如果san值为45，玩家造成的伤害增加50%
        else if (sanity == 45) {
            // 创建一个新的修饰符，增加50%的攻击力
            EntityAttributeModifier modifier = new EntityAttributeModifier(SANITY_ATTACK_MODIFIER_UUID,
                    "Sanity attack boost", 0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            attributeInstance.addPersistentModifier(modifier); // 添加修饰符
        }
    }
    // 监听并设置攻击和防御的函数可以放在每次San值变化后调用
    public void updateCombatStats(PlayerEntity player) {
        setAttackDamage(player);
    }
}
