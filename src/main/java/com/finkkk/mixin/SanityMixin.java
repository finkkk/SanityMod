package com.finkkk.mixin;

import com.finkkk.sanity.SanityAccess;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class SanityMixin implements SanityAccess {
	private static final TrackedData<Integer> SANITY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final String SANITY_TAG = "SanityValue";

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(CallbackInfo ci) {
		DataTracker dataTracker = ((PlayerEntity)(Object)this).getDataTracker();
		dataTracker.startTracking(SANITY, 3);  // 设置默认 SAN 值为 3
	}

	@Override
	public int getSanity() {
		return ((PlayerEntity)(Object)this).getDataTracker().get(SANITY);
	}

	@Override
	public void setSanity(int value) {
		((PlayerEntity)(Object)this).getDataTracker().set(SANITY, Math.max(0, Math.min(value, 100))); // 设置 SAN 值
	}

	@Override
	public void resetSanity() {
		setSanity(0);  // 确保这里有具体的实现
	}

	@Override
	public void incrementSanity() {
		setSanity(((PlayerEntity)(Object)this).getDataTracker().get(SANITY) + 1);  // 确保这里有具体的实现
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void writeSanityToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putInt(SANITY_TAG, getSanity());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void readSanityFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains(SANITY_TAG)) {
			setSanity(nbt.getInt(SANITY_TAG));
		}
	}
}
