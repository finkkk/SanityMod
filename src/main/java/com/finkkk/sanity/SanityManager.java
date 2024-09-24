package com.finkkk.sanity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SanityManager implements ModInitializer {
    // 定义一个唯一的 Identifier 用于标识 SAN 值的同步数据包
    private static final Identifier SANITY_SYNC_PACKET_ID = new Identifier("sanity", "sync_sanity");

    @Override
    public void onInitialize() {
        // 注册客户端的 Packet 接收器，用于接收服务端同步过来的 SAN 值
        ClientPlayNetworking.registerGlobalReceiver(SANITY_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            int receivedSanity = buf.readInt();  // 读取 SAN 值
            client.execute(() -> {
                if (client.player instanceof SanityAccess sanityPlayer) {
                    sanityPlayer.setSanity(receivedSanity);  // 设置客户端的 SAN 值
                }
            });
        });
        // 注册 HUD 渲染回调
        HudRenderCallback.EVENT.register(this::onRenderHUD);
    }

    // 服务端同步 SAN 值到客户端
    private void syncSanityToClient(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer && player instanceof SanityAccess sanityPlayer) {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeInt(sanityPlayer.getSanity());  // 将 SAN 值写入数据包
            ServerPlayNetworking.send(serverPlayer, SANITY_SYNC_PACKET_ID, buffer);  // 发送数据包到客户端
        }
    }

    // 渲染 HUD 显示 SAN 值
    private void onRenderHUD(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player instanceof SanityAccess sanityPlayer) { // 使用接口
            int sanityValue = sanityPlayer.getSanity();
            int x = 10; // X 轴位置
            int y = 10; // Y 轴位置
            drawContext.drawText(client.textRenderer, Text.of("SAN值: " + sanityValue), x, y, 0xFFFFFF, false);
        }
    }


}
