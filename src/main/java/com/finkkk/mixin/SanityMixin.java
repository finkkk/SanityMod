package com.finkkk.mixin;

import com.finkkk.sanity.SanityAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerEntity.class)
public abstract class SanityMixin implements SanityAccess {
	// 定义 SAN 值的默认值、最大值、最小值
	@Unique
	private int sanityValue =1;  // 默认 SAN 值
	private static final String SANITY_TAG = "SanityValue"; // NBT 中的键名

	// 从 NBT 读取 SAN 值
	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void readSanityFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains(SANITY_TAG)) {
			this.sanityValue = nbt.getInt(SANITY_TAG);
			System.out.println("读取 SAN 值: " + this.sanityValue);  // 调试信息
		}
		else {
			System.out.println("SAN 值未找到，使用默认值: " + this.sanityValue);
		}
	}

	// 写入 SAN 值到 NBT
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void writeSanityToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putInt(SANITY_TAG, this.sanityValue);
		System.out.println("保存 SAN 值: " + this.sanityValue);  // 调试信息
	}


	// 获取 SAN 值
	@Override
	public int getSanity() {
		return this.sanityValue;
	}

	// 设置 SAN 值
	@Override
	public void setSanity(int value) {
		this.sanityValue = Math.max(MIN_SANITY, Math.min(value, MAX_SANITY));
	}

	// 增加 SAN 值
	@Override
	public void incrementSanity() {
		setSanity(this.sanityValue + 1);
	}

	// 重置 SAN 值
	@Override
	public void resetSanity() {
		setSanity(0);
	}
}
