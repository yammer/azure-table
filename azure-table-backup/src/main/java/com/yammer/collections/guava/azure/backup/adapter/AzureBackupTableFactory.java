package com.yammer.collections.guava.azure.backup.adapter;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.collections.guava.azure.BaseAzureTable;
import com.yammer.collections.guava.azure.backup.lib.BackupTableFactory;
import com.yammer.collections.guava.azure.serialisation.json.JsonSerializingTable;
import com.yammer.collections.transforming.TransformingTable;

import java.net.URISyntaxException;
import java.util.Date;

import static com.yammer.collections.guava.azure.backup.lib.Backup.BackupStatus;

public class AzureBackupTableFactory implements BackupTableFactory {
    private static final String BACKUP_LIST_TABLE_NAME = "comYammerAzureTableBackupListTable";
    private static final String BACKUP_TABLE_NAME_TEMPLATE = "BackupNAME%sDATE%s";
    private static final Function<Date, String> DATE_MARSHALLER = new Function<Date, String>() {
        @Override
        public String apply(Date unmarshalled) {
            return Long.toString(unmarshalled.getTime());
        }
    };
    private static final Function<String, Date> DATE_UNMARSHALLER = new Function<String, Date>() {
        @Override
        public Date apply(String marshalled) {
            return new Date(Long.valueOf(marshalled));
        }
    };
    private static final Function<BackupStatus, String> BACKUP_STATUS_MARSHALLER = new Function<BackupStatus, String>() {

        @Override
        public String apply(BackupStatus unmarshalled) {
            return unmarshalled.toString();
        }
    };
    private static final Function<String, BackupStatus> BACKUP_STATUS_UNMARSHALLER = new Function<String, BackupStatus>() {
        @Override
        public BackupStatus apply(String marshalled) {
            return BackupStatus.valueOf(marshalled);
        }
    };
    private final CloudTableClient cloudTableClient;

    public AzureBackupTableFactory(CloudTableClient cloudTableClient) {
        this.cloudTableClient = cloudTableClient;
    }

    @Override
    public Table<String, Date, BackupStatus> getBackupListTable() {
        try {
            return new JsonSerializingTable<>(
                    getOrCreateTable(BACKUP_LIST_TABLE_NAME),
                    String.class,
                    Date.class,
                    BackupStatus.class);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> createBackupTable(Date backupDate, String backupName) {
        try {
            final String backupTableName = createBackupTableName(backupDate, backupName);
            return getOrCreateTable(backupTableName);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void removeTable(Date backupDate, String backupName) {
        final String backupTableName = createBackupTableName(backupDate, backupName);
        try {
            CloudTable cloudTable = cloudTableClient.getTableReference(backupTableName);
            cloudTable.deleteIfExists();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Table<String, String, String> getBackupTable(Date backupDate, String backupName) {
        try {
            final String backupTableName = createBackupTableName(backupDate, backupName);
            CloudTable cloudTable = cloudTableClient.getTableReference(backupTableName);
            if (cloudTable.exists()) {
                return new BaseAzureTable(backupTableName, cloudTableClient);
            }
            return null;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String createBackupTableName(Date backupDate, String backupName) {
        return String.format(BACKUP_TABLE_NAME_TEMPLATE, backupName, backupDate.getTime());
    }

    private BaseAzureTable getOrCreateTable(String tableName) throws URISyntaxException, StorageException {
        CloudTable table = cloudTableClient.getTableReference(tableName);
        table.createIfNotExist();
        return new BaseAzureTable(tableName, cloudTableClient);
    }

}
