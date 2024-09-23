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



    // 存储每个玩家上一次被攻击的时间，用于冷却机制
    private long lastAttackTime = 0;

    // 设置 SAN 值的最大值和最小值
    private static final int MAX_SANITY = 45;
    private static final int MIN_SANITY = -45;
    private static final String SANITY_TAG = "SanityValue";  // NBT 数据中的键名

    @Override
    public void onInitialize() {
        // 注册复活事件监听
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer instanceof SanityAccess sanityPlayer) { // 使用接口来访问 SAN 方法
                sanityPlayer.resetSanity();
                syncSanityToClient(newPlayer);  // 同步 SAN 值到客户端
                // 发送消息到玩家的聊天框
                newPlayer.sendMessage(Text.of("SAN 值被重置了,现在的san值是"+sanityPlayer.getSanity()), false);
            }
        });

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
