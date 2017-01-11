package com.iagocanalejas.dualcache.interfaces;

/**
 * Created by Iagocanalejas on 11/01/2017.
 * Handles key hashing for using in cache
 */
public interface Hasher<K> {

    /**
     * Hash a key for the cache
     *
     * @param key to hash
     * @return hashed key
     */
    String hash(K key);

}
