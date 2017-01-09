package com.iagocanalejas.retrofit;

import com.iagocanalejas.dualcache.DualCache;

/**
 * Created by Canalejas on 09/01/2017.
 */

public class BaseCache {

    private static final String CACHE_NAME = "dualcache_retrofit";
    private static final long REASONABLE_DISK_SIZE = 1024 * 1024; // 1 MB
    private static final int REASONABLE_MEM_ENTRIES = 50; // 50 entries

    public static DualCache<byte[]> getInstance(int appVersion) {
        return new DualCache.Builder<byte[]>(CACHE_NAME, appVersion)
                .build();
    }

}
