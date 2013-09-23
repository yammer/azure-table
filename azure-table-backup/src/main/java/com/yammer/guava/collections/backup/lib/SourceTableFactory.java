package com.yammer.guava.collections.backup.lib;


import com.google.common.collect.Table;


public interface SourceTableFactory {

    Table<String, String, String> getSourceTable();

    String getTableName();
}
