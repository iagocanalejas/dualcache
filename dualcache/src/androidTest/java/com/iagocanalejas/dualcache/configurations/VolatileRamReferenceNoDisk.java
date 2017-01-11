package com.iagocanalejas.dualcache.configurations;

import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.DualCacheTest;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;

public class VolatileRamReferenceNoDisk extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<String, AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useReferenceInRam(RAM_MAX_SIZE, new DualCacheTest.SizeOfVehicleForTesting())
                .noDisk()
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
