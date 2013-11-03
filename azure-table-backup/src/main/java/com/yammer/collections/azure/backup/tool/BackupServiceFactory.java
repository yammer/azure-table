package com.yammer.collections.azure.backup.tool;


import com.yammer.collections.azure.backup.adapter.AzureBackupTableFactory;
import com.yammer.collections.azure.backup.adapter.AzureSourceTableFactory;
import com.yammer.collections.azure.backup.lib.BackupService;
import com.yammer.collections.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.azure.backup.lib.SourceTableFactory;
import com.yammer.collections.azure.backup.lib.TableCopy;

import static com.yammer.collections.azure.util.AzureTables.clientForAccount;

public class BackupServiceFactory {

    private BackupTableFactory getBackupTableFactory(BackupConfiguration configuration) {
        return new AzureBackupTableFactory(
                clientForAccount(
                        configuration.getBackupAccountName(),
                        configuration.getBackupAccountKey()
                ).build()
        );
    }

    private SourceTableFactory getSourceTableFactory(BackupConfiguration configuration) {
        return new AzureSourceTableFactory(
                clientForAccount(
                        configuration.getSourceAccountName(),
                        configuration.getSourceAccountKey()
                ).build(),
                configuration.getSourceTableName());
    }

    public BackupService createBackupService(BackupConfiguration configuration) {
        BackupTableFactory backupTableFactory = getBackupTableFactory(configuration);
        SourceTableFactory sourceTableFactory = getSourceTableFactory(configuration);
        TableCopy<String, String, String> tableCopy = new TableCopy<>();
        return new BackupService(tableCopy, sourceTableFactory, backupTableFactory);
    }
}
