package com.iagocanalejas.tests.serializers;

import com.iagocanalejas.jacksonserializer.JacksonSerializer;
import com.iagocanalejas.tests.DualCacheTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

/**
 * Created by Canalejas on 31/12/2016.
 */

public abstract class DualCacheJacksonTest extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        defaultCacheSerializer = new JacksonSerializer<>(AbstractVehicule.class);
    }

}
