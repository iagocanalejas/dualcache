package com.iagocanalejas.dualcache.caches;

import android.util.Log;

import com.iagocanalejas.dualcache.interfaces.Cache;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;

/**
 * Created by Iago on 07/01/2017.
 */
public class DiskCache implements Cache<String, String> {
    private static final String TAG = DiskCache.class.getSimpleName();

    private final DiskLruCache mDiskLruCache;
    private final DiskLock mDiskLock = new DiskLock();

    public DiskCache(DiskLruCache diskLruCache) {
        this.mDiskLruCache = diskLruCache;
    }

    @Override
    public boolean contains(String key) {
        return get(key) != null;
    }

    @Override
    public String get(String key) {
        try {
            mDiskLock.lockDiskEntryWrite(key);
            DiskLruCache.Snapshot snapshotObject = mDiskLruCache.get(key);
            if (snapshotObject != null) {
                return snapshotObject.getString(0);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mDiskLock.unLockDiskEntryWrite(key);
        }
        return null;
    }

    @Override
    public String put(String key, String value) {
        try {
            mDiskLock.lockDiskEntryWrite(key);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            editor.set(0, value);
            editor.commit();
            return value;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mDiskLock.unLockDiskEntryWrite(key);
        }
        return null;
    }

    @Override
    public int size() {
        return (int) mDiskLruCache.size();
    }

    @Override
    public String remove(String key) {
        try {
            mDiskLock.lockDiskEntryWrite(key);
            DiskLruCache.Snapshot snapshotObject = mDiskLruCache.get(key);
            if (snapshotObject != null) {
                mDiskLruCache.remove(key);
                return snapshotObject.getString(0);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mDiskLock.unLockDiskEntryWrite(key);
        }
        return null;
    }

    @Override
    public void clear() {
        try {
            mDiskLock.lockFullDiskWrite();
            mDiskLruCache.delete();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mDiskLock.unLockFullDiskWrite();
        }
    }
}
