package com.yammer.collections.azure.backup.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackupConfiguration {
    private static final String CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s";
    private final String sourceTableName;
    private final String sourceAccountName;
    private final String sourceAccountKey;
    private final String backupAccountName;

    public BackupConfiguration(
            @JsonProperty("sourceTableName") String sourceTableName,
            @JsonProperty("sourceAccountName") String sourceAccountName,
            @JsonProperty("sourceAccountKey") String sourceAccountKey,
            @JsonProperty("backupAccountName") String backupAccountName,
            @JsonProperty("backupAccountKey") String backupAccountKey) {
        this.sourceTableName = sourceTableName;
        this.sourceAccountName = sourceAccountName;
        this.sourceAccountKey = sourceAccountKey;
        this.backupAccountName = backupAccountName;
        this.backupAccountKey = backupAccountKey;
    }

    private final String backupAccountKey;

    private static String getConnectionString(String accountName, String accountKey) {
        return String.format(CONNECTION_STRING_TEMPLATE, accountName, accountKey);
    }

    public String getSourceConnectionString() {
        return getConnectionString(sourceAccountName, sourceAccountKey);
    }

    public String getBackupConnectionString() {
        return getConnectionString(backupAccountName, backupAccountKey);
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

}
