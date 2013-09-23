package com.yammer.guava.collections.backup.azure;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.azure.StringAzureTable;
import com.yammer.guava.collections.backup.lib.SourceTableFactory;

public class AzureSourceTableFactory implements SourceTableFactory {
    private final CloudTableClient cloudTableClient;
    private final String tableName;


    public AzureSourceTableFactory(CloudTableClient cloudTableClient, String tableName) {
        this.cloudTableClient = cloudTableClient;
        this.tableName = tableName;
    }

    @Override
    public Table<String, String, String> getSourceTable() {
        try {
            CloudTable table = cloudTableClient.getTableReference(tableName);
            table.createIfNotExist();
            return new StringAzureTable(tableName, cloudTableClient);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }

}
