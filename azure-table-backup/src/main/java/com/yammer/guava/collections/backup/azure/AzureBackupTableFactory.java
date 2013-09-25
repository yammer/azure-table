package com.yammer.guava.collections.backup.azure;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.table.client.CloudTable;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.azure.AzureTable;
import com.yammer.guava.collections.azure.StringAzureTable;
import com.yammer.guava.collections.backup.lib.BackupTableFactory;

import java.net.URISyntaxException;
import java.util.Date;

import static com.yammer.guava.collections.backup.lib.Backup.BackupStatus;

public class AzureBackupTableFactory implements BackupTableFactory {
    private static final String BACKUP_LIST_TABLE_NAME = "comYammerAzureTableBackupListTable";
    private static final String BACKUP_TABLE_NAME_TEMPLATE = "BackupNAME%sDATE%s"; // TODO lets change it to md5/sha2
    private static final AzureTable.Marshaller<String, String> IDENTITY_MARSHALLER = new AzureTable.Marshaller<String, String>() {
        @Override
        public String marshal(String unmarshalled) {
            return unmarshalled;
        }

        @Override
        public String unmarshal(String marshalled) {
            return marshalled;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    };
    private static final AzureTable.Marshaller<Date, String> DATE_MARSHALLER = new AzureTable.Marshaller<Date, String>() {
        @Override
        public String marshal(Date unmarshalled) {
            return Long.toString(unmarshalled.getTime());
        }

        @Override
        public Date unmarshal(String marshalled) {
            return new Date(Long.parseLong(marshalled));
        }

        @Override
        public Class<Date> getType() {
            return Date.class;
        }
    };
    private static final AzureTable.Marshaller<BackupStatus, String> BACKUP_STATUS_MARSHALLER = new AzureTable.Marshaller<BackupStatus, String>() {

        @Override
        public String marshal(BackupStatus unmarshalled) {
            return unmarshalled.toString();
        }

        @Override
        public BackupStatus unmarshal(String marshalled) {
            return BackupStatus.valueOf(marshalled);
        }

        @Override
        public Class<BackupStatus> getType() {
            return BackupStatus.class;
        }
    };
    private final CloudTableClient cloudTableClient;

    public AzureBackupTableFactory(CloudTableClient cloudTableClient) {
        this.cloudTableClient = cloudTableClient;
    }

    @Override
    public Table<String, Date, BackupStatus> getBackupListTable() {
        try {
            return new AzureTable<>(
                    IDENTITY_MARSHALLER,
                    DATE_MARSHALLER,
                    BACKUP_STATUS_MARSHALLER,
                    getOrCreateTable(BACKUP_LIST_TABLE_NAME)
            );
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
            if(cloudTable.exists()) {
                return new StringAzureTable(backupTableName, cloudTableClient);
            }
            return null;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String createBackupTableName(Date backupDate, String backupName) {
        return String.format(BACKUP_TABLE_NAME_TEMPLATE, backupName, backupDate.getTime());
    }

    private StringAzureTable getOrCreateTable(String tableName) throws URISyntaxException, StorageException {
        CloudTable table = cloudTableClient.getTableReference(tableName);
        table.createIfNotExist();
        return new StringAzureTable(tableName, cloudTableClient);
    }

}
