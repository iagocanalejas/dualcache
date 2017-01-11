package com.iagocanalejas.gsonserializer.configurations;


import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.DualCacheTest;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.gsonserializer.DualCacheGsonTest;

public class RamCustomSerializerNoDisk extends DualCacheGsonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new DualCacheTest.SerializerForTesting())
                .noDisk()
                .build();
    }
}
