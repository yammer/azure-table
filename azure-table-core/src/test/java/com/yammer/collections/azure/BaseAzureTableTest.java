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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings({"ClassWithTooManyMethods", "InstanceVariableMayNotBeInitialized"})
@RunWith(MockitoJUnitRunner.class)
public class BaseAzureTableTest {
    private static final Bytes ROW_KEY_1 = new Bytes("rown_name_1".getBytes());
    private static final Bytes ROW_KEY_2 = new Bytes("row_name_2".getBytes());
    private static final Bytes COLUMN_KEY_1 = new Bytes("column_key_1".getBytes());
    private static final Bytes COLUMN_KEY_2 = new Bytes("column_key_2".getBytes());
    private static final Bytes NON_EXISTENT_COLUMN_KEY = new Bytes("non_existent_column_key".getBytes());
    private static final Bytes VALUE_1 = new Bytes("value1".getBytes());
    private static final Bytes VALUE_2 = new Bytes("value3".getBytes());
    private static final String TABLE_NAME = "secretie_table";
    private static final Table.Cell<Bytes, Bytes, Bytes> CELL_1 = Tables.immutableCell(ROW_KEY_1, COLUMN_KEY_1, VALUE_1);
    private static final Table.Cell<Bytes, Bytes, Bytes> CELL_2 = Tables.immutableCell(ROW_KEY_2, COLUMN_KEY_2, VALUE_2);
    @Mock
    private AzureTableCloudClient azureTableCloudClientMock;
    @Mock
    private AzureTableRequestFactory azureTableRequestFactoryMock;
    private BaseAzureTable baseAzureTable;

    private static String encode(Bytes stringToBeEncoded) {
        return AzureTestUtil.encode(stringToBeEncoded);
    }

    @Before
    public void setUp() throws IOException {
        baseAzureTable = new BaseAzureTable(TABLE_NAME, azureTableCloudClientMock, azureTableRequestFactoryMock);
    }

    @Test(expected = NullPointerException.class)
    public void tableNameCannotBeNull() throws URISyntaxException {
        BaseAzureTable.create(null, new CloudTableClient(new URI("http://localhost/"), null));
    }

    @Test(expected = NullPointerException.class)
    public void cloudTableClientCannotBeNull() {
        BaseAzureTable.create(TABLE_NAME, null);
    }

    @Test
    public void get_table_name_returns_table_name() {
        assertThat(baseAzureTable.getTableName(), is(equalTo(TABLE_NAME)));
    }

    @Test
    public void when_columnKeySet_requested_then_all_keys_returned() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<Bytes> columnKeySet = baseAzureTable.columnKeySet();

        assertThat(columnKeySet, containsInAnyOrder(COLUMN_KEY_1, COLUMN_KEY_2));
    }

    @Test
    public void when_rowKeySet_requested_then_all_keys_returned() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<Bytes> rowKeySet = baseAzureTable.rowKeySet();

        assertThat(rowKeySet, containsInAnyOrder(ROW_KEY_1, ROW_KEY_2));
    }

    @Test
    public void get_of_an_existing_value_returns_result_from_azure_table_returned() throws StorageException {
        setAzureTableToContain(CELL_1);

        Bytes value = baseAzureTable.get(ROW_KEY_1, COLUMN_KEY_1);

        assertThat(value, is(equalTo(VALUE_1)));
    }

    @Test
    public void get_on_null_rowKey_returns_null() throws StorageException {
        setAzureTableToContain(CELL_1);

        assertThat(baseAzureTable.get(null, COLUMN_KEY_1), is(nullValue()));
    }

    @Test
    public void get_on_null_columnKey_returns_null() throws StorageException {
        setAzureTableToContain(CELL_1);

        assertThat(baseAzureTable.get(ROW_KEY_1, null), is(nullValue()));
    }

    @Test
    public void get_of_non_existing_entry_returns_null() throws StorageException {
        setAzureTableToContain(CELL_1);

        Bytes value = baseAzureTable.get(ROW_KEY_2, COLUMN_KEY_2);

        assertThat(value, is(nullValue()));
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_get_then_exception_rethrown() throws StorageException {
        setAzureTableToContain(CELL_1);
        setToThrowStorageExceptionOnRetrievalOf(CELL_1);

        baseAzureTable.get(ROW_KEY_1, COLUMN_KEY_1);
    }

    @Test
    public void when_put_then_value_added_or_replaced_in_azure() throws StorageException {
        TableOperation putTableOperationMock = mockPutTableOperation(CELL_2);

        baseAzureTable.put(ROW_KEY_2, COLUMN_KEY_2, VALUE_2);

        verify(azureTableCloudClientMock).execute(TABLE_NAME, putTableOperationMock);
    }

    @Test(expected = NullPointerException.class)
    public void when_put_null_row_key_then_error() {
        baseAzureTable.put(null, COLUMN_KEY_1, VALUE_1);
    }

    @Test(expected = NullPointerException.class)
    public void when_put_null_column_key_then_error() {
        baseAzureTable.put(ROW_KEY_1, null, VALUE_1);
    }

    @Test(expected = NullPointerException.class)
    public void when_put_null_value_then_error() {
        baseAzureTable.put(ROW_KEY_1, COLUMN_KEY_1, null);
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_put_then_exception_rethrown() throws StorageException {
        TableOperation putTableOperationMock = mockPutTableOperation(CELL_1);
        setupThrowStorageExceptionOnTableOperation(putTableOperationMock);

        baseAzureTable.put(ROW_KEY_1, COLUMN_KEY_1, VALUE_1);
    }

    @Test
    public void when_delete_then_deleted_in_azure() throws StorageException {
        setAzureTableToContain(CELL_1);
        TableOperation deleteTableOperationMock = mockDeleteTableOperation(CELL_1);

        baseAzureTable.remove(ROW_KEY_1, COLUMN_KEY_1);

        verify(azureTableCloudClientMock).execute(TABLE_NAME, deleteTableOperationMock);
    }

    @Test
    public void when_key_does_not_exist_then_delete_return_null() {
        assertThat(baseAzureTable.remove(ROW_KEY_1, NON_EXISTENT_COLUMN_KEY), is(equalTo(null)));
    }

    @Test
    public void when_remove_null_row_key_then_delete_return_null() {
        assertThat(baseAzureTable.remove(null, COLUMN_KEY_1), is(equalTo(null)));
    }

    @Test
    public void when_remove_null_column_key_then_delete_return_null() {
        assertThat(baseAzureTable.remove(ROW_KEY_1, null), is(equalTo(null)));
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_delete_then_exception_rethrown() throws StorageException {
        setAzureTableToContain(CELL_1);
        TableOperation deleteTableOperationMock = mockDeleteTableOperation(CELL_1);
        setupThrowStorageExceptionOnTableOperation(deleteTableOperationMock);

        baseAzureTable.remove(ROW_KEY_1, COLUMN_KEY_1);
    }

    @Test
    public void cellSet_returns_all_table_cells() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<Table.Cell<Bytes, Bytes, Bytes>> cellSet = baseAzureTable.cellSet();

        assertThat(cellSet, containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void when_contains_value_for_row_and_key_then_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1);

        assertThat(baseAzureTable.contains(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(true)));
    }

    @Test
    public void when_does_not_contain_value_for_row_and_key_then_returns_false() {
        assertThat(baseAzureTable.contains(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(false)));
    }

    @Test
    public void when_contain_value_for_null_row_key_and_then_returns_false() {
        assertThat(baseAzureTable.contains(null, COLUMN_KEY_1), is(equalTo(false)));
    }

    @Test
    public void when_contain_value_for_null_column_key_and_then_returns_false() {
        assertThat(baseAzureTable.contains(ROW_KEY_1, null), is(equalTo(false)));
    }

    @Test
    public void row_returns_column_map_with_appropriate_contents() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<Bytes, Bytes> columnMap = baseAzureTable.row(ROW_KEY_1);

        assertThat(columnMap.containsKey(COLUMN_KEY_1), is(equalTo(true)));
        assertThat(columnMap.containsKey(COLUMN_KEY_2), is(equalTo(false)));
    }

    @Test(expected = NullPointerException.class)
    public void row_cannot_be_called_with_null_argument() throws StorageException {
        baseAzureTable.row(null);
    }

    @Test
    public void when_contains_value_for_given_row_contains_row_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsRow(ROW_KEY_1), is(equalTo(true)));
    }

    @Test
    public void when_row_object_is_not_a_string_then_contains_row_returns_false() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsRow(new Object()), is(equalTo(false)));
    }

    @Test
    public void containsRow_for_null_returns_false() {
        assertThat(baseAzureTable.containsRow(null), is(equalTo(false)));
    }

    @Test
    public void when_does_not_contain_a_value_for_given_row_contains_row_returns_false() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.containsRow(ROW_KEY_1), is(equalTo(false)));
    }

    @Test
    public void values_returns_all_values() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.values(), containsInAnyOrder(VALUE_1, VALUE_2));
    }

    @Test
    public void contains_value_returns_true_if_value_contains() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsValue(VALUE_1), is(equalTo(true)));
    }

    @Test
    public void contains_value_returns_false_if_does_not_contain_value() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.containsValue(VALUE_1), is(equalTo(false)));
    }

    @Test
    public void contains_value_returns_false_if_called_with_null_argument() {
        assertThat(baseAzureTable.containsValue(null), is(equalTo(false)));
    }

    @Test
    public void contains_value_returns_false_if_object_not_string() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.containsValue(new Object()), is(equalTo(false)));
    }

    @Test
    public void when_contains_values_iSempty_returns_false() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.isEmpty(), is(equalTo(false)));
    }

    @Test
    public void when_empty_then_is_empty_returns_true() throws StorageException {
        setAzureTableToContain();

        assertThat(baseAzureTable.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void size_returns_correct_size() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.size(), is(equalTo(2)));
    }

    @Test
    public void clear_deletes_all_in_cell_set() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);
        TableOperation deleteTableOperationMock1 = mockDeleteTableOperation(CELL_1);
        TableOperation deleteTableOperationMock2 = mockDeleteTableOperation(CELL_2);

        baseAzureTable.clear();

        verify(azureTableCloudClientMock).execute(TABLE_NAME, deleteTableOperationMock1);
        verify(azureTableCloudClientMock).execute(TABLE_NAME, deleteTableOperationMock2);
    }

    @Test
    public void put_all_puts_all_the_values() throws StorageException {
        Table<Bytes, Bytes, Bytes> sourceTable = HashBasedTable.create();
        sourceTable.put(ROW_KEY_1, COLUMN_KEY_1, VALUE_1);
        sourceTable.put(ROW_KEY_2, COLUMN_KEY_2, VALUE_2);
        TableOperation putTableOperationMock1 = mockPutTableOperation(CELL_1);
        TableOperation putTableOperationMock2 = mockPutTableOperation(CELL_2);

        baseAzureTable.putAll(sourceTable);

        verify(azureTableCloudClientMock).execute(TABLE_NAME, putTableOperationMock1);
        verify(azureTableCloudClientMock).execute(TABLE_NAME, putTableOperationMock2);
    }

    @Test(expected = NullPointerException.class)
    public void putAll_cannot_be_called_with_null_argument() throws StorageException {
        baseAzureTable.putAll(null);
    }

    @Test
    public void column_returns_row_map_with_appropriate_contents() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<Bytes, Bytes> columnMap = baseAzureTable.column(COLUMN_KEY_1);

        assertThat(columnMap.containsKey(ROW_KEY_1), is(equalTo(true)));
        assertThat(columnMap.containsKey(ROW_KEY_2), is(equalTo(false)));
    }

    @Test(expected = NullPointerException.class)
    public void column_cannot_be_called_with_null_argument() throws StorageException {
        baseAzureTable.column(null);
    }

    @Test
    public void when_contains_value_for_given_column_contains_column_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsColumn(COLUMN_KEY_1), is(equalTo(true)));
    }

    @Test
    public void when_column_object_is_not_a_string_then_contains_column_returns_false() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsColumn(new Object()), is(equalTo(false)));
    }

    @Test
    public void when_does_not_contain_a_value_for_given_column_contains_column_returns_false() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.containsColumn(COLUMN_KEY_1), is(equalTo(false)));
    }

    @Test
    public void rowMap_returns_correct_map() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<Bytes, Map<Bytes, Bytes>> rowMap = baseAzureTable.rowMap();

        assertThat(rowMap.size(), is(equalTo(2)));
        // row 1
        Map<Bytes, Bytes> rowKey1Map = rowMap.get(ROW_KEY_1);
        assertThat(rowKey1Map.size(), is(equalTo(1)));
        assertThat(rowKey1Map.get(COLUMN_KEY_1), is(equalTo(VALUE_1)));
        // row 2
        Map<Bytes, Bytes> rowKey2Map = rowMap.get(ROW_KEY_2);
        assertThat(rowKey2Map.size(), is(equalTo(1)));
        assertThat(rowKey2Map.get(COLUMN_KEY_2), is(equalTo(VALUE_2)));
    }

    //
    // Utility methods
    //

    @Test
    public void columnMap_returns_correct_map() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<Bytes, Map<Bytes, Bytes>> columnMap = baseAzureTable.columnMap();

        assertThat(columnMap.size(), is(equalTo(2)));
        // row 1
        Map<Bytes, Bytes> columnKeyMap1 = columnMap.get(COLUMN_KEY_1);
        assertThat(columnKeyMap1.size(), is(equalTo(1)));
        assertThat(columnKeyMap1.get(ROW_KEY_1), is(equalTo(VALUE_1)));
        // row 2
        Map<Bytes, Bytes> columnKeyMap2 = columnMap.get(COLUMN_KEY_2);
        assertThat(columnKeyMap2.size(), is(equalTo(1)));
        assertThat(columnKeyMap2.get(ROW_KEY_2), is(equalTo(VALUE_2)));
    }

    @SafeVarargs
    private final void setAzureTableToContain(Table.Cell<Bytes, Bytes, Bytes>... cells) throws StorageException {
        AzureTestUtil.setAzureTableToContain(TABLE_NAME, azureTableRequestFactoryMock, azureTableCloudClientMock, cells);
    }

    private void setToThrowStorageExceptionOnRetrievalOf(Table.Cell<Bytes, Bytes, Bytes> cell) throws StorageException {
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(azureTableRequestFactoryMock.retrieve(encode(cell.getRowKey()), encode(cell.getColumnKey()))).thenReturn(retriveTableOperationMock);
        setupThrowStorageExceptionOnTableOperation(retriveTableOperationMock);
    }

    private TableOperation mockPutTableOperation(Table.Cell<Bytes, Bytes, Bytes> cell) {
        TableOperation putTableOperationMock = mock(TableOperation.class);
        when(azureTableRequestFactoryMock.put(encode(cell.getRowKey()), encode(cell.getColumnKey()), encode(cell.getValue()))).thenReturn(putTableOperationMock);
        return putTableOperationMock;
    }

    private void setupThrowStorageExceptionOnTableOperation(TableOperation tableOperationMock) throws StorageException {
        StorageException storageExceptionMock = mock(StorageException.class);
        when(azureTableCloudClientMock.execute(TABLE_NAME, tableOperationMock)).thenThrow(storageExceptionMock);
    }

    private TableOperation mockDeleteTableOperation(Table.Cell<Bytes, Bytes, Bytes> cell) throws StorageException {
        TableOperation retrieveOperation = azureTableRequestFactoryMock.retrieve(encode(cell.getRowKey()), encode(cell.getColumnKey()));
        AzureEntity result = azureTableCloudClientMock.execute(TABLE_NAME, retrieveOperation);
        TableOperation deleteTableOperationMock = mock(TableOperation.class);
        when(azureTableRequestFactoryMock.delete(result)).thenReturn(deleteTableOperationMock);
        return deleteTableOperationMock;
    }


}