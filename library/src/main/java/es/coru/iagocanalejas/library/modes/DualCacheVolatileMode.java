package es.coru.iagocanalejas.library.modes;

/**
 * Created by Iago on 27/12/2016.
 */

public enum DualCacheVolatileMode {
    /**
     * Means no volatile configuration were set on the cache so all entries will be persistent
     */
    PERSISTENCE,

    /**
     * Same persistence time for all cache entries
     */
    VOLATILE_CACHE,

    /**
     * Persistence time can be set for each entry
     */
    VOLATILE_ENTRY
}
