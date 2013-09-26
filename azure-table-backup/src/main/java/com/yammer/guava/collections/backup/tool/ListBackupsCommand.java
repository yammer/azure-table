package com.yammer.guava.collections.backup.tool;

import com.yammer.guava.collections.backup.lib.Backup;

import java.util.Collection;
import java.util.Date;

class ListBackupsCommand extends AbstractBackupToolCommand {
    private final Date thresholdDate;


    ListBackupsCommand(BackupConfiguration configuration, Printer printer, long time) throws Exception {
        super(configuration, printer);
        this.thresholdDate = new Date(time);
    }

    @Override
    public void run() throws Exception {
        Collection<Backup> backups = getBackupService().listAllBackups(thresholdDate);
        for (Backup backup : backups) {
            println(format(backup));
        }
    }
}
