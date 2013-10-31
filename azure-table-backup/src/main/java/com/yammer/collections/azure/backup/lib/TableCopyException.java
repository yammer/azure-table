package com.yammer.collections.azure.backup.lib;

public class TableCopyException extends Exception {
    public TableCopyException(Exception e) {
        super(e);
    }
}
