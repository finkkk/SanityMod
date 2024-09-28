package com.finkkk.mixin;

import com.finkkk.status.SanityManager;
import com.finkkk.status.StatAccessor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin{
    @Unique
    private int cachedSanity = 0;         // 缓存的 Sanity 值
    @Unique
    private int cachedSanDifference = 0;  // 缓存的差值
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract int getHeartCount(LivingEntity entity);

    @Shadow
    public abstract TextRenderer getTextRenderer();
    @Inject(method = "render", at = @At("TAIL"))
    public void renderSanityValue(DrawContext context, float tickDelta, CallbackInfo ci) {
        // 检查 client 和 player 是否不为空
        if (client != null && client.player != null) {
            // 获取玩家的 SanityManager
            SanityManager sanityManager = ((StatAccessor) client.player).getSanityManager();
            // 只有在 Sanity 值发生变化时更新 HUD
            if (sanityManager.shouldUpdateHUD()) {
                cachedSanity = sanityManager.getSanity();  // 更新缓存的 Sanity 值
                cachedSanDifference = sanityManager.getSanDifference();  // 更新缓存的差值
            }
            // 设置显示的文本
            String sanityText = "San值: " + cachedSanity;
            // 设置文本颜色，白色
            int color = 0xFFFFFF;
            // 渲染 Sanity 值
            context.drawText(client.textRenderer, sanityText, 10, 10, color, false);
        }
    }
}
