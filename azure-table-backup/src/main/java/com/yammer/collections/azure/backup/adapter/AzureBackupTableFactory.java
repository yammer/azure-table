package com.yammer.collections.azure.backup.adapter;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.azure.BaseAzureTable;
import com.yammer.collections.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.transforming.TransformingTable;

import java.net.URISyntaxException;
import java.util.Date;

import static com.yammer.collections.azure.backup.lib.Backup.BackupStatus;

public class AzureBackupTableFactory implements BackupTableFactory {
    private static final String BACKUP_LIST_TABLE_NAME = "comYammerAzureTableBackupListTable";
    private static final String BACKUP_TABLE_NAME_TEMPLATE = "BackupNAME%sDATE%s";
    private static final Function<Date, String> DATE_SERIALIZER_FUNCTION = new Function<Date, String>() {
        @Override
        public String apply(Date input) {
            return Long.toString(input.getTime());
        }
    };
    private static final Function<String, Date> DATE_DESERIALIZER_FUNCTION = new Function<String, Date>() {
        @Override
        public Date apply(String input) {
            return new Date(Long.parseLong(input));
        }
    };
    @SuppressWarnings("ConstantNamingConvention")
    private static final Function<BackupStatus, String> BACKUP_STATUS_SERIALIZER_FUNCTION = new Function<BackupStatus, String>() {
        @Override
        public String apply(BackupStatus input) {
            return input.toString();
        }
    };
    @SuppressWarnings("ConstantNamingConvention")
    private static final Function<String, BackupStatus> BACKUP_STATUS_DESERIALIZER_FUNCTION = new Function<String, BackupStatus>() {
        @Override
        public BackupStatus apply(String input) {
            return BackupStatus.valueOf(input);
        }
    };
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
            return TransformingTable.create(
                    getOrCreateTable(BACKUP_LIST_TABLE_NAME),
                    Functions.<String>identity(), Functions.<String>identity(),
                    DATE_SERIALIZER_FUNCTION, DATE_DESERIALIZER_FUNCTION,
                    BACKUP_STATUS_SERIALIZER_FUNCTION, BACKUP_STATUS_DESERIALIZER_FUNCTION
            );
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
                return BaseAzureTable.create(backupTableName, cloudTableClient);
            }
            return null;
        } catch (StorageException | URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    private Table<String, String, String> getOrCreateTable(String tableName) throws URISyntaxException, StorageException {
        CloudTable table = cloudTableClient.getTableReference(tableName);
        table.createIfNotExist();
        return BaseAzureTable.create(tableName, cloudTableClient);
    }

}
