package com.yammer.collections.azure;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * This test uses a persistent database. This has its consequences in that if a test fails, the cleanup process
 * can be affected and influence other tests.
 */
@Ignore("talks to azure directly, switched off by default")
@SuppressWarnings({"InstanceVariableMayNotBeInitialized", "ClassWithTooManyMethods"})
public class BaseAzureTableIntegrationTest {
    private static final String ACCOUNT_NAME = "secretietest";
    private static final String ACCOUNT_KEY = "e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==";
    private static final String CONNECTION_STRING = String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s", ACCOUNT_NAME, ACCOUNT_KEY);
    private static final String TABLE_NAME = "baseTableIntegrationTest";
    private static final String ROW_KEY_1 = "rown_name_1";
    private static final String ROW_KEY_2 = "row_name_2";
    private static final String COLUMN_KEY_1 = "column_key_1";
    private static final String COLUMN_KEY_2 = "column_key_2";
    private static final String NON_EXISTENT_COLUMN_KEY = "non_existent_column_key";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value3";
    private static final Table.Cell<String, String, String> CELL_1 = Tables.immutableCell(ROW_KEY_1, COLUMN_KEY_1, VALUE_1);
    private static final Table.Cell<String, String, String> CELL_2 = Tables.immutableCell(ROW_KEY_2, COLUMN_KEY_2, VALUE_2);
    private Table<String, String, String> baseAzureTable;

    @Before
    public void setUp() throws URISyntaxException, InvalidKeyException, StorageException {
        CloudTableClient cloudTableClient = CloudStorageAccount.parse(CONNECTION_STRING).createCloudTableClient();
        CloudTable table = cloudTableClient.getTableReference(TABLE_NAME);
        table.createIfNotExist();
        baseAzureTable = BaseAzureTable.create(TABLE_NAME, cloudTableClient);

        baseAzureTable.clear();
    }

    @After
    public void cleanUp() {
        baseAzureTable.clear();
    }

    @Test
    public void when_columnKeySet_requested_then_all_keys_returned() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<String> columnKeySet = baseAzureTable.columnKeySet();

        assertThat(columnKeySet, containsInAnyOrder(COLUMN_KEY_1, COLUMN_KEY_2));
    }

    @Test
    public void when_rowKeySet_requested_then_all_keys_returned() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<String> rowKeySet = baseAzureTable.rowKeySet();

        assertThat(rowKeySet, containsInAnyOrder(ROW_KEY_1, ROW_KEY_2));
    }

    @Test
    public void get_of_an_existing_value_returns_result_from_azure_table_returned() throws StorageException {
        setAzureTableToContain(CELL_1);

        String value = baseAzureTable.get(ROW_KEY_1, COLUMN_KEY_1);

        assertThat(value, is(equalTo(VALUE_1)));
    }

    @Test
    public void get_of_non_existing_entry_returns_null() throws StorageException {
        setAzureTableToContain(CELL_1);

        String value = baseAzureTable.get(ROW_KEY_2, COLUMN_KEY_2);

        assertThat(value, is(nullValue()));
    }

    @Test
    public void when_put_then_value_added_or_replaced_in_azure() throws StorageException {

        baseAzureTable.put(ROW_KEY_2, COLUMN_KEY_2, VALUE_2);

        assertThat(baseAzureTable.get(ROW_KEY_2, COLUMN_KEY_2), is(equalTo(VALUE_2)));
    }

    @Test
    public void when_delete_then_deleted_in_azure() throws StorageException {
        setAzureTableToContain(CELL_1);

        baseAzureTable.remove(ROW_KEY_1, COLUMN_KEY_1);

        assertThat(baseAzureTable.get(ROW_KEY_1, COLUMN_KEY_1), is(nullValue()));
    }

    @Test
    public void when_key_does_not_exist_then_delete_return_null() throws StorageException {
        assertThat(baseAzureTable.remove(ROW_KEY_1, NON_EXISTENT_COLUMN_KEY), is(equalTo(null)));
    }

    @Test
    public void cellSet_returns_all_table_cells() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        Set<Table.Cell<String, String, String>> cellSet = baseAzureTable.cellSet();

        //noinspection unchecked
        assertThat(cellSet, containsInAnyOrder(CELL_1, CELL_2));
    }

    @Test
    public void when_contains_value_for_row_and_key_then_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1);

        assertThat(baseAzureTable.contains(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(true)));
    }

    @Test
    public void when_does_not_contain_value_for_row_and_key_then_returns_false() throws StorageException {
        assertThat(baseAzureTable.contains(ROW_KEY_1, COLUMN_KEY_1), is(equalTo(false)));
    }

    @Test
    public void when_contains_value_for_given_row_contains_row_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsRow(ROW_KEY_1), is(equalTo(true)));
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
    public void when_contains_values_the_is_empty_returns_false() throws StorageException {
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
    public void when_contains_value_for_given_column_contains_column_returns_true() throws StorageException {
        setAzureTableToContain(CELL_1, CELL_2);

        assertThat(baseAzureTable.containsColumn(COLUMN_KEY_1), is(equalTo(true)));
    }

    @Test
    public void when_does_not_contain_a_value_for_given_column_contains_column_returns_false() throws StorageException {
        setAzureTableToContain(CELL_2);

        assertThat(baseAzureTable.containsColumn(COLUMN_KEY_1), is(equalTo(false)));
    }

    @Test
    public void columnView_contains_correct_values() {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<String, String> column = baseAzureTable.column(COLUMN_KEY_1);

        assertThat(column.containsValue(VALUE_1), is(equalTo(true)));
        assertThat(column.containsValue(VALUE_2), is(equalTo(false)));
    }

    @Test
    public void columnView_contains_correct_entries() {
        setAzureTableToContain(CELL_1, CELL_2);

        Iterator<Map.Entry<String, String>> entries = baseAzureTable.column(COLUMN_KEY_1).entrySet().iterator();

        entries.hasNext(); // needed to initialize the iterator, due to a bug in the azure sdk

        Map.Entry<String, String> entry = entries.next();
        assertThat(entries.hasNext(), is(equalTo(false)));
        assertThat(entry.getKey(), is(equalTo(ROW_KEY_1)));
        assertThat(entry.getValue(), is(equalTo(VALUE_1)));
    }

    @Test
    public void rowView_contains_correct_values() {
        setAzureTableToContain(CELL_1, CELL_2);

        Map<String, String> column = baseAzureTable.row(ROW_KEY_1);

        assertThat(column.containsValue(VALUE_1), is(equalTo(true)));
        assertThat(column.containsValue(VALUE_2), is(equalTo(false)));
    }

    @Test
    public void rowView_contains_correct_entries() {
        setAzureTableToContain(CELL_1, CELL_2);

        Iterator<Map.Entry<String, String>> entries = baseAzureTable.row(ROW_KEY_1).entrySet().iterator();

        entries.hasNext(); // needed to initialize the iterator, due to a bug in the azure sdk

        Map.Entry<String, String> entry = entries.next();
        assertThat(entries.hasNext(), is(equalTo(false)));
        assertThat(entry.getKey(), is(equalTo(COLUMN_KEY_1)));
        assertThat(entry.getValue(), is(equalTo(VALUE_1)));
    }

    @SafeVarargs
    private final void setAzureTableToContain(Table.Cell<String, String, String>... cells) {
        for (Table.Cell<String, String, String> cell : cells) {
            baseAzureTable.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }


}
