package com.yammer.guava.collections.azure;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCode;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SecretsAzureTableDeprecated implements Table<String, Key, Secret> {
    public static final Timer GET_TIMER = createTimerFor("get");
    public static final Timer SELECT_ALL_TIMER = createTimerFor("select-all-rows-and-columns");
    public static final Timer PUT_TIMER = createTimerFor("put");
    public static final Timer REMOVE_TIMER = createTimerFor("remove");
    private static final Function<SecretieEntity, Key> COLUMN_KEY_EXTRACTOR = new Function<SecretieEntity, Key>() {
        @Override
        public Key apply(SecretieEntity input) {
            return input.getKey();
        }
    };
    private static final Function<SecretieEntity, Cell<String, Key, Secret>> TABLE_CELL_CREATOR =
            new Function<SecretieEntity, Cell<String, Key, Secret>>() {
                @Override
                public Cell<String, Key, Secret> apply(SecretieEntity input) {
                    return Tables.immutableCell(input.getPartitionKey(), input.getKey(), input.getSecret());
                }
            };
    private final String tableName;
    private final SecretieCloudTableClient cloudTableClient;
    private final SecretieTableRequestFactory secretieTableOperationFactory;

    /* package */ SecretsAzureTableDeprecated(String tableName, SecretieCloudTableClient cloudTableClient, SecretieTableRequestFactory secretieTableOperationFactory) {
        this.tableName = tableName;
        this.cloudTableClient = cloudTableClient;
        this.secretieTableOperationFactory = secretieTableOperationFactory;
    }

    public SecretsAzureTableDeprecated(String secretieTableName, CloudTableClient tableClient) {
        this(secretieTableName, new SecretieCloudTableClient(tableClient), new SecretieTableRequestFactory());
    }

    private static Timer createTimerFor(String name) {
        return Metrics.newTimer(SecretsAzureTableDeprecated.class, name);
    }

    @Override
    public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsRow(@Nullable Object rowKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsColumn(@Nullable Object columnKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Secret get(Object rowKey, Object columnKey) {
        return entityToSecret(rawGet(rowKey, columnKey));
    }

    private Secret entityToSecret(SecretieEntity secretieEntity) {
        return secretieEntity == null ? null : secretieEntity.getSecret();
    }

    private SecretieEntity rawGet(Object rowKey, Object columnKey) {
        if (!(rowKey instanceof String && columnKey instanceof Key)) {
            return null;
        }

        String row = (String) rowKey;
        Key column = (Key) columnKey;

        TableOperation getSecretOperation = secretieTableOperationFactory.retrieve(row, column);

        try {
            return timedTableOperation(GET_TIMER, getSecretOperation);
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    private SecretieEntity timedTableOperation(Timer contextSpecificTimer, TableOperation tableOperation) throws StorageException {
        TimerContext context = contextSpecificTimer.time();
        try {
            return cloudTableClient.execute(tableName, tableOperation);
        } finally {
            context.stop();
        }
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Secret put(String rowKey, Key columnKey, Secret value) {
        TableOperation putSecretieOperation = secretieTableOperationFactory.put(rowKey, columnKey, value);

        try {
            return entityToSecret(timedTableOperation(PUT_TIMER, putSecretieOperation));
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void putAll(Table<? extends String, ? extends Key, ? extends Secret> table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Secret remove(Object rowKey, Object columnKey) {
        SecretieEntity entityToBeDeleted = rawGet(rowKey, columnKey);

        if (entityToBeDeleted == null) {
            return null;
        }

        TableOperation deleteSecretieOperation = secretieTableOperationFactory.delete(entityToBeDeleted);

        try {
            return entityToSecret(timedTableOperation(REMOVE_TIMER, deleteSecretieOperation));
        } catch (StorageException e) {
            if (notFound(e)) {
                return null;
            }
            throw Throwables.propagate(e);
        }
    }

    private boolean notFound(StorageException e) {
        return StorageErrorCode.RESOURCE_NOT_FOUND.toString().equals(e.getErrorCode())
                || "ResourceNotFound".equals(e.getErrorCode());
    }

    @Override
    public Map<Key, Secret> row(String rowKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Secret> column(Key columnKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Cell<String, Key, Secret>> cellSet() {
        Iterable<Cell<String, Key, Secret>> cellSetIterable = Iterables.transform(selectAll(), TABLE_CELL_CREATOR);
        return Collections.unmodifiableSet(Sets.newHashSet(cellSetIterable));
    }

    @Override
    public Set<String> rowKeySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Key> columnKeySet() {
        Iterable<Key> columnKeyIterable = Iterables.transform(selectAll(), COLUMN_KEY_EXTRACTOR);
        return Collections.unmodifiableSet(Sets.newHashSet(columnKeyIterable));
    }

    private Iterable<SecretieEntity> selectAll() {
        TableQuery<SecretieEntity> keySetQuery = secretieTableOperationFactory.selectAll(tableName);
        return timedExecuteQuery(SELECT_ALL_TIMER, keySetQuery);
    }

    private Iterable<SecretieEntity> timedExecuteQuery(Timer contextTimer, TableQuery<SecretieEntity> query) {
        TimerContext context = contextTimer.time();
        try {
            return cloudTableClient.execute(query);
        } finally {
            context.stop();
        }
    }

    @Override
    public Collection<Secret> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<Key, Secret>> rowMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Key, Map<String, Secret>> columnMap() {
        throw new UnsupportedOperationException();
    }
}
