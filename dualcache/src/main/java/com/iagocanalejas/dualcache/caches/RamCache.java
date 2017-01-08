package com.iagocanalejas.dualcache.caches;


import com.iagocanalejas.dualcache.caches.base.LruCache;
import com.iagocanalejas.dualcache.interfaces.SizeOf;

/**
 * This is the LRU cache used for the RAM layer when configured to used references.
 *
 * @param <T> is the class of object stored in the cache.
 */
public class RamCache<T> extends LruCache<String, T> {

    private SizeOf<T> mHandlerSizeOf;

    /**
     * @param handler computes the size of each object stored in the RAM cache layer.
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public RamCache(SizeOf<T> handler, int maxSize) {
        super(maxSize);
        mHandlerSizeOf = handler;
    }

    @Override
    protected int sizeOf(String key, T value) {
        return mHandlerSizeOf.sizeOf(value);
    }
}
