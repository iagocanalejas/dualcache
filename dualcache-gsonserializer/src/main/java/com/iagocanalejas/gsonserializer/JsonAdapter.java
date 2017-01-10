package com.iagocanalejas.gsonserializer;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by Canalejas on 30/12/2016.
 */

public class JsonAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final Class<T> mClass; //Used as workaround for T

    public JsonAdapter(Class<T> tClass) {
        this.mClass = tClass;
    }

    @Override
    public final JsonElement serialize(
            final T object,
            final Type interfaceType,
            final JsonSerializationContext context) {
        final JsonObject member = new JsonObject();

        member.addProperty("type", object.getClass().getName());

        member.add("data", context.serialize(object));

        return member;
    }

    @Override
    public final T deserialize(
            final JsonElement elem,
            final Type interfaceType,
            final JsonDeserializationContext context) {

        final JsonObject member = (JsonObject) elem;
        final JsonElement typeString = get(member, "type");
        final JsonElement data = get(member, "data");
        final Type actualType = typeForName(typeString);

        return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
        try {
            return Class.forName(typeElem.getAsString());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    private JsonElement get(final JsonObject wrapper, final String memberName) {
        final JsonElement elem = wrapper.get(memberName);

        if (elem == null) {
            throw new JsonParseException(
                    "no '" + memberName + "' member found in json file.");
        }
        return elem;
    }
}
