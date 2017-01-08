package com.iagocanalejas.tests.serializers.jackson;


import com.iagocanalejas.dualcache.DualCache;
import com.iagocanalejas.tests.serializers.DualCacheJacksonTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

public class VolatileRamDefaultSerializerNoDisk extends DualCacheJacksonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new DualCache.Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, mDefaultParser)
                .noDisk()
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
