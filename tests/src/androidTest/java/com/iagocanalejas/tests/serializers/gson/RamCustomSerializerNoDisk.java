package com.iagocanalejas.tests.serializers.gson;


import com.iagocanalejas.dualcache.DualCache;
import com.iagocanalejas.tests.serializers.DualCacheGsonTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

public class RamCustomSerializerNoDisk extends DualCacheGsonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new DualCache.Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
                .noDisk()
                .build();
    }
}
