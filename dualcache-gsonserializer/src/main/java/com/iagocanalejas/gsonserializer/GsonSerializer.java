package com.iagocanalejas.gsonserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iagocanalejas.dualcache.interfaces.Serializer;

/**
 * Created by Canalejas on 29/12/2016.
 */

public class GsonSerializer<T> implements Serializer<T> {

    private final Gson mGson;
    private final Class<T> mClazz;

    public GsonSerializer(Class<T> clazz) {
        this.mClazz = clazz;

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(clazz, new JsonAdapter<>(clazz))
                .setPrettyPrinting();

        this.mGson = gsonBuilder.create();
    }

    @Override
    public T fromString(String data) {
        return mGson.fromJson(data, mClazz);
    }

    @Override
    public String toString(T object) {
        return mGson.toJson(object, mClazz);
    }

}