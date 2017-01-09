package com.iagocanalejas.dualcache.caches;

import android.util.Log;

import com.iagocanalejas.dualcache.interfaces.Cache;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

/**
 * Created by Iago on 07/01/2017.
 */
public class DiskCache implements Cache<String, String> {
    private static final String TAG = DiskCache.class.getSimpleName();

    private static final int VALUES_PER_CACHE_ENTRY = 1;

    private final File mCacheDirectory;
    private final int mAppVersion;
    private final long mMaxCacheSize;

    private DiskLruCache mDiskLruCache;
    private final CacheLock mCacheLock = new CacheLock();

    public DiskCache(File directory, int appVersion, long maxSize) throws IOException {
        this.mCacheDirectory = directory;
        this.mAppVersion = appVersion;
        this.mMaxCacheSize = maxSize;

        this.mDiskLruCache = DiskLruCache.open(mCacheDirectory, this.mAppVersion,
                VALUES_PER_CACHE_ENTRY, this.mMaxCacheSize);

    }

    @Override
    public boolean contains(String key) {
        return get(key) != null;
    }

    @Override
    public String get(String key) {
        try {
            mCacheLock.lockDiskEntryWrite(key);
            DiskLruCache.Snapshot snapshotObject = mDiskLruCache.get(key);
            if (snapshotObject != null) {
                return snapshotObject.getString(0);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mCacheLock.unLockDiskEntryWrite(key);
        }
        return null;
    }

    @Override
    public String put(String key, String value) {
        String previous = null;
        try {
            mCacheLock.lockDiskEntryWrite(key);
            // Find previous entry
            DiskLruCache.Snapshot snapshotObject = mDiskLruCache.get(key);
            if (snapshotObject != null) {
                previous = snapshotObject.getString(0);
            }

            // Modify entry
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            editor.set(0, value);
            editor.commit();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mCacheLock.unLockDiskEntryWrite(key);
        }
        return previous;
    }

    @Override
    public int size() {
        return (int) mDiskLruCache.size();
    }

    @Override
    public String remove(String key) {
        try {
            mCacheLock.lockDiskEntryWrite(key);
            DiskLruCache.Snapshot snapshotObject = mDiskLruCache.get(key);
            if (snapshotObject != null) {
                mDiskLruCache.remove(key);
                return snapshotObject.getString(0);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mCacheLock.unLockDiskEntryWrite(key);
        }
        return null;
    }

    @Override
    public void clear() {
        try {
            mCacheLock.lockFullDiskWrite();
            mDiskLruCache.delete();
            mDiskLruCache = DiskLruCache.open(mCacheDirectory, this.mAppVersion,
                    VALUES_PER_CACHE_ENTRY, this.mMaxCacheSize);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mCacheLock.unLockFullDiskWrite();
        }
    }

}
