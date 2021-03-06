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

import com.microsoft.windowsazure.services.table.client.TableConstants;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;

@SuppressWarnings("MethodMayBeStatic")
class AzureTableRequestFactory {
    TableOperation put(String rowString, String columnString, String value) {
        AzureEntity secretieEntity = new AzureEntity(rowString, columnString, value);
        return TableOperation.insertOrReplace(secretieEntity);
    }

    TableOperation retrieve(String row, String column) {
        return TableOperation.retrieve(row, column, AzureEntity.class);
    }

    TableOperation delete(AzureEntity entityToBeDeleted) {
        return TableOperation.delete(entityToBeDeleted);
    }

    TableQuery<AzureEntity> selectAll(String tableName) {
        return TableQuery.from(tableName, AzureEntity.class);
    }

    TableQuery<AzureEntity> containsValueQuery(String tableName, String value) {
        return selectAll(tableName).where(generateValueFilter(value));
    }

    TableQuery<AzureEntity> selectAllForRow(String tableName, String rowKey) {
        return selectAll(tableName).where(generatePartitionFilter(rowKey));
    }

    TableQuery<AzureEntity> containsValueForRowQuery(String tableName, String rowKey, String value) {
        String rowValueFilter = TableQuery.combineFilters(
                generatePartitionFilter(rowKey),
                TableQuery.Operators.AND,
                generateValueFilter(value)
                );
        return selectAll(tableName).where(rowValueFilter);
    }


    TableQuery<AzureEntity> selectAllForColumn(String tableName, String columnKey) {
        return selectAll(tableName).where(generateColumnFilter(columnKey));
    }

    TableQuery<AzureEntity> containsValueForColumnQuery(String tableName, String columnKey, String value) {
        String columnValueFilter = TableQuery.combineFilters(
                generateColumnFilter(columnKey),
                TableQuery.Operators.AND,
                generateValueFilter(value)
        );
        return selectAll(tableName).where(columnValueFilter);
    }

    private static String generatePartitionFilter(String rowKey) {
        return TableQuery.generateFilterCondition(
                TableConstants.PARTITION_KEY,
                TableQuery.QueryComparisons.EQUAL,
                rowKey);
    }

    private static String generateColumnFilter(String columnKey) {
        return TableQuery.generateFilterCondition(
                TableConstants.ROW_KEY,
                TableQuery.QueryComparisons.EQUAL,
                columnKey);
    }

    private static String generateValueFilter(String value) {
        return TableQuery.generateFilterCondition(
                AzureEntity.VALUE,
                TableQuery.QueryComparisons.EQUAL,
                value);
    }

}
