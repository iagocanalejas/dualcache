package com.iagocanalejas.tests.serializers.jackson;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.tests.serializers.DualCacheJacksonTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

public class VolatileNoRamDiskCustomSerializer extends DualCacheJacksonTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .noRam()
                .useSerializerInDisk(
                        DISK_MAX_SIZE,
                        true,
                        new SerializerForTesting(),
                        getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }
}