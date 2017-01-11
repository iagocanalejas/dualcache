package com.iagocanalejas.jacksonserializer;

import android.support.test.runner.AndroidJUnit4;

import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;

import org.junit.runner.RunWith;

/**
 * Created by Canalejas on 31/12/2016.
 */
@RunWith(AndroidJUnit4.class)
public abstract class DualCacheJacksonTest extends com.iagocanalejas.dualcache.DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDefaultParser = new JacksonSerializer<>(AbstractVehicle.class);
    }

}
