package com.finkkk;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SanityManager implements ModInitializer {

    private int sanityValue = 0;
    // 设置 SAN 值的最大值
    private static final int MAX_SANITY = 45;
    // 设置 SAN 值的最小值
    private static final int MIN_SANITY = -45;

    @Override
    public void onInitialize() {
// 注册复活事件监听
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            resetSanity();
        });


        // 注册 HUD 渲染回调
        HudRenderCallback.EVENT.register(this::onRenderHUD);
    }

    // 增加 SAN 值
    private void incrementSanity() {
        int currentSanity = getSanity();
        if (currentSanity < MAX_SANITY) {
            setSanity(currentSanity + 1);
        }
    }

    // 重置 SAN 值为 10
    private void resetSanity() {
        setSanity(getSanity()+10);
        MinecraftClient client = MinecraftClient.getInstance();
        // 发送消息到玩家的聊天框
        client.player.sendMessage(Text.of("SAN 值被重置了"), false); // false 表示不以系统消息发送
    }

    // 获取玩家的 SAN 值
    private int getSanity() {
        return sanityValue; // 读取 SAN 值
    }

    // 设置玩家的 SAN 值
    private void setSanity(int value) {
        sanityValue = value;
    }

    // 渲染 HUD 显示 SAN 值
    private void onRenderHUD(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            sanityValue = getSanity();
            int x = 10; // X 轴位置
            int y = 10; // Y 轴位置
            drawContext.drawText(client.textRenderer, Text.of("SAN值: " + sanityValue), x, y, 0xFFFFFF, false);
        }
    }


}
