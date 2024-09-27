package com.finkkk.mixin;

import com.finkkk.status.StatAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin{
    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract int getHeartCount(LivingEntity entity);

    @Shadow
    public abstract TextRenderer getTextRenderer();
    @Inject(method = "render", at = @At("HEAD"))
    public void renderSanityValue(DrawContext context, float tickDelta, CallbackInfo ci) {
        // 检查 client 和 player 是否不为空
        if (client != null && client.player != null) {
            // 获取当前的 sanity 值
            int sanity = ((StatAccessor) this.client.player).getSanityManager().getSanity();

            // 设置显示的文本
            String sanityText = "San值: " + sanity;

            // 设置文本颜色，白色
            int color = 0xFFFFFF;
            // 在屏幕左上角渲染文本，X 和 Y 为 10
            context.drawText(client.textRenderer, sanityText, 10, 10, color, false);
        }
    }
}
