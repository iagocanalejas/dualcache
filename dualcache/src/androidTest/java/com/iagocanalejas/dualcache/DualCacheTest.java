package com.iagocanalejas.dualcache;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.iagocanalejas.dualcache.interfaces.Serializer;
import com.iagocanalejas.dualcache.interfaces.SizeOf;
import com.iagocanalejas.dualcache.modes.DualCacheDiskMode;
import com.iagocanalejas.dualcache.modes.DualCacheRamMode;
import com.iagocanalejas.dualcache.testobjects.AbstractVehicle;
import com.iagocanalejas.dualcache.testobjects.CoolBike;
import com.iagocanalejas.dualcache.testobjects.CoolCar;
import com.iagocanalejas.dualcache.testobjects.JsonSerializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public abstract class DualCacheTest {

    protected static final int RAM_MAX_SIZE = 1000;
    protected static final int DISK_MAX_SIZE = 20 * RAM_MAX_SIZE;
    protected static final String CACHE_NAME = "test";
    protected static final int TEST_APP_VERSION = 0;
    protected DualCache<String, AbstractVehicle> cache;
    protected Serializer<AbstractVehicle> mDefaultSerializer;
    private Context context;

    protected Context getContext() {
        return context;
    }

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        // Default Serializer
        mDefaultSerializer = new JsonSerializer<>(AbstractVehicle.class);
    }

    @After
    public void tearDown() throws Exception {
        cache.clear();
    }

    @Test
    public void testBasicOperations() throws Exception {
        CoolCar car = new CoolCar(CoolCar.class.getSimpleName());
        String keyCar = "car";
        cache.put(keyCar, car);
        if (cache.getRamMode().equals(DualCacheRamMode.DISABLE) &&
                cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        } else {
            assertEquals(car, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
        }

        cache.clearRam();
        if (cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        } else {
            assertEquals(car, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
        }

        cache.put(keyCar, car);
        if (cache.getRamMode().equals(DualCacheRamMode.DISABLE) &&
                cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        } else {
            assertEquals(car, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
        }

        cache.clear();
        assertNull(cache.get(keyCar));
        assertEquals(false, cache.contains(keyCar));

        CoolBike bike = new CoolBike(CoolBike.class.getSimpleName());
        cache.put(keyCar, car);
        String keyBike = "bike";
        cache.put(keyBike, bike);
        if (cache.getRamMode().equals(DualCacheRamMode.DISABLE) &&
                cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
            assertNull(cache.get(keyBike));
            assertEquals(false, cache.contains(keyBike));
        } else {
            assertEquals(cache.get(keyCar), car);
            assertEquals(true, cache.contains(keyCar));
            assertEquals(cache.get(keyBike), bike);
            assertEquals(true, cache.contains(keyBike));
        }
    }

    @Test
    public void testBasicOperations2() throws Exception {
        CoolCar car = new CoolCar(CoolCar.class.getSimpleName());
        String keyCar = "car";
        cache.put(keyCar, car);
        cache.clearRam();
        if (cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        } else {
            assertEquals(car, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
            cache.clearRam();
        }

        cache.clearDisk();
        assertNull(cache.get(keyCar));
        assertEquals(false, cache.contains(keyCar));

        cache.put(keyCar, car);
        cache.clearRam();
        if (cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        } else {
            assertEquals(car, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
        }

        cache.clear();
        assertNull(cache.get(keyCar));
        assertEquals(false, cache.contains(keyCar));

        CoolBike bike = new CoolBike(CoolBike.class.getSimpleName());
        String keyBike = "bike";
        cache.put(keyCar, car);
        cache.put(keyBike, bike);
        cache.remove(keyCar);
        cache.remove(keyBike);
        assertNull(cache.get(keyCar));
        assertEquals(false, cache.contains(keyCar));
        assertNull(cache.get(keyBike));
        assertEquals(false, cache.contains(keyBike));
    }

    @Test
    public void testLRUPolicy() {
        cache.clear();
        CoolCar carToEvict = new CoolCar(CoolCar.class.getSimpleName());
        String keyCar = "car";
        cache.put(keyCar, carToEvict);
        long size = cache.getRamUsedInBytes();
        int numberOfItemsToAddForRAMEviction = (int) (RAM_MAX_SIZE / size);
        for (int i = 0; i < numberOfItemsToAddForRAMEviction; i++) {
            cache.put(keyCar + i, new CoolCar(CoolCar.class.getSimpleName()));
        }
        cache.clearDisk();
        assertNull(cache.get(keyCar));
        assertEquals(false, cache.contains(keyCar));

        cache.put(keyCar, carToEvict);
        for (int i = 0; i < numberOfItemsToAddForRAMEviction; i++) {
            cache.put(keyCar + i, new CoolCar(CoolCar.class.getSimpleName()));
        }
        if (!cache.getDiskMode().equals(DualCacheDiskMode.DISABLE)) {
            assertEquals(carToEvict, cache.get(keyCar));
            assertEquals(true, cache.contains(keyCar));
        } else {
            assertNull(cache.get(keyCar));
            assertEquals(false, cache.contains(keyCar));
        }
    }

    @Test
    public void testConcurrentAccess() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(createWorkerThread(cache));
        }
        Log.d("dualcachedebuglogti", "start worker threads");
        for (Thread thread : threads) {
            thread.start();
        }

        Log.d("dualcachedebuglogti", "joining worker threads");
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("dualcachedebuglogti", "join done");
        assertFalse("test", false);
    }

    private Thread createWorkerThread(final DualCache<String, AbstractVehicle> cache) {
        return new Thread() {
            int sMaxNumberOfRun = 1000;

            @Override
            public void run() {
                String key = "key";
                try {
                    int numberOfRun = 0;
                    while (numberOfRun++ < sMaxNumberOfRun) {
                        Thread.sleep((long) (Math.random() * 2));
                        double choice = Math.random();
                        if (choice < 0.4) {
                            cache.put(key, new CoolCar(CoolCar.class.getSimpleName()));
                        } else if (choice < 0.5) {
                            cache.remove(key);
                        } else if (choice < 0.8) {
                            cache.get(key);
                        } else if (choice < 0.9) {
                            cache.contains(key);
                        } else if (choice < 1) {
                            cache.clear();
                        } else {
                            // do nothing
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static class SerializerForTesting implements Serializer<AbstractVehicle> {

        @Override
        public AbstractVehicle fromString(String data) {
            if (data.equals(CoolBike.class.getSimpleName())) {
                return new CoolBike(CoolBike.class.getSimpleName());
            } else if (data.equals(CoolCar.class.getSimpleName())) {
                return new CoolCar(CoolCar.class.getSimpleName());
            } else {
                return null;
            }
        }

        @Override
        public String toString(AbstractVehicle object) {
            return object.getClass().getSimpleName();
        }
    }

    public static class SizeOfVehicleForTesting implements SizeOf<AbstractVehicle> {

        @Override
        public int sizeOf(AbstractVehicle object) {
            int size = 0;
            size += object.getName().length() * 2; // we suppose that char = 2 bytes
            size += 4; // we suppose that int = 4 bytes
            return size;
        }
    }
}
