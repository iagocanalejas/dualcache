package com.iagocanalejas.dualcache.issues;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.iagocanalejas.dualcache.Builder;
import com.iagocanalejas.dualcache.DualCache;
import com.iagocanalejas.dualcache.interfaces.Parser;
import com.iagocanalejas.dualcache.testobjects.JsonSerializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class LanguageChangeNullPointer {
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int CACHE_RAM_ENTRIES = 25;
    protected static final String CACHE_NAME = "test";
    protected static final int TEST_APP_VERSION = 0;
    protected DualCache<String, String> mCache;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        File cacheDir = new File(context.getCacheDir(), CACHE_NAME);
        Parser<String> jsonSerializer = new JsonSerializer<>(String.class);
        mCache = new Builder<String, String>(CACHE_NAME, 0)
                .enableLog()
                .useSerializerInRam(CACHE_RAM_ENTRIES, jsonSerializer)
                .useSerializerInDisk(CACHE_SIZE, cacheDir, jsonSerializer)
                .build();
    }

    @After
    public void tearDown() throws Exception {
        mCache.clear();
    }

    @Test
    public void testConcurrentAccess() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(createWrokerThread(mCache));
        }
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertFalse("test", false);
    }

    private Thread createWrokerThread(final DualCache<String, String> cache) {
        return new Thread() {
            int sMaxNumberOfRun = 1000;

            @Override
            public void run() {
                try {
                    int numberOfRun = 0;
                    while (numberOfRun++ < sMaxNumberOfRun) {
                        Thread.sleep((long) (Math.random() * 2));
                        double choice = Math.random();
                        if (choice < 0.4) {
                            cache.put("key", "test");
                        } else if (choice < 0.5) {
                            cache.remove("key");
                        } else if (choice < 0.8) {
                            cache.get("key");
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
}
