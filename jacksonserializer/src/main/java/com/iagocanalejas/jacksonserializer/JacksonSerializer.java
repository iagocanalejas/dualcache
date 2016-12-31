package com.iagocanalejas.jacksonserializer;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iagocanalejas.core.interfaces.CacheSerializer;

import java.io.IOException;

/**
 * Created by Canalejas on 30/12/2016.
 */

public class JacksonSerializer<T> implements CacheSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    public JacksonSerializer(Class<T> clazz) {
        this.clazz = clazz;
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public T fromString(String data) {
        try {
            return mapper.readValue(data, clazz);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
        throw new IllegalStateException();
    }
}
