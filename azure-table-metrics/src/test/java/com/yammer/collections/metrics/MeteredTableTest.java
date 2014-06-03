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
package com.yammer.collections.metrics;


import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@SuppressWarnings({"ClassWithTooManyMethods", "InstanceVariableMayNotBeInitialized"})
@RunWith(MockitoJUnitRunner.class)
public class MeteredTableTest {
    private static final Float ROW_KEY_1 = 0.5f;
    private static final Long COLUMN_KEY_1 = 23l;
    private static final Integer VALUE_1 = 1;
    @Mock
    private Table<Float, Long, Integer> backingTableMock;
    private Table<Float, Long, Integer> meteredTable;

    @Before
    public void setUp() {
        meteredTable = MeteredTable.create(backingTableMock, new MetricRegistry());
    }

    @Test(expected = NullPointerException.class)
    public void backingTable_cannotBeNull() {
        MeteredTable.create(null, new MetricRegistry());
    }

    @Test
    public void get_delegates_to_backing_table() {
        when(backingTableMock.get(ROW_KEY_1, COLUMN_KEY_1)).thenReturn(VALUE_1);

        assertThat(meteredTable.get(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(VALUE_1)));
    }

    @Test
    public void put_delegates_to_backing_table() {
        when(backingTableMock.put(ROW_KEY_1, COLUMN_KEY_1, VALUE_1)).thenReturn(VALUE_1);

        assertThat(meteredTable.put(ROW_KEY_1, COLUMN_KEY_1, VALUE_1), is(equalTo(VALUE_1)));
    }

    @Test
    public void remove_delegates_to_backing_table() {
        when(backingTableMock.remove(ROW_KEY_1, COLUMN_KEY_1)).thenReturn(VALUE_1);

        assertThat(meteredTable.remove(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(VALUE_1)));
    }

    @Test
    public void row_delegates() {
        when(backingTableMock.row(ROW_KEY_1)).thenReturn(ImmutableMap.of(COLUMN_KEY_1, VALUE_1));

        assertThat(meteredTable.row(ROW_KEY_1), is(equalTo(
                (Map<Long, Integer>) ImmutableMap.of(COLUMN_KEY_1, VALUE_1)
        )));
    }

    @Test
    public void column_delegates() {
        when(backingTableMock.column(COLUMN_KEY_1)).thenReturn(ImmutableMap.of(ROW_KEY_1, VALUE_1));

        assertThat(meteredTable.column(COLUMN_KEY_1), is(equalTo(
                (Map<Float, Integer>) ImmutableMap.of(ROW_KEY_1, VALUE_1)
        )));
    }
}
