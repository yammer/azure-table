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

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JsonDeserializationFunctionTest {
    private static final Long PRIMITIVE_VALUE = 123l;
    private static final TestValuePojo POJO_VALUE = new TestValuePojo("Michal", Arrays.asList(29, 1, 1980));
    private static final String SERIALIZED_PRIMITIVE_VALUE = PRIMITIVE_VALUE.toString();
    private static final String SERIALIZED_POJO_VALUE = "{\"name\":\"Michal\",\"numbers\":[29,1,1980]}";

    @Test
    public void serializez_primitives() {
        JsonDeserializationFunction<Long> primitiveDeserializingFunction = new JsonDeserializationFunction<>(Long.class);
        assertThat(primitiveDeserializingFunction.apply(SERIALIZED_PRIMITIVE_VALUE), is(equalTo(PRIMITIVE_VALUE)));
    }

    @Test
    public void serializez_pojos() {
        JsonDeserializationFunction<TestValuePojo> pojoDeserializingFunction = new JsonDeserializationFunction<>(TestValuePojo.class);
        assertThat(pojoDeserializingFunction.apply(SERIALIZED_POJO_VALUE), is(equalTo(POJO_VALUE)));
    }
}
