/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iagocanalejas.core;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache that holds strong references to a limited number of values. Each time
 * a value is accessed, it is moved to the head of a queue. When a value is
 * added to a full cache, the value at the end of that queue is evicted and may
 * become eligible for garbage collection.
 * <p>
 * <p>If your cached values hold resources that need to be explicitly released,
 * override {@link #entryRemoved}.
 * <p>
 * <p>If a cache miss should be computed on demand for the corresponding keys,
 * override {@link #create}. This simplifies the calling code, allowing it to
 * assume a value will always be returned, even when there's a cache miss.
 * <p>
 * <p>By default, the cache mCacheSize is measured in the number of entries. Override
 * {@link #sizeOf} to mCacheSize the cache in different units. For example, this cache
 * is limited to 4MiB of bitmaps:
 * <pre>   {@code
 *   int cacheSize = 4 * 1024 * 1024; // 4MiB
 *   LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
 *       protected int sizeOf(String key, Bitmap value) {
 *           return value.getByteCount();
 *       }
 *   }}</pre>
 * <p>
 * <p>This class is thread-safe. Perform multiple cache operations atomically by
 * synchronizing on the cache: <pre>   {@code
 *   synchronized (cache) {
 *     if (cache.get(key) == null) {
 *         cache.put(key, value);
 *     }
 *   }}</pre>
 * <p>
 * <p>This class does not allow null to be used as a key or value. A return
 * value of null from {@link #get}, {@link #put} or {@link #remove} is
 * unambiguous: the key was not in the cache.
 * <p>
 * <p>This class appeared in Android 3.1 (Honeycomb MR1); it's available as part
 * of <a href="http://developer.android.com/sdk/compatibility-library.html">Android's
 * Support Package</a> for earlier releases.
 */
class RamLruCache<K, V> {
    private final LinkedHashMap<K, V> mCacheMap;

    /**
     * Size of this cache in units. Not necessarily the number of elements.
     */
    private int mCacheSize;
    private int mMaxCacheSize;

    private int mPutCount;
    private int mCreateCount;
    private int mEvictionCount;
    private int mHitCount;
    private int mMissCount;

    /**
     * @param maxCacheSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public RamLruCache(int maxCacheSize) {
        if (maxCacheSize <= 0) {
            throw new IllegalArgumentException("mMaxCacheSize <= 0");
        }
        this.mMaxCacheSize = maxCacheSize;
        this.mCacheMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    /**
     * Sets the mCacheSize of the cache.
     *
     * @param maxSize The new maximum mCacheSize.
     */
    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("mMaxCacheSize <= 0");
        }

        synchronized (this) {
            this.mMaxCacheSize = maxSize;
        }
        trimToSize(maxSize);
    }

    /**
     * Returns the value for {@code key} if it exists in the cache or can be
     * created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     */
    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V mapValue;
        synchronized (this) {
            mapValue = mCacheMap.get(key);
            if (mapValue != null) {
                mHitCount++;
                return mapValue;
            }
            mMissCount++;
        }

        /*
         * Attempt to create a value. This may take a long time, and the mCacheMap
         * may be different when create() returns. If a conflicting value was
         * added to the mCacheMap while create() was working, we leave that value in
         * the mCacheMap and release the created value.
         */

        V createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        synchronized (this) {
            mCreateCount++;
            mapValue = mCacheMap.put(key, createdValue);

            if (mapValue != null) {
                // There was a conflict so undo that last put
                mCacheMap.put(key, mapValue);
            } else {
                mCacheSize += safeSizeOf(key, createdValue);
            }
        }

        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue;
        } else {
            trimToSize(mMaxCacheSize);
            return createdValue;
        }
    }

    /**
     * Caches {@code value} for {@code key}. The value is moved to the head of
     * the queue.
     *
     * @return the previous value mapped by {@code key}.
     */
    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        V previous;
        synchronized (this) {
            mPutCount++;
            mCacheSize += safeSizeOf(key, value);
            previous = mCacheMap.put(key, value);
            if (previous != null) {
                mCacheSize -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(mMaxCacheSize);
        return previous;
    }

    /**
     * Remove the eldest entries until the total of remaining entries is at or
     * below the requested mCacheSize.
     *
     * @param maxSize the maximum mCacheSize of the cache before returning. May be -1
     *                to evict even 0-sized elements.
     */
    public void trimToSize(int maxSize) {
        while (true) {
            K key;
            V value;
            synchronized (this) {
                if (mCacheSize < 0 || (mCacheMap.isEmpty() && mCacheSize != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                if (mCacheSize <= maxSize) {
                    break;
                }
                Map.Entry<K, V> toEvict = null;
                try {
                    toEvict = (Map.Entry<K, V>) mCacheMap.getClass().getMethod("eldest").invoke(mCacheMap);

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();
                mCacheMap.remove(key);
                mCacheSize -= safeSizeOf(key, value);
                mEvictionCount++;
            }
            entryRemoved(true, key, value, null);
        }
    }

    /**
     * Removes the entry for {@code key} if it exists.
     *
     * @return the previous value mapped by {@code key}.
     */
    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        V previous;
        synchronized (this) {
            previous = mCacheMap.remove(key);
            if (previous != null) {
                mCacheSize -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    /**
     * Called for entries that have been evicted or removed. This method is
     * invoked when a value is evicted to make space, removed by a call to
     * {@link #remove}, or replaced by a call to {@link #put}. The default
     * implementation does nothing.
     * <p>
     * <p>The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     *
     * @param evicted  true if the entry is being removed to make space, false
     *                 if the removal was caused by a {@link #put} or {@link #remove}.
     * @param newValue the new value for {@code key}, if it exists. If non-null,
     *                 this removal was caused by a {@link #put}. Otherwise it was caused by
     *                 an eviction or a {@link #remove}.
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }

    /**
     * Called after a cache miss to compute a value for the corresponding key.
     * Returns the computed value or null if no value can be computed. The
     * default implementation returns null.
     * <p>
     * <p>The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     * <p>
     * <p>If a value for {@code key} exists in the cache when this method
     * returns, the created value will be released with {@link #entryRemoved}
     * and discarded. This can occur when multiple threads request the same key
     * at the same time (causing multiple values to be created), or when one
     * thread calls {@link #put} while another is creating a value for the same
     * key.
     */
    protected V create(K key) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative mCacheSize: " + key + "=" + value);
        }
        return result;
    }

    /**
     * Returns the mCacheSize of the entry for {@code key} and {@code value} in
     * user-defined units.  The default implementation returns 1 so that mCacheSize
     * is the number of entries and max mCacheSize is the maximum number of entries.
     * <p>
     * <p>An entry's mCacheSize must not change while it is in the cache.
     */
    protected int sizeOf(K key, V value) {
        return 1;
    }

    /**
     * Clear the cache, calling {@link #entryRemoved} on each removed entry.
     */
    public final void evictAll() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache.
     */
    public synchronized final int size() {
        return mCacheSize;
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the maximum
     * number of entries in the cache. For all other caches, this returns the
     * maximum sum of the sizes of the entries in this cache.
     */
    public synchronized final int maxSize() {
        return mMaxCacheSize;
    }

    /**
     * Returns the number of times {@link #get} returned a value that was
     * already present in the cache.
     */
    public synchronized final int hitCount() {
        return mHitCount;
    }

    /**
     * Returns the number of times {@link #get} returned null or required a new
     * value to be created.
     */
    public synchronized final int missCount() {
        return mMissCount;
    }

    /**
     * Returns the number of times {@link #create(Object)} returned a value.
     */
    public synchronized final int createCount() {
        return mCreateCount;
    }

    /**
     * Returns the number of times {@link #put} was called.
     */
    public synchronized final int putCount() {
        return mPutCount;
    }

    /**
     * Returns the number of values that have been evicted.
     */
    public synchronized final int evictionCount() {
        return mEvictionCount;
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(mCacheMap);
    }

    @Override
    public synchronized final String toString() {
        int accesses = mHitCount + mMissCount;
        int hitPercent = accesses != 0 ? (100 * mHitCount / accesses) : 0;
        return String.format("LruCache[mMaxCacheSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
                mMaxCacheSize, mHitCount, mMissCount, hitPercent);
    }
}
