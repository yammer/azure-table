package com.yammer.collections.azure.util;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.BaseAzureTable;
import com.yammer.collections.azure.serialization.json.JsonSerializingTable;
import com.yammer.collections.metrics.MeteredTable;
import com.yammer.collections.transforming.TransformingTable;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@SuppressWarnings("UnusedDeclaration")
public final class AzureTables {
    private AzureTables() {
    }

    public static AzureTableClientBuilder clientForAccount(String accountName, String accountKey) {
        return new AzureTableClientBuilder(accountName, accountKey);
    }

    public static TableWithClientBuilder tableWithName(String name) {
        return new TableWithClientBuilder(name);
    }

    // account builder
    public static class AzureTableClientBuilder {
        private final CloudTableClient cloudTableClient;

        private AzureTableClientBuilder(String accountName, String accountKey) {
            cloudTableClient = createCloudTableClient(accountName, accountKey);
        }

        private static String getConnectionString(String accountName, String accountKey) {
            return String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s", accountName, accountKey);
        }

        private static CloudTableClient createCloudTableClient(String accountName, String accountKey) {
            try {
                CloudStorageAccount storageAccount = CloudStorageAccount.parse(getConnectionString(accountName, accountKey));
                return storageAccount.createCloudTableClient();
            } catch (URISyntaxException | InvalidKeyException e) {
                throw Throwables.propagate(e);
            }
        }

        public AzureTableClientBuilder withTimeoutInMs(int timeout) {
            cloudTableClient.setTimeoutInMs(timeout);
            return this;
        }

        public AzureTableClientBuilder withLinearReplyPolicy(int retryIntervalInMs, int retryAttempts) {
            cloudTableClient.setRetryPolicyFactory(new RetryLinearRetry(retryIntervalInMs, retryAttempts));
            return this;
        }

        public TableRef tableWithName(String name) {
            return new TableRef(name, cloudTableClient);
        }

        public CloudTableClient build() {
            return cloudTableClient;
        }
    }

    // table ref
    public static class TableRef {
        private final String name;
        private final CloudTableClient tableClient;

        private TableRef(String name, CloudTableClient tableClient) {
            this.name = name;
            this.tableClient = tableClient;
        }

        private CloudTable cloudTable() {
            try {
                return new CloudTable(name, tableClient);
            } catch (URISyntaxException e) {
                throw Throwables.propagate(e);
            }
        }

        public void delete() throws StorageException {
            cloudTable().delete();
        }

        public void deleteIfExists() throws StorageException {
            cloudTable().deleteIfExists();
        }

        public TableBuilder create() throws StorageException {
            cloudTable().create();
            return new TableBuilder(name, tableClient);
        }

        public TableBuilder createIfDoesNotExist() throws StorageException {
            cloudTable().createIfNotExist();
            return new TableBuilder(name, tableClient);
        }
    }

    public static class TableBuilder {
        private final Table<String, String, String> backingTable;
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        private boolean metrics;

        private TableBuilder(String name, CloudTableClient tableClient) {
            backingTable = BaseAzureTable.create(name, tableClient);
        }

        public TableBuilder andAddMetrics() {
            metrics = true;
            return this;
        }

        public <R, C, V> Table<R, C, V> buildWithJsonSerialization(Class<R> rowClass, Class<C> columnClass, Class<V> valueClass) {
            return addMetricsIfChosen(
                    JsonSerializingTable.create(
                            backingTable,
                            rowClass,
                            columnClass,
                            valueClass
                    )
            );
        }

        public <R, C, V> Table<R, C, V> buildUsingCustomSerialization(
                Function<R, String> rowSerializingFunction,
                Function<String, R> rowDeserializingFunction,
                Function<C, String> columnSerializingFunction,
                Function<String, C> columnDeserializingFunction,
                Function<V, String> valueSerializingFunction,
                Function<String, V> valueDeserializingFunction
        ) {
            return addMetricsIfChosen(
                    TransformingTable.create(
                            backingTable,
                            rowSerializingFunction, rowDeserializingFunction,
                            columnSerializingFunction, columnDeserializingFunction,
                            valueSerializingFunction, valueDeserializingFunction
                    )
            );
        }

        public Table<String, String, String> buildWithNoSerialization() {
            return addMetricsIfChosen(backingTable);
        }

        private <R, C, V> Table<R, C, V> addMetricsIfChosen(Table<R, C, V> table) {
            if (metrics) {
                return MeteredTable.create(table);
            }
            return table;
        }
    }

    // table with client builder
    private static class TableWithClientBuilder {
        private final String name;

        private TableWithClientBuilder(String name) {
            this.name = name;
        }

        public TableRef using(CloudTableClient client) {
            return new TableRef(name, client);
        }
    }
}
