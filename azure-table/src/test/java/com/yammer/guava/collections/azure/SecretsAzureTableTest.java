package com.yammer.guava.collections.azure;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SecretsAzureTableTest {
    private static final String ROW_NAME = "row_name_fixed";
    private static final Key EXISTING_KEY = new Key("key1");
    private static final Key EXISTING_KEY_2 = new Key("key2");
    private static final Key NON_EXISTENT_KEY = new Key("nonExistingKey");
    private static final Secret SECRET = new Secret("secret1".getBytes());
    private static final Secret NEW_SECRET = new Secret("new secret".getBytes());
    private static final String TABLE_NAME = "secretie_table";
    @Mock
    private SecretieCloudTableClient cloudTableClientMock;
    @Mock
    private SecretieTableRequestFactory secretieTableOperationFactoryMock;
    private SecretsAzureTable secretsAzureTable;

    @Before
    public void setUp() throws IOException, ConfigurationException {
        secretsAzureTable = new SecretsAzureTable(TABLE_NAME, cloudTableClientMock, secretieTableOperationFactoryMock);
    }

    @Test
    public void when_columnKeySet_requested_then_all_keys_returned() {
        // setup
        TableQuery<SecretieEntity> tableQuery = mock(TableQuery.class);
        when(secretieTableOperationFactoryMock.selectAll(TABLE_NAME)).thenReturn(tableQuery);
        when(cloudTableClientMock.execute(tableQuery)).thenReturn(Arrays.asList(new SecretieEntity(ROW_NAME, EXISTING_KEY, SECRET), new SecretieEntity(ROW_NAME, EXISTING_KEY_2, SECRET)));

        // call under test
        Set<Key> keySet = secretsAzureTable.columnKeySet();

        // assertions
        assertThat(keySet, containsInAnyOrder(EXISTING_KEY, EXISTING_KEY_2));
    }

    @Test
    public void get_of_an_existing_value_returns_result_from_azure_table_returned() throws StorageException {
        // setup
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, EXISTING_KEY)).thenReturn(retriveTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, retriveTableOperationMock)).thenReturn(new SecretieEntity(ROW_NAME, EXISTING_KEY, SECRET));

        // call under test
        Secret secret = secretsAzureTable.get(ROW_NAME, EXISTING_KEY);

        // assertions
        assertThat(secret, is(equalTo(SECRET)));
    }

    @Test
    public void get_of_non_existing_entry_returns_null() {
        // setup
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, NON_EXISTENT_KEY)).thenReturn(retriveTableOperationMock);

        // call under test
        Secret secret = secretsAzureTable.get(ROW_NAME, NON_EXISTENT_KEY);

        // assertions
        assertThat(secret, is(nullValue()));
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_get_then_exception_rethrown() throws StorageException {
        // setup
        StorageException storageExceptionMock = mock(StorageException.class);
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, EXISTING_KEY)).thenReturn(retriveTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, retriveTableOperationMock)).thenThrow(storageExceptionMock);

        // call under test
        secretsAzureTable.get(ROW_NAME, EXISTING_KEY);
    }

    @Test
    public void when_put_then_value_added_or_replaced_in_azure() throws StorageException {
        // setup
        TableOperation putTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.put(ROW_NAME, NON_EXISTENT_KEY, NEW_SECRET)).thenReturn(putTableOperationMock);

        // call under test
        secretsAzureTable.put(ROW_NAME, NON_EXISTENT_KEY, NEW_SECRET);

        // assert
        verify(cloudTableClientMock).execute(TABLE_NAME, putTableOperationMock);
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_put_then_exception_rethrown() throws StorageException {
        // setup
        StorageException storageExceptionMock = mock(StorageException.class);
        TableOperation putTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.put(ROW_NAME, EXISTING_KEY, SECRET)).thenReturn(putTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, putTableOperationMock)).thenThrow(storageExceptionMock);

        // call under test
        secretsAzureTable.put(ROW_NAME, EXISTING_KEY, SECRET);
    }

    @Test
    public void when_delete_then_deleted_in_azure() throws StorageException {
        // setup
        SecretieEntity result = new SecretieEntity(ROW_NAME, EXISTING_KEY, SECRET);
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, EXISTING_KEY)).thenReturn(retriveTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, retriveTableOperationMock)).thenReturn(result);

        TableOperation deleteTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.delete(result)).thenReturn(deleteTableOperationMock);


        // call under test
        secretsAzureTable.remove(ROW_NAME, EXISTING_KEY);

        // assertions
        verify(cloudTableClientMock).execute(TABLE_NAME, deleteTableOperationMock);
    }

    @Test
    public void when_key_does_not_exist_then_delete_return_null() throws StorageException {
        // setup
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, NON_EXISTENT_KEY)).thenReturn(retriveTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, retriveTableOperationMock)).thenReturn(null);

        // call under test
        secretsAzureTable.remove(ROW_NAME, NON_EXISTENT_KEY);
    }

    @Test(expected = RuntimeException.class)
    public void when_table_client_throws_storage_exception_during_delete_then_exception_rethrown() throws StorageException {
        // internal get
        SecretieEntity result = new SecretieEntity(ROW_NAME, EXISTING_KEY, SECRET);
        TableOperation retriveTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.retrieve(ROW_NAME, EXISTING_KEY)).thenReturn(retriveTableOperationMock);
        when(cloudTableClientMock.execute(TABLE_NAME, retriveTableOperationMock)).thenReturn(result);
        // delete
        TableOperation deleteTableOperationMock = mock(TableOperation.class);
        when(secretieTableOperationFactoryMock.delete(result)).thenReturn(deleteTableOperationMock);
        // delete error
        StorageException storageExceptionMock = mock(StorageException.class);
        when(cloudTableClientMock.execute(TABLE_NAME, deleteTableOperationMock)).thenThrow(storageExceptionMock);

        secretsAzureTable.remove(ROW_NAME, EXISTING_KEY);
    }

    @Test
    public void cellSet_returns_all_table_cells() {
        // setup
        TableQuery<SecretieEntity> tableQuery = mock(TableQuery.class);
        when(secretieTableOperationFactoryMock.selectAll(TABLE_NAME)).thenReturn(tableQuery);
        when(cloudTableClientMock.execute(tableQuery)).thenReturn(Arrays.asList(new SecretieEntity(ROW_NAME, EXISTING_KEY, SECRET), new SecretieEntity(ROW_NAME, EXISTING_KEY_2, SECRET)));

        // call under test
        Set<Table.Cell<String, Key, Secret>> cellSet = secretsAzureTable.cellSet();

        // assertions
        final Table.Cell<String, Key, Secret> expectedCell1 = Tables.immutableCell(ROW_NAME, EXISTING_KEY, SECRET);
        final Table.Cell<String, Key, Secret> expectedCell2 = Tables.immutableCell(ROW_NAME, EXISTING_KEY_2, SECRET);
        assertThat(cellSet, containsInAnyOrder(expectedCell1, expectedCell2));
    }
}
