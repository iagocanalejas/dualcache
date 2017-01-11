package com.iagocanalejas.dualcache.modes;

/**
 * Created by Iagocanalejas on 11/01/2017.
 * Define de behaviour of the key
 */
public enum DualCacheKeyMode {

    /**
     * Cache is using a {@link com.iagocanalejas.dualcache.interfaces.Hasher}
     * for handle keys
     */
    HASHED_KEY,

    /**
     * Not require to hash the cache key
     */
    KEY

}
