package com.yammer.collections.azure.util;

import org.codehaus.jackson.annotate.JsonProperty;

public class AzureTableConfiguration {
    private final String accountName;
    private final String accountKey;
    private final String tableName;
    private final Integer connectionTimeoutInMs;
    private final Integer retryIntervalInMs;
    private final Integer retryAttempts;
    private final String accountConfigurationFilePath;

    public AzureTableConfiguration(
            @JsonProperty("accountName") String accountName,
            @JsonProperty("accountKey") String accountKey,
            @JsonProperty("tableName") String tableName,
            @JsonProperty("connectionTimeout") Integer connectionTimeoutInMs,
            @JsonProperty("retryInterval") Integer retryIntervalInMs,
            @JsonProperty("retryAttempts") Integer retryAttempts,
            @JsonProperty("accountConfigurationFilePath") String accountConfigurationFilePath) {
        this.accountName = accountName;
        this.accountKey = accountKey;
        this.tableName = tableName;
        this.connectionTimeoutInMs = connectionTimeoutInMs;
        this.retryIntervalInMs = retryIntervalInMs;
        this.retryAttempts = retryAttempts;
        this.accountConfigurationFilePath = accountConfigurationFilePath;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeoutInMs;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public Integer getRetryInterval() {
        return retryIntervalInMs;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAccountConfigurationFilePath() {
        return accountConfigurationFilePath;
    }
}
