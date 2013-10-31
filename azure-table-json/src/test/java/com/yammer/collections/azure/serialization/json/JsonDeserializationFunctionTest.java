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
