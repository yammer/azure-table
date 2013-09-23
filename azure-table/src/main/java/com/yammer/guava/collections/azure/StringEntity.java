package com.yammer.guava.collections.azure;

import com.microsoft.windowsazure.services.table.client.TableServiceEntity;

// TODO consider adding B64 encoding for the strings here, or somewhere
public class StringEntity extends TableServiceEntity {
    private String value;

    public StringEntity() { // needed by azure java api
    }

    public StringEntity(String rowKey, String columnKey, String value) {
        this.partitionKey = rowKey;
        this.rowKey = columnKey;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}