package com.iagocanalejas.configurationsToTest;


import com.iagocanalejas.Builder;
import com.iagocanalejas.DualCacheTest;
import com.iagocanalejas.testobjects.AbstractVehicule;


public class NoRamDiskCustomSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .noRam()
                .useSerializerInDisk(
                        DISK_MAX_SIZE,
                        true,
                        new DualCacheTest.SerializerForTesting(),
                        getContext())
                .build();
    }
}