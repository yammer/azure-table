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
package com.yammer.collections.azure.util;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.core.storage.RetryLinearRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.BaseAzureTable;
import com.yammer.collections.azure.Bytes;
import com.yammer.collections.azure.serialization.json.JsonSerializingTable;
import com.yammer.collections.metrics.MeteredTable;
import com.yammer.collections.transforming.TransformingTable;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("UnusedDeclaration")
public final class AzureTables {
    private AzureTables() {
    }

    public static AzureTableClientBuilder clientForAccount(String accountName, String accountKey) {
        return new AzureTableClientBuilder(accountName, accountKey);
    }

    public static TableRefBuilder clientForConfiguration(AzureTableConfiguration configuration) {
        AzureTableClientBuilder tableClientBuilder = clientForAccount(
                checkNotNull(configuration.getAccountName()),
                checkNotNull(configuration.getAccountKey()));

        // timeout if specified
        if(configuration.getConnectionTimeout() != null) {
            tableClientBuilder.withTimeoutInMs(configuration.getConnectionTimeout());
        }

        // retry policy if specified
        Integer retryAttempts = configuration.getRetryAttempts();
        Integer retryInterval = configuration.getRetryInterval();
        if(retryAttempts != null && retryInterval != null) {
            tableClientBuilder.withLinearReplyPolicy(
                    configuration.getRetryInterval(),
                    configuration.getRetryAttempts());
        } else if(retryAttempts != null || retryInterval != null) {
            throw new IllegalArgumentException("You need to specify both: retryAttempts and retryInterval, or neither");
        }

        return tableClientBuilder.tableWithName(checkNotNull(configuration.getTableName()));
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

        public TableRefBuilder tableWithName(String name) {
            return new TableRefBuilder(name, cloudTableClient);
        }

        public CloudTableClient build() {
            return cloudTableClient;
        }
    }

    // table ref
    public static class TableRefBuilder {
        private final String name;
        private final CloudTableClient tableClient;

        private TableRefBuilder(String name, CloudTableClient tableClient) {
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

        public Optional<TableBuilder> ifExists() throws StorageException {
            if (cloudTable().exists()) {
                return Optional.of(new TableBuilder(name, tableClient));
            }
            return Optional.absent();
        }
    }

    public static class TableBuilder {
        private final Table<Bytes, Bytes, Bytes> backingTable;
        @SuppressWarnings("InstanceVariableMayNotBeInitialized")
        private Optional<MetricRegistry> metrics = Optional.absent();

        private TableBuilder(String name, CloudTableClient tableClient) {
            backingTable = BaseAzureTable.create(name, tableClient);
        }

        public TableBuilder andAddMetrics(MetricRegistry metrics) {
            this.metrics = Optional.fromNullable(metrics);
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
                Function<R, Bytes> rowSerializingFunction,
                Function<Bytes, R> rowDeserializingFunction,
                Function<C, Bytes> columnSerializingFunction,
                Function<Bytes, C> columnDeserializingFunction,
                Function<V, Bytes> valueSerializingFunction,
                Function<Bytes, V> valueDeserializingFunction
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

        public Table<Bytes, Bytes, Bytes> buildWithNoSerialization() {
            return addMetricsIfChosen(backingTable);
        }

        private <R, C, V> Table<R, C, V> addMetricsIfChosen(Table<R, C, V> table) {
            if (metrics.isPresent()) {
                return MeteredTable.create(table, metrics.get());
            }
            return table;
        }
    }

    // table with client builder
    public static class TableWithClientBuilder {
        private final String name;

        private TableWithClientBuilder(String name) {
            this.name = name;
        }

        public TableRefBuilder using(CloudTableClient client) {
            return new TableRefBuilder(name, client);
        }
    }
}
