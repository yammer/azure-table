package com.yammer.collections.azure.backup.adapter;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.microsoft.windowsazure.services.table.client.TableServiceException;
import com.yammer.collections.azure.backup.lib.SourceTableFactory;

import java.net.URISyntaxException;

import static com.yammer.collections.azure.util.AzureTables.tableWithName;

public class AzureSourceTableFactory implements SourceTableFactory {
    // http://msdn.microsoft.com/en-us/library/windowsazure/dd179387.aspx
    // table deletion takes at least 40s. We are quite pesymistic here
    private static final int HTTP_CONFILICT = 409;
    private static final int RECREATE_RETRY = 10000; // 1s
    private static final int MAX_NUMBER_OF_RETRIES = 12; // 2 minutes
    private final CloudTableClient cloudTableClient;
    private final String tableName;


    public AzureSourceTableFactory(CloudTableClient cloudTableClient, String tableName) {
        this.cloudTableClient = cloudTableClient;
        this.tableName = tableName;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    private static void tryCreateAfterDelete(CloudTable tableToBeRecreated) throws StorageException {
        boolean failure = true;
        int count = 0;
        while (failure && count++ < MAX_NUMBER_OF_RETRIES) {
            try {
                tableToBeRecreated.create();
                failure = false;
            } catch (TableServiceException e) {
                if (e.getHttpStatusCode() != HTTP_CONFILICT) {
                    throw e;
                }
                sleep();
            }
        }

        if (count > MAX_NUMBER_OF_RETRIES) {
            throw new RuntimeException("RESTORE FAILED. Failed to recreate source table after: " + MAX_NUMBER_OF_RETRIES + " attempts. Try again.");
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(RECREATE_RETRY);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> getSourceTable() {
        try {
            return tableWithName(tableName).
                    using(cloudTableClient).
                    createIfDoesNotExist().
                    buildWithNoSerialization();
        } catch (StorageException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void clearSourceTable() {
        try { // there is no single clear operation on azure table, so we implement this using drop and create :(
            CloudTable tableToBeCleared = cloudTableClient.getTableReference(tableName);
            tableToBeCleared.deleteIfExists();
            tryCreateAfterDelete(tableToBeCleared);
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

}
