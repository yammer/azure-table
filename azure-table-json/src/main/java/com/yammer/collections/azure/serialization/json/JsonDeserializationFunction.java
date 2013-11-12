package com.yammer.collections.azure.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Throwables;

import java.io.IOException;

public class JsonDeserializationFunction<T> implements Function<String, T> {
    private final ObjectMapper objectMapper;
    private final Class<T> deserializedClass;

    public JsonDeserializationFunction(Class<T> deserializedClass) {
        this.deserializedClass = deserializedClass;
        objectMapper = new ObjectMapper();
    }

    @Override
    public T apply(String input) {
        try {
            return objectMapper.readValue(input, deserializedClass);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
