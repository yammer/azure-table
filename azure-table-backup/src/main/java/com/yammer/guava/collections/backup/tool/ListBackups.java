package com.yammer.guava.collections.backup.tool;

import com.yammer.guava.collections.backup.lib.Backup;

import java.util.Collection;
import java.util.Date;

class ListBackups extends BackupToolCommand {
    private final Date thresholdDate;


    ListBackups(BackupConfiguration configuration, long time) throws Exception {
        super(configuration);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            System.out.println(format(backup));
        }
    }
}
