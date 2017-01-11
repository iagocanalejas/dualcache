package com.iagocanalejas.tests;

import android.support.test.runner.AndroidJUnit4;

import com.iagocanalejas.jacksonserializer.JacksonSerializer;
import com.iagocanalejas.tests.testobjects.AbstractVehicle;

import org.junit.runner.RunWith;

/**
 * Created by Canalejas on 31/12/2016.
 */
@RunWith(AndroidJUnit4.class)
public abstract class DualCacheJacksonTest extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDefaultParser = new JacksonSerializer<>(AbstractVehicle.class);
    }

}
