package com.iagocanalejas.gsonserializer.configurationToTest;


import com.iagocanalejas.core.Builder;
import com.iagocanalejas.gsonserializer.DualCacheTest;
import com.iagocanalejas.gsonserializer.GsonSerializer;
import com.iagocanalejas.gsonserializer.testobjects.AbstractVehicule;

/**
 * Created by Iago on 26/12/2016.
 */

public class VolatileRamCustomSerializerDiskCustomSerializer extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, new GsonSerializer<>(AbstractVehicule.class))
                .useSerializerInDisk(DISK_MAX_SIZE, true, new GsonSerializer<>(AbstractVehicule.class), getContext())
                .useVolatileCache(1000 * 60) // 1 min
                .build();
    }

}
