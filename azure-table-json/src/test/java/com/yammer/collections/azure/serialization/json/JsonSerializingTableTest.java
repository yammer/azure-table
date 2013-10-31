package com.yammer.collections.azure.serialization.json;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class JsonSerializingTableTest {
    @SuppressWarnings("ConstantNamingConvention")
    private static final Float ROW = 11.34f;
    private static final Long COLUMN = 123l;
    private static final TestValuePojo VALUE = new TestValuePojo("Michal", Arrays.asList(29, 1, 1980));
    private static final String SERIALIED_ROW = ROW.toString();
    private static final String SERIALIZED_COLUMN = COLUMN.toString();
    private static final String SERIALIZED_VALUE = "{\"name\":\"Michal\",\"numbers\":[29,1,1980]}";
    private Table<String, String, String> backingTable;
    private JsonSerializingTable<Float, Long, TestValuePojo> jsonSerializingTable;

    @Before
    public void setUp() {
        backingTable = HashBasedTable.create();
        jsonSerializingTable = new JsonSerializingTable<>(
                backingTable, Float.class, Long.class, TestValuePojo.class);
    }

    @Test
    public void put_correctly_serializes() {
        jsonSerializingTable.put(ROW, COLUMN, VALUE);

        assertThat(backingTable.get(SERIALIED_ROW, SERIALIZED_COLUMN), is(equalTo(SERIALIZED_VALUE)));
    }

    @Test
    public void get_correctly_deserializes() {
        backingTable.put(SERIALIED_ROW, SERIALIZED_COLUMN, SERIALIZED_VALUE);

        assertThat(jsonSerializingTable.get(ROW, COLUMN), is(equalTo(VALUE)));
    }

}
