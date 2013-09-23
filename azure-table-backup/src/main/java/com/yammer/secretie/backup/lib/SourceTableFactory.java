package com.yammer.secretie.backup.lib;


import com.google.common.collect.Table;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;


public interface SourceTableFactory {

    Table<String, Key, Secret> getSourceTable();

    String getTableName();
}
