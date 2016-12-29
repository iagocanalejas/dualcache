package com.iagocanalejas.core.configurationsToTest;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.core.DualCacheTest;
import com.iagocanalejas.core.testobjects.AbstractVehicule;

public class VolatileNoRamDiskDefaultSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .noRam()
                .useSerializerInDisk(DISK_MAX_SIZE, true, defaultCacheSerializer, getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}