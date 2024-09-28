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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.function.BiPredicate;

public class SanityManager implements ModInitializer {
    public static final String SANITY_NBT = "sanity";
    private int sanity = 0;
    private final int max_sanity = 45;
    private final int min_sanity = -45;
    private int lastSanity = 0;
    private int sanDifference = 0;
    //在 InGameHud 中不要计算 sanity 和 lastSanity 的差值，因为它的刷新速度比 PlayerEntity 中的 ticks() 快得多，导致指示上升和下降趋势的箭头闪烁
    private boolean needsUpdate = false;  // 标志是否需要更新 HUD
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
        needsUpdate = true; // 标记 HUD 需要更新
    }

    public void addSanity(int val) {
        setSanity(sanity + val);
        updateDifference();
        needsUpdate = true; // 标记 HUD 需要更新
    }

    public void resetSanity() {
        sanity = 0;
        lastSanity = 0;
        updateDifference();
        needsUpdate = true; // 标记 HUD 需要更新
    }
    public boolean shouldUpdateHUD() {
        if (needsUpdate) {
            needsUpdate = false;  // 一旦读取到变化，重置更新标志
            return true;
        }
        return false;
    }
    public void markNeedsUpdate() {
        needsUpdate = true;
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
        // 发送同步消息给客户端
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(this.getSanity());  // 把当前的 sanity 值发送给客户端
        ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
    }
    @Override
    public void onInitialize() {
        // 注册玩家加入事件
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // 确保在玩家加入时将服务端的 Sanity 数据同步到客户端
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(this.getSanity());  // 把当前的 sanity 值发送给客户端
            ServerPlayNetworking.send(player, new Identifier("sanity-mod", "sync_sanity"), buf);
        });
// 监听玩家复活事件
        ServerPlayerEvents.AFTER_RESPAWN.register((player,source,server) -> {
            SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
            sanityManager.handleAfterRespawn(player);
        });
    }
}
