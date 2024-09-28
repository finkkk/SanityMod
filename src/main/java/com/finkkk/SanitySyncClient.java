package com.finkkk;

import com.finkkk.status.StatAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SanitySyncClient {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("sanity-mod", "sync_sanity"), (client, handler, buf, responseSender) -> {
            int sanity = buf.readInt();  // 读取从服务端发来的 sanity 值

            // 在客户端线程中更新 SanityManager 的值
            client.execute(() -> {
                if (client.player != null) {
                    ((StatAccessor) client.player).getSanityManager().setSanity(sanity);
                }
            });
        });
    }
}
