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
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.microsoft.windowsazure.services.table.client.TableQuery;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yammer.collections.azure.AzureEntityUtil.decode;

/**
 * This class implements the set interface, however it does not enforce it as it only a view.
 */
/* package */
class CellSetMutableView extends AbstractSet<Table.Cell<byte[], byte[], byte[]>> {
    private static final Function<AzureEntity, Table.Cell<byte[], byte[], byte[]>> TABLE_CELL_CREATOR =
            new Function<AzureEntity, Table.Cell<byte[], byte[], byte[]>>() {
                @Override
                public Table.Cell<byte[], byte[], byte[]> apply(AzureEntity input) {
                    return Tables.immutableCell(
                            decode(input.getPartitionKey()),
                            decode(input.getRowKey()),
                            decode(input.getValue()));
                }
            };
    private final BaseAzureTable baseAzureTable;
    private final AzureTableCloudClient stringCloudTableClient;
    private final AzureTableRequestFactory azureTableRequestFactory;

    CellSetMutableView(BaseAzureTable azureTable,
                       AzureTableCloudClient stringCloudTableClient,
                       AzureTableRequestFactory azureTableRequestFactory) {
        baseAzureTable = azureTable;
        this.stringCloudTableClient = stringCloudTableClient;
        this.azureTableRequestFactory = azureTableRequestFactory;
    }

    @Override
    public int size() {
        return Iterables.size(getBackingIterable());
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Table.Cell) {
            Table.Cell<Object, Object, Object> cell = (Table.Cell<Object, Object, Object>) o;
            return baseAzureTable.contains(cell.getRowKey(), cell.getColumnKey());
        }

        return false;
    }

    private Iterable<AzureEntity> getBackingIterable() {
        TableQuery<AzureEntity> query = azureTableRequestFactory.selectAll(baseAzureTable.getTableName());
        return stringCloudTableClient.execute(query);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Table.Cell<byte[], byte[], byte[]>> iterator() {
        return Iterables.transform(
                getBackingIterable(),
                TABLE_CELL_CREATOR).iterator();
    }

    @Override
    public boolean add(Table.Cell<byte[], byte[], byte[]> cell) {
        checkNotNull(cell);
        return baseAzureTable.put(
                cell.getRowKey(),
                cell.getColumnKey(),
                cell.getValue()
        ) == null;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Table.Cell)) {
            return false;
        }

        Table.Cell<Object, Object, Object> cell = (Table.Cell) o;

        return baseAzureTable.remove(
                cell.getRowKey(),
                cell.getColumnKey()
        ) != null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean containsAll(Collection<?> c) {
        checkNotNull(c);
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean addAll(Collection<? extends Table.Cell<byte[], byte[], byte[]>> c) {
        checkNotNull(c);
        boolean change = false;
        for (Table.Cell<byte[], byte[], byte[]> cell : c) {
            if (add(cell)) {
                change = true;
            }
        }

        return change;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkNotNull(c);
        boolean change = false;
        for (Object o : c) {
            if (remove(o)) {
                change = true;
            }
        }

        return change;
    }

    @Override
    public void clear() {
        removeAll(this); // this works, because the iterator is only a view onto a remote collection
    }

    @Override
    public String toString() {
        return super.toString() + "AZURE_TABLE_NAME: " + baseAzureTable.getTableName();
    }
}
