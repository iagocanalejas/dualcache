package com.iagocanalejas.tests.serializers;


import com.iagocanalejas.gsonserializer.GsonSerializer;
import com.iagocanalejas.tests.DualCacheTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

/**
 * Created by Canalejas on 31/12/2016.
 * Gson objects need a default empty constructor to work
 */
public abstract class DualCacheGsonTest extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDefaultParser = new GsonSerializer<>(AbstractVehicule.class);
    }
}
