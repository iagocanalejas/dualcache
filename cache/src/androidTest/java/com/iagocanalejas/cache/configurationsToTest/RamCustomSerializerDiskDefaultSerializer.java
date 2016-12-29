package com.iagocanalejas.cache.configurationsToTest;


import com.iagocanalejas.cache.Builder;
import com.iagocanalejas.cache.DualCacheTest;
import com.iagocanalejas.cache.testobjects.AbstractVehicule;

public class RamCustomSerializerDiskDefaultSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
                .useSerializerInDisk(DISK_MAX_SIZE, true, defaultCacheSerializer, getContext())
                .build();
    }
}
