package com.iagocanalejas.core.configurationsToTest;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.core.DualCacheTest;
import com.iagocanalejas.core.testobjects.AbstractVehicule;

public class RamDefaultSerializerNoDisk extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, defaultCacheSerializer)
                .noDisk()
                .build();
    }
}