package com.yammer.collections.azure.serialization.json;

import com.google.common.base.Function;
import com.google.common.collect.Table;
import com.yammer.collections.transforming.TransformingTable;

public final class JsonSerializingTable {
    private JsonSerializingTable() {
    }

    public static <R, C, V> Table<R, C, V> create(Table<String, String, String> backingTable,
                                                  Class<R> rowClass, Class<C> columnClass, Class<V> valueClass) {
        Function<R, String> toRowFunction = new JsonSerializationFunction<>();
        Function<String, R> fromRowFunction = new JsonDeserializationFunction<>(rowClass);
        Function<C, String> toColumnFunction = new JsonSerializationFunction<>();
        Function<String, C> fromColumnFunction = new JsonDeserializationFunction<>(columnClass);
        Function<V, String> toValueFunction = new JsonSerializationFunction<>();
        Function<String, V> fromValueFunction = new JsonDeserializationFunction<>(valueClass);

        return TransformingTable.create(backingTable,
                toRowFunction, fromRowFunction,
                toColumnFunction, fromColumnFunction,
                toValueFunction, fromValueFunction
        );
    }


}
