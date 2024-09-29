package com.finkkk.mixin;

import com.finkkk.SanityMod;
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
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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

    private static final Identifier SANITY_BACKGROUND = new Identifier(SanityMod.MOD_ID, "textures/gui/sanity_background.png"); // 定义背景图
    @Inject(method = "render", at = @At("TAIL"))
    public void renderSanityValue(DrawContext context, float tickDelta, CallbackInfo ci) {
        // 检查 client 和 player 是否不为空
        if (client != null && client.player != null) {
            // 获取玩家的 SanityManager
            SanityManager sanityManager = ((StatAccessor) client.player).getSanityManager();
            // 只有在 Sanity 值发生变化时更新 HUD
            cachedSanity = sanityManager.getSanity();  // 更新缓存的 Sanity 值
            cachedSanDifference = sanityManager.getSanDifference();  // 更新缓存的差值
            // 设置背景图的绘制位置
            int xPos = 18;  // 背景图的X坐标
            int yPos = 0;  // 背景图的Y坐标
            int bgWidth = 90;  // 背景图的宽度
            int bgHeight = 50;  // 背景图的高度

            // 渲染背景图
            client.getTextureManager().bindTexture(SANITY_BACKGROUND);  // 绑定背景图
            context.drawTexture(SANITY_BACKGROUND, xPos, yPos, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);  // 绘制背景图

            // 设置显示的文本
            String sanityText = "理智值 " + cachedSanity + "/45";
            int textXPos = xPos + 10;  // 文本的X坐标，稍微偏移到背景图内部
            int textYPos = yPos + 21;  // 文本的Y坐标，居中显示
            int color = 0xFFFFFF;  // 白色文字颜色

            // 渲染 Sanity 值，确保它显示在背景图上层
            context.drawText(client.textRenderer, sanityText, textXPos, textYPos, color, false);
        }
    }
}
