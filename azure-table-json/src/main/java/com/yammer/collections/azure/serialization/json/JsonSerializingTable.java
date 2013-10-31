package com.yammer.collections.azure.serialization.json;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.yammer.collections.transforming.TransformingTable;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class JsonSerializingTable {
    private JsonSerializingTable() {};

    public static <R, C, V> Table<R, C, V> create(Table<String, String, String> backingTable,
                                                  Class<R> rowClass, Class<C> columnClass, Class<V> valueClass) {
        Function<R,String> toRowFunction = new JsonSerializationFunction<>();
        Function<String,R> fromRowFunction = new JsonDeserializationFunction<>(rowClass);
        Function<C,String> toColumnFunction = new JsonSerializationFunction<>();
        Function<String,C> fromColumnFunction = new JsonDeserializationFunction<>(columnClass);
        Function<V,String> toValueFunction = new JsonSerializationFunction<>();
        Function<String,V> fromValueFunction = new JsonDeserializationFunction<>(valueClass);

        return new TransformingTable<>(backingTable,
                toRowFunction, fromRowFunction,
                toColumnFunction, fromColumnFunction,
                toValueFunction, fromValueFunction
                );
    }



}
