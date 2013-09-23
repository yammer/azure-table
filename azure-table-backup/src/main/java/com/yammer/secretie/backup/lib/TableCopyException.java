package com.yammer.secretie.backup.lib;

public class TableCopyException extends Exception {
    public TableCopyException(Exception e) {
        super(e);
    }
}
