package es.coru.iagocanalejas.library.configurationsToTest;


import es.coru.iagocanalejas.library.Builder;
import es.coru.iagocanalejas.library.DualCacheTest;
import es.coru.iagocanalejas.library.testobjects.AbstractVehicule;

public class RamDefaultSerializerNoDisk extends DualCacheTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new Builder<AbstractVehicule>(CACHE_NAME, TEST_APP_VERSION)
                .enableLog()
                .useSerializerInRam(RAM_MAX_SIZE, defaultCacheSerializer)
                .noDisk()
                .build();
    }
}
