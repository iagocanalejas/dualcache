package com.iagocanalejas.dualcache.interfaces;

/**
 * Created by Iagocanalejas on 06/01/2017.
 * Interface containing the methods required
 * to handle Caching for V objects with K keys.
 */
public interface Cache<K, V> {

    /**
     * Find a key in the cache
     *
     * @param key to find
     * @return true if key is cached false otherwise
     */
    boolean contains(K key);

    /**
     * Try to find the given key in the cache.
     *
     * @param key to find.
     * @return found value or null.
     */
    V get(K key);

    /**
     * Put a new value in the cache.
     *
     * @param key   for the value.
     * @param value to add.
     * @return previous existing object with given key.
     */
    V put(K key, V value);

    /**
     * @return if cache do not override {@link SizeOf#sizeOf}, this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache.
     */
    int size();

    /**
     * Delete given key from the cache.
     *
     * @param key to remove.
     * @return deleted value or null.
     */
    V remove(K key);

    /**
     * Remove all data in cache.
     */
    void clear();

}
