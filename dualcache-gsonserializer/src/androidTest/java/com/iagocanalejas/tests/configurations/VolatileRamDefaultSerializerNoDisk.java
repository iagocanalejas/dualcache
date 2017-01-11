package com.iagocanalejas.tests.configurations;


import com.iagocanalejas.dualcache.DualCache;
import com.iagocanalejas.tests.DualCacheGsonTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicle;

public class VolatileRamDefaultSerializerNoDisk extends DualCacheGsonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new DualCache.Builder<AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, mDefaultParser)
                .noDisk()
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}