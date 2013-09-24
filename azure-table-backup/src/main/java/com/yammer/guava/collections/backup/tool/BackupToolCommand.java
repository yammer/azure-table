package com.yammer.guava.collections.backup.tool;

import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;
import com.microsoft.windowsazure.services.table.client.CloudTableClient;
import com.yammer.guava.collections.backup.azure.AzureBackupTableFactory;
import com.yammer.guava.collections.backup.azure.AzureSourceTableFactory;
import com.yammer.guava.collections.backup.lib.*;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

abstract class BackupToolCommand {
    private final DateFormat dateFormat = new SimpleDateFormat();
    private final BackupService backupService;

    BackupToolCommand(String configPath) throws Exception {
        BackupConfiguration configuration = parseConfiguration(configPath);
        backupService = createBackupService(configuration);
    }

    private static BackupConfiguration parseConfiguration(String configPath) {
        // TODO add parsing
        BackupConfiguration configuration = new BackupConfiguration();
        configuration.setSourceTableName("backupToolValues");
        configuration.setSourceAccountName("secretietest");
        configuration.setSourceAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
        configuration.setBackupAccountName("secretietest");
        configuration.setBackupAccountKey("e5LnQoZei2cFH+56TFxDmO6AhnzMKill1NyVUs1M3R7OFNfCLnIGe17TLUex0mYYGQFjNvmArsLa8Iq3b0FNAg==");
        return configuration;
    }

    private static CloudTableClient createCloudTableClient(String connectionString) throws URISyntaxException, InvalidKeyException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
        return storageAccount.createCloudTableClient();
    }

    private static BackupTableFactory getBackupTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        CloudTableClient tableClient = createCloudTableClient(configuration.getBackupConnectionString());
        return new AzureBackupTableFactory(tableClient);
    }

    private static SourceTableFactory getSourceTableFactory(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        CloudTableClient tableClient = createCloudTableClient(configuration.getSourceConnectionString());
        return new AzureSourceTableFactory(tableClient, configuration.getSourceTableName());
    }

    private static BackupService createBackupService(BackupConfiguration configuration) throws URISyntaxException, InvalidKeyException {
        BackupTableFactory backupTableFactory = getBackupTableFactory(configuration);
        SourceTableFactory sourceTableFactory = getSourceTableFactory(configuration);
        TableCopy<String, String, String> tableCopy = new TableCopy<>();
        return new BackupService(tableCopy, sourceTableFactory, backupTableFactory);
    }

    protected final BackupService getBackupService() {
        return backupService;
    }

    protected String format(Backup backup) {
        return String.format("Backup: NAME=%s DATE=%s STATUS=%s", backup.getName(), dateFormat.format(backup.getDate()), backup.getStatus());
    }

    public abstract void run() throws Exception;

}
