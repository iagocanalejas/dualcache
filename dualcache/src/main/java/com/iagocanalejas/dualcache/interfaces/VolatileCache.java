package com.iagocanalejas.dualcache.interfaces;


import android.support.annotation.NonNull;

/**
 * Created by Iago on 07/01/2017.
 */
public interface VolatileCache<K, V> extends Cache<K, V> {

    /**
     * Put an object in cache with a lifetime if we are in a
     * {@link com.iagocanalejas.dualcache.modes.DualCacheVolatileMode#VOLATILE} cache
     *
     * @param key       the key of the object.
     * @param object    the object to put in cache.
     * @param entryLife persistence time for given entry
     */
    V put(@NonNull K key, V object, long entryLife);

}
