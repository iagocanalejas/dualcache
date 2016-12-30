package com.iagocanalejas.logansquareserializer;

import com.bluelinelabs.logansquare.LoganSquare;
import com.iagocanalejas.core.interfaces.CacheSerializer;

import java.io.IOException;

/**
 * Created by Canalejas on 30/12/2016.
 */

public class LoganSquareSerializer<T> implements CacheSerializer<T> {

    private final Class<T> mClazz;

    public LoganSquareSerializer(Class<T> clazz) {
        this.mClazz = clazz;
    }

    @Override
    public T fromString(String data) {
        try {
            return LoganSquare.parse(data, mClazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString(T object) {
        try {
            return LoganSquare.serialize(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

}
