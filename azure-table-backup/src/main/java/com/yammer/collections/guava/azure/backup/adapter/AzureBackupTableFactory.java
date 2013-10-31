package com.yammer.collections.guava.azure.backup.adapter;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.serialization.json.JsonSerializingTable;
import com.yammer.collections.guava.azure.BaseAzureTable;
import com.yammer.collections.guava.azure.backup.lib.BackupTableFactory;

import java.net.URISyntaxException;
import java.util.Date;

import static com.yammer.collections.guava.azure.backup.lib.Backup.BackupStatus;

public class AzureBackupTableFactory implements BackupTableFactory {
    private static final String BACKUP_LIST_TABLE_NAME = "comYammerAzureTableBackupListTable";
    private static final String BACKUP_TABLE_NAME_TEMPLATE = "BackupNAME%sDATE%s";
    private final CloudTableClient cloudTableClient;

    public AzureBackupTableFactory(CloudTableClient cloudTableClient) {
        this.cloudTableClient = cloudTableClient;
    }

    private static String createBackupTableName(Date backupDate, String backupName) {
        return String.format(BACKUP_TABLE_NAME_TEMPLATE, backupName, backupDate.getTime());
    }

    @Override
    public Table<String, Date, BackupStatus> getBackupListTable() {
        try {
            return new JsonSerializingTable<>(
                    getOrCreateTable(BACKUP_LIST_TABLE_NAME),
                    String.class,
                    Date.class,
                    BackupStatus.class);
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> createBackupTable(Date backupDate, String backupName) {
        try {
            String backupTableName = createBackupTableName(backupDate, backupName);
            return getOrCreateTable(backupTableName);
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void removeTable(Date backupDate, String backupName) {
        String backupTableName = createBackupTableName(backupDate, backupName);
        try {
            CloudTable cloudTable = cloudTableClient.getTableReference(backupTableName);
            cloudTable.deleteIfExists();
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> getBackupTable(Date backupDate, String backupName) {
        try {
            String backupTableName = createBackupTableName(backupDate, backupName);
            CloudTable cloudTable = cloudTableClient.getTableReference(backupTableName);
            if (cloudTable.exists()) {
                return new BaseAzureTable(backupTableName, cloudTableClient);
            }
            return null;
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    private BaseAzureTable getOrCreateTable(String tableName) throws URISyntaxException, StorageException {
        CloudTable table = cloudTableClient.getTableReference(tableName);
        table.createIfNotExist();
        return new BaseAzureTable(tableName, cloudTableClient);
    }

}
