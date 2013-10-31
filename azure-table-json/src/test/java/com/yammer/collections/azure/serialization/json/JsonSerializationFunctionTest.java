package com.yammer.collections.azure.serialization.json;


import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JsonSerializationFunctionTest {
    private static final Long PRIMITIVE_VALUE = 123l;
    private static final TestValuePojo POJO_VALUE = new TestValuePojo("Michal", Arrays.asList(29, 1, 1980));
    private static final String SERIALIZED_PRIMITIVE_VALUE = PRIMITIVE_VALUE.toString();
    private static final String SERIALIZED_POJO_VALUE = "{\"name\":\"Michal\",\"numbers\":[29,1,1980]}";


    @Test
    public void serializez_primitives() {
        JsonSerializationFunction<Long> primitiveSerializingFunction = new JsonSerializationFunction<>();
        assertThat(primitiveSerializingFunction.apply(PRIMITIVE_VALUE), is(equalTo(SERIALIZED_PRIMITIVE_VALUE)));
    }


    @Test
    public void serializez_pojos() {
        JsonSerializationFunction<TestValuePojo> pojoSerializingFunction = new JsonSerializationFunction<>();
        assertThat(pojoSerializingFunction.apply(POJO_VALUE), is(equalTo(SERIALIZED_POJO_VALUE)));
    }

}
