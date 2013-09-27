package com.yammer.collections.guava.azure.backup.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackupConfiguration {
    private static final String CONNECTION_STRING_TEMPLATE = "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s";
    private String sourceTableName;
    private String sourceAccountName;
    private String sourceAccountKey;
    private String backupAccountName;
    private String backupAccountKey;

    private static String getConnectionString(String accountName, String accountKey) {
        return String.format(CONNECTION_STRING_TEMPLATE, accountName, accountKey);
    }

    @JsonProperty
    public void setSourceAccountName(String accountName) {
        this.sourceAccountName = accountName;
    }

    @JsonProperty
    public void setSourceAccountKey(String accountKey) {
        this.sourceAccountKey = accountKey;
    }

    public String getSourceConnectionString() {
        return getConnectionString(sourceAccountName, sourceAccountKey);
    }

    @JsonProperty
    public void setBackupAccountName(String accountName) {
        this.backupAccountName = accountName;
    }

    @JsonProperty
    public void setBackupAccountKey(String accountKey) {
        this.backupAccountKey = accountKey;
    }

    public String getBackupConnectionString() {
        return getConnectionString(backupAccountName, backupAccountKey);
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    @JsonProperty
    public void setSourceTableName(String tableName) {
        this.sourceTableName = tableName;
    }

}
