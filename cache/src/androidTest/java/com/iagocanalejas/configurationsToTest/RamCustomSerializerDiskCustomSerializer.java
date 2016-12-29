package com.iagocanalejas.configurationsToTest;


import com.iagocanalejas.Builder;
import com.iagocanalejas.DualCacheTest;
import com.iagocanalejas.testobjects.AbstractVehicule;

public class RamCustomSerializerDiskCustomSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new SerializerForTesting())
                .useSerializerInDisk(DISK_MAX_SIZE, true, new SerializerForTesting(), getContext())
                .build();
    }
}
