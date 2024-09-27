package com.finkkk.status;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
    public int getSanity(){
        if (sanity > max_sanity) sanity = max_sanity;
        else if (sanity < min_sanity) sanity = min_sanity;
        return sanity;
    }

    public void setSanity(int val) {
        if (val > max_sanity) val = max_sanity;
        else if (val < min_sanity) val = min_sanity;
        sanity = val;
    }

    public void addSanity(int val) {
        setSanity(sanity + val);
    }

    public void resetSanity() {
        sanity = 0;
        lastSanity = 0;
        updateDifference();
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
    @Override
    public void onInitialize() {
        // 注册 HUD 渲染回调
        //HudRenderCallback.EVENT.register(this::renderSanityHud);
    }
}
