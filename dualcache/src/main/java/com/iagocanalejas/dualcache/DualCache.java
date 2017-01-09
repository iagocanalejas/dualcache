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

import android.content.Context;

import com.iagocanalejas.dualcache.caches.DiskCache;
import com.iagocanalejas.dualcache.caches.RamCache;
import com.iagocanalejas.dualcache.caches.RamSerializedCache;
import com.iagocanalejas.dualcache.caches.base.LruCache;
import com.iagocanalejas.dualcache.interfaces.Parser;
import com.iagocanalejas.dualcache.interfaces.SizeOf;
import com.iagocanalejas.dualcache.interfaces.VolatileCache;
import com.iagocanalejas.dualcache.modes.DualCacheDiskMode;
import com.iagocanalejas.dualcache.modes.DualCacheRamMode;
import com.iagocanalejas.dualcache.modes.DualCacheVolatileMode;
import com.iagocanalejas.dualcache.wrappers.VolatileEntry;
import com.iagocanalejas.dualcache.wrappers.VolatileParser;
import com.iagocanalejas.dualcache.wrappers.VolatileSizeOf;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * This class intent to provide a very easy to use, reliable, highly configurable caching library
 * for Android.
 *
 * @param <V> is the Class of object to cache.
 */
public class DualCache<V> implements VolatileCache<String, V> {
    private static final String TAG = DualCache.class.getSimpleName();

    // Basic conf
    private final int mAppVersion;
    private final Logger mLogger;

    // Disk conf
    private final int mMaxDiskSizeBytes;
    private final DualCacheDiskMode mDiskMode;
    private DiskCache mDiskLruCache;

    // Ram conf
    private final LruCache mLruCache;
    private final DualCacheRamMode mRamMode;

    // Serializers
    private final VolatileParser<V> mVolatileRamSerializer;
    private final VolatileParser<V> mVolatileDiskSerializer;
    private final Parser<V> mDiskSerializer;
    private final Parser<V> mRamSerializer;

    // Persistence conf
    private final DualCacheVolatileMode mVolatileMode;
    private final Long mDefaultPersistenceTime;


    DualCache(int appVersion, Logger logger, DualCacheRamMode ramMode,
              Parser<V> ramSerializer, int maxRamSizeBytes, SizeOf<V> sizeOf,
              DualCacheDiskMode diskMode, Parser<V> diskSerializer, int maxDiskSizeBytes,
              File diskFolder, DualCacheVolatileMode volatileMode, Long defaultPersistenceTIme) {

        this.mAppVersion = appVersion;
        this.mRamMode = ramMode;
        this.mRamSerializer = ramSerializer;
        this.mDiskMode = diskMode;
        this.mDiskSerializer = diskSerializer;
        this.mLogger = logger;
        this.mDefaultPersistenceTime = defaultPersistenceTIme;
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
                this.mMaxDiskSizeBytes = maxDiskSizeBytes;
                try {
                    this.mDiskLruCache = new DiskCache(diskFolder, mAppVersion, mMaxDiskSizeBytes);
                } catch (IOException e) {
                    mLogger.logError(TAG, e);
                }
                break;
            default:
                this.mMaxDiskSizeBytes = 0;
                this.mDiskLruCache = null;
        }


        //region Init Volatile Serializers
        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && ramMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileRamSerializer = new VolatileParser<>(ramSerializer);
        } else {
            mVolatileRamSerializer = null;
        }

        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && diskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileDiskSerializer = new VolatileParser<>(diskSerializer);
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
     * Return the way objects are cached in RAM layer.
     *
     * @return the way objects are cached in RAM layer.
     */
    public DualCacheRamMode getRAMMode() {
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
    public V put(String key, V object, long entryLife) {
        if (!mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            throw new UnsupportedOperationException(
                    "Operation only supported for VOLATILE cache type");
        }
        return putVolatileEntry(key, object, entryLife);
    }

    /**
     * Put an object in cache.
     *
     * @param key    is the key of the object.
     * @param object is the object to put in cache.
     */
    @Override
    public V put(String key, V object) {
        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return putVolatileEntry(key, object, mDefaultPersistenceTime);
        }
        return putEntry(key, object);
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
                    remove(key);
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
                    remove(key);
                    return null;
                }
                return cacheEntry.getItem();
            } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                cacheEntry = mVolatileRamSerializer.fromString((String) ramResult);
                if (cacheEntry.getTimestamp().before(Calendar.getInstance().getTime())
                        && !(cacheEntry.getTimestamp().getTime() == 0)) {
                    remove(key);
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
    public V get(String key) {
        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return getVolatileEntry(key);
        }
        return getEntry(key);
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
     */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(String key) {
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
        invalidateDisk();
        invalidateRAM();
    }

    /**
     * Remove all objects from RAM.
     */
    public void invalidateRAM() {
        if (!mRamMode.equals(DualCacheRamMode.DISABLE)) {
            mLruCache.clear();
        }
    }

    /**
     * Remove all objects from Disk.
     */
    public void invalidateDisk() {
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
    public boolean contains(String key) {
        return !mRamMode.equals(DualCacheRamMode.DISABLE) && mLruCache.snapshot().containsKey(key)
                || !mDiskMode.equals(DualCacheDiskMode.DISABLE) && mDiskLruCache.contains(key);
    }

    /**
     * Class used to build a cache.
     *
     * @param <T> is the class of object to store in cache.
     */
    public static class Builder<T> {

        /**
         * Defined the sub folder from {@link Context#getCacheDir()} used to store all
         * the data generated from the use of this library.
         */
        private static final String CACHE_FILE_PREFIX = "dualcache";

        // Basic conf
        private String mCacheId;
        private int mAppVersion;
        private boolean mLogEnabled;

        // Ram conf
        private int mMaxRamSizeBytes;
        private DualCacheRamMode mRamMode;
        private Parser<T> mRamSerializer;
        private SizeOf<T> mSizeOf;

        // Disk conf
        private int mMaxDiskSizeBytes;
        private DualCacheDiskMode mDiskMode;
        private Parser<T> mDiskSerializer;
        private File mDiskFolder;

        // Persistence conf
        private DualCacheVolatileMode mVolatileMode;
        private Long mDefaultPersistenceTime;

        /**
         * Start the building of the cache.
         *
         * @param cacheId    is the mCacheId of the cache (should be unique).
         * @param appVersion is the app version of the app. If data are already stored in disk cache
         *                   with previous app version, it will be clear.
         */
        public Builder(String cacheId, int appVersion) {
            this.mCacheId = cacheId;
            this.mAppVersion = appVersion;
            this.mRamMode = null;
            this.mDiskMode = null;
            this.mDefaultPersistenceTime = null;
            // By default all entries are persistent
            this.mVolatileMode = DualCacheVolatileMode.PERSISTENCE;
            this.mLogEnabled = false;
        }

        /**
         * Enabling log from the cache. By default disable.
         *
         * @return the builder.
         */
        public Builder<T> enableLog() {
            this.mLogEnabled = true;
            return this;
        }

        /**
         * Builder the cache. Exception will be thrown if it can not be created.
         *
         * @return the cache instance.
         */
        public DualCache<T> build() {
            if (mRamMode == null) {
                throw new IllegalStateException("No ram mode set");
            }
            if (mDiskMode == null) {
                throw new IllegalStateException("No disk mode set");
            }

            DualCache<T> cache = new DualCache<>(mAppVersion, new Logger(mLogEnabled), mRamMode,
                    mRamSerializer, mMaxRamSizeBytes, mSizeOf, mDiskMode, mDiskSerializer,
                    mMaxDiskSizeBytes, mDiskFolder, mVolatileMode, mDefaultPersistenceTime
            );

            boolean isRamDisable = cache.getRAMMode().equals(DualCacheRamMode.DISABLE);
            boolean isDiskDisable = cache.getDiskMode().equals(DualCacheDiskMode.DISABLE);

            if (isRamDisable && isDiskDisable) {
                throw new IllegalStateException(
                        "The ram cache layer and the disk cache layer are "
                                + "disable. You have to use at least one of those "
                                + "layers.");
            }

            return cache;
        }

        /**
         * Use Json serialization/deserialization to store and retrieve object from ram cache.
         *
         * @param maxRamSizeBytes is the max amount of ram in bytes which can be used by the cache.
         * @param serializer      is the cache interface which provide serialization/deserialization
         *                        methods
         *                        for the ram cache layer.
         * @return the builder.
         */
        public Builder<T> useSerializerInRam(
                int maxRamSizeBytes, Parser<T> serializer
        ) {
            this.mRamMode = DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER;
            this.mMaxRamSizeBytes = maxRamSizeBytes;
            this.mRamSerializer = serializer;
            return this;
        }

        /**
         * Store directly objects in ram (without serialization/deserialization).
         * You have to provide a way to compute the size of an object in
         * ram to be able to used the LRU capacity of the ram cache.
         *
         * @param maxRamSizeBytes is the max amount of ram which can be used by the ram cache.
         * @param handlerSizeOf   computes the size of object stored in ram.
         * @return the builder.
         */
        public Builder<T> useReferenceInRam(
                int maxRamSizeBytes, SizeOf<T> handlerSizeOf
        ) {
            this.mRamMode = DualCacheRamMode.ENABLE_WITH_REFERENCE;
            this.mMaxRamSizeBytes = maxRamSizeBytes;
            this.mSizeOf = handlerSizeOf;
            return this;
        }

        /**
         * The ram cache will not be used, meaning that only the disk cache will be used.
         *
         * @return the builder for the disk cache layer.
         */
        public Builder<T> noRam() {
            this.mRamMode = DualCacheRamMode.DISABLE;
            return this;
        }

        /**
         * Use custom serialization/deserialization to store and retrieve objects from disk cache.
         *
         * @param maxDiskSizeBytes is the max size of disk in bytes which an be used by the cache
         *                         layer.
         * @param usePrivateFiles  is true if you want to use {@link Context#MODE_PRIVATE} with the
         *                         default disk cache folder.
         * @param serializer       provides serialization/deserialization methods for the disk cache
         *                         layer.
         * @param context          is used to access file system.
         * @return the builder.
         */
        public Builder<T> useSerializerInDisk(
                int maxDiskSizeBytes,
                boolean usePrivateFiles,
                Parser<T> serializer,
                Context context
        ) {
            File folder = getDefaultDiskCacheFolder(usePrivateFiles, context);
            return useSerializerInDisk(maxDiskSizeBytes, folder, serializer);
        }

        /**
         * Use custom serialization/deserialization to store and retrieve object from disk cache.
         *
         * @param maxDiskSizeBytes is the max size of disk in bytes which an be used by the cache
         *                         layer.
         * @param diskCacheFolder  is the folder where the disk cache will be stored.
         * @param serializer       provides serialization/deserialization methods for the disk cache
         *                         layer.
         * @return the builder.
         */
        public Builder<T> useSerializerInDisk(
                int maxDiskSizeBytes, File diskCacheFolder, Parser<T> serializer
        ) {
            this.mDiskFolder = diskCacheFolder;
            this.mDiskMode = DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER;
            this.mMaxDiskSizeBytes = maxDiskSizeBytes;
            this.mDiskSerializer = serializer;
            return this;
        }

        /**
         * Use this if you do not want use the disk cache layer, meaning that only the
         * ram cache layer will be used.
         *
         * @return the builder.
         */
        public Builder<T> noDisk() {
            this.mDiskMode = DualCacheDiskMode.DISABLE;
            return this;
        }

        /**
         * Set a persistence time for all cache entries
         *
         * @param seconds time a cache entry can persist in seconds
         * @return the builder
         */
        public Builder<T> useVolatileCache(long seconds) {
            this.mDefaultPersistenceTime = seconds * 1000;
            this.mVolatileMode = DualCacheVolatileMode.VOLATILE;
            return this;
        }

        private File getDefaultDiskCacheFolder(boolean usePrivateFiles, Context context) {
            File folder;
            if (usePrivateFiles) {
                folder = context.getDir(CACHE_FILE_PREFIX + this.mCacheId, Context.MODE_PRIVATE);
            } else {
                folder = new File(context.getCacheDir().getPath()
                        + "/" + CACHE_FILE_PREFIX
                        + "/" + this.mCacheId
                );
            }
            return folder;
        }

    }
}
