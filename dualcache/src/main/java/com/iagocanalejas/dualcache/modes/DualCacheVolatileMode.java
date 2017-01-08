package com.iagocanalejas.dualcache.modes;

/**
 * Created by Iago on 27/12/2016.
 */

public enum DualCacheVolatileMode {
    /**
     * Means no volatile configuration were set on the cache so all entries will be persistent
     */
    PERSISTENCE,

    /**
     * Cache for a persistence time for entries
     */
    VOLATILE
}
