package com.iagocanalejas.gsonserializer.configurations;


import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.gsonserializer.DualCacheGsonTest;

public class VolatileNoRamDiskDefaultSerializer extends DualCacheGsonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .noRam()
                .useSerializerInDisk(DISK_MAX_SIZE, true, mDefaultSerializer, getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
