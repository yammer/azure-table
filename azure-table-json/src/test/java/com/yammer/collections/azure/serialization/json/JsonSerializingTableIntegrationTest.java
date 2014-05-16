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

import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "ConstantNamingConvention"})
@RunWith(MockitoJUnitRunner.class)
public class JsonSerializingTableIntegrationTest {
    private static final Float ROW = 11.34f;
    private static final Long COLUMN = 123l;
    private static final TestValuePojo VALUE = new TestValuePojo("Ala", Arrays.asList(15, 1, 1690));
    private static final byte[] SERIALIED_ROW = ROW.toString().getBytes();
    private static final byte[] SERIALIZED_COLUMN = COLUMN.toString().getBytes();
    private static final byte[] SERIALIZED_VALUE = "{\"name\":\"Ala\",\"numbers\":[15,1,1690]}".getBytes();
    @Mock
    private Table<byte[], byte[], byte[]> backingTableMock;
    private Table<Float, Long, TestValuePojo> jsonSerializingTable;

    @Before
    public void setUp() {
        jsonSerializingTable = JsonSerializingTable.create(
                backingTableMock, Float.class, Long.class, TestValuePojo.class);
    }

    @Test
    public void put_correctly_serializes() {
        jsonSerializingTable.put(ROW, COLUMN, VALUE);

        verify(backingTableMock).put(SERIALIED_ROW, SERIALIZED_COLUMN, SERIALIZED_VALUE);
    }

    @Test
    public void get_correctly_deserializes() {
        when(backingTableMock.get(SERIALIED_ROW, SERIALIZED_COLUMN)).thenReturn(SERIALIZED_VALUE);

        assertThat(jsonSerializingTable.get(ROW, COLUMN), is(equalTo(VALUE)));
    }


}
