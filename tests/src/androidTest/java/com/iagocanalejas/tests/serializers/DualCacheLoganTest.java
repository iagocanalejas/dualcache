package com.iagocanalejas.tests.serializers;

import com.iagocanalejas.logansquareserializer.LoganSquareSerializer;
import com.iagocanalejas.tests.DualCacheTest;
import com.iagocanalejas.tests.testobjects.AbstractVehicule;

/**
 * Created by Canalejas on 31/12/2016.
 * Requires classes to be annotated with {@link com.bluelinelabs.logansquare.annotation.JsonObject}
 * and {@link com.bluelinelabs.logansquare.annotation.JsonField}
 */
public abstract class DualCacheLoganTest extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        defaultCacheSerializer = new LoganSquareSerializer<>(AbstractVehicule.class);
    }

}
