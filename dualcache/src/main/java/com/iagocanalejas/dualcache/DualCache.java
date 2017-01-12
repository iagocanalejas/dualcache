/*
 * Copyright 2014 Vincent Brison.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iagocanalejas.dualcache;

import android.support.annotation.NonNull;

import com.iagocanalejas.dualcache.caches.DiskCache;
import com.iagocanalejas.dualcache.caches.RamCache;
import com.iagocanalejas.dualcache.caches.RamSerializedCache;
import com.iagocanalejas.dualcache.interfaces.Cache;
import com.iagocanalejas.dualcache.interfaces.Hasher;
import com.iagocanalejas.dualcache.interfaces.Serializer;
import com.iagocanalejas.dualcache.interfaces.SizeOf;
import com.iagocanalejas.dualcache.interfaces.VolatileCache;
import com.iagocanalejas.dualcache.modes.DualCacheDiskMode;
import com.iagocanalejas.dualcache.modes.DualCacheKeyMode;
import com.iagocanalejas.dualcache.modes.DualCacheRamMode;
import com.iagocanalejas.dualcache.modes.DualCacheVolatileMode;
import com.iagocanalejas.dualcache.utils.VolatileEntry;
import com.iagocanalejas.dualcache.utils.VolatileSerializer;
import com.iagocanalejas.dualcache.utils.VolatileSizeOf;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * This class intent to provide a very easy to use, reliable, highly configurable caching library
 * for Android.
 *
 * @param <V> is the Class of object to cache.
 */
public class DualCache<K, V> implements VolatileCache<K, V> {
    private static final String TAG = DualCache.class.getSimpleName();

    // Basic conf
    private final int mAppVersion;
    private final Logger mLogger;

    // Key conf
    private final DualCacheKeyMode mKeyMode;
    private final Hasher<K> mHasher;

    // Disk conf
    private final DualCacheDiskMode mDiskMode;
    private Cache<String, String> mDiskLruCache;

    // Ram conf
    private final Cache mLruCache;
    private final DualCacheRamMode mRamMode;

    // Serializers
    private final VolatileSerializer<V> mVolatileRamSerializer;
    private final VolatileSerializer<V> mVolatileDiskSerializer;
    private final Serializer<V> mDiskSerializer;
    private final Serializer<V> mRamSerializer;

    // Persistence conf
    private final DualCacheVolatileMode mVolatileMode;
    private final Long mDefaultPersistenceTime;


    DualCache(int appVersion, Logger logger, DualCacheKeyMode keyMode, Hasher<K> hasher,
              DualCacheRamMode ramMode, Serializer<V> ramSerializer, int maxRamSizeBytes,
              SizeOf<V> sizeOf, DualCacheDiskMode diskMode, Serializer<V> diskSerializer,
              int maxDiskSizeBytes, File diskFolder, DualCacheVolatileMode volatileMode,
              Long defaultPersistenceTime) {

        this.mAppVersion = appVersion;
        this.mLogger = logger;
        this.mKeyMode = keyMode;
        this.mHasher = hasher;
        this.mRamMode = ramMode;
        this.mRamSerializer = ramSerializer;
        this.mDiskMode = diskMode;
        this.mDiskSerializer = diskSerializer;
        this.mDefaultPersistenceTime = defaultPersistenceTime;
        this.mVolatileMode = volatileMode;

        switch (ramMode) {
            case ENABLE_WITH_SPECIFIC_SERIALIZER:
                this.mLruCache = new RamSerializedCache(maxRamSizeBytes);
                break;
            case ENABLE_WITH_REFERENCE:
                if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)) {
                    this.mLruCache = new RamCache<>(new VolatileSizeOf<>(sizeOf), maxRamSizeBytes);
                } else {
                    this.mLruCache = new RamCache<>(sizeOf, maxRamSizeBytes);
                }
                break;
            default:
                this.mLruCache = null;
        }

        switch (diskMode) {
            case ENABLE_WITH_SPECIFIC_SERIALIZER:
                try {
                    this.mDiskLruCache = new DiskCache(diskFolder, mAppVersion, maxDiskSizeBytes);
                } catch (IOException e) {
                    mLogger.logError(TAG, e);
                }
                break;
            default:
                this.mDiskLruCache = null;
        }


        //region Init Volatile Serializers
        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && ramMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileRamSerializer = new VolatileSerializer<>(ramSerializer);
        } else {
            mVolatileRamSerializer = null;
        }

        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && diskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileDiskSerializer = new VolatileSerializer<>(diskSerializer);
        } else {
            mVolatileDiskSerializer = null;
        }
        //endregion

    }

    public int getCacheVersion() {
        return mAppVersion;
    }

    public long getRamUsedInBytes() {
        if (mLruCache == null) {
            return -1;
        } else {
            return mLruCache.size();
        }
    }

    public long getDiskUsedInBytes() {
        if (mDiskLruCache == null) {
            return -1;
        } else {
            return mDiskLruCache.size();
        }

    }

    /**
     * Return the way keys are handled.
     *
     * @return the way keys are handled.
     */
    public DualCacheKeyMode getKeyMode() {
        return mKeyMode;
    }

    /**
     * Return the way objects are cached in RAM layer.
     *
     * @return the way objects are cached in RAM layer.
     */
    public DualCacheRamMode getRamMode() {
        return mRamMode;
    }

    /**
     * Return the way objects are cached in disk layer.
     *
     * @return the way objects are cached in disk layer.
     */
    public DualCacheDiskMode getDiskMode() {
        return mDiskMode;
    }


    /**
     * Return the way cache persistence is working
     *
     * @return the way cache persistence is working
     */
    public DualCacheVolatileMode getPersistenceMode() {
        return mVolatileMode;
    }

    //region PUT

    /**
     * Put a volatile entry on the cache
     *
     * @param key       the key of the object.
     * @param object    the object to put in cache.
     * @param entryLife persistence time for given entry, <= 0 means persistence entry
     */
    @SuppressWarnings("unchecked")
    private V putVolatileEntry(String key, V object, long entryLife) {
        long lifetime = (entryLife > 0)
                ? Calendar.getInstance().getTimeInMillis() + entryLife
                : 0; // Same as a persistence entry

        V previous = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            VolatileEntry<V> prev = ((VolatileEntry<V>) mLruCache.put(key,
                    new VolatileEntry<>(lifetime, object)));
            if (prev != null && prev.getTimestamp().before(Calendar.getInstance().getTime())) {
                previous = prev.getItem();
            }
        }

        String ramSerialized = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = mVolatileRamSerializer.toString(new VolatileEntry<>(lifetime, object));
            String prev = (String) mLruCache.put(key, ramSerialized);
            if (prev != null) {
                VolatileEntry<V> prevEntry = mVolatileRamSerializer.fromString(prev);
                if (prevEntry.getTimestamp().before(Calendar.getInstance().getTime())) {
                    previous = prevEntry.getItem();
                }
            }
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            if (mVolatileRamSerializer == mVolatileDiskSerializer) {
                // Optimization if using same serializer
                mDiskLruCache.put(key, ramSerialized);
            } else {
                String prev = mDiskLruCache.put(key,
                        mVolatileDiskSerializer.toString(new VolatileEntry<>(lifetime, object)));
                if (prev != null && previous == null) {
                    VolatileEntry<V> prevEntry = mVolatileDiskSerializer.fromString(prev);
                    if (prevEntry.getTimestamp().before(Calendar.getInstance().getTime())) {
                        previous = prevEntry.getItem();
                    }
                }
            }
        }
        return previous;
    }

    /**
     * Put a persistent entry on the cache
     *
     * @param key    the key of the object.
     * @param object the object to put in cache.
     */
    @SuppressWarnings("unchecked")
    private V putEntry(String key, V object) {
        V previous = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            previous = (V) mLruCache.put(key, object);
        }

        String ramSerialized = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = mRamSerializer.toString(object);
            String prev = (String) mLruCache.put(key, ramSerialized);
            if (prev != null) {
                previous = mRamSerializer.fromString(prev);
            }
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            if (mRamSerializer == mDiskSerializer) {
                // Optimization if using same serializer
                mDiskLruCache.put(key, ramSerialized);
            } else {
                String prev = mDiskLruCache.put(key, mDiskSerializer.toString(object));
                if (prev != null && previous == null) {
                    previous = mDiskSerializer.fromString(prev);
                }
            }
        }
        return previous;
    }

    /**
     * Put an object in cache with a lifetime if we are in a
     * {@link DualCacheVolatileMode#VOLATILE} cache
     *
     * @param key       the key of the object.
     * @param object    the object to put in cache.
     * @param entryLife persistence time for given entry
     */
    @Override
    public V put(@NonNull K key, V object, long entryLife) {
        String nKey = (mKeyMode.equals(DualCacheKeyMode.HASHED_KEY))
                ? mHasher.hash(key)
                : (String) key;

        if (!mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            throw new UnsupportedOperationException(
                    "Operation only supported for VOLATILE cache type");
        }
        return putVolatileEntry(nKey, object, entryLife);
    }

    /**
     * Put an object in cache.
     *
     * @param key    is the key of the object.
     * @param object is the object to put in cache.
     */
    @Override
    public V put(@NonNull K key, V object) {
        String nKey = (mKeyMode.equals(DualCacheKeyMode.HASHED_KEY))
                ? mHasher.hash(key)
                : (String) key;

        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return putVolatileEntry(nKey, object, mDefaultPersistenceTime);
        }
        return putEntry(nKey, object);
    }

    @Override
    public int size() {
        return (int) (getRamUsedInBytes() + getDiskUsedInBytes());
    }
    //endregion

    //region GET

    /**
     * Try to get key object from disk
     *
     * @param key given string to search
     * @return entry string or null
     */
    private String getFromDisk(String key) {
        String value = mDiskLruCache.get(key);
        if (value != null) {
            mLogger.logEntryForKeyIsOnDisk(key);
        } else {
            mLogger.logEntryForKeyIsNotOnDisk(key);
        }
        return value;
    }

    /**
     * Try to find a volatile entry on the cache
     *
     * @param key given entry to find
     * @return entry or null
     */
    @SuppressWarnings("unchecked")
    private V getVolatileEntry(String key) {
        Object ramResult = null;
        String diskResult = null;

        // Try to get the object from RAM.
        boolean isRamSerialized = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = mLruCache.get(key);
        }

        VolatileEntry<V> cacheEntry;
        if (ramResult == null) {
            // Try to get the cached object from disk.
            mLogger.logEntryForKeyIsNotInRam(key);
            if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                diskResult = getFromDisk(key);
            }

            if (diskResult != null) {
                // Load object, no need to check disk configuration since diskResult != null.
                cacheEntry = mVolatileDiskSerializer.fromString(diskResult);

                //Invalidate cache if required
                if (cacheEntry.getTimestamp().before(Calendar.getInstance().getTime())
                        && !(cacheEntry.getTimestamp().getTime() == 0)) {
                    //Invalidate cache
                    removeHashed(key);
                    return null;
                }

                // Refresh object in ram.
                if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                    if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                        mLruCache.put(key, cacheEntry);
                    }
                } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                    if (mDiskSerializer == mRamSerializer) {
                        mLruCache.put(key, diskResult);
                    } else {
                        mLruCache.put(key, mVolatileRamSerializer.toString(cacheEntry));
                    }
                }
                return cacheEntry.getItem();
            }
        } else { // ramResult != null
            mLogger.logEntryForKeyIsInRam(key);
            if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                cacheEntry = (VolatileEntry<V>) ramResult;
                //Invalidate cache if needed
                if (cacheEntry.getTimestamp().before(Calendar.getInstance().getTime())
                        && !(cacheEntry.getTimestamp().getTime() == 0)) {
                    removeHashed(key);
                    return null;
                }
                return cacheEntry.getItem();
            } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                cacheEntry = mVolatileRamSerializer.fromString((String) ramResult);
                if (cacheEntry.getTimestamp().before(Calendar.getInstance().getTime())
                        && !(cacheEntry.getTimestamp().getTime() == 0)) {
                    removeHashed(key);
                    return null;
                }
                return cacheEntry.getItem();
            }
        }

        // No data is available.
        return null;
    }

    /**
     * Try to find a persistent entry on cache
     *
     * @param key given entry to find
     * @return entry or null
     */
    @SuppressWarnings("unchecked")
    private V getEntry(String key) {
        Object ramResult = null;
        String diskResult = null;

        // Try to get the object from RAM.
        boolean isRamSerialized = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = mLruCache.get(key);
        }

        if (ramResult == null) {
            // Try to get the cached object from disk.
            mLogger.logEntryForKeyIsNotInRam(key);
            if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                diskResult = getFromDisk(key);
            }

            if (diskResult != null) {
                // Load object, no need to check disk configuration since diskResult != null.
                V objectFromStringDisk = mDiskSerializer.fromString(diskResult);
                // Refresh object in ram.
                if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                    if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                        mLruCache.put(key, objectFromStringDisk);
                    }
                } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                    if (mDiskSerializer == mRamSerializer) {
                        mLruCache.put(key, diskResult);
                    } else {
                        mLruCache.put(key, mRamSerializer.toString(objectFromStringDisk));
                    }
                }
                return objectFromStringDisk;
            }
        } else { // ramResult != null
            mLogger.logEntryForKeyIsInRam(key);
            if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                return (V) ramResult;
            } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                return mRamSerializer.fromString((String) ramResult);
            }
        }

        // No data is available.
        return null;
    }

    /**
     * Return the object of the corresponding key from the cache. In no object is available,
     * return null.
     *
     * @param key is the key of the object.
     * @return the object of the corresponding key from the cache. In no object is available,
     * return null.
     */
    @Override
    public V get(@NonNull K key) {
        String nKey = (mKeyMode.equals(DualCacheKeyMode.HASHED_KEY))
                ? mHasher.hash(key)
                : (String) key;

        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return getVolatileEntry(nKey);
        }
        return getEntry(nKey);
    }
    //endregion

    @SuppressWarnings("unchecked")
    private V removeVolatileEntry(String key) {
        V previous = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            VolatileEntry<V> previousEntry = ((VolatileEntry<V>) mLruCache.remove(key));
            if (previousEntry != null) {
                previous = previousEntry.getItem();
            }
        }
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            String prev = (String) mLruCache.remove(key);
            if (prev != null) {
                VolatileEntry<V> prevEntry = mVolatileRamSerializer.fromString(prev);
                if (prevEntry.getTimestamp().before(Calendar.getInstance().getTime())) {
                    previous = prevEntry.getItem();
                }
            }
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            if (mVolatileRamSerializer == mVolatileDiskSerializer) {
                mDiskLruCache.remove(key);
            } else {
                String prev = mDiskLruCache.remove(key);
                if (prev != null) {
                    VolatileEntry<V> prevEntry = mVolatileDiskSerializer.fromString(prev);
                    if (prevEntry.getTimestamp().before(Calendar.getInstance().getTime())) {
                        previous = prevEntry.getItem();
                    }
                }
            }
        }
        return previous;
    }

    @SuppressWarnings("unchecked")
    private V removeEntry(String key) {
        V previous = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            previous = (V) mLruCache.remove(key);
        }
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            String prev = (String) mLruCache.remove(key);
            if (prev != null) {
                previous = mRamSerializer.fromString(prev);
            }
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            if (mVolatileRamSerializer == mVolatileDiskSerializer) {
                mDiskLruCache.remove(key);
            } else {
                String prev = mDiskLruCache.remove(key);
                if (prev != null && previous == null) {
                    previous = mDiskSerializer.fromString(prev);
                }
            }
        }
        return previous;
    }

    /**
     * Delete the corresponding object in cache.
     *
     * @param key is the key of the object.
     * @return removed entry or null
     */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(@NonNull K key) {
        String nKey = (mKeyMode.equals(DualCacheKeyMode.HASHED_KEY))
                ? mHasher.hash(key)
                : (String) key;

        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return removeVolatileEntry(nKey);
        }
        return removeEntry(nKey);
    }

    /**
     * Used if key is already hashed
     *
     * @param key for the entry
     * @return removed entry or null
     */
    private V removeHashed(String key) {
        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return removeVolatileEntry(key);
        }
        return removeEntry(key);
    }

    /**
     * Remove all objects from cache (both RAM and disk).
     */
    @Override
    public void clear() {
        clearDisk();
        clearRam();
    }

    /**
     * Remove all objects from RAM.
     */
    public void clearRam() {
        if (!mRamMode.equals(DualCacheRamMode.DISABLE)) {
            mLruCache.clear();
        }
    }

    /**
     * Remove all objects from Disk.
     */
    public void clearDisk() {
        if (!mDiskMode.equals(DualCacheDiskMode.DISABLE)) {
            mDiskLruCache.clear();
        }
    }

    /**
     * Test if an object is present in cache.
     *
     * @param key is the key of the object.
     * @return true if the object is present in cache, false otherwise.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(@NonNull K key) {
        String nKey = (mKeyMode.equals(DualCacheKeyMode.HASHED_KEY))
                ? mHasher.hash(key)
                : (String) key;

        return !mRamMode.equals(DualCacheRamMode.DISABLE) && mLruCache.contains(nKey)
                || !mDiskMode.equals(DualCacheDiskMode.DISABLE) && mDiskLruCache.contains(nKey);
    }

}
