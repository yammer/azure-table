package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;

public class StringTableRequestFactory {
    public TableOperation put(String rowString, String columnString, String value) {
        StringEntity secretieEntity = new StringEntity(rowString, columnString, value);
        return TableOperation.insertOrReplace(secretieEntity);
    }

    public TableOperation retrieve(String row, String column) {
        return TableOperation.retrieve(row, column, StringEntity.class);
    }

    public TableOperation delete(StringEntity entityToBeDeleted) {
        return TableOperation.delete(entityToBeDeleted);
    }

    public TableQuery<StringEntity> selectAll(String tableName) {
        return TableQuery.from(tableName, StringEntity.class);
    }
}
