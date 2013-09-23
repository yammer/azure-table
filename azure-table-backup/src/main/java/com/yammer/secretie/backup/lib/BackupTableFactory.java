package com.yammer.secretie.backup.lib;

import com.google.common.collect.Table;
import com.yammer.secretie.api.model.Key;
import com.yammer.secretie.api.model.Secret;

import java.util.Date;

public interface BackupTableFactory {

    public Table<String, Date, Backup.BackupStatus> getBackupListTable();

    Table<String, Key, Secret> createBackupTable(Date backupDate, String backupName);

    void removeTable(Date backupDate, String backupName);

    Table<String, Key, Secret> getBackupTable(Date date, String name);
}
