package com.iagocanalejas.gsonserializer;

import com.google.gson.Gson;
import com.iagocanalejas.core.interfaces.CacheSerializer;

/**
 * Created by Canalejas on 29/12/2016.
 */

public class GsonSerializer<T> implements CacheSerializer<T> {

    private static Gson sGson;
    private final Class<T> mClazz;

    static {
        sGson = new Gson();
    }

    public GsonSerializer(Class<T> clazz) {
        this.mClazz = clazz;
    }

    @Override
    public T fromString(String data) {
        return sGson.fromJson(data, mClazz);
    }

    @Override
    public String toString(T object) {
        return sGson.toJson(object, mClazz);
    }

}