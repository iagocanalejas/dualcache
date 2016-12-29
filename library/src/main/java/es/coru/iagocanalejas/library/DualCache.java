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

package es.coru.iagocanalejas.library;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import es.coru.iagocanalejas.library.interfaces.CacheSerializer;
import es.coru.iagocanalejas.library.interfaces.SizeOf;
import es.coru.iagocanalejas.library.modes.DualCacheDiskMode;
import es.coru.iagocanalejas.library.modes.DualCacheRamMode;
import es.coru.iagocanalejas.library.modes.DualCacheVolatileMode;

/**
 * This class intent to provide a very easy to use, reliable, highly configurable caching library
 * for Android.
 *
 * @param <T> is the Class of object to cache.
 */
public class DualCache<T> {

    private static final int VALUES_PER_CACHE_ENTRY = 1;

    // Basic conf
    private final int mAppVersion;
    private final Logger mLogger;
    private final LoggerHelper mLoggerHelper;
    private final DualCacheLock mDualCacheLock = new DualCacheLock();

    // Disk conf
    private final int mMaxDiskSizeBytes;
    private final File mDiskCacheFolder;
    private final DualCacheDiskMode mDiskMode;
    private DiskLruCache mDiskLruCache;

    // Ram conf
    private final RamLruCache mRamLruCache;
    private final DualCacheRamMode mRamMode;

    // Serializers
    private final VolatileCacheSerializer<T> mVolatileRamSerializer;
    private final VolatileCacheSerializer<T> mVolatileDiskSerializer;
    private final CacheSerializer<T> mDiskSerializer;
    private final CacheSerializer<T> mRamSerializer;

    // Persistence conf
    private final DualCacheVolatileMode mVolatileMode;
    private final Long mDefaultPersistenceTime;


    DualCache(int appVersion, Logger logger, DualCacheRamMode ramMode, CacheSerializer<T> ramSerializer,
              int maxRamSizeBytes, SizeOf<T> sizeOf, DualCacheDiskMode diskMode, CacheSerializer<T> diskSerializer,
              int maxDiskSizeBytes, File diskFolder, DualCacheVolatileMode volatileMode, Long defaultPersistenceTIme) {

        this.mAppVersion = appVersion;
        this.mRamMode = ramMode;
        this.mRamSerializer = ramSerializer;
        this.mDiskMode = diskMode;
        this.mDiskSerializer = diskSerializer;
        this.mDiskCacheFolder = diskFolder;
        this.mLogger = logger;
        this.mLoggerHelper = new LoggerHelper(logger);
        this.mDefaultPersistenceTime = defaultPersistenceTIme;
        this.mVolatileMode = volatileMode;

        switch (ramMode) {
            case ENABLE_WITH_SPECIFIC_SERIALIZER:
                this.mRamLruCache = new StringLruCache(maxRamSizeBytes);
                break;
            case ENABLE_WITH_REFERENCE:
                if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)) {
                    this.mRamLruCache = new ReferenceLruCache<>(maxRamSizeBytes, new VolatileSizeOf<>(sizeOf));
                } else {
                    this.mRamLruCache = new ReferenceLruCache<>(maxRamSizeBytes, sizeOf);
                }
                break;
            default:
                this.mRamLruCache = null;
        }

        switch (diskMode) {
            case ENABLE_WITH_SPECIFIC_SERIALIZER:
                this.mMaxDiskSizeBytes = maxDiskSizeBytes;
                try {
                    openDiskLruCache(diskFolder);
                } catch (IOException e) {
                    logger.logError(e);
                }
                break;
            default:
                this.mMaxDiskSizeBytes = 0;
        }


        //region Init Volatile Serializers
        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && ramMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileRamSerializer = new VolatileCacheSerializer<T>(ramSerializer);
        } else {
            mVolatileRamSerializer = null;
        }

        if (!volatileMode.equals(DualCacheVolatileMode.PERSISTENCE)
                && diskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            mVolatileDiskSerializer = new VolatileCacheSerializer<T>(diskSerializer);
        } else {
            mVolatileDiskSerializer = null;
        }
        //endregion

    }

    private void openDiskLruCache(File diskFolder) throws IOException {
        this.mDiskLruCache = DiskLruCache.open(
                diskFolder,
                this.mAppVersion,
                VALUES_PER_CACHE_ENTRY,
                this.mMaxDiskSizeBytes
        );
    }

    public long getRamUsedInBytes() {
        if (mRamLruCache == null) {
            return -1;
        } else {
            return mRamLruCache.size();
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

    //region PUT

    /**
     * Put a volatile entry on the cache
     *
     * @param key       the key of the object.
     * @param object    the object to put in cache.
     * @param entryLife persistence time for given entry, <= 0 means persistence entry
     */
    private void putVolatileEntry(String key, T object, long entryLife) {
        long lifetime = (entryLife > 0)
                ? Calendar.getInstance().getTimeInMillis() + entryLife
                : 0; // Same as a persistence entry

        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            mRamLruCache.put(key, new VolatileCacheEntry<>(lifetime, object));
        }

        String ramSerialized = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = mVolatileRamSerializer.toString(new VolatileCacheEntry<>(lifetime, object));
            mRamLruCache.put(key, ramSerialized);
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            try {
                mDualCacheLock.lockDiskEntryWrite(key);
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (mRamSerializer == mDiskSerializer) {
                    // Optimization if using same serializer
                    editor.set(0, ramSerialized);
                } else {
                    editor.set(0, mVolatileDiskSerializer.toString(new VolatileCacheEntry<>(lifetime, object)));
                }
                editor.commit();
            } catch (IOException e) {
                mLogger.logError(e);
            } finally {
                mDualCacheLock.unLockDiskEntryWrite(key);
            }
        }
    }

    /**
     * Put a persistent entry on the cache
     *
     * @param key    the key of the object.
     * @param object the object to put in cache.
     */
    private void putEntry(String key, T object) {
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
            mRamLruCache.put(key, object);
        }

        String ramSerialized = null;
        if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            ramSerialized = mRamSerializer.toString(object);
            mRamLruCache.put(key, ramSerialized);
        }

        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            try {
                mDualCacheLock.lockDiskEntryWrite(key);
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (mRamSerializer == mDiskSerializer) {
                    // Optimization if using same serializer
                    editor.set(0, ramSerialized);
                } else {
                    editor.set(0, mDiskSerializer.toString(object));
                }
                editor.commit();
            } catch (IOException e) {
                mLogger.logError(e);
            } finally {
                mDualCacheLock.unLockDiskEntryWrite(key);
            }
        }
    }

    /**
     * Put an object in cache with a lifetime if we are in a {@link DualCacheVolatileMode#VOLATILE} cache
     *
     * @param key       the key of the object.
     * @param object    the object to put in cache.
     * @param entryLife persistence time for given entry
     */
    public void put(String key, T object, long entryLife) {
        if (!mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            throw new UnsupportedOperationException("Operation only supported for VOLATILE cache type");
        }
        putVolatileEntry(key, object, entryLife);
    }

    /**
     * Put an object in cache.
     *
     * @param key    is the key of the object.
     * @param object is the object to put in cache.
     */
    public void put(String key, T object) {
        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            putVolatileEntry(key, object, mDefaultPersistenceTime);
        } else {
            putEntry(key, object);
        }
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
        DiskLruCache.Snapshot snapshotObject = null;
        if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
            try {
                mDualCacheLock.lockDiskEntryWrite(key);
                snapshotObject = mDiskLruCache.get(key);
            } catch (IOException e) {
                mLogger.logError(e);
            } finally {
                mDualCacheLock.unLockDiskEntryWrite(key);
            }

            if (snapshotObject != null) {
                mLoggerHelper.logEntryForKeyIsOnDisk(key);
                try {
                    return snapshotObject.getString(0);
                } catch (IOException e) {
                    mLogger.logError(e);
                }
            } else {
                mLoggerHelper.logEntryForKeyIsNotOnDisk(key);
            }
        }
        return null;
    }

    /**
     * Try to find a volatile entry on the cache
     *
     * @param key given entry to find
     * @return entry or null
     */
    private T getVolatileEntry(String key) {
        Object ramResult = null;
        String diskResult = null;

        // Try to get the object from RAM.
        boolean isRamSerialized = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = mRamLruCache.get(key);
        }

        VolatileCacheEntry<T> cacheEntry;
        if (ramResult == null) {
            // Try to get the cached object from disk.
            mLoggerHelper.logEntryForKeyIsNotInRam(key);
            if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                diskResult = getFromDisk(key);
            }

            if (diskResult != null) {
                // Load object, no need to check disk configuration since diskResult != null.
                cacheEntry = mVolatileDiskSerializer.fromString(diskResult);

                //Invalidate cache if required
                if (!cacheEntry.getTimestamp().after(Calendar.getInstance().getTime())) {
                    //Invalidate cache
                    delete(key);
                    return null;
                }

                // Refresh object in ram.
                if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                    if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                        mRamLruCache.put(key, cacheEntry);
                    }
                } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                    if (mDiskSerializer == mRamSerializer) {
                        mRamLruCache.put(key, diskResult);
                    } else {
                        mRamLruCache.put(key, mVolatileRamSerializer.toString(cacheEntry));
                    }
                }
                return cacheEntry.getItem();
            }
        } else { // ramResult != null
            mLoggerHelper.logEntryForKeyIsInRam(key);
            if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                cacheEntry = (VolatileCacheEntry<T>) ramResult;
                //Invalidate cache if needed
                if (!cacheEntry.getTimestamp().after(Calendar.getInstance().getTime())) {
                    delete(key);
                    return null;
                }
                return cacheEntry.getItem();
            } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                cacheEntry = mVolatileRamSerializer.fromString((String) ramResult);
                if (!cacheEntry.getTimestamp().after(Calendar.getInstance().getTime())) {
                    delete(key);
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
    private T getEntry(String key) {
        Object ramResult = null;
        String diskResult = null;

        // Try to get the object from RAM.
        boolean isRamSerialized = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER);
        boolean isRamReferenced = mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE);
        if (isRamSerialized || isRamReferenced) {
            ramResult = mRamLruCache.get(key);
        }

        if (ramResult == null) {
            // Try to get the cached object from disk.
            mLoggerHelper.logEntryForKeyIsNotInRam(key);
            if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                diskResult = getFromDisk(key);
            }

            if (diskResult != null) {
                // Load object, no need to check disk configuration since diskResult != null.
                T objectFromStringDisk = mDiskSerializer.fromString(diskResult);
                // Refresh object in ram.
                if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                    if (mDiskMode.equals(DualCacheDiskMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                        mRamLruCache.put(key, objectFromStringDisk);
                    }
                } else if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_SPECIFIC_SERIALIZER)) {
                    if (mDiskSerializer == mRamSerializer) {
                        mRamLruCache.put(key, diskResult);
                    } else {
                        mRamLruCache.put(key, mRamSerializer.toString(objectFromStringDisk));
                    }
                }
                return objectFromStringDisk;
            }
        } else { // ramResult != null
            mLoggerHelper.logEntryForKeyIsInRam(key);
            if (mRamMode.equals(DualCacheRamMode.ENABLE_WITH_REFERENCE)) {
                return (T) ramResult;
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
    public T get(String key) {
        if (mVolatileMode.equals(DualCacheVolatileMode.VOLATILE)) {
            return getVolatileEntry(key);
        }

        return getEntry(key);
    }
    //endregion

    /**
     * Delete the corresponding object in cache.
     *
     * @param key is the key of the object.
     */
    public void delete(String key) {
        if (!mRamMode.equals(DualCacheRamMode.DISABLE)) {
            mRamLruCache.remove(key);
        }
        if (!mDiskMode.equals(DualCacheDiskMode.DISABLE)) {
            try {
                mDualCacheLock.lockDiskEntryWrite(key);
                mDiskLruCache.remove(key);
            } catch (IOException e) {
                mLogger.logError(e);
            } finally {
                mDualCacheLock.unLockDiskEntryWrite(key);
            }
        }
    }

    /**
     * Remove all objects from cache (both RAM and disk).
     */
    public void invalidate() {
        invalidateDisk();
        invalidateRAM();
    }

    /**
     * Remove all objects from RAM.
     */
    public void invalidateRAM() {
        if (!mRamMode.equals(DualCacheRamMode.DISABLE)) {
            mRamLruCache.evictAll();
        }
    }

    /**
     * Remove all objects from Disk.
     */
    public void invalidateDisk() {
        if (!mDiskMode.equals(DualCacheDiskMode.DISABLE)) {
            try {
                mDualCacheLock.lockFullDiskWrite();
                mDiskLruCache.delete();
                openDiskLruCache(mDiskCacheFolder);
            } catch (IOException e) {
                mLogger.logError(e);
            } finally {
                mDualCacheLock.unLockFullDiskWrite();
            }
        }
    }

    /**
     * Test if an object is present in cache.
     *
     * @param key is the key of the object.
     * @return true if the object is present in cache, false otherwise.
     */
    public boolean contains(String key) {
        if (!mRamMode.equals(DualCacheRamMode.DISABLE) && mRamLruCache.snapshot().containsKey(key)) {
            return true;
        }
        try {
            mDualCacheLock.lockDiskEntryWrite(key);
            if (!mDiskMode.equals(DualCacheDiskMode.DISABLE) && mDiskLruCache.get(key) != null) {
                return true;
            }
        } catch (IOException e) {
            mLogger.logError(e);
        } finally {
            mDualCacheLock.unLockDiskEntryWrite(key);
        }
        return false;
    }
}
