package com.yammer.collections.azure.serialization.json;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class JsonSerializationFunction<T> implements Function<T, String> {
    private final ObjectMapper objectMapper;

    public JsonSerializationFunction() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String apply(T input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
