package com.iagocanalejas;


import com.iagocanalejas.interfaces.CacheSerializer;

/**
 * Created by Iago on 26/12/2016.
 */

class VolatileCacheSerializer<T> implements CacheSerializer<VolatileCacheEntry<T>> {

    private static final String TIMESTAMP_KEY = " timestamp_key:";

    private CacheSerializer<T> mSerializer;

    VolatileCacheSerializer(CacheSerializer<T> serializer) {
        this.mSerializer = serializer;
    }

    /**
     * Create a {@link VolatileCacheEntry} from a string
     *
     * @param data is the byte array representing the serialized data.
     * @return {@link VolatileCacheEntry}
     */
    @Override
    public VolatileCacheEntry<T> fromString(String data) {
        String[] items = data.split(TIMESTAMP_KEY);
        return new VolatileCacheEntry<T>(Long.valueOf(items[1]), mSerializer.fromString(items[0]));
    }

    /**
     * Add the timestamp to the serializable object
     *
     * @param object is the object to serialize.
     * @return object serialized + timestamp serialized
     */
    @Override
    public String toString(VolatileCacheEntry<T> object) {
        return mSerializer.toString(object.getItem()) + TIMESTAMP_KEY + object.getTimestamp().getTime();
    }
}
