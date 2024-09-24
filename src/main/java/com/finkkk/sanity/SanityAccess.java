package com.finkkk.sanity;

public interface SanityAccess {
	// 设置 SAN 值的最大值和最小值
    int MAX_SANITY = 45;
	int MIN_SANITY = -45;
	int getSanity();
	void setSanity(int value);
	void incrementSanity();
	void resetSanity();
}
