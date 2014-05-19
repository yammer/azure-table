/**
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS
 * OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
 *
 * See the Apache Version 2.0 License for specific language governing permissions and limitations under
 * the License.
 */
package com.yammer.collections.azure.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.yammer.collections.azure.Bytes;

import java.io.IOException;

public class JsonDeserializationFunction<T> implements Function<Bytes, T> {
    private final ObjectMapper objectMapper;
    private final Class<T> deserializedClass;

    public JsonDeserializationFunction(Class<T> deserializedClass) {
        this.deserializedClass = deserializedClass;
        objectMapper = new ObjectMapper();
    }

    @Override
    public T apply(Bytes input) {
        try {
            return objectMapper.readValue(input.getBytes(), deserializedClass);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
