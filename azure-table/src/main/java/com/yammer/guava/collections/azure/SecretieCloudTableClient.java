package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableOperation;
import com.microsoft.windowsazure.services.table.client.TableQuery;
// TODO to be removed

/**
 * Wrapper to facilitate testing
 */
/* package */class SecretieCloudTableClient {
    private final CloudTableClient delegate;

    public SecretieCloudTableClient(CloudTableClient delegate) {
        this.delegate = delegate;
    }

    public SecretieEntity execute(String tableName, TableOperation operation) throws StorageException {
        return delegate.execute(tableName, operation).getResultAsType();
    }

    public Iterable<SecretieEntity> execute(TableQuery<SecretieEntity> query) {
        return delegate.execute(query);
    }
}
