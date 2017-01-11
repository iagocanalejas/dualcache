package com.iagocanalejas.jacksonserializer.configurations;


import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.jacksonserializer.DualCacheJacksonTest;

public class NoRamDiskDefaultSerializer extends DualCacheJacksonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .noRam()
                .useSerializerInDisk(DISK_MAX_SIZE, true, mDefaultParser, getContext())
                .build();
    }
}
