package com.iagocanalejas.dualcache;

import android.content.Context;

import com.iagocanalejas.dualcache.interfaces.Hasher;
import com.iagocanalejas.dualcache.interfaces.Parser;
import com.iagocanalejas.dualcache.interfaces.SizeOf;
import com.iagocanalejas.dualcache.modes.DualCacheDiskMode;
import com.iagocanalejas.dualcache.modes.DualCacheKeyMode;
import com.iagocanalejas.dualcache.modes.DualCacheRamMode;
import com.iagocanalejas.dualcache.modes.DualCacheVolatileMode;

import java.io.File;

/**
 * Class used to build a cache.
 *
 * @param <V> is the class of object to store in cache.
 */
public class Builder<K, V> {

    /**
     * Defined the sub folder from {@link Context#getCacheDir()} used to store all
     * the data generated from the use of this library.
     */
    private static final String CACHE_FILE_PREFIX = "dualcache";

    // Basic conf
    private String mCacheId;
    private int mAppVersion;
    private boolean mLogEnabled;

    // Key conf
    private DualCacheKeyMode mKeyMode;
    private Hasher<K> mHasher;


    // Ram conf
    private int mMaxRamSizeBytes;
    private DualCacheRamMode mRamMode;
    private Parser<V> mRamSerializer;
    private SizeOf<V> mSizeOf;

    // Disk conf
    private int mMaxDiskSizeBytes;
    private DualCacheDiskMode mDiskMode;
    private Parser<V> mDiskSerializer;
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
        this.mKeyMode = DualCacheKeyMode.KEY;

        this.mDefaultPersistenceTime = null;
        this.mVolatileMode = DualCacheVolatileMode.PERSISTENCE;

        this.mLogEnabled = false;
    }

    /**
     * Builder the cache. Exception will be thrown if it can not be created.
     *
     * @return the cache instance.
     */
    public DualCache<K, V> build() {
        if (mRamMode == null) {
            throw new IllegalStateException("No ram mode set");
        }
        if (mDiskMode == null) {
            throw new IllegalStateException("No disk mode set");
        }

        DualCache<K, V> cache = new DualCache<>(mAppVersion, new Logger(mLogEnabled), mKeyMode,
                mHasher, mRamMode, mRamSerializer, mMaxRamSizeBytes, mSizeOf, mDiskMode,
                mDiskSerializer, mMaxDiskSizeBytes, mDiskFolder, mVolatileMode,
                mDefaultPersistenceTime);

        boolean isRamDisable = cache.getRamMode().equals(DualCacheRamMode.DISABLE);
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
     * Should be called if your cache key is not a string.
     *
     * @param hasher {@link Hasher}.
     * @return the builder.
     */
    public Builder<K, V> useKeyHasher(Hasher<K> hasher) {
        this.mKeyMode = DualCacheKeyMode.HASHED_KEY;
        this.mHasher = hasher;
        return this;
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
    public Builder<K, V> useSerializerInRam(int maxRamSizeBytes, Parser<V> serializer) {
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
    public Builder<K, V> useReferenceInRam(int maxRamSizeBytes, SizeOf<V> handlerSizeOf) {
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
    public Builder<K, V> noRam() {
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
    public Builder<K, V> useSerializerInDisk(int maxDiskSizeBytes, boolean usePrivateFiles,
                                             Parser<V> serializer, Context context) {

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
    public Builder<K, V> useSerializerInDisk(int maxDiskSizeBytes, File diskCacheFolder,
                                             Parser<V> serializer) {

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
    public Builder<K, V> noDisk() {
        this.mDiskMode = DualCacheDiskMode.DISABLE;
        return this;
    }

    /**
     * Set a persistence time for all cache entries
     *
     * @param seconds time a cache entry can persist in seconds
     * @return the builder
     */
    public Builder<K, V> useVolatileCache(long seconds) {
        this.mDefaultPersistenceTime = seconds * 1000;
        this.mVolatileMode = DualCacheVolatileMode.VOLATILE;
        return this;
    }

    /**
     * Enabling log from the cache. By default disable.
     *
     * @return the builder.
     */
    public Builder<K, V> enableLog() {
        this.mLogEnabled = true;
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
