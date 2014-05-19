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
package com.yammer.collections.azure;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "SuspiciousMethodCalls"})
@RunWith(MockitoJUnitRunner.class)
public class ColumnViewTest {
    private static final Bytes ROW_KEY = new Bytes("rowKey".getBytes());
    private static final Bytes COLUMN_KEY_1 = new Bytes("columnKey1".getBytes());
    private static final Bytes VALUE_1 = new Bytes("value1".getBytes());
    private static final Bytes COLUMN_KEY_2 = new Bytes("columnKey2".getBytes());
    private static final Bytes VALUE_2 = new Bytes("value2".getBytes());
    private static final Bytes RET_VALUE = new Bytes("ret_value".getBytes());
    private static final Bytes OTHER_ROW_KEY = new Bytes("otherRow".getBytes());
    private static final Bytes OTHER_COLUMN_KEY = new Bytes("otherKey".getBytes());
    private static final Bytes OTHER_VALUE = new Bytes("otherValue".getBytes());
    private static final String TABLE_NAME = "secretie_table";
    private static final Table.Cell<Bytes, Bytes, Bytes> CELL_1 = Tables.immutableCell(ROW_KEY, COLUMN_KEY_1, VALUE_1);
    private static final Table.Cell<Bytes, Bytes, Bytes> CELL_2 = Tables.immutableCell(ROW_KEY, COLUMN_KEY_2, VALUE_2);
    private static final Table.Cell<Bytes, Bytes, Bytes> CELL_WITH_OTHER_ROW_KEY = Tables.immutableCell(OTHER_ROW_KEY, OTHER_COLUMN_KEY, OTHER_VALUE);
    private static final Function<Map.Entry, TestMapEntry> MAP_TO_ENTRIES = new Function<Map.Entry, TestMapEntry>() {
        @SuppressWarnings("ClassEscapesDefinedScope")
        @Override
        public TestMapEntry apply(Map.Entry input) {
            return new TestMapEntry(input);
        }
    };
    @Mock
    private AzureTableCloudClient azureTableCloudClientMock;
    @Mock
    private AzureTableRequestFactory azureTableRequestFactoryMock;
    @Mock
    private BaseAzureTable baseAzureTable;
    private ColumnView columnView;

    @Before
    public void setUp() {
        when(baseAzureTable.getTableName()).thenReturn(TABLE_NAME);
        columnView = new ColumnView(baseAzureTable, ROW_KEY, azureTableCloudClientMock, azureTableRequestFactoryMock);
    }

    @Test
    public void put_delegates_to_table() {
        when(baseAzureTable.put(ROW_KEY, COLUMN_KEY_1, VALUE_1)).thenReturn(RET_VALUE);

        assertThat(columnView.put(COLUMN_KEY_1, VALUE_1), is(equalTo(RET_VALUE)));
    }

    @Test
    public void get_delegates_to_table() {
        when(baseAzureTable.get(ROW_KEY, COLUMN_KEY_1)).thenReturn(VALUE_1);

        assertThat(columnView.get(COLUMN_KEY_1), is(equalTo(VALUE_1)));
    }

    @Test
    public void remove_delegates_to_table() {
        when(baseAzureTable.remove(ROW_KEY, COLUMN_KEY_1)).thenReturn(VALUE_1);

        assertThat(columnView.remove(COLUMN_KEY_1), is(equalTo(VALUE_1)));
    }

    @Test
    public void contains_key_delegates_to_table() {
        when(baseAzureTable.contains(ROW_KEY, COLUMN_KEY_1)).thenReturn(true);

        assertThat(columnView.containsKey(COLUMN_KEY_1), is(equalTo(true)));
    }

    @Test
    public void putAll_delegates_to_table() {
        columnView.putAll(
                ImmutableMap.of(
                        COLUMN_KEY_1, VALUE_1,
                        COLUMN_KEY_2, VALUE_2
                ));

        verify(baseAzureTable).put(ROW_KEY, COLUMN_KEY_1, VALUE_1);
        verify(baseAzureTable).put(ROW_KEY, COLUMN_KEY_2, VALUE_2);
    }

    @Test
    public void keySet_returns_contained_keys() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);

        assertThat(columnView.keySet(), containsInAnyOrder(COLUMN_KEY_1, COLUMN_KEY_2));
    }

    @Test
    public void values_returns_contained_values() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);

        assertThat(columnView.values(), containsInAnyOrder(VALUE_1, VALUE_2));
    }

    @Test
    public void entrySet_returns_contained_entries() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);

        assertThat(
                Iterables.transform(columnView.entrySet(), MAP_TO_ENTRIES),
                containsInAnyOrder(
                        new TestMapEntry(COLUMN_KEY_1, VALUE_1),
                        new TestMapEntry(COLUMN_KEY_2, VALUE_2)
                ));
    }

    @Test
    public void setValue_on_entry_updates_backing_table() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);
        when(baseAzureTable.put(ROW_KEY, COLUMN_KEY_1, OTHER_VALUE)).thenReturn(RET_VALUE);
        when(baseAzureTable.put(ROW_KEY, COLUMN_KEY_2, OTHER_VALUE)).thenReturn(RET_VALUE);

        Map.Entry<Bytes, Bytes> someEntry = columnView.entrySet().iterator().next();

        assertThat(someEntry.setValue(OTHER_VALUE), is(equalTo(RET_VALUE)));
        verify(baseAzureTable).put(ROW_KEY, someEntry.getKey(), OTHER_VALUE);
    }

    @Test
    public void size_returns_correct_value() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);

        assertThat(columnView.size(), is(equalTo(2)));
    }

    @Test
    public void clear_deletes_values_from_key_set() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2, CELL_WITH_OTHER_ROW_KEY);

        columnView.clear();

        verify(baseAzureTable).remove(ROW_KEY, COLUMN_KEY_1);
        verify(baseAzureTable).remove(ROW_KEY, COLUMN_KEY_2);
    }

    @Test
    public void isEmpty_returns_false_if_no_entires() throws StorageException {
        setAzureTableToContain(CELL_WITH_OTHER_ROW_KEY);

        assertThat(columnView.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void isEmpty_returns_true_if_there_are_entires() throws StorageException {
        setAzureTableToContain(CELL_1);

        assertThat(columnView.isEmpty(), is(equalTo(false)));
    }

    @Test
    public void contains_value_returns_true_if_value_contains() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_WITH_OTHER_ROW_KEY);

        assertThat(columnView.containsValue(VALUE_1), is(equalTo(true)));
    }

    @Test
    public void contains_value_returns_false_if_does_not_contain_value_in_row() throws StorageException {
        setAzureTableToContain(Tables.immutableCell(OTHER_ROW_KEY, COLUMN_KEY_1, VALUE_1));

        assertThat(columnView.containsValue(VALUE_1), is(equalTo(false)));
    }

    @Test
    public void contains_value_returns_false_if_object_not_string() throws StorageException {
        setAzureTableToContain();

        assertThat(columnView.containsValue(new Object()), is(equalTo(false)));
    }


    //----------------------
    // Utilities
    //----------------------

    @SafeVarargs
    private final void setAzureTableToContain(Table.Cell<Bytes, Bytes, Bytes>... cells) throws StorageException {
        for (Table.Cell<Bytes, Bytes, Bytes> cell : cells) {
            when(baseAzureTable.get(cell.getRowKey(), cell.getColumnKey())).thenReturn(cell.getValue());
        }
        AzureTestUtil.setAzureTableToContain(TABLE_NAME, azureTableRequestFactoryMock, azureTableCloudClientMock, cells);
    }

    private static class TestMapEntry implements Map.Entry<Bytes, Bytes> {
        private final Bytes key;
        private final Bytes value;

        public TestMapEntry(Map.Entry<Bytes, Bytes> entry) {
            this(entry.getKey(), entry.getValue());
        }

        public TestMapEntry(Bytes key, Bytes value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Bytes getKey() {
            return key;
        }

        @Override
        public Bytes getValue() {
            return value;
        }

        @Override
        public Bytes setValue(Bytes value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "TestMapEntry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestMapEntry that = (TestMapEntry) o;

            if (key != null ? !key.equals(that.key) : that.key != null) return false;
            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }


}
