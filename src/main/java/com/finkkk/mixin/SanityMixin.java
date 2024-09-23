package com.finkkk.mixin;

import com.finkkk.sanity.SanityAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerEntity.class)
public abstract class SanityMixin implements SanityAccess {
	// 定义 SAN 值的默认值、最大值、最小值
	private static final int MAX_SANITY = 45;
	private static final int MIN_SANITY = -45;
	private int sanityValue = 10;  // 默认 SAN 值
	private static final String SANITY_TAG = "SanityValue"; // NBT 中的键名

	@Inject(method = "readCustomDataFromNbt",at = @At("TAIL"))
	private void readSanityFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains(SANITY_TAG)) {
			this.sanityValue = nbt.getInt(SANITY_TAG);
		}
	}

	// 注入写入自定义 NBT 数据的方法
	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void writeSanityToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putInt(SANITY_TAG, this.sanityValue);
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
		setSanity(2);
	}

	// 保存到 NBT（在玩家退出时调用）
	public void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putInt("SanityValue", sanityValue);
	}

	// 从 NBT 读取（在玩家加载时调用）
	public void readCustomDataFromNbt(NbtCompound nbt) {
		if (nbt.contains("SanityValue")) {
			sanityValue = nbt.getInt("SanityValue");
		}
	}
}
