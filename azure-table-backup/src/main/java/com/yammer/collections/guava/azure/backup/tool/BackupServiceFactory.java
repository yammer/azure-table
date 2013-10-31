package com.yammer.collections.guava.azure.backup.tool;


import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.guava.azure.backup.adapter.AzureBackupTableFactory;
import com.yammer.collections.guava.azure.backup.adapter.AzureSourceTableFactory;
import com.yammer.collections.guava.azure.backup.lib.BackupService;
import com.yammer.collections.guava.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.guava.azure.backup.lib.SourceTableFactory;
import com.yammer.collections.guava.azure.backup.lib.TableCopy;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public class BackupServiceFactory {
    @SuppressWarnings("MethodMayBeStatic")
    private CloudTableClient createCloudTableClient(String connectionString) throws URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
        return storageAccount.createCloudTableClient();
    }

    private BackupTableFactory getBackupTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        CloudTableClient tableClient = createCloudTableClient(configuration.getBackupConnectionString());
        return new AzureBackupTableFactory(tableClient);
    }

    private SourceTableFactory getSourceTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        CloudTableClient tableClient = createCloudTableClient(configuration.getSourceConnectionString());
        return new AzureSourceTableFactory(tableClient, configuration.getSourceTableName());
    }

    public BackupService createBackupService(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        BackupTableFactory backupTableFactory = getBackupTableFactory(configuration);
        SourceTableFactory sourceTableFactory = getSourceTableFactory(configuration);
        TableCopy<String, String, String> tableCopy = new TableCopy<>();
        return new BackupService(tableCopy, sourceTableFactory, backupTableFactory);
    }
}
