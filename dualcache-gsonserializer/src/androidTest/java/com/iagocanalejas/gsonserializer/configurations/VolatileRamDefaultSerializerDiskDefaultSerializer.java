package com.iagocanalejas.gsonserializer.configurations;


import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.gsonserializer.DualCacheGsonTest;

public class VolatileRamDefaultSerializerDiskDefaultSerializer extends DualCacheGsonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, mDefaultParser)
                .useSerializerInDisk(DISK_MAX_SIZE, true, mDefaultParser, getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
