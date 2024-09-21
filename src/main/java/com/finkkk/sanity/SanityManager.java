package com.finkkk.sanity;
import com.finkkk.mixin.SanityMixin;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class SanityManager implements ModInitializer {


    // 定义一个唯一的 Identifier 用于标识 SAN 值的同步数据包
    private static final Identifier SANITY_SYNC_PACKET_ID = new Identifier("sanity", "sync_sanity");


    public interface SanityAccess {
        int getSanity();
        void setSanity(int value);
        void incrementSanity();
        void resetSanity();
    }

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

        // 注册玩家受到伤害的事件
        //ServerLivingEntityEvents.ALLOW_DAMAGE.register(this::onPlayerHurt);

        // 注册被敌对生物攻击事件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (MinecraftClient.getInstance().player != null) {
                handleEntityHurtEvent();
            }
        });
    }

    // 实现减少 SAN 值的逻辑
    private void handleEntityHurtEvent() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        long currentTime = System.currentTimeMillis();

        // 判断是否有冷却
        if (player != null && player instanceof SanityAccess sanityPlayer) {
            if (player.hurtTime > 0) {
                // 判断攻击者是否为敌对生物
                if (player.getAttacker() instanceof HostileEntity) {
                    sanityPlayer.setSanity(sanityPlayer.getSanity() - 1);  // 扣除 SAN 值
                    syncSanityToClient(player);  // 同步到客户端
                    player.sendMessage(Text.of("你被敌对生物攻击了，SAN 值减少了！"), false);
                    lastAttackTime = currentTime;  // 记录攻击时间
                }
            }
            else {
                // 判断攻击者是否为敌对生物
                if (player.getAttacker() instanceof HostileEntity) {
                    sanityPlayer.setSanity(sanityPlayer.getSanity() - 1);  // 扣除 SAN 值
                    syncSanityToClient(player);  // 同步到客户端
                    player.sendMessage(Text.of("还未冷却，但你被敌对生物攻击了，SAN 值减少了！"), false);
                    lastAttackTime = currentTime;  // 记录攻击时间
                }
            }
        }
    }

/*    // 处理玩家受伤的逻辑
    private boolean onPlayerHurt(LivingEntity entity, DamageSource s, float damage) {
        if (entity instanceof PlayerEntity player && entity instanceof SanityAccess sanityPlayer) {
            // 获取攻击者，并确保它是敌对生物
            LivingEntity attacker = player.getAttacker();
            if (attacker instanceof HostileEntity) {
                sanityPlayer.setSanity(sanityPlayer.getSanity() - 1); // 减少 1 点 SAN
                player.sendMessage(Text.of("你被敌对生物攻击了，SAN 值减少了！"), false);
                return true;
            }
        }
        return false;
    }*/

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

            // 调试输出
            System.out.println("当前 HUD 渲染时获取的 SAN 值: " + sanityValue);

            drawContext.drawText(client.textRenderer, Text.of("SAN值: " + sanityValue), x, y, 0xFFFFFF, false);
        }
    }


}
