package com.iagocanalejas.tests.serializers;


import android.support.test.runner.AndroidJUnit4;

import com.iagocanalejas.gsonserializer.GsonSerializer;
import com.iagocanalejas.tests.DualCacheTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicle;

import org.junit.runner.RunWith;

/**
 * Created by Canalejas on 31/12/2016.
 * Gson objects need a default empty constructor to work
 */
@RunWith(AndroidJUnit4.class)
public abstract class DualCacheGsonTest extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDefaultParser = new GsonSerializer<>(AbstractVehicle.class);
    }
}
