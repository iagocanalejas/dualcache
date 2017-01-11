package com.iagocanalejas.tests.configurations;


import com.iagocanalejas.dualcache.DualCache;
import com.iagocanalejas.tests.DualCacheTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicle;

public class VolatileRamReferenceDiskCustomSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new DualCache.Builder<AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useReferenceInRam(RAM_MAX_SIZE, new DualCacheTest.SizeOfVehicleForTesting())
                .useSerializerInDisk(DISK_MAX_SIZE, true, new DualCacheTest.SerializerForTesting(), getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
