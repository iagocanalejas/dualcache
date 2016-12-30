package com.iagocanalejas.gsonserializer.configurationToTest;

import com.iagocanalejas.core.Builder;
import com.iagocanalejas.gsonserializer.DualCacheTest;
import com.iagocanalejas.gsonserializer.testobjects.AbstractVehicule;

public class VolatileRamReferenceNoDisk extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useReferenceInRam(RAM_MAX_SIZE, new SizeOfVehiculeForTesting())
                .noDisk()
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}
