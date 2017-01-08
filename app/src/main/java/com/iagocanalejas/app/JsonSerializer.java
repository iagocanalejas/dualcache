package com.iagocanalejas.app;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iagocanalejas.dualcache.interfaces.Parser;

import java.io.IOException;

/**
 * Created by Canalejas on 27/12/2016.
 */

public class JsonSerializer<T> implements Parser<T> {
    private final ObjectMapper mMapper;
    private final Class<T> mClass;

    /**
     * Default constructor.
     *
     * @param clazz is the class of object to serialize/deserialize.
     */
    public JsonSerializer(Class<T> clazz) {
        this.mClass = clazz;
        mMapper = new ObjectMapper();
        mMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public T fromString(String data) {
        try {
            return mMapper.readValue(data, mClass);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString(T object) {
        try {
            return mMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
        throw new IllegalStateException();
    }
}