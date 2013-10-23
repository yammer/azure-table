package com.yammer.collections.guava.azure;

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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.yammer.collections.guava.azure.StringEntityUtil.decode;
import static com.yammer.collections.guava.azure.StringEntityUtil.encode;

public class StringAzureTable implements Table<String, String, String> {
    private static final Timer GET_TIMER = createTimerFor("get");
    private static final Timer SELECT_ALL_TIMER = createTimerFor("select-all-rows-and-columns");
    private static final Timer PUT_TIMER = createTimerFor("put");
    private static final Timer REMOVE_TIMER = createTimerFor("remove");
    private static final Function<StringEntity, String> COLUMN_KEY_EXTRACTOR = new Function<StringEntity, String>() {
        @Override
        public String apply(StringEntity input) {
            return decode(input.getRowKey());
        }
    };
    private static final Function<StringEntity, Cell<String, String, String>> TABLE_CELL_CREATOR =
            new Function<StringEntity, Cell<String, String, String>>() {
                @Override
                public Cell<String, String, String> apply(StringEntity input) {
                    return Tables.immutableCell(
                            decode(input.getPartitionKey()),
                            decode(input.getRowKey()),
                            decode(input.getValue()));
                }
            };
    private final String tableName;
    private final StringTableCloudClient cloudTableClient;
    private final StringTableRequestFactory secretieTableOperationFactory;

    /* package */ StringAzureTable(String tableName, StringTableCloudClient cloudTableClient, StringTableRequestFactory secretieTableOperationFactory) {
        this.tableName = tableName;
        this.cloudTableClient = cloudTableClient;
        this.secretieTableOperationFactory = secretieTableOperationFactory;
    }

    public StringAzureTable(String secretieTableName, CloudTableClient tableClient) {
        this(secretieTableName, new StringTableCloudClient(tableClient), new StringTableRequestFactory());
    }

    private static Timer createTimerFor(String name) {
        return Metrics.newTimer(StringAzureTable.class, name);
    }

    @Override
    public boolean contains(Object rowString, Object columnString) {
        return get(rowString, columnString) != null;
    }

    @Override
    public boolean containsRow(Object rowString) {
        throw new UnsupportedOperationException();  // todo doable with column map
    }

    @Override
    public boolean containsColumn(Object columnString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException(); // TODO doable with cellset
    }

    @Override
    public String get(Object rowString, Object columnString) {
        return entityToValue(rawGet(rowString, columnString));
    }

    private String entityToValue(StringEntity stringEntity) {
        return stringEntity == null ? null : decode(stringEntity.getValue());
    }

    private StringEntity rawGet(Object rowString, Object columnString) {
        if (!(rowString instanceof String && columnString instanceof String)) {
            return null;
        }

        String row = encode((String) rowString);
        String column = encode((String) columnString);

        TableOperation retrieveEntityOperation = secretieTableOperationFactory.retrieve(row, column);

        try {
            return timedTableOperation(GET_TIMER, retrieveEntityOperation);
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    private StringEntity timedTableOperation(Timer contextSpecificTimer, TableOperation tableOperation) throws StorageException {
        TimerContext context = contextSpecificTimer.time();
        try {
            return cloudTableClient.execute(tableName, tableOperation);
        } finally {
            context.stop();
        }
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();   // TODO doable with cellset
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException(); // TODO doable with cellset
    }

    @Override
    public void clear() { // cannot be done meaningfully at the moment
        throw new UnsupportedOperationException();   // TODO doable with cellset
    }

    @Override
    public String put(String rowString, String columnString, String value) {
        TableOperation putStringieOperation = secretieTableOperationFactory.put(encode(rowString), encode(columnString), encode(value));

        try {
            return entityToValue(timedTableOperation(PUT_TIMER, putStringieOperation));
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void putAll(Table<? extends String, ? extends String, ? extends String> table) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object rowString, Object columnString) {
        StringEntity entityToBeDeleted = rawGet(rowString, columnString);

        if (entityToBeDeleted == null) {
            return null;
        }

        TableOperation deleteStringieOperation = secretieTableOperationFactory.delete(entityToBeDeleted);

        try {
            return entityToValue(timedTableOperation(REMOVE_TIMER, deleteStringieOperation));
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
    public Map<String, String> row(String rowString) {// todo doable with column map
        return new ColumnMap(this, rowString, cloudTableClient, secretieTableOperationFactory);
    }

    @Override
    public Map<String, String> column(String columnString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Cell<String, String, String>> cellSet() {
        Iterable<Cell<String, String, String>> cellSetIterable = Iterables.transform(selectAll(), TABLE_CELL_CREATOR);
        return Collections.unmodifiableSet(Sets.newHashSet(cellSetIterable));
    }

    @Override
    public Set<String> rowKeySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> columnKeySet() {
        Iterable<String> columnStringIterable = Iterables.transform(selectAll(), COLUMN_KEY_EXTRACTOR);
        return Collections.unmodifiableSet(Sets.newHashSet(columnStringIterable));
    }

    private Iterable<StringEntity> selectAll() {
        TableQuery<StringEntity> keySetQuery = secretieTableOperationFactory.selectAll(tableName);
        return timedExecuteQuery(SELECT_ALL_TIMER, keySetQuery);
    }

    private Iterable<StringEntity> timedExecuteQuery(Timer contextTimer, TableQuery<StringEntity> query) {
        TimerContext context = contextTimer.time();
        try {
            return cloudTableClient.execute(query);
        } finally {
            context.stop();
        }
    }

    @Override
    public Collection<String> values() {  // TODO doable
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, String>> rowMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, String>> columnMap() {
        throw new UnsupportedOperationException();
    }

    public String getTableName() {
        return tableName;
    }
}
