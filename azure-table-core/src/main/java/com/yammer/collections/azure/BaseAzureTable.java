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
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCode;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.collections.azure.AzureEntityUtil.EXTRACT_VALUE;
import static com.yammer.collections.azure.AzureEntityUtil.decode;
import static com.yammer.collections.azure.AzureEntityUtil.encode;

@SuppressWarnings("ClassWithTooManyMethods")
public class BaseAzureTable implements Table<Bytes, Bytes, Bytes> {
    private static final Function<AzureEntity, Bytes> COLUMN_KEY_EXTRACTOR = new Function<AzureEntity, Bytes>() {
        @Override
        public Bytes apply(AzureEntity input) {
            return decode(input.getRowKey());
        }
    };
    private static final Function<AzureEntity, Bytes> ROW_KEY_EXTRACTOR = new Function<AzureEntity, Bytes>() {
        @Override
        public Bytes apply(AzureEntity input) {
            return decode(input.getPartitionKey());
        }
    };
    private final String tableName;
    private final AzureTableCloudClient azureTableCloudClient;
    private final AzureTableRequestFactory azureTableRequestFactory;

    // internal and test use only
    BaseAzureTable(String tableName, AzureTableCloudClient azureTableCloudClient, AzureTableRequestFactory azureTableRequestFactory) {
        this.tableName = tableName;
        this.azureTableCloudClient = azureTableCloudClient;
        this.azureTableRequestFactory = azureTableRequestFactory;
    }

    public static Table<Bytes, Bytes, Bytes> create(String tableName, CloudTableClient cloudTableClient) {
        return new BaseAzureTable(
                checkNotNull(tableName),
                new AzureTableCloudClient(checkNotNull(cloudTableClient)),
                new AzureTableRequestFactory()
        );
    }

    private static Bytes entityToValue(AzureEntity azureEntity) {
        return azureEntity == null ? null : decode(azureEntity.getValue());
    }

    private static boolean notFound(StorageException e) {
        return StorageErrorCode.RESOURCE_NOT_FOUND.toString().equals(e.getErrorCode())
                || "ResourceNotFound".equals(e.getErrorCode());
    }

    @Override
    public boolean contains(Object row, Object column) {
        return get(row, column) != null;
    }

    @Override
    public boolean containsRow(Object row) {
        return row instanceof Bytes && !row((Bytes) row).isEmpty();
    }

    @Override
    public boolean containsColumn(Object column) {
        return column instanceof Bytes && !column((Bytes) column).isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof Bytes)) {
            return false;
        }

        TableQuery<AzureEntity> valueQuery = azureTableRequestFactory.containsValueQuery(tableName, encode((Bytes) value));
        return azureTableCloudClient.execute(valueQuery).iterator().hasNext();
    }

    @Override
    public Bytes get(Object row, Object column) {
        return entityToValue(rawGet(row, column));
    }

    private AzureEntity rawGet(Object row, Object column) {
        if (!(row instanceof Bytes && column instanceof Bytes)) {
            return null;
        }

        String rowAsString = encode((Bytes) row);
        String columnAsString = encode((Bytes) column);

        TableOperation retrieveEntityOperation = azureTableRequestFactory.retrieve(rowAsString, columnAsString);

        try {
            return azureTableCloudClient.execute(tableName, retrieveEntityOperation);
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean isEmpty() {
        return cellSet().isEmpty();
    }

    @Override
    public int size() {
        return cellSet().size();
    }

    @Override
    public void clear() {
        for (Cell<Bytes, Bytes, Bytes> cell : cellSet()) {
            remove(cell.getRowKey(), cell.getColumnKey());
        }
    }

    @Override
    public Bytes put(Bytes row, Bytes column, Bytes value) {
        checkNotNull(row);
        checkNotNull(column);
        checkNotNull(value);
        TableOperation putStringieOperation = azureTableRequestFactory.put(encode(row), encode(column), encode(value));

        try {
            return entityToValue(azureTableCloudClient.execute(tableName, putStringieOperation));
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void putAll(Table<? extends Bytes, ? extends Bytes, ? extends Bytes> table) {
        checkNotNull(table);
        for (Cell<? extends Bytes, ? extends Bytes, ? extends Bytes> cell : table.cellSet()) {
            put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    @Override
    public Bytes remove(Object row, Object column) {
        AzureEntity entityToBeDeleted = rawGet(row, column);

        if (entityToBeDeleted == null) {
            return null;
        }

        TableOperation deleteStringieOperation = azureTableRequestFactory.delete(entityToBeDeleted);

        try {
            return entityToValue(azureTableCloudClient.execute(tableName, deleteStringieOperation));
        } catch (StorageException e) {
            if (notFound(e)) {
                return null;
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Map<Bytes, Bytes> row(Bytes row) {
        checkNotNull(row);
        return new ColumnView(this, row, azureTableCloudClient, azureTableRequestFactory);
    }

    @Override
    public Map<Bytes, Bytes> column(Bytes column) {
        checkNotNull(column);
        return new RowView(this, column, azureTableCloudClient, azureTableRequestFactory);
    }

    @Override
    public Set<Cell<Bytes, Bytes, Bytes>> cellSet() {
        return new CellSetMutableView(this, azureTableCloudClient, azureTableRequestFactory);
    }

    @Override
    public Set<Bytes> rowKeySet() {
        return SetView.fromCollectionView(
                new TableCollectionView<>(this, ROW_KEY_EXTRACTOR, azureTableCloudClient, azureTableRequestFactory)
        );
    }

    @Override
    public Set<Bytes> columnKeySet() {
        return SetView.fromCollectionView(
                new TableCollectionView<>(this, COLUMN_KEY_EXTRACTOR, azureTableCloudClient, azureTableRequestFactory)
        );
    }

    @Override
    public Collection<Bytes> values() {
        return new TableCollectionView<>(this, EXTRACT_VALUE, azureTableCloudClient, azureTableRequestFactory);
    }

    @Override
    public Map<Bytes, Map<Bytes, Bytes>> rowMap() {
        return new RowMapView(this);
    }

    @Override
    public Map<Bytes, Map<Bytes, Bytes>> columnMap() {
        return new ColumnMapView<>(this);
    }

    public String getTableName() {
        return tableName;
    }

    private static final class TableCollectionView<E> extends AbstractCollectionView<E> {
        private final BaseAzureTable baseAzureTable;
        private final AzureTableCloudClient azureTableCloudClient;
        private final AzureTableRequestFactory azureTableRequestFactory;

        public TableCollectionView(BaseAzureTable baseAzureTable, Function<AzureEntity, E> typeExtractor, AzureTableCloudClient azureTableCloudClient, AzureTableRequestFactory azureTableRequestFactory) {
            super(typeExtractor);
            this.baseAzureTable = baseAzureTable;
            this.azureTableCloudClient = azureTableCloudClient;
            this.azureTableRequestFactory = azureTableRequestFactory;
        }

        @Override
        protected Iterable<AzureEntity> getBackingIterable() {
            TableQuery<AzureEntity> query = azureTableRequestFactory.selectAll(baseAzureTable.getTableName());
            return azureTableCloudClient.execute(query);
        }
    }
}
