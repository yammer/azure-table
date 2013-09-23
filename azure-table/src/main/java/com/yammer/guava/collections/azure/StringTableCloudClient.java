package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;

public class StringTableCloudClient {
    private final CloudTableClient delegate;

    public StringTableCloudClient(CloudTableClient delegateClient) {
        this.delegate = delegateClient;
    }

    public StringEntity execute(String tableName, TableOperation tableOperation) throws StorageException {
        return delegate.execute(tableName, tableOperation).getResultAsType();
    }

    public Iterable<StringEntity> execute(TableQuery<StringEntity> query) {
        return delegate.execute(query);
    }
}
