package com.yammer.collections.azure.backup.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackupConfiguration {
    private final String sourceTableName;
    private final String sourceAccountName;
    private final String sourceAccountKey;
    private final String backupAccountName;
    private final String backupAccountKey;

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

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public String getSourceAccountKey() {
        return sourceAccountKey;
    }

    public String getBackupAccountName() {
        return backupAccountName;
    }

    public String getBackupAccountKey() {
        return backupAccountKey;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

}
